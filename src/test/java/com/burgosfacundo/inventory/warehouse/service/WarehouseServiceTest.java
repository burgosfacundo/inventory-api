package com.burgosfacundo.inventory.warehouse.service;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.dto.WarehouseRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressInvalidException;
import com.burgosfacundo.inventory.warehouse.exception.AddressNotFoundException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseCodeAlreadyExistsException;
import com.burgosfacundo.inventory.warehouse.exception.WarehouseNotFoundException;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import com.burgosfacundo.inventory.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    @Mock
    private WarehouseRepository repository;

    @Mock
    private AddressValidator addressValidator;

    private WarehouseService service;

    @BeforeEach
    void setUp() {
        service = new WarehouseServiceImpl(
                repository,
                addressValidator
        );
    }

    private AddressRequest addressRequest() {
        return new AddressRequest(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR"
        );
    }

    private WarehouseRequest warehouseRequest() {
        return new WarehouseRequest(
                "WH-001",
                "Main Warehouse",
                addressRequest()
        );
    }

    private Address validatedAddress() {
        return new Address(
                "Avenida Independencia",
                "1234",
                "B7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0055"),
                new BigDecimal("-57.5426")
        );
    }

    private Warehouse warehouseWithId() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                validatedAddress()
        );

        ReflectionTestUtils.setField(
                warehouse,
                "id",
                1L
        );

        return warehouse;
    }

    // Save

    @Test
    void shouldCreateWarehouse() {
        WarehouseRequest request = warehouseRequest();
        Address address = validatedAddress();

        when(repository.existsByCode(request.code()))
                .thenReturn(false);

        when(addressValidator.validate(request.address()))
                .thenReturn(address);

        when(repository.save(any(Warehouse.class)))
                .thenAnswer(invocation -> {
                    Warehouse warehouse =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            warehouse,
                            "id",
                            1L
                    );

                    return warehouse;
                });

        var response = service.save(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code())
                .isEqualTo(request.code());
        assertThat(response.name())
                .isEqualTo(request.name());

        assertThat(response.address().street())
                .isEqualTo(address.getStreet());
        assertThat(response.address().number())
                .isEqualTo(address.getNumber());
        assertThat(response.address().postalCode())
                .isEqualTo(address.getPostalCode());
        assertThat(response.address().city())
                .isEqualTo(address.getCity());
        assertThat(response.address().province())
                .isEqualTo(address.getProvince());
        assertThat(response.address().countryCode())
                .isEqualTo(address.getCountryCode());

        assertThat(response.address().latitude())
                .isEqualByComparingTo(
                        address.getLatitude()
                );

        assertThat(response.address().longitude())
                .isEqualByComparingTo(
                        address.getLongitude()
                );

        verify(repository)
                .existsByCode(request.code());

        verify(addressValidator)
                .validate(request.address());

        verify(repository)
                .save(any(Warehouse.class));
    }

    @Test
    void shouldThrowWhenCodeAlreadyExists() {
        WarehouseRequest request = warehouseRequest();

        when(repository.existsByCode(request.code()))
                .thenReturn(true);

        var exception = assertThrows(
                WarehouseCodeAlreadyExistsException.class,
                () -> service.save(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        "WAREHOUSE_CODE_ALREADY_EXISTS"
                );

        verify(repository)
                .existsByCode(request.code());

        verifyNoInteractions(addressValidator);

        verify(repository, never())
                .save(any(Warehouse.class));
    }

    @Test
    void shouldNotSaveWhenAddressNotFound() {
        WarehouseRequest request = warehouseRequest();

        when(repository.existsByCode(request.code()))
                .thenReturn(false);

        when(addressValidator.validate(request.address()))
                .thenThrow(
                        new AddressNotFoundException()
                );

        assertThrows(
                AddressNotFoundException.class,
                () -> service.save(request)
        );

        verify(repository)
                .existsByCode(request.code());

        verify(addressValidator)
                .validate(request.address());

        verify(repository, never())
                .save(any(Warehouse.class));
    }

    // Find By Id

    @Test
    void shouldFindWarehouseById() {
        Warehouse warehouse = warehouseWithId();

        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(warehouse)
                );

        var response = service.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id())
                .isEqualTo(warehouse.getId());
        assertThat(response.code())
                .isEqualTo(warehouse.getCode());
        assertThat(response.name())
                .isEqualTo(warehouse.getName());

        assertThat(response.address().street())
                .isEqualTo(
                        warehouse.getAddress().getStreet()
                );

        assertThat(response.address().latitude())
                .isEqualByComparingTo(
                        warehouse.getAddress()
                                .getLatitude()
                );

        assertThat(response.address().longitude())
                .isEqualByComparingTo(
                        warehouse.getAddress()
                                .getLongitude()
                );

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowWhenWarehouseNotFound() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                WarehouseNotFoundException.class,
                () -> service.findById(1L)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo("WAREHOUSE_NOT_FOUND");

        verify(repository).findById(1L);
    }

    // Find All

    @Test
    void shouldReturnPagedWarehouses() {
        Warehouse warehouse1 =
                warehouseWithId();

        Warehouse warehouse2 =
                new Warehouse(
                        "WH-002",
                        "Secondary Warehouse",
                        validatedAddress()
                );

        ReflectionTestUtils.setField(
                warehouse2,
                "id",
                2L
        );

        Warehouse warehouse3 =
                new Warehouse(
                        "WH-003",
                        "Third Warehouse",
                        validatedAddress()
                );

        ReflectionTestUtils.setField(
                warehouse3,
                "id",
                3L
        );

        Pageable pageable =
                PageRequest.of(0, 20);

        Page<Warehouse> page =
                new PageImpl<>(
                        List.of(
                                warehouse1,
                                warehouse2,
                                warehouse3
                        ),
                        pageable,
                        3
                );

        when(repository.findAll(pageable))
                .thenReturn(page);

        var response =
                service.findAll(pageable);

        assertThat(response)
                .hasSize(3);

        assertThat(response.getContent()
                .getFirst()
                .id())
                .isEqualTo(1L);

        assertThat(response.getContent()
                .get(1)
                .id())
                .isEqualTo(2L);

        assertThat(response.getContent()
                .getLast()
                .id())
                .isEqualTo(3L);

        verify(repository)
                .findAll(pageable);
    }

    // Update

    @Test
    void shouldUpdateWarehouse() {
        Warehouse warehouse =
                warehouseWithId();

        AddressRequest newAddressRequest =
                new AddressRequest(
                        "San Martin",
                        "2500",
                        "7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR"
                );

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-002",
                        "Updated Warehouse",
                        newAddressRequest
                );

        Address validatedNewAddress =
                new Address(
                        "Avenida San Martin",
                        "2500",
                        "B7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR",
                        new BigDecimal("-38.0010"),
                        new BigDecimal("-57.5500")
                );

        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(warehouse)
                );

        when(repository.existsByCodeAndIdNot(
                request.code(),
                1L
        )).thenReturn(false);

        when(addressValidator.validate(
                request.address()
        )).thenReturn(validatedNewAddress);

        var response =
                service.update(1L, request);

        assertThat(response.id())
                .isEqualTo(1L);

        assertThat(response.code())
                .isEqualTo("WH-002");

        assertThat(response.name())
                .isEqualTo("Updated Warehouse");

        assertThat(response.address().street())
                .isEqualTo("Avenida San Martin");

        assertThat(response.address().number())
                .isEqualTo("2500");

        assertThat(response.address().latitude())
                .isEqualByComparingTo(
                        "-38.0010"
                );

        assertThat(response.address().longitude())
                .isEqualByComparingTo(
                        "-57.5500"
                );

        assertThat(warehouse.getCode())
                .isEqualTo(request.code());

        assertThat(warehouse.getName())
                .isEqualTo(request.name());

        assertThat(warehouse.getAddress())
                .isSameAs(validatedNewAddress);

        verify(repository).findById(1L);

        verify(repository)
                .existsByCodeAndIdNot(
                        request.code(),
                        1L
                );

        verify(addressValidator)
                .validate(request.address());

        verify(repository, never())
                .save(any(Warehouse.class));
    }

    @Test
    void shouldThrowWhenWarehouseNotFoundForUpdate() {
        WarehouseRequest request =
                warehouseRequest();

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                WarehouseNotFoundException.class,
                () -> service.update(
                        1L,
                        request
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        "WAREHOUSE_NOT_FOUND"
                );

        verify(repository).findById(1L);

        verify(repository, never())
                .existsByCodeAndIdNot(
                        anyString(),
                        anyLong()
                );

        verifyNoInteractions(addressValidator);
    }

    @Test
    void shouldThrowWhenCodeBelongsToAnotherWarehouse() {
        Warehouse warehouse =
                warehouseWithId();

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-002",
                        "Updated Warehouse",
                        addressRequest()
                );

        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(warehouse)
                );

        when(repository.existsByCodeAndIdNot(
                request.code(),
                1L
        )).thenReturn(true);

        var exception = assertThrows(
                WarehouseCodeAlreadyExistsException.class,
                () -> service.update(
                        1L,
                        request
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        "WAREHOUSE_CODE_ALREADY_EXISTS"
                );

        verify(repository).findById(1L);

        verify(repository)
                .existsByCodeAndIdNot(
                        request.code(),
                        1L
                );

        verifyNoInteractions(addressValidator);

        assertThat(warehouse.getCode())
                .isEqualTo("WH-001");

        assertThat(warehouse.getName())
                .isEqualTo("Main Warehouse");
    }

    @Test
    void shouldAllowKeepingSameCodeWhenUpdating() {
        Warehouse warehouse =
                warehouseWithId();

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-001",
                        "Updated Main Warehouse",
                        addressRequest()
                );

        Address newAddress =
                new Address(
                        "Avenida Independencia",
                        "1234",
                        "B7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "AR",
                        new BigDecimal("-38.0055"),
                        new BigDecimal("-57.5426")
                );

        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(warehouse)
                );

        when(repository.existsByCodeAndIdNot(
                "WH-001",
                1L
        )).thenReturn(false);

        when(addressValidator.validate(
                request.address()
        )).thenReturn(newAddress);

        var response =
                service.update(
                        1L,
                        request
                );

        assertThat(response.code())
                .isEqualTo("WH-001");

        assertThat(response.name())
                .isEqualTo(
                        "Updated Main Warehouse"
                );

        verify(repository)
                .existsByCodeAndIdNot(
                        "WH-001",
                        1L
                );

        verify(addressValidator)
                .validate(request.address());
    }

    @Test
    void shouldNotUpdateWhenAddressValidationFails() {
        Warehouse warehouse =
                warehouseWithId();

        String originalCode =
                warehouse.getCode();

        String originalName =
                warehouse.getName();

        Address originalAddress =
                warehouse.getAddress();

        WarehouseRequest request =
                new WarehouseRequest(
                        "WH-002",
                        "Updated Warehouse",
                        addressRequest()
                );

        when(repository.findById(1L))
                .thenReturn(
                        Optional.of(warehouse)
                );

        when(repository.existsByCodeAndIdNot(
                request.code(),
                1L
        )).thenReturn(false);

        when(addressValidator.validate(
                request.address()
        )).thenThrow(
                new AddressInvalidException()
        );

        assertThrows(
                AddressInvalidException.class,
                () -> service.update(
                        1L,
                        request
                )
        );

        assertThat(warehouse.getCode())
                .isEqualTo(originalCode);

        assertThat(warehouse.getName())
                .isEqualTo(originalName);

        assertThat(warehouse.getAddress())
                .isSameAs(originalAddress);

        verify(repository).findById(1L);

        verify(repository)
                .existsByCodeAndIdNot(
                        request.code(),
                        1L
                );

        verify(addressValidator)
                .validate(request.address());
    }

    // Delete

    @Test
    void shouldDeleteWarehouse() {
        when(repository.existsById(1L))
                .thenReturn(true);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenWarehouseNotFoundForDelete() {
        when(repository.existsById(1L))
                .thenReturn(false);

        var exception = assertThrows(
                WarehouseNotFoundException.class,
                () -> service.delete(1L)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        "WAREHOUSE_NOT_FOUND"
                );

        verify(repository).existsById(1L);

        verify(repository, never())
                .deleteById(anyLong());
    }
}