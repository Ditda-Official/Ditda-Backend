-- ===========================================================================
-- Description: notification_outboxes 스키마를 type 기반으로 전환
--
-- Background:
--  - 메일 발송을 워커(RabbitMQ)로 분리하면서, Outbox가 subject/template 대신 NotificationType(enum)만 저장하도록 변경
--
-- Note:
--  - 실행 전 PENDING 행이 없는지 확인 (기존 행은 notification_type=''로 채워짐)
--  - subject / template_name 컬럼 제거
-- ===========================================================================

ALTER TABLE notification_outboxes
    ADD COLUMN notification_type VARCHAR(50) NOT NULL,
    DROP COLUMN subject,
    DROP COLUMN template_name;