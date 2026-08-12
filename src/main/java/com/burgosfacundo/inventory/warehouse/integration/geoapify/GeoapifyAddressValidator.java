package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressInvalidException;
import com.burgosfacundo.inventory.warehouse.exception.AddressNotFoundException;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyResult;
import com.burgosfacundo.inventory.warehouse.model.Address;
import com.burgosfacundo.inventory.warehouse.service.AddressValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class GeoapifyAddressValidator
        implements AddressValidator {

    private final GeoapifyClient client;
    private final GeoapifyProperties properties;

    @Override
    public Address validate(AddressRequest request) {
        var response = client.search(request);

        if (response == null || response.results() == null) {
            throw new AddressInvalidException();
        }

        if (response.results().isEmpty()) {
            throw new AddressNotFoundException();
        }

        GeoapifyResult result =
                response.results().getFirst();

        validateResult(result);

        return new Address(
                result.street(),
                result.housenumber(),
                valueOrFallback(
                        result.postcode(),
                        request.postalCode()
                ),
                result.city(),
                valueOrFallback(
                        result.state(),
                        request.province()
                ),
                result.countryCode()
                        .toUpperCase(Locale.ROOT),
                result.lat(),
                result.lon()
        );
    }

    private void validateResult(GeoapifyResult result) {
        if (result == null
                || isBlank(result.street())
                || isBlank(result.housenumber())
                || isBlank(result.city())
                || isBlank(result.countryCode())
                || result.lat() == null
                || result.lon() == null
                || result.rank() == null
                || result.rank().confidence() == null) {

            throw new AddressInvalidException();
        }

        if (result.rank()
                .confidence()
                .compareTo(properties.minConfidence()) < 0) {

            throw new AddressInvalidException();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String valueOrFallback(
            String normalizedValue,
            String originalValue
    ) {
        return isBlank(normalizedValue)
                ? originalValue
                : normalizedValue;
    }
}