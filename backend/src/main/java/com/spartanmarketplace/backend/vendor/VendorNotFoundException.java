package com.spartanmarketplace.backend.vendor;

import java.util.UUID;

public class VendorNotFoundException extends RuntimeException {

    public VendorNotFoundException(UUID vendorId) {
        super("No vendor found with id " + vendorId);
    }
}
