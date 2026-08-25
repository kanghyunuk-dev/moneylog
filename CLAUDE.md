# MoneyLog 프로젝트 가이드

## 문서 지도
이 파일은 "지금 뭘 하기로 했는가 + AI가 항상 지켜야 할 규칙"만 짧게 담는다. 상세 내용은 아래를 참고:
- **`docs/specs.md`** — 무엇을 만들기로 했는가 (화면+API 단위 기능 명세)
- **`docs/decisions.md`** — 왜 이렇게 설계/선택했는가 (기술 스택, DB 설계 판단 등)
- **`docs/troubleshooting.md`** — 어떤 문제를 겪고 어떻게 풀었는가 (인증 개념 학습 기록 포함)
- **`docs/setup.md`** — 새 컴퓨터에서 시작할 때 체크리스트
- **`docs/db-schema.sql`** — 실행 가능한 DB 스키마 (정답 소스)
- **`docs/design-reference/`** — 화면 디자인 참고 스크린샷

## 이 프로젝트를 하는 목적 (왜)
1. **바이브코딩 체화** — 작게 나눠 시키기, 검증 후 수락, 정확한 컨텍스트 제공, 룰셋, 프롬프트 루프, Git 세이프티, 에러/테스트, 자율성 관리, MCP 같은 원칙을 실제 역량으로 남기는 것이 목표. 완성된 앱 자체보다 "이 과정을 거쳤다"는 경험이 진짜 산출물.
2. **인증 확장 기술 학습** — 소셜로그인(OAuth2), Redis 연동을 이번 프로젝트에서 직접 구현하며 익힌다.
3. **React 실전 감각 익히기** — TypeScript는 이번엔 배제하고 React+JS 단독으로 먼저 익힌다. 완료 목표: 에러가 났을 때 "React 문제인지 순수 JS 문제인지"를 스스로 구분할 수 있는 수준.
4. **주제 선정 이유** — 단순 CRUD를 넘어서는 집계/통계 로직이 있어 학습 밀도가 적당하고, 개인 프로젝트로 다루기 좋은 규모의 도메인이라 선택.

**새 기능을 추가할지 판단하는 기준**: ① 바이브코딩 원칙을 연습할 거리가 있는가 → ② 소셜로그인/Redis 같은 미경험 기술에 해당하는가 → ③ 지금 도메인(1인용 가계부)에 자연스러운가.

## 학습 방침 (AI 활용 원칙) — 중요
- AI가 제안한 코드를 그대로 적용하지 않는다 — 검증하고 이해한 뒤에만 반영한다.
- 막히는 개념이 나오면 먼저 설명을 요청 → 이해 확인 → 그 다음 구현 순서로 진행한다.
- 코드를 바로 생성하기보다, 무엇을·왜 바꾸는지 먼저 설명한 뒤 구현할 것.
- 불확실한 부분은 추측하지 말고 사용자에게 질문할 것.

## 문서 관리 원칙 — 중요
- **새로운 내용이 생기면 CLAUDE.md에 계속 추가하지 않는다.** "무엇을 만들지"는 `docs/specs.md`, "왜 이렇게 했는지"는 `docs/decisions.md`, "문제/해결 기록"은 `docs/troubleshooting.md`로 보낸다. CLAUDE.md에는 그 문서를 가리키는 포인터만 남긴다.
- **"현재 상태" 섹션은 작업할 때마다 실제 상태와 맞는지 확인하고 갱신한다.** 과거 기록과 현재 상태가 섞이면 안 됨 — 지난 시도/변경 과정은 `docs/troubleshooting.md`나 `docs/decisions.md`로, 지금 유효한 결론만 여기 남긴다.
- **커밋 전에는 항상 "현재 상태" 섹션이 실제 변경사항과 일치하는지 먼저 확인하고, 필요하면 갱신 여부를 먼저 묻는다.**

## 스택
- 프론트: React (JS) — TypeScript 배제
- 백엔드: Java 21 + Spring Boot 4.x (Gradle) + JPA + MySQL
- Node.js / Python은 이번 프로젝트 범위에서 제외
- 버전/기술 선택 이유는 `docs/decisions.md` 참고 (Spring Boot 4.x 채택 배경, JPA vs MyBatis, Vite vs Next.js 등)

## 진행 순서
| 단계 | 기능 | 목적 | 상태 |
|---|---|---|---|
| ① | 로그인/회원가입(JWT) | 인증 기초 — JWT 기반 인증 구조 설계 및 구현 | 착수함(`feature/auth`) |
| ② | 거래/예산/카테고리/대시보드 | React+Spring Boot 기초(폼, API 연동, JPA, 상태관리) 체화 | 예정 |
| ②-2 | 목표자산(Goal) | ②단계 완료 후 확장 | 예정 |
| ③ | 소셜로그인(구글) | OAuth2 기반 인증 확장 학습 | 예정 |
| ④ | Redis 연동 | 세션/토큰 캐시 관리, JWT 무상태성 트레이드오프 최적화 | 예정 |
| ⑤ | 환율 API 연동(선택) | 외부 REST API 연동 실무 경험 | 선택 |

**제외**: 관리자 페이지 (1인용 도메인에 부적합, 2차 게시판형 프로젝트로 이관). 순서 변경 이유, 범위 결정 배경은 `docs/decisions.md` 참고.

## 현재 상태 (2026-08-25 기준)
- [x] JWT/Spring Security 인증 개념 학습 완료 (8단계, `docs/troubleshooting.md` 참고)
- [x] ①②②-2③ 기능 명세 확정 (`docs/specs.md`)
- [x] DB 스키마 6개 테이블 설계 + MySQL 실행 검증 완료 (`docs/db-schema.sql`)
- [x] Figma Make 디자인 확보 + 명세 대조 완료 (`docs/design-reference/`)
- [x] 백엔드 뼈대 생성 (Spring Boot 4.0.7, `./gradlew test` 성공)
- [x] 프론트 뼈대 생성 (Vite+React, `npm run dev` 성공)
- [x] `frontend/CLAUDE.md` 작성 완료 (스타일링/상태관리/컴포넌트/API 분리/토큰 저장 방침)
- [x] `backend/CLAUDE.md` 작성 완료 (패키지 구조/JPA 규칙/코드 스타일/인증 보안, 판단 근거는 `docs/decisions.md` "백엔드 구현 판단" 참고)
- [x] ①단계 1/8단계(회원가입+BCrypt) 완료
- [x] ①단계 2~6/8단계 완료 — Spring Security 골격, JWT 발급/검증, 401/403 처리, CORS, 프론트 로그인·회원가입·홈 화면 및 라우트 보호까지 실동작 검증 완료(`feature/auth` 브랜치)
- [x] localStorage → httpOnly+CSRF 전환 착수 — 백엔드(AuthController 쿠키 발급, JWTAuthorizationFilter 쿠키 인식, SecurityConfig CSRF 재활성화) 완료, 판단 근거는 `docs/decisions.md` "백엔드 구현 판단" 참고
- [ ] httpOnly+CSRF 전환 나머지 — 프론트(authFetch/auth.js/AuthProvider 쿠키 기반 전환), 로그인 상태 확인 API·로그아웃 API 신설 필요
- [ ] ①단계 나머지(7~8단계: 마이페이지, 소프트삭제) 순서대로 진행
- [ ] ②단계 이후 순서대로 진행

다른 컴퓨터에서 이어갈 때는 `docs/setup.md` 체크리스트부터 확인.

## 배경
2차 프로젝트(게시판형 서비스, 도메인 미정): MoneyLog 완료 후 별도 저장소로 진행 예정. React+TypeScript 도입, 파일업로드/댓글/좋아요·찜(N:M)/페이지네이션/관리자 페이지, MyBatis 후보.
