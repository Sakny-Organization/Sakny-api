package com.sakny.property.service;

import com.sakny.common.exception.BusinessException;
import com.sakny.common.service.StorageService;
import com.sakny.property.dto.PropertyFilterRequest;
import com.sakny.property.dto.PropertyRequest;
import com.sakny.property.dto.PropertyResponse;
import com.sakny.property.entity.Amenity;
import com.sakny.property.entity.Property;
import com.sakny.property.entity.PropertyImage;
import com.sakny.property.exception.PropertyErrorCode;
import com.sakny.property.mapper.PropertyMapper;
import com.sakny.property.repository.AmenityRepository;
import com.sakny.property.repository.PropertyImageRepository;
import com.sakny.property.repository.PropertyRepository;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import com.sakny.user.repository.CityRepository;
import com.sakny.user.repository.GovernorateRepository;
import com.sakny.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository imageRepository;
    private final AmenityRepository amenityRepository;
    private final GovernorateRepository governorateRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PropertyMapper propertyMapper;
    private final StorageService storageService;

    @Transactional
    public PropertyResponse createProperty(Long ownerId, PropertyRequest request, List<MultipartFile> images) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.USER_NOT_FOUND));

        Governorate governorate = governorateRepository.findById(request.getGovernorateId())
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.GOVERNORATE_NOT_FOUND));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.CITY_NOT_FOUND));

        if (!city.getGovernorate().getId().equals(request.getGovernorateId())) {
            throw new BusinessException(PropertyErrorCode.CITY_GOVERNORATE_MISMATCH);
        }

        Set<Amenity> amenities = resolveAmenities(request.getAmenityIds());
        Property property = propertyMapper.toEntity(request, owner, governorate, city, amenities);
        Property saved = propertyRepository.save(property);

        uploadPropertyImages(saved, images);

        log.info("Property created with ID {} by user {}", saved.getId(), ownerId);
        return propertyMapper.toResponse(propertyRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> getProperties(PropertyFilterRequest filter, Pageable pageable) {
        return propertyRepository.findAll(buildSpec(filter), pageable).map(propertyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PropertyResponse getProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.PROPERTY_NOT_FOUND));
        return propertyMapper.toResponse(property);
    }

    @Transactional
    public PropertyResponse updateProperty(Long ownerId, Long propertyId, PropertyRequest request) {
        Property property = getOwnedProperty(ownerId, propertyId);

        Governorate governorate = governorateRepository.findById(request.getGovernorateId())
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.GOVERNORATE_NOT_FOUND));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.CITY_NOT_FOUND));

        if (!city.getGovernorate().getId().equals(request.getGovernorateId())) {
            throw new BusinessException(PropertyErrorCode.CITY_GOVERNORATE_MISMATCH);
        }

        Set<Amenity> amenities = resolveAmenities(request.getAmenityIds());

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setPropertyType(request.getPropertyType());
        property.setGovernorate(governorate);
        property.setCity(city);
        property.setAddress(request.getAddress());
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setRoomsCount(request.getRoomsCount());
        property.setBathroomsCount(request.getBathroomsCount());
        property.setFloorNumber(request.getFloorNumber());
        property.setIsFullyFurnished(request.getIsFullyFurnished());
        property.setAvailableFrom(request.getAvailableFrom());
        property.setAmenities(amenities);

        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    @Transactional
    public void deleteProperty(Long ownerId, Long propertyId) {
        Property property = getOwnedProperty(ownerId, propertyId);

        if (property.getImages() != null) {
            for (PropertyImage image : property.getImages()) {
                try {
                    String key = storageService.extractObjectKey(image.getImageUrl());
                    if (key != null) storageService.deleteFile(key);
                } catch (Exception e) {
                    log.warn("Failed to delete property image {}: {}", image.getId(), e.getMessage());
                }
            }
        }

        propertyRepository.delete(property);
        log.info("Property {} deleted by owner {}", propertyId, ownerId);
    }

    @Transactional
    public PropertyResponse addImages(Long ownerId, Long propertyId, List<MultipartFile> images) {
        Property property = getOwnedProperty(ownerId, propertyId);
        uploadPropertyImages(property, images);
        return propertyMapper.toResponse(propertyRepository.findById(propertyId).orElse(property));
    }

    @Transactional
    public PropertyResponse deleteImage(Long ownerId, Long propertyId, Long imageId) {
        getOwnedProperty(ownerId, propertyId);

        PropertyImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.IMAGE_NOT_FOUND));

        if (!image.getProperty().getId().equals(propertyId)) {
            throw new BusinessException(PropertyErrorCode.IMAGE_PROPERTY_MISMATCH);
        }

        try {
            String key = storageService.extractObjectKey(image.getImageUrl());
            if (key != null) storageService.deleteFile(key);
        } catch (Exception e) {
            log.warn("Failed to delete image file {}: {}", imageId, e.getMessage());
        }

        imageRepository.delete(image);
        return propertyMapper.toResponse(propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.PROPERTY_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getMyProperties(Long ownerId) {
        return propertyMapper.toResponseList(propertyRepository.findByOwnerId(ownerId));
    }

    // ===== Helpers =====

    private Property getOwnedProperty(Long ownerId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(PropertyErrorCode.PROPERTY_NOT_FOUND));
        if (!property.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(PropertyErrorCode.NOT_PROPERTY_OWNER);
        }
        return property;
    }

    private Set<Amenity> resolveAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) return new HashSet<>();
        Set<Amenity> amenities = new HashSet<>();
        for (Long id : amenityIds) {
            amenityRepository.findById(id).ifPresent(amenities::add);
        }
        return amenities;
    }

    private void uploadPropertyImages(Property property, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        boolean hasPrimary = property.getImages() != null &&
                property.getImages().stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) continue;
            try {
                String url = storageService.uploadProfilePhoto(file, property.getOwner().getId());
                boolean isPrimary = !hasPrimary && i == 0;
                PropertyImage image = PropertyImage.builder()
                        .property(property)
                        .imageUrl(url)
                        .isPrimary(isPrimary)
                        .build();
                imageRepository.save(image);
                if (isPrimary) hasPrimary = true;
            } catch (Exception e) {
                log.warn("Failed to upload property image at index {}: {}", i, e.getMessage());
            }
        }
    }

    private Specification<Property> buildSpec(PropertyFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getGovernorateId() != null) {
                predicates.add(cb.equal(root.get("governorate").get("id"), filter.getGovernorateId()));
            }
            if (filter.getCityId() != null) {
                predicates.add(cb.equal(root.get("city").get("id"), filter.getCityId()));
            }
            if (filter.getPropertyType() != null && !filter.getPropertyType().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("propertyType")),
                        filter.getPropertyType().toLowerCase()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getFurnished() != null) {
                predicates.add(cb.equal(root.get("isFullyFurnished"), filter.getFurnished()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
