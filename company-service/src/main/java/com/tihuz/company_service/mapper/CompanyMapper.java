package com.tihuz.company_service.mapper;

import com.tihuz.company_service.dto.CompanyRequest;
import com.tihuz.company_service.dto.CompanyResponse;
import com.tihuz.company_service.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    Company toEntity(CompanyRequest request);

    CompanyResponse toResponse(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Company company, CompanyRequest request);
}