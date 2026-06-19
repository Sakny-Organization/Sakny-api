package com.sakny.property.service;

import com.sakny.common.config.CacheConfig;
import com.sakny.property.dto.AmenityResponse;
import com.sakny.property.mapper.PropertyMapper;
import com.sakny.property.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final PropertyMapper propertyMapper;

    @Cacheable(CacheConfig.AMENITIES)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(propertyMapper::toAmenityResponse)
                .toList();
    }
}
