package com.sakny.user.repository;

import com.sakny.user.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findByGovernorateId(Integer governorateId);
}
