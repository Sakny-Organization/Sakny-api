package com.sakny.listing.service;

import com.sakny.common.dto.*;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.ListingErrorCode;
import com.sakny.common.model.ListingStatus;
import com.sakny.common.service.StorageService;
import com.sakny.listing.entity.Listing;
import com.sakny.listing.entity.ListingImage;
import com.sakny.listing.entity.SavedListing;
import com.sakny.listing.mapper.ListingMapper;
import com.sakny.listing.repository.*;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import com.sakny.user.entity.User;
import com.sakny.user.repository.CityRepository;
import com.sakny.user.repository.GovernorateRepository;
import com.sakny.user.repository.UserProfileRepository;
import com.sakny.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final SavedListingRepository savedListingRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final GovernorateRepository governorateRepository;
    private final CityRepository cityRepository;
    private final ListingMapper listingMapper;
    private final StorageService storageService;

    private static final int MAX_IMAGES_PER_LISTING = 10;

    /**
     * Create a new listing.
     */
    @Transactional
    public ListingResponse createListing(Long userId, ListingRequest request, List<MultipartFile> images) {
        log.info("Creating listing for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.USER_NOT_FOUND));

        // Verify user has completed profile
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(ListingErrorCode.PROFILE_REQUIRED);
        }

        // Validate locations
        Governorate governorate = resolveGovernorate(request.getGovernorateId());
        City city = resolveCity(request.getCityId(), request.getGovernorateId());

        // Validate roommate count
        validateRoommateCount(request.getCurrentRoommates(), request.getTotalRoommates());

        // Create entity
        Listing listing = listingMapper.toEntity(request, user, governorate, city);
        Listing saved = listingRepository.save(listing);

        // Upload images
        if (images != null && !images.isEmpty()) {
            uploadListingImages(saved, images);
        }

        log.info("Listing created successfully with ID: {}", saved.getId());
        return listingMapper.toResponse(listingRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    /**
     * Update an existing listing.
     */
    @Transactional
    public ListingResponse updateListing(Long userId, Long listingId, ListingUpdateRequest request) {
        log.info("Updating listing ID: {} for user ID: {}", listingId, userId);

        Listing listing = listingRepository.findByIdWithDetails(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        // Verify ownership
        if (!listing.getUser().getId().equals(userId)) {
            throw new BusinessException(ListingErrorCode.LISTING_NOT_OWNED);
        }

        // Resolve locations if provided
        Governorate governorate = request.getGovernorateId() != null
                ? resolveGovernorate(request.getGovernorateId())
                : null;
        City city = request.getCityId() != null
                ? resolveCity(request.getCityId(), request.getGovernorateId() != null
                        ? request.getGovernorateId()
                        : listing.getGovernorate().getId())
                : null;

        // Validate roommate count
        Integer total = request.getTotalRoommates() != null ? request.getTotalRoommates() : listing.getTotalRoommates();
        Integer current = request.getCurrentRoommates() != null ? request.getCurrentRoommates() : listing.getCurrentRoommates();
        validateRoommateCount(current, total);

        // Apply updates
        listingMapper.partialUpdateEntity(listing, request, governorate, city);

        Listing saved = listingRepository.save(listing);
        log.info("Listing updated successfully: {}", listingId);

        return listingMapper.toResponse(saved);
    }

    /**
     * Get a listing by ID.
     */
    @Transactional(readOnly = true)
    public ListingResponse getListing(Long listingId) {
        log.debug("Fetching listing ID: {}", listingId);

        Listing listing = listingRepository.findByIdWithDetails(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        return listingMapper.toResponse(listing);
    }

    /**
     * Get all listings for the current user.
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(Long userId) {
        log.debug("Fetching listings for user ID: {}", userId);

        return listingRepository.findByUserId(userId).stream()
                .map(listingMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search listings with filters.
     */
    @Transactional(readOnly = true)
    public Page<ListingResponse> searchListings(ListingSearchCriteria criteria) {
        log.debug("Searching listings with criteria: {}", criteria);

        // Build pageable with sorting
        Sort sort = buildSort(criteria.getSortBy(), criteria.getSortDirection());
        Pageable pageable = PageRequest.of(
                criteria.getPage() != null ? criteria.getPage() : 0,
                criteria.getSize() != null ? criteria.getSize() : 20,
                sort
        );

        // Search with specifications
        Page<Listing> listings = listingRepository.findAll(
                ListingSpecifications.fromCriteria(criteria),
                pageable
        );

        return listings.map(listingMapper::toResponse);
    }

    /**
     * Delete a listing.
     */
    @Transactional
    public void deleteListing(Long userId, Long listingId) {
        log.info("Deleting listing ID: {} for user ID: {}", listingId, userId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        // Verify ownership
        if (!listing.getUser().getId().equals(userId)) {
            throw new BusinessException(ListingErrorCode.LISTING_NOT_OWNED);
        }

        // Delete images from storage
        for (ListingImage image : listing.getImages()) {
            try {
                String objectKey = storageService.extractObjectKey(image.getImageUrl());
                if (objectKey != null) {
                    storageService.deleteFile(objectKey);
                }
            } catch (Exception e) {
                log.warn("Failed to delete image from storage: {}", e.getMessage());
            }
        }

        listingRepository.delete(listing);
        log.info("Listing deleted successfully: {}", listingId);
    }

    /**
     * Change listing status.
     */
    @Transactional
    public ListingResponse changeStatus(Long userId, Long listingId, ListingStatus newStatus) {
        log.info("Changing status of listing ID: {} to: {}", listingId, newStatus);

        Listing listing = listingRepository.findByIdWithDetails(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        // Verify ownership
        if (!listing.getUser().getId().equals(userId)) {
            throw new BusinessException(ListingErrorCode.LISTING_NOT_OWNED);
        }

        if (listing.getStatus() == ListingStatus.CLOSED) {
            throw new BusinessException(ListingErrorCode.LISTING_ALREADY_CLOSED);
        }

        listing.setStatus(newStatus);
        Listing saved = listingRepository.save(listing);

        return listingMapper.toResponse(saved);
    }

    /**
     * Upload images to a listing.
     */
    @Transactional
    public ListingResponse uploadImages(Long userId, Long listingId, List<MultipartFile> images) {
        log.info("Uploading {} images to listing ID: {}", images.size(), listingId);

        Listing listing = listingRepository.findByIdWithDetails(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        // Verify ownership
        if (!listing.getUser().getId().equals(userId)) {
            throw new BusinessException(ListingErrorCode.LISTING_NOT_OWNED);
        }

        // Check max images
        int currentCount = listing.getImages().size();
        if (currentCount + images.size() > MAX_IMAGES_PER_LISTING) {
            throw new BusinessException(ListingErrorCode.TOO_MANY_IMAGES);
        }

        uploadListingImages(listing, images);

        return listingMapper.toResponse(listingRepository.findByIdWithDetails(listingId).orElse(listing));
    }

    /**
     * Save/bookmark a listing.
     */
    @Transactional
    public void saveListing(Long userId, Long listingId) {
        log.info("Saving listing ID: {} for user ID: {}", listingId, userId);

        if (savedListingRepository.existsByUserIdAndListingId(userId, listingId)) {
            return; // Already saved
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.USER_NOT_FOUND));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.LISTING_NOT_FOUND));

        SavedListing saved = SavedListing.builder()
                .user(user)
                .listing(listing)
                .build();

        savedListingRepository.save(saved);
        log.info("Listing saved successfully");
    }

    /**
     * Unsave/unbookmark a listing.
     */
    @Transactional
    public void unsaveListing(Long userId, Long listingId) {
        log.info("Unsaving listing ID: {} for user ID: {}", listingId, userId);
        savedListingRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    /**
     * Get saved listings for a user.
     */
    @Transactional(readOnly = true)
    public Page<ListingResponse> getSavedListings(Long userId, Pageable pageable) {
        log.debug("Fetching saved listings for user ID: {}", userId);

        return savedListingRepository.findByUserIdWithDetails(userId, pageable)
                .map(sl -> listingMapper.toResponse(sl.getListing()));
    }

    /**
     * Check if a listing is saved by user.
     */
    @Transactional(readOnly = true)
    public boolean isListingSaved(Long userId, Long listingId) {
        return savedListingRepository.existsByUserIdAndListingId(userId, listingId);
    }

    // ========== Private Helper Methods ==========

    private Governorate resolveGovernorate(Integer governorateId) {
        return governorateRepository.findById(governorateId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.INVALID_GOVERNORATE));
    }

    private City resolveCity(Integer cityId, Integer governorateId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ListingErrorCode.INVALID_CITY));

        if (!city.getGovernorate().getId().equals(governorateId)) {
            throw new BusinessException(ListingErrorCode.INVALID_CITY);
        }

        return city;
    }

    private void validateRoommateCount(Integer current, Integer total) {
        if (current != null && total != null && current > total) {
            throw new BusinessException(ListingErrorCode.INVALID_ROOMMATE_COUNT);
        }
    }

    private void uploadListingImages(Listing listing, List<MultipartFile> images) {
        int startOrder = listing.getImages().size();
        boolean isFirst = startOrder == 0;

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String imageUrl = storageService.uploadListingImage(file, listing.getId());

            ListingImage image = ListingImage.builder()
                    .listing(listing)
                    .imageUrl(imageUrl)
                    .displayOrder(startOrder + i)
                    .isPrimary(isFirst && i == 0)
                    .build();

            listing.addImage(image);
        }

        listingRepository.save(listing);
    }

    private Sort buildSort(String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String sortField = switch (sortBy != null ? sortBy.toLowerCase() : "date") {
            case "price" -> "rentAmount";
            case "date" -> "createdAt";
            default -> "createdAt";
        };

        return Sort.by(sortDirection, sortField);
    }
}

