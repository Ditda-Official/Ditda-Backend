ALTER TABLE commission_draft_files
    ADD COLUMN watermark_retry_count INT NOT NULL DEFAULT 0;