CREATE TABLE allocation_request (
    id UUID PRIMARY KEY,
    employee_id VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL
);

CREATE INDEX idx_allocation_request_state ON allocation_request(state);
