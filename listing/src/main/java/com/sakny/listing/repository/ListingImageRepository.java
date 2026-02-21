package com.sakny.listing.repository;

import com.sakny.listing.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    List<ListingImage> findByListingIdOrderByDisplayOrderAsc(Long listingId);

    void deleteByListingId(Long listingId);

    int countByListingId(Long listingId);
}

