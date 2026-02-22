package com.sakny.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "governorates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Governorate {

    @Id
    private Integer id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ar", nullable = false, length = 100)
    private String nameAr;
}
