# 설계 및 기술 판단 기록

무엇을 만들지가 아니라 "왜 이렇게 정했는지"를 남긴 문서. 무엇을 만들기로 했는지는 `docs/specs.md` 참고.

## 문서 목차
- [스택 선택](#스택-선택) — Spring Boot 4.x, JPA, Vite 등 기술 선택 이유
- [프로젝트 범위 결정](#프로젝트-범위-결정) — 기능 포함/제외/순서 판단
- [기술적 깊이를 남기는 방향성](#기술적-깊이를-남기는-방향성)
- [DB 설계 판단](#db-설계-판단) — 테이블/컬럼/FK 정책, "무엇을 어떻게 설계했는가"
- [백엔드 구현 판단](#백엔드-구현-판단) — validate vs update, 연관관계 fetch 전략 등, "설계된 걸 코드로 어떻게 다루는가"
- [프론트엔드 구현 판단](#프론트엔드-구현-판단) — 라우트 가드, 상태 정리 책임 등, "설계된 걸 React 코드로 어떻게 다루는가"

## 스택 선택

### Spring Boot 4.x (3.5.x 아님)
백엔드 뼈대를 만들려던 시점(2026-08-07)에 확인해보니 **Spring Boot 3.5는 2026-06-30부로 OSS 지원 종료(EOL)**되어 Spring Initializr에서 더 이상 3.5.x를 선택할 수 없는 상태였음. EOL된 버전으로 새 프로젝트를 시작하는 건 보안 패치도 안 나오고 배우는 지식도 곧 구식이 되어 부적절 → Spring Boot 4.x로 진행.

- **학습 방식**: Spring Boot 4.x는 Spring Security 쪽에서 이전 버전과 문법 차이가 큼(DSL 재작성, `authorizeRequests()` 완전 제거, CSRF 기본 정책 변경 등 — Spring Security 7 기반). 인증 개념(JWT, 필터 체인, 인증/인가 흐름) 학습은 개념 자체에 집중해서 정리해두고, 실제 구현 문법은 4.x 기준으로 진행.
- 참고 자료의 문법이 실제 4.x와 다른 지점을 만나면, 이게 "개념을 잘못 이해한 것"인지 "단순 버전 문법 차이"인지 구분하는 것 자체가 하나의 학습 목표.

### JPA 유지, MyBatis는 다음 기회로
백엔드 뼈대 착수 중 "이번엔 MyBatis로 해볼까"라는 고민이 나옴. JPA는 이전에 한 번 다뤄봤지만 아직 "잘 익혔다"고 하기엔 부족한 상태, MyBatis는 수업에서 개념만 배우고 실전 적용은 없었던 상태.

- **결정**: MoneyLog는 JPA로 계속 진행. MyBatis는 2차 프로젝트 또는 별도 미니 프로젝트로 미룸.
- **이유**: React(신규), Spring Boot 4.x(예상 못한 버전 변경 대응), JWT/Security 직접 구현(신규), ③소셜로그인·④Redis(신규)까지 이미 새 학습 부담이 많음 — 여기에 MyBatis까지 더하면 학습 밀도를 넘어섬.
- **JPA를 "잘 익히지 못했다"는 문제의 해결책은 다른 기술로 바꾸는 게 아니라, 같은 기술을 한 번 더 제대로 써보는 것**으로 판단 — 이전에 얕게 쓰고 넘어간 연관관계 매핑·집계 쿼리를 MoneyLog ②단계에서 제대로 붙잡는 것 자체가 목표.
- MyBatis는 2차 프로젝트 또는 완전히 별도의 짧은 미니 프로젝트, 둘 다 후보로 남겨둠(미확정).

### Next.js가 아니라 Vite
MoneyLog는 (1) 로그인 뒤에만 쓰는 개인 서비스라 SEO가 불필요해 SSR 이점이 없고, (2) 백엔드(Spring Boot)가 이미 따로 있어 프론트는 API만 호출하는 순수 SPA면 충분하고, (3) 학습 목표가 "React 자체"이지 Next.js의 추가 개념(서버 컴포넌트 등)이 아님. Vite는 순수 React를 SSR 없이 빠르게 시작할 때의 표준 도구.

## 프로젝트 범위 결정

- **관리자 페이지는 넣지 않음**: 가계부는 1인용 개인 데이터 도메인이라 "여러 사용자 콘텐츠 검수" 구조인 관리자 기능이 자연스럽지 않음. (2차 게시판형 프로젝트로 이관 예정 — 별도 저장소)
- **①(로그인)을 착수 순서 맨 앞으로 앞당김**: 인증 개념을 정리하는 것과 동시에 MoneyLog에도 바로 로그인 기능을 만들어보는 것이 이해에 더 효과적이라 판단.
- **반복거래·다중통화 제외** (②단계 최초 범위): 반복거래는 스케줄링 로직이 추가로 필요해 학습 밀도를 넘어섬 — 안정된 후 별도 확장 기능으로 검토. 다중통화는 ⑤단계(환율 API)와 겹쳐 거기서 처리하기로 함.
- **사용자 정의 카테고리 보류**: 고정 카테고리로 CRUD+통계 감각을 먼저 익힌 뒤 "카테고리 자체의 CRUD"를 별도 확장으로 추가할 예정. 제외가 아니라 나중 작업으로 확정.
- **대시보드 집계는 백엔드로 확정**: 프론트/백엔드 중 어디서 처리할지가 의도적으로 남겨둔 기술적 의사결정 지점이었는데, 실무 표준(SQL GROUP BY가 원본 데이터 프론트 전송보다 효율적)과 학습 목표(JPA 집계 쿼리 작성 경험)를 근거로 백엔드 집계로 결정.
- **②-2(목표자산)을 확장 단계로 분리**: "가계부 기록"에 그치지 않고 자산을 목표 지향적으로 관리할 수 있으면 실사용자에게 유용하다고 판단해 반영. 다만 학습 순서를 깨지 않기 위해 확장 단계로 분리.
- **③(소셜로그인) 자동 연결 정책**: "물어보고 사용자가 선택 + 본인확인" 방식이 더 안전하지만 확인 모달·재인증 로직까지 필요해 학습 밀도를 넘어섬 — 1인 서비스라 계정 분리 리스크도 낮아 자동 연결로 단순화.
- **①단계 토큰 저장 — localStorage로 시작 후 httpOnly+CSRF로 전환(2026-08-26 완료)**: 실무 표준은 httpOnly+CSRF(XSS에 더 안전)이지만 쿠키·CSRF·CORS까지 한번에 얹으면 로그인 실패 시 원인 구분이 어려워, JWT 흐름 자체는 먼저 localStorage로 격리해 익힘. 로그인/회원가입 실동작 검증이 끝난 시점(①단계 완주 전, 마이페이지·소프트삭제를 만들기 전)으로 전환 시점을 앞당겨 재작업을 줄임. 실제 전환 내용은 아래 "백엔드/프론트엔드 구현 판단" 참고.
- **②단계 대시보드 통계 확장(2026-08-15)**: "전월 대비 증감률", "가장 많이 지출한 항목"은 기존 API를 살짝 확장하는 수준이라 추가 확정. 반면 **사용자별 평균 지출**은 "1인용 데이터" 전제와 충돌(전체 사용자 집계 시 동의·익명화까지 필요)하고, **최근 3개월 소비 패턴**은 "패턴"의 정의 자체가 미확정이라 범위가 흔들릴 위험이 있어 둘 다 보류(제외 아님).

## 기술적 깊이를 남기는 방향성
가계부는 흔한 주제라 그 자체로는 차별점이 약할 수 있음. 이건 주제를 바꿔서 해결할 문제가 아니라 "만들면서 기술적 깊이를 남기는가"의 문제로 판단. 단순 CRUD에 그치지 않도록 카테고리별 통계 쿼리 설계, 월별 집계를 프론트/백엔드 중 어디서 처리할지 같은 기술적 의사결정 지점을 의도적으로 만들고, 그 판단 과정을 문서로 남기는 방향으로 진행.

## DB 설계 판단

테이블 스키마 자체는 `docs/db-schema.sql`이 정답. 여기는 "왜 이렇게 설계했는지"만 기록.

- **User 관련 FK는 처음부터 실제 로그인 사용자 기준으로 설계**: 로그인이 ①단계로 앞당겨지면서 User 테이블과 인증 로직을 처음부터 실제로 구현하게 됨. ②단계(CRUD+통계)에서 등장하는 Transaction/Budget은 임시 고정 사용자를 거치는 중간 단계 없이, 처음부터 실제 로그인된 사용자의 `user_id` FK를 참조하도록 설계.
- **Transaction에 type 컬럼을 두지 않음**: 수입/지출 구분은 항상 Category.type을 JOIN해서 판단(정규화). 중복 컬럼은 두 값이 어긋나는 정합성 리스크가 있고, 학습 목표 자체가 "JOIN·집계 쿼리를 짜보는 것"이라 JOIN을 피할 이유가 없다고 판단.
- **amount는 BIGINT(정수)**: 원화는 소수점 단위가 없어 DECIMAL을 쓸 이유가 없고, 부동소수점 오차 문제를 피함.
- **budget_month는 VARCHAR(7)**: "일자"는 의미가 없고 "년-월"만 필요한데 MySQL에 전용 타입이 마땅치 않아 문자열이 실무에서도 흔히 쓰임. (원래 컬럼명은 `year_month`였으나 MySQL 예약어 충돌로 변경 — `docs/troubleshooting.md` 참고)
- **Category는 Java enum이 아니라 테이블로 설계**: 나중에 사용자 정의 카테고리를 붙일 때 이 테이블에 사용자 소유 행을 추가하는 구조로 자연스럽게 확장 가능하기 때문.
- **RefreshToken을 email이 아닌 user_id(FK)로 연결**: email 문자열로 느슨하게 연결하는 방식(탈퇴 시 스케줄러 정리가 편함)도 검토했으나, DB가 정합성을 보장 못 해 존재하지 않는 이메일을 가리키는 고아 토큰이 생길 수 있음 — `user_id` FK + `ON DELETE CASCADE`로 삭제 편의성과 정합성을 모두 확보.
- **Transaction에 updated_at 추가(2026-09-02)**: created_at만으로는 사용자가 나중에 수정한 거래인지 구분이 안 됨. `PUT /api/transactions/{id}`로 직접 수정 가능한 데이터라 수정 이력 추적 대상(User와 동일) — 반면 수정 자체가 없는 고정 시드 데이터 Category는 제외.
- **transaction_date는 DATE(LocalDate), DATETIME 아님(2026-09-02)**: 거래는 "몇 시"가 아니라 "어느 날짜"만 중요함(가계부 집계가 전부 일/월 단위). 시각까지 저장하면 타임존 변환 과정에서 자정 근처 거래가 하루 밀리는 버그 위험만 생김 — 서버가 기록하는 `created_at`(시각까지 의미)과 사용자가 입력하는 `transaction_date`(날짜만 의미)를 타입부터 구분.

### FK 정책 (`ON DELETE`/`ON UPDATE`)
- **User 참조(RefreshToken, Transaction, Budget, Goal 전체)**: `ON DELETE CASCADE, ON UPDATE CASCADE`. 판단 기준: "부모(User)가 없어지면 자식 데이터가 존재할 이유가 있는가?" — 회원탈퇴는 이 사람의 모든 흔적을 지운다는 의미가 명확하므로, User 삭제(30일 뒤 하드 삭제) 시 연관 데이터도 자동으로 같이 지워지는 게 자연스러움.
- **Category 참조(Transaction, Budget)**: `ON DELETE RESTRICT, ON UPDATE CASCADE`. 판단 기준은 같지만 결론이 반대 — Category(식비 등 고정 목록)는 삭제될 일이 거의 없어야 하는 공유 자원이라, 실수로 하나를 지우면 참조하는 모든 사용자의 거래 기록이 CASCADE로 같이 사라지는 대참사가 될 수 있음. RESTRICT로 삭제 자체를 DB가 막음.
- **ON UPDATE CASCADE는 모든 FK에 공통 적용**: 모든 PK가 AUTO_INCREMENT라 실제로 값이 바뀔 일은 거의 없지만, "PK가 바뀌면 참조도 같이 갱신된다"는 정책을 명시적으로 선언해두는 게 관례.
- **핵심 원리**: CASCADE와 RESTRICT를 가르는 기준은 "부모가 지워질 때 자식이 같이 사라져도 의미가 통하는가"임. User→자식은 통함(주인 없는 거래는 무의미), Category→자식은 안 통함(카테고리 삭제와 거래 기록 보존은 별개 문제).

## 백엔드 구현 판단

DB 설계(위 섹션)가 정해진 뒤, 그걸 JPA 코드로 어떻게 다룰지에 대한 판단. `backend/CLAUDE.md`의 각 규칙이 왜 그런지는 여기 참고.

- **FK 정책 재검증(2026-08-14)**: category가 CASCADE였다면 카테고리 1개("식비") 삭제라는 사소한 작업이 전체 사용자의 거래·예산을 연쇄 삭제하는 사고로 이어짐 — "행위 크기"와 "결과 크기"가 안 맞는 게 CASCADE의 위험. 지금은 카테고리 삭제 API 자체가 없어 이 경로가 당장 트리거되진 않지만, RESTRICT는 향후 관리 기능·DB 직접 조작까지 포함한 방어선. users CASCADE는 반대로 검증 — RESTRICT면 탈퇴 시 자식 데이터를 애플리케이션이 먼저 다 지워야 해서 절차만 복잡해짐.
- **`ddl-auto=validate`(update 아님)**: update는 위험한 변경(컬럼 삭제 등)을 조용히 스킵해 Entity-DB가 소리 없이 어긋날 수 있음. validate는 어긋나면 즉시 에러로 알려줘 FK 정책이 실제로 반영됐는지 신뢰할 수 있음 — 스키마 설계 검증 자체가 학습 목표라 이 신뢰가 필요함.
- **Category 사용자 정의 확장(나중 작업)은 같은 테이블에 `user_id` 컬럼 추가하는 안이 유력**: 별도 테이블로 나누면 `transaction.category_id`가 어느 테이블을 가리키는지 모호해짐. 조회 쿼리가 항상 소유자 조건을 강제하도록 Repository를 설계해야 다른 사용자의 개인 카테고리 노출을 막을 수 있음 — 실제 구현 시점에 재검토.
- **연관관계는 모두 `FetchType.LAZY` 명시**: `@ManyToOne`/`@OneToOne`은 기본값이 EAGER라 N+1 문제가 생길 수 있음. `@OneToMany`/`@ManyToMany`는 기본이 LAZY지만 일관성을 위해 명시. 필요한 곳만 JPQL `fetch join`으로 명시적 즉시 조회.
- **탈퇴 여부 확인은 매 요청 DB 재조회 — 최선이 아니라 ①단계용 단순화**: 실무에서는 Redis 블랙리스트(탈퇴 시 즉시 캐시에 기록), 세션 버전 패턴(이벤트마다 버전 증가, 토큰의 버전과 비교), 짧은 토큰 만료 중 하나가 더 흔하지만, 지금은 JWT 무상태성 트레이드오프(`docs/troubleshooting.md`)를 단순하게 체감하는 게 목표라 DB 재조회로 시작 — ④단계(Redis)에서 이 중 하나를 선택해 최적화 예정.
- **DTO는 Lombok 대신 Java `record` 사용**: 언어 표준 기능이라 Lombok 없이 불변 생성자·getter가 보장됨. Entity는 모든 필드가 강제 final이라 JPA 스펙상 record 사용 불가 — Entity는 계속 Lombok.
- **DTO는 `dto/request`/`dto/response`로 폴더 분리**: 한 클래스가 요청·응답을 겸하면 Mass Assignment 위험(요청에 `role`/`id` 등 임의 필드 주입)과 응답 시 내부 정보 노출 위험이 생김.
- **에러 응답에 `errorCode` 필드 추가(2026-08-28)**: 같은 401 안에 "토큰 문제"(재발급하면 해결)와 "도메인 검증 실패"(재발급해도 안 풀림)가 섞여 `authFetch`가 불필요한 재발급을 시도하던 문제 — 401을 403으로 바꾸는 대신 `ErrorResponse`에 `errorCode`(예: `TOKEN_INVALID`)를 추가해 세분화. 겪은 문제와 원인은 `docs/troubleshooting.md` 참고.
- **일반 가입자도 `users.provider`에 `"LOCAL"`을 명시(NULL 아님)**: `NULL`은 "값 없음/불명"을 뜻해 "일반 가입자임이 확실함"이라는 의도를 표현 못 함. 쿼리도 `WHERE provider IN ('LOCAL','GOOGLE')`이 `NULL` 비교(`= NULL`이 항상 거짓)보다 단순 — OAuth 스키마 설계 실무 컨센서스.
- **예외는 커스텀 클래스 + `GlobalExceptionHandler` 일괄 처리, 표준 예외 즉석 사용 금지**: `IllegalArgumentException`처럼 의미가 모호한 예외를 바로 던지면 못 잡을 시 500으로 뭉개져 사용자 잘못인지 서버 오류인지 구분이 안 됨 — 도메인 의미가 담긴 예외를 던지고 한 곳에서 상태 코드+메시지로 변환해 일관성 확보. `CustomAuthenticationEntryPoint`(401)/`CustomAccessDeniedHandler`(403)도 같은 원리(`docs/troubleshooting.md` 참고).
- **패키지 구조는 계층별(Package by Layer) 유지, 기능별은 채택 안 함**: 기능별은 여러 사람이 각자 다른 도메인을 동시에 건드리는 팀/대규모 서비스에 적합 — MoneyLog는 1인 프로젝트에 도메인도 6개뿐이라 이점이 없고, 계층별 구조가 Controller-Service-Repository 개념을 배우는 학습 목적에 더 명확함. `security`처럼 특정 도메인에 안 속하는 관심사는 별도 폴더로 분리.
- **RefreshToken은 JWT 자체 만료 검증 + DB `expires_at` 재검증을 함께 함(2026-08-22)**: `validateToken()`(서명·형식 검증)만으로는 DB에 저장된 토큰이 실제로 유효한지 확인할 수 없음 — `refresh()`에서 `expiresAt`을 DB와 재비교해, 나중에 특정 토큰을 즉시 무효화하는 관리 기능(강제 로그아웃 등)을 만들 여지도 남김.
- **httpOnly+CSRF 전환 시 CSRF 설정은 `CsrfConfigurer::spa()` 채택, 구버전 수동 설정은 안 씀(2026-08-23)**: 예전엔 `CookieCsrfTokenRepository`+커스텀 `CsrfTokenRequestHandler`까지 손으로 설정해야 했는데, Spring Security 7.0(Spring Boot 4.0과 짝)이 이 조합을 `http.csrf(CsrfConfigurer::spa)` 한 줄로 표준화. 결과물(쿠키/헤더 이름, httpOnly 여부)은 구버전과 동일해 프론트 코드엔 영향 없음.
- **닉네임 길이는 2자 이상 10자 이하로 제한(2026-08-28)**: 긴 닉네임 입력 시 UI가 깨지는 걸 확인, 네이버·카카오 등 한글 서비스의 일반적인 제한(대략 2~10자)을 참고해 `@Size(min=2, max=10)`으로 결정.
- **`UserService`가 Spring Security의 `UsernameNotFoundException`을 오용하던 것을 자체 `UserNotFoundException`으로 교체(2026-08-28)**: 이 클래스는 Spring Security 인증 예외 체계에 속해 `GlobalExceptionHandler`를 못 타고 `CustomAuthenticationEntryPoint`로 새어 엉뚱한 401 메시지가 나감 — "예외는 도메인 커스텀 클래스만" 원칙이 프레임워크 예외 재사용에도 그대로 적용됨을 보여준 사례.
- **쿠키 생성 로직을 `CookieUtils`로 공통화(2026-08-29)**: `AuthController`의 `buildCookie`와 동일한 코드가 `UserController.withdraw()`에도 필요해지며 중복 발생 — 쿠키 옵션이 흩어지면 배포 시 `secure(false)→true` 전환을 한 곳만 놓칠 위험이 있어 `security/CookieUtils`로 추출.
- **탈퇴 회원 이메일 재사용은 30일 유예 기간 동안 차단(2026-08-30)**: 즉시 허용하려면 `users.email` UNIQUE 제약을 부분 유니크로 바꿔야 해 변경 폭이 커짐 — 기존 유예 기간 설계와 맞물리는 "차단"으로 확정. `register()` 로직은 그대로 두고 에러 메시지만 "이미 가입된 이메일입니다" → "사용할 수 없는 이메일입니다"로 정정(탈퇴 계정에 "가입되어 있다"는 표현이 부정확했음).
- **`UserCleanupScheduler`를 `service`가 아닌 별도 `scheduler` 패키지로 분리(2026-08-29)**: "특정 도메인에 안 속하는 횡단 관심사는 별도 폴더로 분리"라는 패키지 구조 원칙을 스케줄러에도 적용 — 비즈니스 로직 제공자(`@Service`)가 아니라 주기적으로 실행되는 작업 단위(`@Component`)라는 점에서 성격이 다름.
- **`AuthService.refresh()`에도 탈퇴 여부 체크 추가(2026-08-29)**: 로그인 시점과 매 요청 인가에서만 확인하던 탈퇴 여부를, RefreshToken 유효기간만 보던 `refresh()`에도 추가 — 탈퇴 계정이 새 AccessToken을 계속 발급받을 수 있는 경로를 막음(Auth0/Cognito 등도 계정 비활성화 시 즉시 토큰 revoke가 표준).
- **거래 조회 N+1은 `fetch join`으로 해결, `@EntityGraph`는 보류(2026-09-08)**: `findByUserAndTransactionDateBetween...`에 `JOIN FETCH t.category` 추가. 지금은 페이징이 없어 `@Query`로 충분 — 페이징 도입 시 `fetch join`은 카운트 쿼리를 따로 관리해야 하는 문제가 있어, 그때 파생 메서드에 붙이기만 하면 되는 `@EntityGraph`로 재검토.
- **`Budget` 등록/수정 요청 DTO는 분리(`BudgetCreateRequest`/`BudgetUpdateRequest`), `Transaction`은 계속 공유(2026-09-09)**: `TransactionRequest`는 등록·수정에 필요한 필드가 완전히 같아 공유해도 검증 규칙이 안 어긋남. `Budget`은 수정이 금액만 바꾸는데(`Budget.update(Long amount)`) 등록은 카테고리·월·금액이 다 필요해, 하나의 DTO로 공유하면 수정 시 불필요한 필드까지 `@NotNull`로 요구하거나 등록 시 검증이 약해지는 문제가 생김 — "등록/수정 필드가 같으면 공유, 다르면 분리"가 기준.

## 프론트엔드 구현 판단

설계(위 섹션)가 정해진 뒤, 그걸 React 코드로 어떻게 다룰지에 대한 판단. `frontend/CLAUDE.md`의 각 규칙이 왜 그런지는 여기 참고.

- **`PrivateRoute`(라우트 가드)는 토큰을 직접 지우지 않고 리다이렉트 판단만 함(2026-08-22)**: 토큰 정리는 401을 실제로 받는 지점(`authFetch`)에서 하고, 라우트 가드는 이미 정리된 상태(`isLoggedIn`)만 보고 판단 — 정리 책임이 여러 곳으로 흩어지면 로직 변경 시 누락 위험이 커짐.
- **로그인/회원가입 요청은 `authFetch`가 아니라 별도 `api/auth.js`(`loginRequest`/`registerRequest`)로 분리(2026-08-18)**: `authFetch`는 "로그인된 사용자의 토큰 자동 첨부 + 401 시 refresh"가 목적인데, 로그인·회원가입 시점엔 토큰이 아예 없고 401도 "비밀번호 틀림"이지 "토큰 만료"가 아님 — "인증이 필요한 요청"과 "인증 자체를 처리하는 요청"을 파일 단위로 나누는 기준으로 삼음.
- **로그인 여부는 localStorage 대신 `GET /api/auth/me` 서버 응답으로 판단(2026-08-26)**: httpOnly+CSRF 전환 이후 토큰이 httpOnly 쿠키에 있어 자바스크립트가 존재 여부조차 알 수 없음 — `AuthProvider`가 마운트될 때마다 서버에 직접 물어보는 방식으로 전환. 확인이 끝나기 전(`isLoading`)에 `PrivateRoute`가 성급하게 `/login`으로 리다이렉트하지 않도록 별도 state로 분리(새로고침 시 잠깐 로그인 화면으로 튕기는 문제 방지).
- **`AuthProvider`의 초기 로그인 확인도 `fetch` 직접 호출이 아니라 `authFetch` 재사용(2026-08-26)**: 마운트 시 `fetch`로 직접 호출하면 AccessToken만 만료되고 RefreshToken은 아직 유효한 상태에서도 자동 refresh 없이 곧장 로그아웃 처리되는 문제가 있어, 401 시 자동 refresh를 갖춘 `authFetch`를 그대로 재사용. `refreshAccessToken`도 실패 시 예외 대신 boolean을 반환하도록 바꿔 미처리 예외를 방지.
- **`AuthProvider`의 로그인 상태 확인은 `async/await` 대신 `.then/.catch/.finally` 체이닝 사용(2026-08-26)**: `useEffect` 안에서 별도 `async function`을 선언해 호출하는 방식이 `eslint-plugin-react-hooks`의 `set-state-in-effect` 규칙에 걸림 — React 팀도 오탐(false positive) 사례로 인지 중인 최신 규칙(`react/react#34743`)이라 로직을 바꾸기보다 공식 문서 권장 방식(프로미스 체이닝)으로 우회.
- **`#root`의 폭 제한(1126px)+테두리를 `.auth-layout` 클래스로 분리(2026-09-04)**: 기존엔 `#root`에 전역으로 걸려 있어 모든 화면이 좁고 가운데 정렬된 카드 형태였음. 사이드바(`Sidebar`+`Layout`) 도입 시 이 제한이 그대로면 사이드바+콘텐츠가 화면 전체 너비를 못 씀 — 로그인/회원가입(좁은 카드 UI)과 로그인 후 화면(사이드바+전체 너비)의 레이아웃 요구가 달라, 폭 제한을 `LoginPage`/`SignupPage` 전용 `.auth-layout` wrapper로 옮김.
- **모달 등 공통 클래스명은 페이지 wrapper로 스코프 격리(2026-09-06)**: `TransactionsPage.css`가 `.modal-overlay` 등 전역 선택자를 `MyPage.css`와 동일한 이름으로 추가하면서, 나중에 로드된 파일이 이겨 마이페이지 회원탈퇴 버튼 색상이 깨지는 회귀가 발생. CSS Modules 없이 순수 CSS로 원리를 배우는 프로젝트라 클래스명이 파일 간에 그대로 전역으로 부딪힘 — 마크업은 그대로 두고 각 페이지의 wrapper 클래스(`.mypage`, `.transactions-page`)를 후손 선택자로 붙여 스코프만 분리. 앞으로도 `.modal-*`/`.error-message`처럼 흔히 재사용될 이름은 처음부터 wrapper 아래 후손 선택자로 작성하는 것을 기본 규칙으로 삼음.
