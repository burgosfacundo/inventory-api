package com.burgosfacundo.inventory.warehouse.exception;

import com.burgosfacundo.inventory.common.exception.ServiceUnavailableException;

public class AddressProviderUnavailableException
        extends ServiceUnavailableException {

    public AddressProviderUnavailableException() {
        super(
                "Address validation provider is unavailable",
                "ADDRESS_PROVIDER_UNAVAILABLE"
        );
    }
}