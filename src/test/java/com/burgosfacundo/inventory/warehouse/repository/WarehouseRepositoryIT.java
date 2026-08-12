package com.burgosfacundo.inventory.warehouse.repository;

import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.model.Warehouse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class WarehouseRepositoryIT extends IntegrationTest {

    @Autowired
    private WarehouseRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Address address() {
        return new Address(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR",
                new BigDecimal("-38.0055000"),
                new BigDecimal("-57.5426000")
        );
    }

    @Test
    void shouldPersistAndRetrieveWarehouse() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                address()
        );

        Warehouse saved =
                repository.saveAndFlush(warehouse);

        entityManager.clear();

        Warehouse found = repository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCode()).isEqualTo("WH-001");
        assertThat(found.getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void shouldPersistAndRetrieveEmbeddedAddress() {
        Warehouse warehouse = new Warehouse(
                "WH-001",
                "Main Warehouse",
                address()
        );

        Warehouse saved =
                repository.saveAndFlush(warehouse);

        entityManager.clear();

        Warehouse found = repository
                .findById(saved.getId())
                .orElseThrow();

        Address foundAddress = found.getAddress();

        assertThat(foundAddress.getStreet())
                .isEqualTo("Av. Independencia");

        assertThat(foundAddress.getNumber())
                .isEqualTo("1234");

        assertThat(foundAddress.getPostalCode())
                .isEqualTo("7600");

        assertThat(foundAddress.getCity())
                .isEqualTo("Mar del Plata");

        assertThat(foundAddress.getProvince())
                .isEqualTo("Buenos Aires");

        assertThat(foundAddress.getCountryCode())
                .isEqualTo("AR");

        assertThat(foundAddress.getLatitude())
                .isEqualByComparingTo("-38.0055000");

        assertThat(foundAddress.getLongitude())
                .isEqualByComparingTo("-57.5426000");
    }

    @Test
    void shouldDetectExistingCode() {
        repository.saveAndFlush(
                new Warehouse(
                        "WH-001",
                        "Main Warehouse",
                        address()
                )
        );

        assertThat(repository.existsByCode("WH-001"))
                .isTrue();

        assertThat(repository.existsByCode("WH-999"))
                .isFalse();
    }

    @Test
    void shouldDetectCodeUsedByAnotherWarehouse() {
        repository.saveAndFlush(
                new Warehouse(
                        "WH-001",
                        "Warehouse 1",
                        address()
                )
        );

        Warehouse warehouse2 =
                repository.saveAndFlush(
                        new Warehouse(
                                "WH-002",
                                "Warehouse 2",
                                address()
                        )
                );

        assertThat(
                repository.existsByCodeAndIdNot(
                        "WH-001",
                        warehouse2.getId()
                )
        ).isTrue();
    }

    @Test
    void shouldNotConsiderOwnCodeAsDuplicate() {
        Warehouse warehouse =
                repository.saveAndFlush(
                        new Warehouse(
                                "WH-001",
                                "Main Warehouse",
                                address()
                        )
                );

        assertThat(
                repository.existsByCodeAndIdNot(
                        "WH-001",
                        warehouse.getId()
                )
        ).isFalse();
    }

    @Test
    void shouldRejectDuplicateWarehouseCode() {
        repository.saveAndFlush(
                new Warehouse(
                        "WH-001",
                        "Warehouse 1",
                        address()
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(
                        new Warehouse(
                                "WH-001",
                                "Warehouse 2",
                                address()
                        )
                )
        );
    }
}