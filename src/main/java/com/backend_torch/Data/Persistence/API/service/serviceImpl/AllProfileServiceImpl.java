package com.backend_torch.Data.Persistence.API.service.serviceImpl;

import com.backend_torch.Data.Persistence.API.dtos.AllProfileResponseDto;
import com.backend_torch.Data.Persistence.API.model.Profiles;
import com.backend_torch.Data.Persistence.API.repository.ProfileRepository;
import com.backend_torch.Data.Persistence.API.service.contracts.AllProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

// AllProfileServiceImpl.java
@Slf4j
@RequiredArgsConstructor
@Service
public class AllProfileServiceImpl implements AllProfileService {

    private final ProfileRepository profileRepository;

    @Override
    public ResponseEntity<AllProfileResponseDto> getProfiles(
            String gender, String ageGroup, String countryId,
            Integer minAge, Integer maxAge,
            Double minGenderProbability, Double minCountryProbability,
            String sortBy, String order,
            int page, int limit) {

        // Validate sort fields
        List<String> validSortFields = List.of("age", "created_at", "gender_probability");
        if (sortBy != null && !validSortFields.contains(sortBy)) {
            return ResponseEntity.status(422).body(
                    AllProfileResponseDto.builder()
                            .status("error")
                            .build()
            );
        }

        // Enforce limit max
        int safeLimit = Math.min(limit, 50);
        int safePage = Math.max(page, 1);

        // Build sort
        String sortField = switch (sortBy != null ? sortBy : "created_at") {
            case "age" -> "age";
            case "gender_probability" -> "genderProbability";
            default -> "createdAt";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit,
                Sort.by(direction, sortField));

        Page<Profiles> result = profileRepository.findWithFilters(
                gender, ageGroup, countryId,
                minAge, maxAge,
                minGenderProbability, minCountryProbability,
                pageable
        );

        List<AllProfileResponseDto.Data> dataList = result.getContent().stream()
                .map(this::mapToData)
                .toList();

        return ResponseEntity.ok(AllProfileResponseDto.builder()
                .status("success")
                .page(safePage)
                .limit(safeLimit)
                .total(result.getTotalElements())
                .data(dataList)
                .build());
    }

    private AllProfileResponseDto.Data mapToData(Profiles p) {
        return AllProfileResponseDto.Data.builder()
                .id(p.getId())
                .name(p.getName())
                .gender(p.getGender())
                .genderProbability(p.getGenderProbability())
                .age(p.getAge())
                .ageGroup(p.getAgeGroup())
                .countryId(p.getCountryId())
                .countryName(p.getCountryName())
                .countryProbability(p.getCountryProbability())
                .createdAt(p.getCreatedAt())
                .build();
    }
}