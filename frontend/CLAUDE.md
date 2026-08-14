# Frontend 컨벤션

루트 `CLAUDE.md`(목적/스택/진행순서)를 먼저 참고. 여기는 frontend 코드를 짤 때 지키는 실무 규칙과 그 이유를 담는다.

## 스타일링 — 순수 CSS
- Tailwind/Bootstrap/Sass 등 프레임워크·전처리기를 의도적으로 배제. 컴포넌트별 `.css` 파일(예: `LoginForm.jsx` + `LoginForm.css`)로 작성.
- 이유: 지금 학습 목표는 React 자체와 CSS 원리(Flexbox/Grid, cascade, CSS 변수)를 직접 익히는 것. 프레임워크는 이 원리를 감싼 문법일 뿐이라 원리를 먼저 다진 뒤 얹는 게 순서상 맞음(Tailwind가 실무 표준인 건 맞지만, 원리를 아는 사람이 생산성 도구로 선택하는 것이지 원리 대신 배우는 게 아님). Tailwind/Sass는 2차 프로젝트 이후 도입 후보.
- 색상/간격/폰트는 `index.css`의 `:root` CSS 변수로 토큰화하고 `@media (prefers-color-scheme: dark)`로 다크모드 오버라이드(`design-reference`에 라이트/다크 쌍이 있으므로 처음부터 변수 기반으로 짤 것).
- 디자인 기준은 `docs/design-reference/`의 스크린샷 — 코드 복사가 아니라 레이아웃·간격·색을 보고 새로 작성.

## 상태 관리 — 별도 라이브러리 없이 React Context
- 지금 전역 상태로 볼 만한 건 로그인 여부/사용자 정보 정도뿐. 거래·예산·대시보드 데이터는 각 페이지가 API로 그때그때 가져오는 서버 상태라 전역 상태가 아님.
- Redux 등은 상태가 복잡하게 얽힐 때 진가를 발휘하는데, 지금 규모에 쓰면 보일러플레이트만 늘고 얻는 이득이 없음. Context+useState로 로그인 여부만 관리.
- 값 저장은 `localStorage`, 리렌더링 트리거는 Context state로 역할을 분리(localStorage 변화 자체는 React가 감지 못하기 때문).
- 나중에 상태가 실제로 복잡해지면 다음 후보는 Redux가 아니라 Zustand(2023~2025 사이 신규 프로젝트의 실질 표준으로 부상, 보일러플레이트가 적음).

## 컴포넌트 — 함수형 + Hooks만
- 클래스 컴포넌트는 쓰지 않음. React 공식 문서도 함수형 기준으로 전환됐고, 신규 프로젝트에서 클래스로 시작하는 경우는 사실상 없음.
- 레거시 클래스 컴포넌트를 나중에 읽게 되더라도 개념은 Hooks와 1:1 대응됨(`this.state`=`useState`, `componentDidMount`=`useEffect(fn, [])`) — 지금 미리 배울 필요 없이, 필요한 순간 대응 관계로 훑으면 충분.

## API 호출 — `src/api/`로 분리, fetch 직접 사용
- 컴포넌트 안에서 `fetch`를 직접 호출하지 않고 `src/api/`의 함수를 통해서만 호출.
- 이유: 여러 화면(로그인/거래/예산/목표자산 등)이 같은 baseURL·헤더·에러 처리·토큰 첨부 로직을 반복하면 API 스펙이나 인증 방식이 바뀔 때 모든 컴포넌트를 일일이 고쳐야 함.
- `authFetch` 패턴(토큰 자동 첨부 + 401 시 refresh 후 재시도)이 로그인 이후 거의 모든 요청에 공통으로 필요하므로 한 곳에서만 구현.
- axios 대신 fetch를 쓰는 이유: axios의 인터셉터가 하는 일이 정확히 이 `authFetch`이므로, 라이브러리로 기능을 가져다 쓰기보다 왜 그 기능이 필요한지 직접 구현해보며 이해하는 쪽을 택함. axios 자체는 실무에서 흔히 쓰이므로 알아야 하는 도구지만, 원리를 먼저 익힌 뒤 봐야 "왜 이렇게 설계됐는지"가 바로 이해됨.

## 인증 토큰 저장 — localStorage (전환 시점은 미확정)
- AccessToken/RefreshToken은 `localStorage`에 저장하고 `Authorization` 헤더로 전송. httpOnly 쿠키+CSRF가 실무 표준(XSS에 더 안전)이지만, 지금은 JWT 흐름 자체를 신규 문제로 격리해서 익히기 위해 localStorage로 시작. 전환 시점은 ①단계 완주 후 판단 — 상세 트레이드오프는 `docs/decisions.md`의 "①단계 토큰 저장" 참고.

## 폴더 구조 (①단계부터 적용)
- `src/pages/` — 라우트 단위 화면(LoginPage, SignupPage, MyPage 등)
- `src/components/` — 여러 화면에서 재사용하는 조각(Header, Button 등)
- `src/api/` — 백엔드 호출 함수 모음(`authFetch` 포함)
- `src/context/` — 로그인 상태 등 전역 상태(React Context)

## 테스트
- 별도 테스트 프레임워크는 아직 도입하지 않음 — `npm run dev`로 실제 화면 동작 확인이 기본 검증 방법.
