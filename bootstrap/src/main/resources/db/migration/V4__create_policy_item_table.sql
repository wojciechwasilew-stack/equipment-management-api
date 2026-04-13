CREATE TABLE policy_item (
    id BIGSERIAL PRIMARY KEY,
    allocation_request_id UUID NOT NULL REFERENCES allocation_request(id),
    equipment_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    minimum_condition_score NUMERIC(3,2),
    preferred_brand VARCHAR(255),
    prefer_recent BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_policy_item_allocation ON policy_item(allocation_request_id);
