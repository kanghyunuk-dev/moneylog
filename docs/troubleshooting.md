# 트러블슈팅 및 학습 기록

실제로 겪은 문제와 해결 과정, 그리고 인증 개념을 학습하며 정리한 기록. 시간순 정리.

## JWT/Spring Security 인증 개념 학습 (2026-07-30 시작, 2026-07-31 완료)

MoneyLog ①단계(로그인 구현) 착수 전, JWT 기반 인증의 전체 흐름을 8단계로 나눠 개념부터 정리.

- **학습 동기**: JWT가 무엇인지·AccessToken과 RefreshToken을 왜 나누는지·토큰을 어디에 저장해야 하는지·소프트 삭제된 계정을 인증 단계에서 어떻게 걸러내는지·Spring Security 설정이 왜 그런 구조인지를 개념부터 코드 구현까지 한 번에 연결해서 정리.
- **다루는 구성 요소** (Spring Security 기반 JWT 인증 시스템의 표준 구성):
  - SecurityConfig(필터 체인/인가 경로 설정)
  - PrincipalDetails + PrincipalDetailsService(Spring Security UserDetails 구현체)
  - JWTTokenProvider + JWTAuthorizationFilter + JWTProperties + TokenInfo(JWT 발급/검증)
  - CustomAuthenticationEntryPoint(401) + CustomAccessDeniedHandler(403)
  - AuthController/UserController, AuthService/UserService
  - User 엔티티(deletedAt 소프트삭제 필드)
  - UserCleanupScheduler(매일 자정 30일 지난 탈퇴 유저 완전 삭제 배치)
  - RefreshToken을 DB 테이블에 저장하는 방식 — MoneyLog ④단계(Redis 연동)에서 이 자리를 대체하는 게 목표
  - 프론트 쪽 `authFetch` 패턴 — 토큰 자동 첨부, 401+ACCESS_TOKEN_EXPIRED 시 자동 refresh 후 재시도
- **학습 순서** (8단계로 세분화):
  1. 회원가입 + 비밀번호 암호화 (BCryptPasswordEncoder)
  2. Spring Security 인증 기본 골격 (AuthenticationManager, UserDetailsService/UserDetails)
  3. 로그인 + JWT 발급 (AccessToken 30분/RefreshToken 7일 분리 이유, JWTTokenProvider)
  4. SecurityConfig 전체 + JWTAuthorizationFilter (필터 체인, SecurityContext, STATELESS 세션 정책)
  5. 인증/인가 실패 처리 (CustomAuthenticationEntryPoint=401 vs CustomAccessDeniedHandler=403 차이)
  6. 프론트 토큰 저장(localStorage) + authFetch의 자동 refresh 로직
  7. 마이페이지 (@AuthenticationPrincipal 패턴)
  8. 소프트 삭제 + UserCleanupScheduler(@Scheduled 배치)
  - 2026-07-31, 1~8단계 전체 정리 완료.

## JWT 무상태성 트레이드오프 (2026-07-31)

인증 개념을 정리하며 확인한 내용. MoneyLog ④단계(Redis 연동)에서 실제로 최적화해볼 대상.

- **문제**: JWT AccessToken은 서명만 검증하면 되는 게 원래 장점(무상태, DB 조회 불필요)인데, 회원탈퇴를 소프트 삭제(deletedAt)로 처리하다 보니 "탈퇴했다"는 사실이 토큰 안에는 반영되지 않음. 그대로 두면 탈퇴한 계정이 AccessToken 만료 전까지(최대 30분) 계속 API를 쓸 수 있는 구멍이 생김.
- **해결 방식**: `JWTTokenProvider.getAuthentication()`에서 토큰 서명 검증 후에도 매 요청마다 `userRepository.existsByEmailAndDeletedAtIsNull()`로 DB를 한 번 더 조회해서 탈퇴 여부를 확인. JWT의 "DB 조회 없이 검증 가능"이라는 이점을 일부 포기하고, 대신 탈퇴가 즉시 반영되는 정확성을 택한 트레이드오프. 같은 이유로 로그인 시점에도 탈퇴 여부 재확인, 탈퇴 처리 시 RefreshToken도 즉시 삭제.
- **더 최적화하는 방법**: DB 조회를 Redis 캐시 조회로 대체 — 탈퇴 처리 시 Redis에 "탈퇴 표시(블랙리스트)"를 즉시 기록해두고, 필터가 매 요청마다 (느린 DB 대신) 빠른 Redis를 조회하도록 바꾸면 "매번 확인한다"는 구조는 유지하면서 성능 손실을 크게 줄일 수 있음. 다만 트래픽 규모가 커야 체감되는 최적화라, 지금 규모에서 DB 재조회 방식 자체가 틀린 선택은 아님.
- **적용 계획**: MoneyLog ④단계(Redis 연동)에서 이 블랙리스트 캐시 패턴을 직접 구현해보는 것을 세션/리프레시 토큰 저장과 함께 실습 목표로 삼음.

## DB 스키마 실행 검증 (2026-08-07)

`docs/db-schema.sql`을 로컬 MySQL 8.0에 실제로 실행하며 발견/수정한 문제들.

- **테이블명 `User` → `users`로 변경**: `USER`는 MySQL 예약어(시스템 계정 관련)라 실행 시 매번 백틱 이스케이프가 필요했음.
- **컬럼명 `year_month` → `budget_month`로 변경**: `YEAR_MONTH`도 MySQL 8.0 예약어(INTERVAL 구문 키워드)라 문법 에러 발생. 매번 이스케이프하는 대신 이름 자체를 바꿔 근본 해결.
- **한글 시드 데이터는 MySQL 클라이언트 접속 시 `--default-character-set=utf8mb4` 지정 필요**: 안 하면 한글 INSERT에서 인코딩 에러 발생(파일 자체는 UTF-8이 맞았음, 클라이언트 접속 인코딩 문제였음).
- 이 세 가지 수정 후 6개 테이블 + FK 6개(CASCADE/RESTRICT 정책 포함) + 시드 데이터 14건까지 전부 의도대로 생성되는 것을 `information_schema` 조회로 최종 확인.

## Figma Make 디자인 - 기능 명세 대조 (2026-08-07)

Figma Make로 받은 8개 화면을 `docs/specs.md`의 기능 명세와 하나씩 대조.

- **①단계 대조**: 로그인(이메일+비밀번호+구글 버튼), 회원가입(이메일/닉네임/비밀번호/비밀번호확인), 마이페이지(닉네임 수정/비밀번호 변경/회원탈퇴) 전부 명세와 정확히 일치. 마이페이지의 "계정 관리" 섹션에 "되돌릴 수 없는 작업입니다" 안내와 함께 회원탈퇴 링크가 위험 액션답게 눈에 덜 띄게 배치되어 있음(08-profile.png).
- **②단계 대조**: 거래내역·예산·통계(대시보드) 화면 모두 명세의 API 구조와 일치. 카테고리 목록도 확정한 14개와 실제 표시된 항목이 정확히 일치. 예산 화면은 초과 카테고리에 "초과" 배지+빨간 진행률 바로 표시되어 색상 가이드도 그대로 지켜짐(05-budget.png). 거래내역 목록은 날짜별로 그룹핑되고, 각 항목은 메모를 제목으로·카테고리를 부제로 보여주는 방식 — 명세는 "카테고리·유형·금액·메모" 순서였는데 실제 화면은 메모를 더 앞에 내세움. 실제 구현 시 표시 순서는 구현 시점에 판단.
- **②-2단계 대조**: 목표자산 화면이 명세(활성 1개 강조 + 대기 중 여러 개 + 완료됨 구분)와 정확히 일치.
- **디자인에는 있지만 명세엔 명시 안 됐던 것**: 홈 화면의 "빠른 액션" 버튼(거래 추가/통계 바로가기) — 나쁘지 않은 추가 UX라 실제 구현 시 그대로 채택해도 무방, 필수는 아님.
- **결론**: 기능 누락 없음.

## 백엔드 뼈대 생성 (2026-08-07)

Spring Initializr(start.spring.io)에서 직접 옵션을 선택해 zip으로 받음.

- **설정값**: Project Gradle-Groovy / Language Java / Spring Boot 4.0.7 / Java 21 / Group `com.moneylog` / Artifact `backend` / Packaging Jar. Dependencies: Spring Web, Spring Security, Spring Data JPA, MySQL Driver, Validation, Lombok, Actuator.
- **압축 해제 시 이중 폴더 문제**: zip을 풀면 `backend/backend/` 형태로 한 겹 더 감싸져서 생성됨(zip 자체가 `backend`라는 최상위 폴더를 포함) — 안쪽 내용물을 `backend/`로 한 단계 끌어올려 정리.
- **`build.gradle` 실제 생성 결과 확인** — Spring Boot 4.x가 스타터 이름을 모듈화하면서 일부 이름이 바뀜:
  - `spring-boot-starter-web` → `spring-boot-starter-webmvc`로 개명(Spring MVC와 WebFlux 구분을 위해)
  - 테스트 의존성도 "기술별 테스트 스타터" 패턴으로 세분화: `-actuator-test`, `-data-jpa-test`, `-security-test`, `-validation-test`, `-webmvc-test` 각각 별도 존재.
- **빌드/테스트 검증 (`./gradlew test`)**: 컴파일은 즉시 성공(4.x 의존성 이름들이 실제로 다 정상 인식됨). 첫 테스트 실행은 `application.properties`가 비어있어 DB 연결 정보 부재로 `contextLoads()` 실패 — DB 설정을 채운 뒤 재실행해서 `BUILD SUCCESSFUL` 확인.
- **`application.properties` 설정**: `spring.datasource.*`로 로컬 `moneylog` DB 연결, 비밀번호는 `${MYSQL_PASSWORD}` 환경변수 참조. `spring.jpa.hibernate.ddl-auto=validate`로 설정 — JPA가 테이블을 자동 생성/변경하게 두면 신중하게 설계한 FK 정책이 Hibernate 기본값으로 덮어써질 위험이 있어, 스키마의 "정답"은 항상 `docs/db-schema.sql`이 갖고 JPA는 그걸 따르기만 하게 함.
- **`.gitignore` 정리**: Initializr가 `backend/.gitignore`를 상세하게 자동 생성해줘서, 루트 `.gitignore`의 중복 항목 제거.

## 프론트엔드 뼈대 생성 (2026-08-07)

`npm create vite@latest frontend -- --template react`로 생성.

- **템플릿**: `react`(JS) — `react-ts`(TypeScript) 아님.
- **린터**: ESLint 선택 (Oxlint라는 신흥 대안도 있었으나, ESLint가 여전히 압도적 표준이고 자료·튜토리얼이 훨씬 많아 학습 단계에 유리).
- **생성 결과 확인**: `src/App.jsx`, `src/main.jsx` 등 `.jsx` 확장자로 정상 생성됨. React 19.2.8. `npm run dev`로 개발 서버 실행 후 기본 화면과 카운터 버튼(state 동작) 정상 확인.
- **"뼈대를 API 없이 미리 만들어도 되는가" 판단**: 원래 "backend API 준비 후 프론트 착수"로 정했던 건 실제 로직(로그인 폼, API 연동, 토큰 저장 등)을 API 없이 짜면 나중에 재작업이 생긴다는 이유였음. 하지만 "뼈대 초기화"는 API에 대한 어떤 가정도 담지 않는 별개 단계라 지금 해둬도 손해가 없다고 재판단.
- **`.gitignore` 정리**: `frontend/.gitignore`도 Vite가 자동 생성 — 루트 `.gitignore`의 중복 항목 제거.

## httpOnly+CSRF 전환 중 CSRF 403 에러 (2026-08-26)

localStorage → httpOnly 쿠키+CSRF 전환 작업 중(`SecurityConfig`에 `CsrfConfigurer::spa()` 적용 직후) 실제로 겪은 문제.

- **증상**: 로그인 요청(`POST /api/auth/login`)이 `CustomAccessDeniedHandler`가 응답하는 403("접근 권한이 없습니다")으로 실패. Application 탭에서 확인하니 `XSRF-TOKEN` 쿠키 자체는 브라우저에 존재.
- **원인**: `CsrfConfigurer::spa()`는 CSRF 검증 방식(쿠키 저장소, 요청 핸들러)만 설정할 뿐, 토큰을 실제로 쿠키에 심는 시점은 지연 평가(deferred)라 누군가 그 값을 명시적으로 "읽어야만" 쿠키가 생성됨. `XSRF-TOKEN` 쿠키가 아예 없는 상태(첫 방문, 쿠키 삭제 후)에서 첫 `POST` 요청을 보내면 CSRF 토큰 없이 나가 거부당함.
- **해결**: `CsrfFilter` 뒤에 `CsrfCookieFilter`(직접 만든 `OncePerRequestFilter`, `request.getAttribute("_csrf")`를 읽어 `CsrfToken.getToken()`을 호출해 강제로 쿠키 생성을 트리거)를 `addFilterAfter`로 추가 — Spring 공식 SPA 가이드의 표준 패턴.
- **재현 조건 확인**: 실제로는 `AuthProvider`가 마운트 시 `GET /api/auth/me`를 항상 먼저 호출해서 이 상황을 우회시켜주고 있어, 정상적인 사용자 흐름(페이지 로드 → 로그인 시도)에서는 문제가 되지 않음. 브라우저 쿠키를 지운 뒤 새로고침 없이 바로 로그인 버튼을 누르는 것처럼, 페이지 마운트 없이 요청만 보내는 테스트 방식에서만 재현됨 — 이 구분을 몰라서 처음엔 필터가 안 먹힌 줄 알고 재검토했었음.

## 마이페이지 닉네임/비밀번호 변경이 DB에 반영 안 됨 (2026-08-27)

`UserController`/`UserService` 작성 중 Postman으로 `PUT /api/users/me`를 테스트하며 실제로 겪은 문제.

- **증상**: `PUT /api/users/me`가 200 OK를 응답하는데, DB의 `users` 테이블 `nickname` 값이 그대로였음. 코드에는 에러가 전혀 안 남.
- **원인**: `@AuthenticationPrincipal`로 받은 `User`는 `JWTAuthorizationFilter`(요청 처리 초입, 별도 트랜잭션 경계)에서 조회된 객체라, `UserService`의 `@Transactional` 메서드가 시작되는 시점엔 이미 JPA 영속성 컨텍스트에서 떨어져 나간(detached) 상태. 더티 체킹(자동 UPDATE)은 "지금 트랜잭션에 관리되고 있는(managed)" 엔티티에서만 동작하는데, detached 엔티티의 필드를 바꿔도 JPA가 추적을 안 해서 UPDATE 쿼리 자체가 안 나감 — 에러 없이 조용히 실패.
- **해결**: `UserService` 메서드 안에서 `userRepository.findById(user.getId())`로 다시 조회한 `managedUser`를 사용 — 이 객체는 지금 트랜잭션에 확실히 attached 상태라 더티 체킹이 정상 동작함. Spring Security + JPA 조합에서 흔히 발생하는 패턴으로, 실무에서도 "Service에서 엔티티를 수정하기 전에 재조회하라"가 표준 해결책으로 알려져 있음(Baeldung 등 확인).
- **부수적으로 발견한 버그**: 처음 수정할 때 `updatePassword()`에서 현재 비밀번호 검증(`passwordEncoder.matches`)에는 여전히 예전 `user`(detached)를 참조하고, 실제 변경(`changePassword`)에만 `managedUser`를 쓰는 실수가 있었음 — 두 시점 모두 `managedUser`로 통일해 수정.
