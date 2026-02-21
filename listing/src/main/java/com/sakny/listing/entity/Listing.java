package com.sakny.listing.entity;

import com.sakny.common.model.*;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a room or apartment listing.
 * Users can create listings when they have space available for roommates.
 */
@Entity
@Table(name = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "rent_amount", nullable = false)
    private Integer rentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(name = "total_bedrooms")
    private Integer totalBedrooms;

    @Column(name = "total_roommates")
    private Integer totalRoommates;

    @Column(name = "current_roommates")
    @Builder.Default
    private Integer currentRoommates = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false, length = 10)
    private Gender preferredGender;

    @Column(name = "minimum_stay_months")
    private Integer minimumStayMonths;

    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(length = 500)
    private String address;

    /**
     * JSON array of amenities.
     * Stored as a TEXT column and converted to/from List<Amenity>.
     */
    @Column(columnDefinition = "TEXT")
    private String amenities;

    @Column(name = "bills_included", nullable = false)
    @Builder.Default
    private Boolean billsIncluded = false;

    @Column(name = "pets_allowed")
    @Builder.Default
    private Boolean petsAllowed = false;

    @Column(name = "smoking_allowed")
    @Builder.Default
    private Boolean smokingAllowed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ListingImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addImage(ListingImage image) {
        images.add(image);
        image.setListing(this);
    }

    public void removeImage(ListingImage image) {
        images.remove(image);
        image.setListing(null);
    }

    public void clearImages() {
        images.forEach(image -> image.setListing(null));
        images.clear();
    }

    /**
     * Calculate available spots in this listing.
     */
    public int getAvailableSpots() {
        if (totalRoommates == null || currentRoommates == null) {
            return 1;
        }
        return Math.max(0, totalRoommates - currentRoommates);
    }
}

