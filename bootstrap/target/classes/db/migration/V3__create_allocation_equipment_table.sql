CREATE TABLE allocation_equipment (
    allocation_request_id UUID NOT NULL REFERENCES allocation_request(id),
    equipment_id UUID NOT NULL,
    PRIMARY KEY (allocation_request_id, equipment_id)
);
