package com.burgosfacundo.inventory.warehouse.service;

import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseResponse;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseCodeAlreadyExistsException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
import com.burgosfacundo.inventory.warehouse.mapper.WarehouseMapper;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final AddressValidator addressValidator;

    @Transactional
    @Override
    public WarehouseResponse save(WarehouseRequest request) {

        if (repository.existsByCode(request.code())) {
            throw new WarehouseCodeAlreadyExistsException(
                    request.code()
            );
        }

        Address address =
                addressValidator.validate(request.address());

        Warehouse warehouse =
                WarehouseMapper.toEntity(request, address);

        Warehouse saved = repository.save(warehouse);

        return WarehouseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public WarehouseResponse findById(Long id) {
        Warehouse warehouse = repository.findById(id)
                .orElseThrow(
                        () -> new WarehouseNotFoundException(id)
                );

        return WarehouseMapper.toResponse(warehouse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<WarehouseResponse> findAll(
            Pageable pageable
    ) {
        return repository.findAll(pageable)
                .map(WarehouseMapper::toResponse);
    }

    @Transactional
    @Override
    public WarehouseResponse update(
            Long id,
            WarehouseRequest request
    ) {
        Warehouse warehouse = repository.findById(id)
                .orElseThrow(
                        () -> new WarehouseNotFoundException(id)
                );

        if (repository.existsByCodeAndIdNot(
                request.code(),
                id
        )) {
            throw new WarehouseCodeAlreadyExistsException(
                    request.code()
            );
        }

        Address address =
                addressValidator.validate(request.address());

        warehouse.update(
                request.code(),
                request.name(),
                address
        );

        return WarehouseMapper.toResponse(warehouse);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new WarehouseNotFoundException(id);
        }

        repository.deleteById(id);
    }
}
