package com.burgosfacundo.inventory.supplier.repository;


import com.burgosfacundo.inventory.config.IntegrationTest;
import com.burgosfacundo.inventory.supplier.model.Supplier;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@SpringBootTest
public class SupplierRepositoryIT extends IntegrationTest {
    @Autowired
    private SupplierRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveSupplier() {
        Supplier supplier = new Supplier("Name","supplier@email.com","22",null);

        var saved = repository.saveAndFlush(supplier);

        entityManager.clear();

        var found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Name");
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.getPhone()).isEqualTo(saved.getPhone());
        assertThat(found.getDescription()).isNull();
    }

    @Test
    void shouldDetectExistingEmail(){
        Supplier supplier = new Supplier("Name","supplier@email.com","22",null);

        repository.saveAndFlush(supplier);

        entityManager.clear();

        assertThat(repository.existsByEmail("supplier@email.com")).isTrue();
        assertThat(repository.existsByEmail("error@email.com")).isFalse();
    }

    @Test
    void shouldDetectEmailUsedByOtherSupplier(){
        Supplier supplier = new Supplier("Name","supplier@email.com","22",null);

        repository.saveAndFlush(supplier);

        Supplier supplier2 = repository.saveAndFlush(
                new Supplier("Name 2","supplier2@email.com","22",null));

        assertThat(
                repository.existsByEmailAndIdNot(
                        "supplier@email.com",
                        supplier2.getId()
                )
        ).isTrue();
    }

    @Test
    void shouldNotConsiderOwnEmailAsDuplicate() {
        Supplier supplier = new Supplier("Name","supplier@email.com","22",null);

        repository.saveAndFlush(supplier);

        assertThat(
                repository.existsByEmailAndIdNot(
                        "supplier@email.com",
                        supplier.getId()
                )
        ).isFalse();
    }


    @Test
    void shouldFilterSuppliersByActiveStatus() {
        Supplier supplier1 = new Supplier(
                "Supplier 1",
                "supplier1@email.com",
                "11",
                null
        );

        Supplier supplier2 = new Supplier(
                "Supplier 2",
                "supplier2@email.com",
                "22",
                null
        );
        supplier2.deactivate();

        Supplier supplier3 = new Supplier(
                "Supplier 3",
                "supplier3@email.com",
                "33",
                null
        );

        repository.saveAllAndFlush(
                List.of(supplier1, supplier2, supplier3)
        );

        Pageable pageable = PageRequest.of(0, 20);

        var all = repository.findAllFiltered(null, pageable);
        var active = repository.findAllFiltered(true, pageable);
        var inactive = repository.findAllFiltered(false, pageable);

        assertThat(all.getContent()).hasSize(3);

        assertThat(active.getContent())
                .extracting(Supplier::getEmail)
                .containsExactlyInAnyOrder(
                        "supplier1@email.com",
                        "supplier3@email.com"
                );

        assertThat(inactive.getContent())
                .extracting(Supplier::getEmail)
                .containsExactly("supplier2@email.com");
    }
}
