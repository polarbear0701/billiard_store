-- When the invoice was paid; revenue reporting is based on this, not on created_at.
ALTER TABLE invoice ADD COLUMN paid_at TIMESTAMP;

-- Best available approximation for invoices completed before this column existed.
UPDATE invoice
SET paid_at = COALESCE(updated_at, created_at)
WHERE complete = TRUE AND paid_at IS NULL;

CREATE INDEX idx_invoice_paid_at ON invoice (paid_at) WHERE complete = TRUE AND is_deleted = FALSE;
CREATE INDEX idx_invoice_session_code ON invoice (session_code);
CREATE INDEX idx_order_item_session_code ON order_item (session_code);
CREATE INDEX idx_play_session_start_time ON play_session (start_time);
CREATE INDEX idx_play_session_table_code ON play_session (table_code);
