package com.burgosfacundo.inventory.supplier.service;

import com.burgosfacundo.inventory.supplier.dto.SupplierRequest;
import com.burgosfacundo.inventory.supplier.exception.SupplierEmailAlreadyExistsException;
import com.burgosfacundo.inventory.supplier.exception.SupplierNotFoundException;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import com.burgosfacundo.inventory.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {
    @Mock
    private SupplierRepository repository;

    private SupplierService service;

    @BeforeEach
    public void setUp() {
        service = new SupplierServiceImpl(repository);
    }

    private Supplier supplierWithId(Long id) {
        Supplier supplier = new Supplier("Name","supplier@email.com","22",null);
        ReflectionTestUtils.setField(supplier, "id", id);
        return supplier;
    }

    //Save Supplier
    @Test
    public void shouldCreateSupplier() {
        SupplierRequest request = new SupplierRequest("Name","supplier@email.com",
                "22",null);

        Supplier supplier = supplierWithId(1L);

        when(repository.existsByEmail(request.email())).thenReturn(false);

        when(repository.save(any(Supplier.class))).thenReturn(supplier);

        var result = service.save(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.email()).isEqualTo(request.email());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.active()).isTrue();

        verify(repository).existsByEmail(request.email());
        verify(repository).save(any(Supplier.class));
    }

    @Test
    public void shouldThrowEmailAlreadyExistException() {
        SupplierRequest request = new SupplierRequest("Name","supplier@email.com",
                "22",null);

        when(repository.existsByEmail(request.email())).thenReturn(true);

        var exception = assertThrows(SupplierEmailAlreadyExistsException.class, () -> service.save(request));
        assertThat(exception.getMessage()).isEqualTo("Supplier with email '" + request.email() + "' already exists.");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_EMAIL_ALREADY_EXISTS");

        verify(repository).existsByEmail(request.email());
        verify(repository,never()).save(any(Supplier.class));
    }

    //Find By Id
    @Test
    public void shouldFindById() {
        Supplier supplier = supplierWithId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        var response = service.findById(supplier.getId());

        assertThat(response.id()).isEqualTo(supplier.getId());
        assertThat(response.name()).isEqualTo(supplier.getName());
        assertThat(response.email()).isEqualTo(supplier.getEmail());
        assertThat(response.description()).isEqualTo(supplier.getDescription());
        assertThat(response.active()).isTrue();

        verify(repository).findById(1L);
    }

    @Test
    public void shouldThrowSupplierNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(SupplierNotFoundException.class, () -> service.findById(1L));
        assertThat(exception.getMessage()).isEqualTo("Supplier not found with id: 1");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_NOT_FOUND");

        verify(repository).findById(1L);
    }

    //Find All
    @Test
    public void shouldFindAllWithoutFilter() {
        Supplier supplier = supplierWithId(1L);
        Supplier supplier2 = supplierWithId(2L);
        Supplier supplier3 = supplierWithId(3L);


        Pageable pageable = PageRequest.of(0, 20);

        Page<Supplier> page = new PageImpl<>(
                List.of(supplier, supplier2, supplier3),
                pageable,
                3
        );

        when(repository.findAllFiltered(null,pageable)).thenReturn(page);

        var result = service.findAll(null,pageable);
        var content = result.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(content.getFirst().id()).isEqualTo(supplier.getId());
        assertThat(content.get(1).id()).isEqualTo(supplier2.getId());
        assertThat(content.getLast().id()).isEqualTo(supplier3.getId());

        verify(repository).findAllFiltered(null,pageable);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldFindAllWithActiveFilter(boolean active) {
        Pageable pageable = PageRequest.of(0, 20);

        when(repository.findAllFiltered(active, pageable))
                .thenReturn(Page.empty(pageable));

        var result = service.findAll(active, pageable);

        assertThat(result).isEmpty();

        verify(repository).findAllFiltered(active, pageable);
    }

    //Update Supplier
    @Test
    public void shouldUpdateSupplier() {
        Supplier supplier = supplierWithId(1L);
        SupplierRequest supplierRequest = new SupplierRequest("Name updated","update@email.com",
                "223","Description updated");

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.existsByEmailAndIdNot(supplierRequest.email(),supplier.getId())).thenReturn(false);

        var  result = service.update(1L,supplierRequest);

        assertThat(result.id()).isEqualTo(supplier.getId());
        assertThat(result.name()).isEqualTo(supplierRequest.name());
        assertThat(result.email()).isEqualTo(supplierRequest.email());
        assertThat(result.phone()).isEqualTo(supplierRequest.phone());
        assertThat(result.description()).isEqualTo(supplierRequest.description());

        verify(repository).findById(1L);
        verify(repository).existsByEmailAndIdNot(supplierRequest.email(), supplier.getId());
        verify(repository, never()).save(supplier);
    }

    @Test
    public void shouldUpdateThrowSupplierNotFoundException(){
        Supplier supplier = supplierWithId(1L);
        SupplierRequest supplierRequest = new SupplierRequest("Name updated","update@email.com",
            "223","Description updated");

        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(SupplierNotFoundException.class, () -> service.update(1L,supplierRequest));

        assertThat(exception.getMessage()).isEqualTo("Supplier not found with id: 1");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_NOT_FOUND");

        verify(repository).findById(1L);
        verify(repository,never()).existsByEmailAndIdNot(supplier.getEmail(),supplier.getId());
    }

    @Test
    public void shouldUpdateThrowSupplierEmailAlreadyExistsException(){
        Supplier supplier = supplierWithId(1L);
        SupplierRequest supplierRequest = new SupplierRequest("Name updated","update@email.com",
                "223","Description updated");

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.existsByEmailAndIdNot(supplierRequest.email(),supplier.getId())).thenReturn(true);

        var exception = assertThrows(SupplierEmailAlreadyExistsException.class,
                () -> service.update(1L,supplierRequest));

        assertThat(exception.getMessage()).isEqualTo("Supplier with email '" + supplierRequest.email() + "' already exists.");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_EMAIL_ALREADY_EXISTS");

        verify(repository).findById(1L);
        verify(repository).existsByEmailAndIdNot(supplierRequest.email(),supplier.getId());
    }

    //Update status
    @ParameterizedTest
    @ValueSource(booleans = {false,true})
    public void shouldUpdateStatus(boolean status){
        Supplier supplier = supplierWithId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        if (status) {
            supplier.deactivate();
        }else {
            supplier.activate();
        }
        var result = service.updateStatus(supplier.getId(),status);

        assertThat(result.id()).isEqualTo(supplier.getId());
        assertThat(result.name()).isEqualTo(supplier.getName());
        assertThat(result.email()).isEqualTo(supplier.getEmail());
        assertThat(result.phone()).isEqualTo(supplier.getPhone());
        assertThat(result.description()).isEqualTo(supplier.getDescription());
        assertThat(result.active()).isEqualTo(status);

        verify(repository).findById(1L);
    }

    @Test
    public void shouldUpdateStatusThrowSupplierNotFoundException(){
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(SupplierNotFoundException.class,
                () -> service.updateStatus(1L,false));
        assertThat(exception.getMessage()).isEqualTo("Supplier not found with id: 1");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_NOT_FOUND");

        verify(repository).findById(1L);
    }

    //Delete Supplier
    @Test
    public void shouldDeleteSupplier(){
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    public void shouldDeleteSupplierThrowSupplierNotFoundException(){
        when(repository.existsById(1L)).thenReturn(false);

        var exception = assertThrows(SupplierNotFoundException.class, () -> service.delete(1L));
        assertThat(exception.getMessage()).isEqualTo("Supplier not found with id: 1");
        assertThat(exception.getErrorCode()).isEqualTo("SUPPLIER_NOT_FOUND");

        verify(repository).existsById(1L);
        verify(repository,never()).deleteById(1L);
    }


}
