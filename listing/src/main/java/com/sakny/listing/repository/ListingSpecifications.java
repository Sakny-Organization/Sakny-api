package com.sakny.listing.repository;

import com.sakny.common.dto.ListingSearchCriteria;
import com.sakny.common.model.Amenity;
import com.sakny.common.model.ListingStatus;
import com.sakny.listing.entity.Listing;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic listing queries.
 */
public class ListingSpecifications {

    private ListingSpecifications() {
        // Utility class
    }

    /**
     * Build specification from search criteria.
     */
    public static Specification<Listing> fromCriteria(ListingSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter for active listings (unless specifically searching for other statuses)
            predicates.add(cb.equal(root.get("status"), ListingStatus.ACTIVE));

            // Location filters
            if (criteria.getGovernorateId() != null) {
                predicates.add(cb.equal(root.get("governorate").get("id"), criteria.getGovernorateId()));
            }

            if (criteria.getCityId() != null) {
                predicates.add(cb.equal(root.get("city").get("id"), criteria.getCityId()));
            }

            if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
                String pattern = "%" + criteria.getLocation().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("address")), pattern),
                        cb.like(cb.lower(root.get("title")), pattern)
                ));
            }

            // Budget filters
            if (criteria.getMinRent() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rentAmount"), criteria.getMinRent()));
            }

            if (criteria.getMaxRent() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rentAmount"), criteria.getMaxRent()));
            }

            // Property and room type
            if (criteria.getPropertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), criteria.getPropertyType()));
            }

            if (criteria.getRoomType() != null) {
                predicates.add(cb.equal(root.get("roomType"), criteria.getRoomType()));
            }

            // Gender preference
            if (criteria.getPreferredGender() != null) {
                predicates.add(cb.equal(root.get("preferredGender"), criteria.getPreferredGender()));
            }

            // Availability date
            if (criteria.getAvailableFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("availableFrom"), criteria.getAvailableFrom()));
            }

            if (criteria.getAvailableTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("availableFrom"), criteria.getAvailableTo()));
            }

            // Boolean filters
            if (criteria.getBillsIncluded() != null && criteria.getBillsIncluded()) {
                predicates.add(cb.equal(root.get("billsIncluded"), true));
            }

            if (criteria.getPetsAllowed() != null && criteria.getPetsAllowed()) {
                predicates.add(cb.equal(root.get("petsAllowed"), true));
            }

            if (criteria.getSmokingAllowed() != null && criteria.getSmokingAllowed()) {
                predicates.add(cb.equal(root.get("smokingAllowed"), true));
            }

            // Bedroom filters
            if (criteria.getMinBedrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalBedrooms"), criteria.getMinBedrooms()));
            }

            if (criteria.getMaxBedrooms() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalBedrooms"), criteria.getMaxBedrooms()));
            }

            // Available spots filter
            if (Boolean.TRUE.equals(criteria.getHasAvailableSpots())) {
                predicates.add(cb.greaterThan(
                        cb.diff(root.get("totalRoommates"), root.get("currentRoommates")),
                        0
                ));
            }

            // Amenities filter (requires JSON contains check - simplified version)
            if (criteria.getRequiredAmenities() != null && !criteria.getRequiredAmenities().isEmpty()) {
                for (Amenity amenity : criteria.getRequiredAmenities()) {
                    predicates.add(cb.like(root.get("amenities"), "%" + amenity.name() + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

