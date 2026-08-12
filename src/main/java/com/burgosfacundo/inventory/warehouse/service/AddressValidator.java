package com.burgosfacundo.inventory.warehouse.service;

import com.burgosfacundo.inventory.warehouse.dto.AddressRequest;
import com.burgosfacundo.inventory.warehouse.model.Address;

public interface AddressValidator {
    Address validate(AddressRequest request);
}
