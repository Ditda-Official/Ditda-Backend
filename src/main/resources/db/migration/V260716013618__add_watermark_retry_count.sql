-- ===========================================================================
-- Description: commission_draft_files에 워터마크 재처리 횟수 컬럼 추가
--
-- Background:
--  - 워터마크 재처리 스케줄러 도입으로 파일별 재시도 횟수 추적이 필요함.
--
-- Note:
--  - 기본값의 단일 진실은 엔티티(@Builder.Default)이며 DB DEFAULT 절은 사용하지 않음.
-- ===========================================================================

ALTER TABLE commission_draft_files ADD COLUMN watermark_retry_count INT;
UPDATE commission_draft_files SET watermark_retry_count = 0;
ALTER TABLE commission_draft_files MODIFY COLUMN watermark_retry_count INT NOT NULL;