-- Buy-in (cost) price per unit. NULL means "not known yet", never 0, so profit reports can tell the difference.
ALTER TABLE product ADD COLUMN cost_price DECIMAL(10, 2) CHECK (cost_price >= 0);

-- The product's cost at the moment the order line was created, so later cost changes do not rewrite past profit.
-- Existing rows stay NULL: their cost was not recorded.
ALTER TABLE order_item ADD COLUMN cost_price DECIMAL(10, 2) CHECK (cost_price >= 0);
