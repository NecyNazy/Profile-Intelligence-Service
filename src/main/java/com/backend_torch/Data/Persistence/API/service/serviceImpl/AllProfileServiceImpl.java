package com.backend_torch.Data.Persistence.API.service.serviceImpl;

import com.backend_torch.Data.Persistence.API.dtos.AllProfileResponseDto;
import com.backend_torch.Data.Persistence.API.specifications.ProfileSpecification;
import com.backend_torch.Data.Persistence.API.model.Profiles;
import com.backend_torch.Data.Persistence.API.repository.ProfileRepository;
import com.backend_torch.Data.Persistence.API.service.contracts.AllProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllProfileServiceImpl implements AllProfileService {

    private final ProfileRepository profileRepository;

    private static final List<String> VALID_SORT_FIELDS = List.of("age", "created_at", "gender_probability");

    @Override
    public ResponseEntity<AllProfileResponseDto> getProfiles(
            String gender, String ageGroup, String countryId,
            Integer minAge, Integer maxAge,
            Double minGenderProbability, Double minCountryProbability,
            String sortBy, String order,
            int page, int limit) {

        // Validate sort_by field
        if (sortBy != null && !VALID_SORT_FIELDS.contains(sortBy)) {
            log.warn("Invalid sort_by value: {}", sortBy);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(AllProfileResponseDto.builder()
                            .status("error")
                            .build());
        }

        // Enforce pagination bounds
        int safeLimit = Math.min(limit, 50);
        int safePage = Math.max(page, 1);

        // Map client-facing sort field name to Java field name
        String sortField = switch (sortBy != null ? sortBy : "created_at") {
            case "age"                -> "age";
            case "gender_probability" -> "genderProbability";
            default                   -> "createdAt";
        };

        // Build sort direction
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Build pageable (page - 1 because Spring is 0-indexed, client sends 1-indexed)
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(direction, sortField));

        log.info("Fetching profiles — gender={}, ageGroup={}, countryId={}, minAge={}, maxAge={}, " +
                        "minGenderProbability={}, minCountryProbability={}, sortBy={}, order={}, page={}, limit={}",
                gender, ageGroup, countryId, minAge, maxAge,
                minGenderProbability, minCountryProbability, sortBy, order, safePage, safeLimit);

        // Query database
        ProfileSpecification spec = new ProfileSpecification(
                gender, ageGroup, countryId,
                minAge, maxAge,
                minGenderProbability, minCountryProbability
        );

        Page<Profiles> result = profileRepository.findAll(spec, pageable);

        log.info("Query returned {} records out of {} total", result.getNumberOfElements(), result.getTotalElements());

        // Map to response DTOs
        List<AllProfileResponseDto.Data> dataList = result.getContent()
                .stream()
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