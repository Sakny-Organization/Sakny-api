package com.sakny.user.service;

import com.sakny.common.dto.LocationDto;
import com.sakny.user.mapper.LocationMapper;
import com.sakny.user.repository.CityRepository;
import com.sakny.user.repository.GovernorateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final GovernorateRepository governorateRepository;
    private final CityRepository cityRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<LocationDto> getAllGovernorates() {
        log.debug("Fetching all governorates");
        return governorateRepository.findAll().stream()
                .map(locationMapper::governorateToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDto> getCitiesByGovernorate(Integer governorateId) {
        log.debug("Fetching cities for governorate ID: {}", governorateId);
        return cityRepository.findByGovernorateId(governorateId).stream()
                .map(locationMapper::cityToDto)
                .collect(Collectors.toList());
    }
}
