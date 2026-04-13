package com.tequipy.equipment.application.port.out;

import com.tequipy.equipment.domain.event.AllocationCreatedEvent;

public interface PublishAllocationEventPort {

    void publish(AllocationCreatedEvent event);
}
