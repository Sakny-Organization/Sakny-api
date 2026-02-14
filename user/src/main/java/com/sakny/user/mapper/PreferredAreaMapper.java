package com.sakny.user.mapper;

import com.sakny.common.dto.PreferredAreaResponse;
import com.sakny.user.entity.PreferredArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface PreferredAreaMapper {

    @Mapping(target = "governorate", source = "governorate")
    @Mapping(target = "city", source = "city")
    PreferredAreaResponse toResponse(PreferredArea area);
}
