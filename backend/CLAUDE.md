# Backend 컨벤션

루트 `CLAUDE.md`(목적/스택/진행순서)를 먼저 참고. 여기는 backend 코드를 짤 때 지키는 실무 규칙을 담는다. 판단 근거는 `docs/decisions.md`의 "백엔드 구현 판단" 섹션 참고.

## 패키지 구조
`com.moneylog.backend` 하위를 `controller`(요청/응답) / `service`(비즈니스 로직) / `repository`(DB 접근) / `entity`(`docs/db-schema.sql` 매핑) / `dto`(API 전달용, Entity 직접 노출 안 함) / `exception`(커스텀 예외 + 전역 처리기)으로 분리.
- 이유: 책임이 섞이면 문제 원인 구분이 어려워지고 테스트도 힘들어짐. Repository/Entity는 JPA 관례 이름이지만 역할 자체는 MyBatis 등에서도 이름만 바꿔(Mapper·DAO, VO) 동일하게 쓰이는 범용 개념.

## JPA 규칙
- `ddl-auto=validate` 고정, 스키마 변경은 `docs/db-schema.sql`을 먼저 고치고 반영한 뒤 Entity를 맞추는 순서.
- FK 정책은 스키마 그대로 반영(User 참조 CASCADE, Category 참조 RESTRICT).
- 모든 연관관계 `FetchType.LAZY` 명시(`@ManyToOne`/`@OneToOne`은 기본값이 EAGER라 필수), 필요한 곳만 `fetch join`.
- 카테고리 사용자 정의 확장(나중 작업)은 `category`에 `user_id` 컬럼 추가하는 안이 유력 후보.

## 코드 스타일
- Entity: `@Getter` + 필요한 것만(`@ToString(exclude=연관관계)`, `@EqualsAndHashCode(of="id")`) + `@NoArgsConstructor(PROTECTED)` + `@Builder`. `@Data`/setter 금지 — 상태 변경은 `changePassword()`처럼 의도가 담긴 메서드로만.
- DTO: Java `record` 사용, 필드 많으면 `@Builder` 추가. `dto/request`/`dto/response`로 폴더 분리.
- 생성자 주입만 사용(필드 `@Autowired` 금지) — 생성자가 하나면 Spring이 자동 인식해 `@Autowired` 생략 가능.

## 인증/보안
- Spring Boot 4.0.7 → Spring Security 7.0.6(`./gradlew dependencies`로 확인, 버전은 항상 재확인하고 추측 금지). 구버전 DSL(`authorizeRequests()` 등) 금지.
- JWT AccessToken 30분/RefreshToken 7일, RefreshToken은 `refresh_token` 테이블 저장(④단계 전까지).
- 탈퇴 여부는 매 요청 DB 재조회(`existsByEmailAndDeletedAtIsNull`) — ①단계용 단순화. ④단계에서 Redis 블랙리스트/세션 버전 패턴으로 최적화 예정.

## 예외 처리
- 도메인 의미가 담긴 커스텀 예외(`DuplicateEmailException` 등, 앞으로 생길 유사 상황도 같은 패턴)를 던지고, `GlobalExceptionHandler`(`@RestControllerAdvice`) 하나가 모든 예외→HTTP 응답(상태 코드+메시지)을 일괄 변환. 자바 표준 예외(`IllegalArgumentException` 등) 즉석 사용 금지 — 의미가 모호하고 처리 로직이 Controller마다 흩어짐.

## 테스트
- ①단계 코드를 실제로 짜면서 정함(첫 테스트를 같이 작성하고 그 경험으로 규칙 정리).
