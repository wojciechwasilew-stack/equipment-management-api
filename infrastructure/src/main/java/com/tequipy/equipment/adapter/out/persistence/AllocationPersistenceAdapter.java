package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.mapper.AllocationPersistenceMapper;
import com.tequipy.equipment.adapter.out.persistence.repository.AllocationRequestJpaRepository;
import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.domain.model.AllocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AllocationPersistenceAdapter implements SaveAllocationPort, LoadAllocationPort {

    private final AllocationRequestJpaRepository allocationRequestJpaRepository;
    private final AllocationPersistenceMapper allocationPersistenceMapper;

    @Override
    public AllocationRequest save(AllocationRequest allocationRequest) {
        var entity = allocationPersistenceMapper.toJpaEntity(allocationRequest);
        var savedEntity = allocationRequestJpaRepository.save(entity);
        return allocationPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AllocationRequest> findById(UUID id) {
        return allocationRequestJpaRepository.findById(id)
                .map(allocationPersistenceMapper::toDomain);
    }
}
