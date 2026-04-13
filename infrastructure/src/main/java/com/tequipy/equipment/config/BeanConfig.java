package com.tequipy.equipment.config;

import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.application.service.AllocationProcessorService;
import com.tequipy.equipment.application.service.AllocationService;
import com.tequipy.equipment.application.service.EquipmentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public EquipmentService equipmentService(SaveEquipmentPort saveEquipmentPort,
                                             LoadEquipmentPort loadEquipmentPort) {
        return new EquipmentService(saveEquipmentPort, loadEquipmentPort);
    }

    @Bean
    public AllocationService allocationService(SaveAllocationPort saveAllocationPort,
                                               LoadAllocationPort loadAllocationPort,
                                               SaveEquipmentPort saveEquipmentPort,
                                               LoadEquipmentPort loadEquipmentPort,
                                               PublishAllocationEventPort publishAllocationEventPort) {
        return new AllocationService(saveAllocationPort, loadAllocationPort, saveEquipmentPort,
                loadEquipmentPort, publishAllocationEventPort);
    }

    @Bean
    public AllocationProcessorService allocationProcessorService(LoadAllocationPort loadAllocationPort,
                                                                  SaveAllocationPort saveAllocationPort,
                                                                  LoadEquipmentPort loadEquipmentPort,
                                                                  SaveEquipmentPort saveEquipmentPort) {
        return new AllocationProcessorService(loadAllocationPort, saveAllocationPort,
                loadEquipmentPort, saveEquipmentPort);
    }
}
