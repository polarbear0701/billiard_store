CREATE TABLE audit_log (
	id UUID PRIMARY KEY,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_code VARCHAR(100),
    old_value JSONB,
    new_value JSONB
);

CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_code);
CREATE INDEX idx_audit_log_actor ON audit_log (actor);