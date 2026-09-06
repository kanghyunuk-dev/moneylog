-- MoneyLog DB 스키마
-- 대상: MySQL 8.0
-- 범위: ①(로그인) ②(거래/예산/카테고리/대시보드) ②-2(목표자산) ③(소셜로그인) 전체 기능 명세 기반
-- 설계 이유 상세는 docs/decisions.md의 "DB 설계 판단" 섹션 참고.
--
-- FK 정책 (2026-08-07 추가):
--   users 참조(모든 테이블) : ON DELETE CASCADE, ON UPDATE CASCADE
--     - 회원이 삭제되면(UserCleanupScheduler가 30일 뒤 하드 삭제) 연관 데이터도 자동 정리.
--   category 참조(transaction, budget) : ON DELETE RESTRICT, ON UPDATE CASCADE
--     - category는 고정 시드 데이터라 삭제될 일이 없어야 함 — 실수로 지우려 하면 DB가 막아줌.
--   ON UPDATE CASCADE는 모든 FK에 공통 적용 — PK는 AUTO_INCREMENT라 실제로 값이 바뀔 일은
--   거의 없지만, "PK 값이 바뀌면 참조도 같이 바뀐다"는 정책을 명시적으로 선언해두는 것이 관례.
--
-- 테이블/컬럼명 참고 (2026-08-07): 원래 User/`year_month`로 설계했으나 실제로 MySQL에 실행해
-- 검증하던 중 둘 다 MySQL 예약어와 충돌해 문법 에러가 발생함(USER는 시스템 계정 관련 예약어,
-- YEAR_MONTH는 INTERVAL 구문 키워드). 매번 백틱으로 이스케이프하는 대신 users / budget_month로
-- 테이블·컬럼명 자체를 바꿔 근본 해결 — JPA 엔티티 매핑 등 이후 모든 지점에서 이스케이프를
-- 신경 쓸 필요가 없어짐.

-- ============================================================
-- User (회원)
-- ============================================================
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,          -- 로그인 아이디로 사용 (별도 username 없음)
    password    VARCHAR(255) NULL,                     -- 구글 전용 가입자는 비밀번호를 받지 않으므로 NULL 허용
    nickname    VARCHAR(50)  NOT NULL,
    provider    VARCHAR(20)  NULL,                     -- "LOCAL" / "GOOGLE"
    provider_id VARCHAR(100) NULL,                     -- 구글이 부여한 고유 ID
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    deleted_at  DATETIME     NULL                      -- 소프트 삭제 (NULL이면 활성 회원)
);

-- ============================================================
-- RefreshToken
-- email 문자열로 느슨하게 연결하는 방식 대신, 정합성을 위해 user_id(FK)로 연결.
-- UNIQUE(user_id) — 사용자당 최신 토큰 1개만 유지.
-- ============================================================
CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL UNIQUE,
    token      VARCHAR(500) NOT NULL,
    expires_at DATETIME     NOT NULL,
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================================
-- Category (카테고리, 고정 목록)
-- 나중에 사용자 정의 카테고리를 붙일 때 이 테이블에 사용자 소유 행을
-- 추가하는 구조로 확장 가능하도록 테이블로 설계 (Java enum 대신).
-- ============================================================
CREATE TABLE category (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    type VARCHAR(10) NOT NULL                          -- "INCOME" / "EXPENSE"
);

-- ============================================================
-- Transaction (거래 기록)
-- type 컬럼을 두지 않음 — 수입/지출 구분은 항상 category.type을 JOIN해서 판단(정규화).
-- amount는 원화 정수 (소수점 불필요, 부동소수점 오차 회피).
-- ============================================================
CREATE TABLE transaction (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT   NOT NULL,
    category_id      BIGINT   NOT NULL,
    amount           BIGINT   NOT NULL,
    transaction_date DATE     NOT NULL,
    memo             VARCHAR(200) NULL,
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ============================================================
-- Budget (예산)
-- (user_id, category_id, budget_month) 조합 UNIQUE — 같은 달·같은 카테고리 예산 중복 방지.
-- budget_month는 "일자"가 의미 없어 DATE 대신 문자열("YYYY-MM")로 저장.
-- ============================================================
CREATE TABLE budget (
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    category_id   BIGINT      NOT NULL,
    budget_month  VARCHAR(7)  NOT NULL,
    amount        BIGINT      NOT NULL,
    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT uk_budget_user_category_month
        UNIQUE (user_id, category_id, budget_month)
);

-- ============================================================
-- Goal (목표자산)
-- status: 활성(진행 중) 1개 + 대기 중 여러 개.
-- display_order: 활성 목표가 DONE 되었을 때 다음 ACTIVE 될 대기 목표 순서 결정용.
-- ============================================================
CREATE TABLE goal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(50)  NOT NULL,
    target_amount   BIGINT       NOT NULL,
    current_amount  BIGINT       NOT NULL DEFAULT 0,
    deadline        DATE         NULL,
    status          VARCHAR(10)  NOT NULL,             -- "ACTIVE" / "WAITING" / "DONE"
    display_order   INT          NOT NULL,
    created_at      DATETIME     NOT NULL,
    CONSTRAINT fk_goal_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================================
-- Category 시드 데이터
-- 지출 10개 + 수입 5개 (docs/specs.md "②단계" 확정 목록)
-- ============================================================
INSERT INTO category (name, type) VALUES
    ('식비', 'EXPENSE'),
    ('교통비', 'EXPENSE'),
    ('주거비', 'EXPENSE'),
    ('통신비', 'EXPENSE'),
    ('문화/여가', 'EXPENSE'),
    ('의료/건강', 'EXPENSE'),
    ('쇼핑', 'EXPENSE'),
    ('교육', 'EXPENSE'),
    ('경조사/선물', 'EXPENSE'),
    ('기타', 'EXPENSE'),
    ('급여', 'INCOME'),
    ('용돈', 'INCOME'),
    ('부수입', 'INCOME'),
    ('금융소득', 'INCOME'),
    ('기타', 'INCOME');
