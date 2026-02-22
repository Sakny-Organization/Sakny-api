package com.sakny.user.mapper;

import com.sakny.common.dto.LocationDto;
import com.sakny.user.entity.City;
import com.sakny.user.entity.Governorate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto governorateToDto(Governorate governorate);

    LocationDto cityToDto(City city);
}
