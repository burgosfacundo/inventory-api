package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.exception.AddressProviderUnavailableException;
import com.burgosfacundo.inventory.warehouse.integration.geoapify.dto.GeoapifyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GeoapifyClient.class)
@EnableConfigurationProperties(GeoapifyProperties.class)
@TestPropertySource(properties = {
        "geoapify.base-url=https://api.geoapify.test",
        "geoapify.api-key=test-key",
        "geoapify.min-confidence=0.95"
})
class GeoapifyClientTest {

    @Autowired
    private GeoapifyClient client;

    @Autowired
    private MockRestServiceServer server;

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
    void shouldRequestGeoapifyAndMapResponse() {
        String body = """
        {
          "results": [
            {
              "street": "Avenida Independencia",
              "housenumber": "1234",
              "postcode": "B7600",
              "city": "Mar del Plata",
              "state": "Buenos Aires",
              "country_code": "ar",
              "lat": -38.0055,
              "lon": -57.5426,
              "rank": {
                "confidence": 0.99
              }
            }
          ]
        }
        """;

        server.expect(
                        requestTo(
                                startsWith(
                                        "https://api.geoapify.test/v1/geocode/search"
                                )
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(
                        queryParam(
                                "housenumber",
                                "1234"
                        )
                )
                .andExpect(
                        queryParam(
                                "street",
                                UriUtils.encodeQueryParam(
                                        "Av. Independencia",
                                        StandardCharsets.UTF_8
                                )
                        )
                )
                .andExpect(
                        queryParam(
                                "city",
                                UriUtils.encodeQueryParam(
                                        "Mar del Plata",
                                        StandardCharsets.UTF_8
                                )
                        )
                )
                .andExpect(
                        queryParam(
                                "state",
                                UriUtils.encodeQueryParam(
                                        "Buenos Aires",
                                        StandardCharsets.UTF_8
                                )
                        )
                )
                .andExpect(
                        queryParam(
                                "postcode",
                                "7600"
                        )
                )
                .andExpect(
                        queryParam(
                                "filter",
                                "countrycode:ar"
                        )
                )
                .andExpect(
                        queryParam(
                                "limit",
                                "1"
                        )
                )
                .andExpect(
                        queryParam(
                                "format",
                                "json"
                        )
                )
                .andExpect(
                        queryParam(
                                "apiKey",
                                "test-key"
                        )
                )
                .andRespond(
                        withSuccess(
                                body,
                                MediaType.APPLICATION_JSON
                        )
                );

        GeoapifyResponse response =
                client.search(request());

        assertThat(response.results())
                .hasSize(1);

        var result = response.results().getFirst();

        assertThat(result.street())
                .isEqualTo("Avenida Independencia");

        assertThat(result.housenumber())
                .isEqualTo("1234");

        assertThat(result.postcode())
                .isEqualTo("B7600");

        assertThat(result.city())
                .isEqualTo("Mar del Plata");

        assertThat(result.state())
                .isEqualTo("Buenos Aires");

        assertThat(result.countryCode())
                .isEqualTo("ar");

        assertThat(result.lat())
                .isEqualByComparingTo("-38.0055");

        assertThat(result.lon())
                .isEqualByComparingTo("-57.5426");

        assertThat(result.rank().confidence())
                .isEqualByComparingTo("0.99");

        server.verify();

        assertThat(response.results())
                .hasSize(1);

        assertThat(
                response.results()
                        .getFirst()
                        .lat()
        ).isEqualByComparingTo("-38.0055");

        server.verify();
    }


    @Test
    void shouldThrowProviderUnavailableWhenGeoapifyFails() {
        server.expect(anything())
                .andRespond(
                        withStatus(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );

        var exception = assertThrows(
                AddressProviderUnavailableException.class,
                () -> client.search(request())
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        "ADDRESS_PROVIDER_UNAVAILABLE"
                );

        server.verify();
    }
}