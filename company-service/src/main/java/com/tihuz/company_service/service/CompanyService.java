package com.tihuz.company_service.service;

import com.tihuz.common.event.CompanyEvent;
import com.tihuz.common.event.UserEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import com.tihuz.company_service.dto.CompanyRequest;
import com.tihuz.company_service.dto.CompanyResponse;
import com.tihuz.company_service.entity.Company;
import com.tihuz.company_service.mapper.CompanyMapper;
import com.tihuz.company_service.repository.CompanyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level= AccessLevel.PRIVATE, makeFinal = true)
public class CompanyService {

      CompanyRepository companyRepository;
      CompanyMapper companyMapper;
      KafkaTemplate<String, Object> kafkaTemplate;

    public CompanyResponse createCompany(CompanyRequest request)
    {
        log.info("Creating company: {}", request.getName());

        if (companyRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }

        Company company = companyMapper.toEntity(request);
        Company saved = companyRepository.save(company);

        CompanyEvent event = new CompanyEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("COMPANY_CREATED");
        event.setSource("company-service");
        event.setCompanyId(saved.getId());
        event.setAction("CREATED");

        // Set đầy đủ thông tin
        event.setCompanyName(saved.getName());
        event.setLogo(saved.getLogo());
        event.setWebsite(saved.getWebsite());
        event.setEmail(saved.getEmail());
        event.setScale(saved.getScale());
        event.setDescription(saved.getDescription());
        event.setAddress(saved.getAddress());
        event.setCategory(saved.getCategory());

        kafkaTemplate.send(CompanyEvent.TOPIC, event);
        log.info("Published COMPANY_CREATED event for company: {}", saved.getId());

        return companyMapper.toResponse(saved);
    }


    public CompanyResponse updateCompany(Long id, CompanyRequest request)
    {
        log.info("Updating company with id: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        companyMapper.updateEntity(company, request);
        Company updated = companyRepository.save(company);


        // GỬI EVENT SAU KHI UPDATE THÀNH CÔNG
        CompanyEvent event = new CompanyEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("COMPANY_UPDATED");
        event.setSource("company-service");
        event.setCompanyId(updated.getId());
        event.setAction("UPDATED");

        // Set tất cả thông tin mới nhất vào event
        event.setCompanyName(updated.getName());
        event.setLogo(updated.getLogo());
        event.setWebsite(updated.getWebsite());
        event.setEmail(updated.getEmail());
        event.setScale(updated.getScale());
        event.setDescription(updated.getDescription());
        event.setAddress(updated.getAddress());
        event.setCategory(updated.getCategory());

        try {
            kafkaTemplate.send(CompanyEvent.TOPIC, event);
            log.info("Published COMPANY_UPDATED event for company: {}", updated.getId());
        } catch (Exception e) {
            log.error("Failed to send Kafka event: {}", e.getMessage(), e);
        }

        return companyMapper.toResponse(updated);
    }

    public CompanyResponse getCompanyById(Long id)
    {
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toResponse(company);
    }


    public CompanyResponse getCompanyByName(String name) {
        log.info("Getting company by name: {}", name);

        Company company = companyRepository.findByNameAndIsDeletedFalse(name)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        return companyMapper.toResponse(company);
    }

    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        log.info("Getting all companies");

        Page<Company> companies = companyRepository.findByIsDeletedFalse(pageable);
        return companies.map(companyMapper::toResponse);
    }

    public List<CompanyResponse> getTopCompanies(int limit) {
        log.info("Getting top {} companies", limit);

        return companyRepository.findByIsDeletedFalse(PageRequest.of(0,limit))
                .stream()
                .map(companyMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<String> getAllCategories() {
        List<Company> companies = companyRepository.findAllByIsDeletedFalse();

        // Lọc category không null, loại trùng, sắp xếp
        List<String> categories = companies.stream()
                .map(Company::getCategory)           // Lấy category
                .filter(category -> category != null && !category.trim().isEmpty()) // Bỏ null và rỗng
                .distinct()                           // Loại trùng
                .sorted()                             // Sắp xếp A-Z
                .collect(Collectors.toList());

        return categories;
    }



    public List<CompanyResponse> searchCompanies(String keyword) {
        log.info("Searching companies with keyword: {}", keyword);

        return companyRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword).stream()
                .map(companyMapper::toResponse)
                .collect(Collectors.toList());
    }




    public Page<CompanyResponse> getActiveCompanies(Pageable pageable, String keyword) {
        Page<Company> page;
        if (keyword != null && !keyword.isEmpty())
        {
            page = companyRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(keyword, pageable);
        }
        else
        {
            page = companyRepository.findByIsDeletedFalse(pageable);
        }
        return page.map(companyMapper::toResponse);
    }

    public Page<CompanyResponse> getDeletedCompanies(Pageable pageable, String keyword)
    {
        Page<Company> page;
        if (keyword != null && !keyword.isEmpty())
        {
            page = companyRepository.findByIsDeletedTrueAndNameContainingIgnoreCase(keyword, pageable);
        }
        else
        {
            page = companyRepository.findByIsDeletedTrue(pageable);
        }
        return page.map(companyMapper::toResponse);
    }

    public void restoreCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        company.setIsDeleted(false);
        companyRepository.save(company);
    }



    public void softDeleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setIsDeleted(true);
        companyRepository.save(company);

        // Publish event
        CompanyEvent event = new CompanyEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("COMPANY_SOFT_DELETED");
        event.setSource("company-service");
        event.setCompanyId(company.getId());
        event.setCompanyName(company.getName());
        event.setAction("SOFT_DELETED");

        kafkaTemplate.send(CompanyEvent.TOPIC, event);
        log.info("Published COMPANY_SOFT_DELETED  event for company: {}", company.getId());

    }



    public void hardDeleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        companyRepository.delete(company);

        // Publish event
        CompanyEvent event = new CompanyEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("COMPANY_HARD_DELETED");
        event.setSource("company-service");
        event.setCompanyId(company.getId());
        event.setCompanyName(company.getName());
        event.setAction("HARD_DELETED");

        kafkaTemplate.send(CompanyEvent.TOPIC, event);
        log.info("Published COMPANY_HARD_DELETED event for company: {}", company.getId());

    }

//
//    @KafkaListener(topics = UserEvent.TOPIC, groupId ="company-service-group" )
//    public void handleUserEvent( UserEvent event)
//    {
//        log.info("Company Service received UserEvent: Action={}, UserId={}, CompanyId={}",
//                event.getAction(), event.getUserId(), event.getCompanyId());
//
//        switch (event.getAction())
//        {
//            case "SOFT_DELETED":
//            case "HARD_DELETED":
//                if (event.getCompanyId() != null)
//                {
//                    // Find company theo companyId
//                    Optional<Company> companyOpt = companyRepository.findByIdAndIsDeletedFalse(event.getCompanyId());
//                    if (companyOpt.isPresent())
//                    {
//                        Company company = companyOpt.get();
//                        if ("SOFT_DELETED".equals(event.getAction()))
//                        {
//                            company.setIsDeleted(true);
//                            companyRepository.save(company);
//                            log.info("Soft deleted company {} for deleted user {}", company.getId(), event.getUserId());
//                        }
//                        else
//                        {
//                            companyRepository.delete(company);
//                            log.info("Hard deleted company {} for deleted user {}", company.getId(), event.getUserId());
//                        }
//                    }
//                }
//                break;
//            case "CREATED":
//                log.info("New user created: {}", event.getUserId());
//                break;
//            default:
//                log.debug("No action needed for user event: {}", event.getAction());
//        }
//    }

    @KafkaListener(topics = UserEvent.TOPIC, groupId ="company-service-group" )
    public void handleUserEvent(UserEvent event) {
        log.info("Company Service received UserEvent: Action={}, UserId={}, CompanyId={}",
                event.getAction(), event.getUserId(), event.getCompanyId());

        switch (event.getAction()) {
            case "SOFT_DELETED":
                if (event.getCompanyId() != null)
                {
                    softDeleteCompany(event.getCompanyId());
                    log.info("Company {} soft deleted via user event", event.getCompanyId());
                }
                break;
            case "HARD_DELETED":
                if (event.getCompanyId() != null)
                {
                    // Call method hardDeleteCompany
                    hardDeleteCompany(event.getCompanyId());
                    log.info("Company {} hard deleted via user event", event.getCompanyId());
                }
                break;


            case "CREATED":
                log.info("New user created: {}", event.getUserId());
                break;

            case "UPDATE":
                if (event.getCompanyId() == null || event.getEmail() == null) break;

                Company company = companyRepository.findByIdAndIsDeletedFalse(event.getCompanyId()).orElse(null);
                if (company != null)
                {
                    company.setEmail(event.getEmail());
                    companyRepository.save(company);
                }
                break;
            default:
                log.debug("No action needed for user event: {}", event.getAction());
        }
    }


}