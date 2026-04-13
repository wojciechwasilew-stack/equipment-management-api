CREATE TABLE equipment (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    condition_score NUMERIC(3,2) NOT NULL,
    purchase_date DATE NOT NULL,
    retire_reason VARCHAR(500)
);

CREATE INDEX idx_equipment_state ON equipment(state);
CREATE INDEX idx_equipment_type ON equipment(type);
