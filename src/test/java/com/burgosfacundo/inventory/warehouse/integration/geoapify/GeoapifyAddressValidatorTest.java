package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressInvalidException;
import com.burgosfacundo.inventory.warehouse.exception.AddressNotFoundException;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyRank;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyResponse;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyResult;
import com.burgosfacundo.inventory.warehouse.model.Address;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoapifyAddressValidatorTest {

    @Mock
    private GeoapifyClient client;

    private GeoapifyAddressValidator validator;

    @BeforeEach
    void setUp() {
        GeoapifyProperties properties =
                new GeoapifyProperties(
                        "https://api.geoapify.test",
                        "test-key",
                        new BigDecimal("0.95")
                );

        validator =
                new GeoapifyAddressValidator(
                        client,
                        properties
                );
    }

    private AddressRequest request() {
        return new AddressRequest(
                "Av. Independencia",
                "1234",
                "7600",
                "Mar del Plata",
                "Buenos Aires",
                "AR"
        );
    }

    @Test
    void shouldValidateAndNormalizeAddress() {
        var result = getGeoapifyResult();

        when(client.search(request()))
                .thenReturn(
                        new GeoapifyResponse(
                                List.of(result)
                        )
                );

        Address address =
                validator.validate(request());

        assertThat(address.getStreet())
                .isEqualTo("Avenida Independencia");

        assertThat(address.getNumber())
                .isEqualTo("1234");

        assertThat(address.getPostalCode())
                .isEqualTo("B7600");

        assertThat(address.getCity())
                .isEqualTo("Mar del Plata");

        assertThat(address.getProvince())
                .isEqualTo("Buenos Aires");

        assertThat(address.getCountryCode())
                .isEqualTo("AR");

        assertThat(address.getLatitude())
                .isEqualByComparingTo("-38.0055");

        assertThat(address.getLongitude())
                .isEqualByComparingTo("-57.5426");
    }

    private static @NonNull GeoapifyResult getGeoapifyResult() {
        var rank =
                new GeoapifyRank(
                        new BigDecimal("0.99")
                );

        return new GeoapifyResult(
                "Avenida Independencia",
                "1234",
                "B7600",
                "Mar del Plata",
                "Buenos Aires",
                "ar",
                new BigDecimal("-38.0055"),
                new BigDecimal("-57.5426"),
                rank
        );
    }

    @Test
    void shouldThrowAddressNotFoundWhenNoResultsExist() {
        when(client.search(request()))
                .thenReturn(
                        new GeoapifyResponse(List.of())
                );

        assertThrows(
                AddressNotFoundException.class,
                () -> validator.validate(request())
        );
    }

    @Test
    void shouldThrowAddressInvalidWhenConfidenceIsTooLow() {
        var result =
                new GeoapifyResult(
                        "Avenida Independencia",
                        "1234",
                        "B7600",
                        "Mar del Plata",
                        "Buenos Aires",
                        "ar",
                        new BigDecimal("-38.0055"),
                        new BigDecimal("-57.5426"),
                        new GeoapifyRank(
                                new BigDecimal("0.50")
                        )
                );

        when(client.search(request()))
                .thenReturn(
                        new GeoapifyResponse(
                                List.of(result)
                        )
                );

        assertThrows(
                AddressInvalidException.class,
                () -> validator.validate(request())
        );
    }

    @Test
    void shouldThrowAddressInvalidWhenProviderResultIsIncomplete() {
        var result =
                new GeoapifyResult(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        when(client.search(request()))
                .thenReturn(
                        new GeoapifyResponse(
                                List.of(result)
                        )
                );

        assertThrows(
                AddressInvalidException.class,
                () -> validator.validate(request())
        );
    }
}
