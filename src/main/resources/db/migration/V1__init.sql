-- =========================================================
-- V1__init.sql
-- Ditda 초기 schema
--
-- 정책:
--  - Enum: VARCHAR + @Enumerated(EnumType.STRING) 매핑
--  - Timestamp: DATETIME(6) 사용
--  - BaseEntity(@MappedSuperclass): 각 테이블에 인라인으로 전개
--  - 제약 네이밍 : Hibernate 자동 생성 난수 이름 대신 명시적 이름 사용
--               (PK 컬럼 기본, uk_<table>_<cols> / idx_<table>_<cols> / fk_<table>_<cols>)
-- =========================================================


-- =========================================================
-- ADMIN
-- =========================================================

CREATE TABLE admins
(
    admin_id   BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(20)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (admin_id),
    UNIQUE KEY uk_admins_username (username)
);

-- =========================================================
-- USER
-- =========================================================

CREATE TABLE users
(
    user_id           BIGINT       NOT NULL AUTO_INCREMENT,
    username          VARCHAR(20)  NOT NULL,
    password          VARCHAR(255) NOT NULL,
    name              VARCHAR(50)  NOT NULL,
    email             VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(255) NOT NULL,
    phone             VARCHAR(20)  NOT NULL,
    role              VARCHAR(20)  NOT NULL,
    email_verified_at DATETIME(6) DEFAULT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_username (username)
);

CREATE TABLE designers
(
    designer_id    BIGINT       NOT NULL,
    level          VARCHAR(20)  NOT NULL,
    exp            INT          NOT NULL,
    bank_name      VARCHAR(50)  NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    account_holder VARCHAR(50)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (designer_id),
    CONSTRAINT fk_designers_user_id FOREIGN KEY (designer_id) REFERENCES users (user_id)
);

CREATE TABLE instructors
(
    instructor_id BIGINT      NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (instructor_id),
    CONSTRAINT fk_instructors_user_id FOREIGN KEY (instructor_id) REFERENCES users (user_id)
);

CREATE TABLE portfolios
(
    portfolio_id  BIGINT       NOT NULL AUTO_INCREMENT,
    designer_id   BIGINT       NOT NULL,
    portfolio_url VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (portfolio_id),
    KEY idx_portfolios_designer_id (designer_id),
    CONSTRAINT fk_portfolios_designer_id FOREIGN KEY (designer_id) REFERENCES designers (designer_id)
);

-- =========================================================
-- AUTH
-- =========================================================

CREATE TABLE refresh_tokens
(
    session_id         VARCHAR(36)  NOT NULL,
    user_id            BIGINT       NOT NULL,
    refresh_token_hash VARCHAR(128) NOT NULL,
    expires_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (session_id),
    KEY idx_refresh_tokens_user_id (user_id),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- =========================================================
-- COMMISSION
-- =========================================================

CREATE TABLE commissions
(
    commission_id        BIGINT      NOT NULL AUTO_INCREMENT,
    instructor_id        BIGINT      NOT NULL,
    assigned_designer_id BIGINT       DEFAULT NULL,
    plan_code            VARCHAR(10) NOT NULL,
    title                VARCHAR(50) NOT NULL,
    category_type        VARCHAR(50) NOT NULL,
    size                 VARCHAR(10) NOT NULL,
    additional_concept   VARCHAR(300) DEFAULT NULL,
    color_selection_mode VARCHAR(30) NOT NULL,
    first_draft_deadline DATE        NOT NULL,
    application_deadline DATE        NOT NULL,
    final_deadline       DATE        NOT NULL,
    status               VARCHAR(30) NOT NULL,
    max_revision         INT         NOT NULL,
    selected_at          DATETIME(6)  DEFAULT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NOT NULL,
    PRIMARY KEY (commission_id),
    KEY idx_commissions_assigned_designer_id (assigned_designer_id),
    KEY idx_commissions_instructor_id (instructor_id),
    CONSTRAINT fk_commissions_assigned_designer_id FOREIGN KEY (assigned_designer_id) REFERENCES designers (designer_id),
    CONSTRAINT fk_commissions_instructor_id FOREIGN KEY (instructor_id) REFERENCES instructors (instructor_id)
);


CREATE TABLE commission_colors
(
    commission_color_id BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id       BIGINT      NOT NULL,
    color_code          VARCHAR(7)  NOT NULL,
    role                VARCHAR(10) NOT NULL,
    PRIMARY KEY (commission_color_id),
    UNIQUE KEY uk_commission_colors_commission_id_role (commission_id, role),
    CONSTRAINT fk_commission_colors_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);

CREATE TABLE commission_concepts
(
    commission_concept_id BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id         BIGINT      NOT NULL,
    concept               VARCHAR(30) NOT NULL,
    PRIMARY KEY (commission_concept_id),
    UNIQUE KEY uk_commission_concepts_commission_id_concept (commission_id, concept),
    CONSTRAINT fk_commission_concepts_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);

CREATE TABLE commission_files
(
    commission_file_id BIGINT       NOT NULL AUTO_INCREMENT,
    commission_id      BIGINT       NOT NULL,
    file_kind          VARCHAR(20)  NOT NULL,
    file_url           VARCHAR(255) NOT NULL,
    description        VARCHAR(300) DEFAULT NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (commission_file_id),
    KEY idx_commission_files_commission_id (commission_id),
    CONSTRAINT fk_commission_files_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);


-- =========================================================
-- APPLICATION
-- =========================================================

CREATE TABLE commission_applications
(
    commission_application_id BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id             BIGINT      NOT NULL,
    designer_id               BIGINT      NOT NULL,
    status                    VARCHAR(20) NOT NULL,
    active_key                VARCHAR(64) GENERATED ALWAYS AS (
        CASE
            WHEN status <> 'CANCELLED'
                THEN CONCAT(commission_id, '-', designer_id) END
        ) VIRTUAL,
    created_at                DATETIME(6) NOT NULL,
    updated_at                DATETIME(6) NOT NULL,
    PRIMARY KEY (commission_application_id),
    UNIQUE KEY uk_commission_applications_active_key (active_key),
    KEY idx_commission_applications_commission_id (commission_id),
    KEY idx_commission_applications_designer_id (designer_id),
    CONSTRAINT fk_commission_applications_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id),
    CONSTRAINT fk_commission_applications_designer_id FOREIGN KEY (designer_id) REFERENCES designers (designer_id)
);

-- =========================================================
-- DRAFT
-- =========================================================

CREATE TABLE commission_drafts
(
    commission_draft_id       BIGINT      NOT NULL AUTO_INCREMENT,
    commission_application_id BIGINT      NOT NULL,
    round                     INT         NOT NULL,
    created_at                DATETIME(6) NOT NULL,
    updated_at                DATETIME(6) NOT NULL,
    PRIMARY KEY (commission_draft_id),
    UNIQUE KEY uk_commission_drafts_commission_application_id_round (commission_application_id, round),
    CONSTRAINT fk_commission_drafts_commission_application_id FOREIGN KEY (commission_application_id) REFERENCES commission_applications (commission_application_id)
);


CREATE TABLE commission_draft_files
(
    commission_draft_file_id BIGINT       NOT NULL AUTO_INCREMENT,
    commission_draft_id      BIGINT       NOT NULL,
    file_order               INT          NOT NULL,
    file_url                 VARCHAR(255) NOT NULL,
    watermarked_file_url     VARCHAR(255) DEFAULT NULL,
    watermark_status         VARCHAR(20)  NOT NULL,
    created_at               DATETIME(6)  NOT NULL,
    updated_at               DATETIME(6)  NOT NULL,
    PRIMARY KEY (commission_draft_file_id),
    UNIQUE KEY uk_commission_draft_files_commission_draft_id_file_order (commission_draft_id, file_order),
    CONSTRAINT fk_commission_draft_files_commission_draft_id FOREIGN KEY (commission_draft_id) REFERENCES commission_drafts (commission_draft_id)
);

-- =========================================================
-- REVISION
-- =========================================================

CREATE TABLE revision_requests
(
    revision_request_id BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id       BIGINT      NOT NULL,
    target_draft_id     BIGINT      NOT NULL,
    checked             BIT(1)      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    PRIMARY KEY (revision_request_id),
    UNIQUE KEY uk_revision_requests_target_draft_id (target_draft_id),
    KEY idx_revision_requests_commission_id (commission_id),
    CONSTRAINT fk_revision_requests_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id),
    CONSTRAINT fk_revision_requests_target_draft_id FOREIGN KEY (target_draft_id) REFERENCES commission_drafts (commission_draft_id)
);

CREATE TABLE revision_responses
(
    revision_request_id BIGINT      NOT NULL,
    produced_draft_id   BIGINT      NOT NULL,
    designer_comment    VARCHAR(300) DEFAULT NULL,
    checked             BIT(1)      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    PRIMARY KEY (revision_request_id),
    UNIQUE KEY uk_revision_responses_produced_draft_id (produced_draft_id),
    CONSTRAINT fk_revision_responses_produced_draft_id FOREIGN KEY (produced_draft_id) REFERENCES commission_drafts (commission_draft_id),
    CONSTRAINT fk_revision_responses_revision_request_id FOREIGN KEY (revision_request_id) REFERENCES revision_requests (revision_request_id)
);

CREATE TABLE revision_details
(
    revision_detail_id  BIGINT       NOT NULL AUTO_INCREMENT,
    revision_request_id BIGINT       NOT NULL,
    category            VARCHAR(20)  NOT NULL,
    comment             VARCHAR(300) NOT NULL,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NOT NULL,
    PRIMARY KEY (revision_detail_id),
    KEY idx_revision_details_revision_request_id (revision_request_id),
    CONSTRAINT fk_revision_details_revision_request_id FOREIGN KEY (revision_request_id) REFERENCES revision_requests (revision_request_id)
);

-- =========================================================
-- TEXTBOOK
-- =========================================================

CREATE TABLE textbooks
(
    commission_id   BIGINT      NOT NULL,
    title           VARCHAR(50) NOT NULL,
    instructor_name VARCHAR(50) NOT NULL,
    subject         VARCHAR(50) NOT NULL,
    PRIMARY KEY (commission_id),
    CONSTRAINT fk_textbooks_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);

CREATE TABLE textbook_pages
(
    textbook_page_id BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id    BIGINT      NOT NULL,
    page_type        VARCHAR(30) NOT NULL,
    page_description VARCHAR(150) DEFAULT NULL,
    PRIMARY KEY (textbook_page_id),
    UNIQUE KEY uk_textbook_pages_commission_id_page_type (commission_id, page_type),
    CONSTRAINT fk_textbook_pages_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);

-- =========================================================
-- PAYMENT
-- =========================================================

CREATE TABLE payments
(
    payment_id          BIGINT      NOT NULL AUTO_INCREMENT,
    commission_id       BIGINT      NOT NULL,
    amount              INT         NOT NULL,
    depositor_name      VARCHAR(50) DEFAULT NULL,
    status              VARCHAR(30) NOT NULL,
    paid_at             DATETIME(6) DEFAULT NULL,
    deposit_notified_at DATETIME(6) DEFAULT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    PRIMARY KEY (payment_id),
    KEY idx_payments_commission_id (commission_id),
    CONSTRAINT fk_payments_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id)
);

-- =========================================================
-- SETTLEMENT
-- =========================================================

CREATE TABLE settlements
(
    settlement_id   BIGINT      NOT NULL AUTO_INCREMENT,
    designer_id     BIGINT      NOT NULL,
    commission_id   BIGINT      NOT NULL,
    settlement_type VARCHAR(20) NOT NULL,
    amount          INT         NOT NULL,
    status          VARCHAR(20) NOT NULL,
    settled_at      DATETIME(6) DEFAULT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_settlements_designer_id_commission_id_settlement_type (designer_id, commission_id, settlement_type),
    KEY idx_settlements_commission_id (commission_id),
    CONSTRAINT fk_settlements_commission_id FOREIGN KEY (commission_id) REFERENCES commissions (commission_id),
    CONSTRAINT fk_settlements_designer_id FOREIGN KEY (designer_id) REFERENCES designers (designer_id)
);

-- =========================================================
-- OUTBOX
-- =========================================================

CREATE TABLE notification_outboxes
(
    notification_outbox_id BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_email        VARCHAR(100) NOT NULL,
    subject                VARCHAR(150) NOT NULL,
    template_name          VARCHAR(100) NOT NULL,
    template_variables     TEXT         NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    scheduled_at           DATETIME(6)  NOT NULL,
    retry_count            INT          NOT NULL,
    error_message          TEXT,
    sent_at                DATETIME(6) DEFAULT NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,
    PRIMARY KEY (notification_outbox_id),
    KEY idx_notification_outboxes_status_scheduled_at (status, scheduled_at)
);

-- =========================================================
-- TERM
-- =========================================================

CREATE TABLE user_terms
(
    user_term_id BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    term_type    VARCHAR(50) NOT NULL,
    version      VARCHAR(20) NOT NULL,
    is_agreed    BIT(1)      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    PRIMARY KEY (user_term_id),
    KEY idx_user_terms_user_id (user_id),
    CONSTRAINT fk_user_terms_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE payment_terms
(
    payment_term_id BIGINT      NOT NULL AUTO_INCREMENT,
    payment_id      BIGINT      NOT NULL,
    term_type       VARCHAR(50) NOT NULL,
    version         VARCHAR(20) NOT NULL,
    is_agreed       BIT(1)      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (payment_term_id),
    KEY idx_payment_terms_payment_id (payment_id),
    CONSTRAINT fk_payment_terms_payment_id FOREIGN KEY (payment_id) REFERENCES payments (payment_id)
);
