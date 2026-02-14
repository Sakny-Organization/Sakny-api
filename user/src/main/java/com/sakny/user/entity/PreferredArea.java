package com.sakny.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preferred_areas",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_preferred_area_location",
                columnNames = {"profile_id", "governorate_id", "city_id"}
        ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "profile")
@EqualsAndHashCode(exclude = "profile")
public class PreferredArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(length = 255)
    private String street;
}
