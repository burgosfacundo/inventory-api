package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressProviderUnavailableException;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

@Component
public class GeoapifyClient {

    private final RestClient restClient;
    private final GeoapifyProperties properties;

    public GeoapifyClient(
            RestClient.Builder restClientBuilder,
            GeoapifyProperties properties
    ) {
        this.properties = properties;

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

    public GeoapifyResponse search(AddressRequest address) {
        try {
            return restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/geocode/search")
                            .queryParam(
                                    "housenumber",
                                    address.number()
                            )
                            .queryParam(
                                    "street",
                                    address.street()
                            )
                            .queryParam(
                                    "postcode",
                                    address.postalCode()
                            )
                            .queryParam(
                                    "city",
                                    address.city()
                            )
                            .queryParam(
                                    "state",
                                    address.province()
                            )
                            .queryParam(
                                    "filter",
                                    "countrycode:" +
                                            address.countryCode()
                                                    .toLowerCase(Locale.ROOT)
                            )
                            .queryParam("limit", 1)
                            .queryParam("format", "json")
                            .queryParam(
                                    "apiKey",
                                    properties.apiKey()
                            )
                            .build()
                    )
                    .retrieve()
                    .body(GeoapifyResponse.class);

        } catch (RestClientException ex) {
            throw new AddressProviderUnavailableException();
        }
    }
}