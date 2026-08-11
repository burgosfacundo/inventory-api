package com.burgosfacundo.inventory.supplier.service;

import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.dto.SupplierResponse;
import com.burgosfacundo.inventory.supplier.exception.SupplierEmailAlreadyExistsException;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
import com.burgosfacundo.inventory.supplier.mapper.SupplierMapper;
import com.burgosfacundo.inventory.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository repository;

    @Transactional
    @Override
    public SupplierResponse save(SupplierRequest request) {
        var email = request.email();
        if (repository.existsByEmail(email)){
            throw new SupplierEmailAlreadyExistsException(email);
        }

        var saved = repository.save(SupplierMapper.toEntity(request));

        return SupplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public SupplierResponse findById(Long id) {
        var found = repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        return SupplierMapper.toResponse(found);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SupplierResponse> findAll(Boolean active, Pageable pageable) {
        return repository.findAllFiltered(active,pageable)
                .map(SupplierMapper::toResponse);
    }

    @Transactional
    @Override
    public SupplierResponse update(Long id, SupplierRequest request) {
        var found = repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (repository.existsByEmailAndIdNot(request.email(), id)){
            throw new SupplierEmailAlreadyExistsException(request.email());
        }

        found.update(request.name(), request.email(), request.phone(), request.description());

        return SupplierMapper.toResponse(found);
    }

    @Transactional
    @Override
    public SupplierResponse updateStatus(Long id, boolean status) {
        var found = repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (status) {
            found.activate();
        }else {
            found.deactivate();
        }
        return SupplierMapper.toResponse(found);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)){
            throw new SupplierNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
