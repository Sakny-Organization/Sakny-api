package com.sakny.property.entity;

import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "property_type", nullable = false)
    private String propertyType; // e.g., Apartment, Room, Studio

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "rooms_count")
    private Integer roomsCount;

    @Column(name = "bathrooms_count")
    private Integer bathroomsCount;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "is_fully_furnished")
    private Boolean isFullyFurnished;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "deposit")
    private BigDecimal deposit;

    @Column(name = "minimum_stay_months")
    private Integer minimumStayMonths;

    @Column(name = "payment_period", length = 20)
    private String paymentPeriod;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(name = "parking_spots")
    private Integer parkingSpots;

    @Column(name = "utilities_included")
    private Boolean utilitiesIncluded;

    @Column(name = "internet_included")
    private Boolean internetIncluded;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed;

    @Column(name = "smoking_allowed")
    private Boolean smokingAllowed;

    @Column(name = "preferred_tenant", length = 50)
    private String preferredTenant;

    @ManyToMany
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImage> images;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isFullyFurnished == null) isFullyFurnished = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
