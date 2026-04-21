package com.sakny.property.mapper;

import com.sakny.property.dto.ReservationRequest;
import com.sakny.property.dto.ReservationResponse;
import com.sakny.property.entity.Reservation;
import com.sakny.property.entity.Property;
import com.sakny.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", source = "property")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "startDate", source = "request.startDate")
    @Mapping(target = "endDate", source = "request.endDate")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", expression = "java(com.sakny.property.entity.ReservationStatus.PENDING)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "note", source = "request.note")
    Reservation toEntity(ReservationRequest request, Property property, User user, BigDecimal totalPrice);

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    ReservationResponse toResponse(Reservation reservation);

    List<ReservationResponse> toResponseList(List<Reservation> reservations);
}
