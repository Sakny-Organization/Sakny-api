package com.sakny.common.model;

/**
 * Status of a listing.
 */
public enum ListingStatus {
    ACTIVE,    // Listing is live and accepting inquiries
    PAUSED,    // Temporarily hidden by owner
    RENTED,    // Room/property has been rented
    EXPIRED,   // Listing has expired
    CLOSED     // Permanently closed by owner
}

