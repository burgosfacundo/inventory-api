package com.burgosfacundo.inventory.common.web;

import com.burgosfacundo.inventory.common.exception.InvalidSortException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortUtils {

    public static Sort build(
            String sortBy,
            String direction,
            Set<String> allowedFields
    ) {
        if (!allowedFields.contains(sortBy)) {
            throw new InvalidSortException(
                    "Invalid sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromOptionalString(direction)
                        .orElseThrow(() ->
                                new InvalidSortException(
                                        "Invalid sort direction: " + direction
                                )
                        );

        return Sort.by(sortDirection, sortBy);
    }
}