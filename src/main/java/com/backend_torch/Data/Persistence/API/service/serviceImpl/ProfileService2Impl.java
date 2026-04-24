package com.backend_torch.Data.Persistence.API.service.serviceImpl;

import com.backend_torch.Data.Persistence.API.dtos.ProfileResponse2Dto;
import com.backend_torch.Data.Persistence.API.model.Profiles;
import com.backend_torch.Data.Persistence.API.repository.ProfileRepository;
import com.backend_torch.Data.Persistence.API.service.contracts.Profile2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService2Impl implements Profile2Service {
    private final ProfileRepository profileRepository;

    @Override
    public ResponseEntity<ProfileResponse2Dto> getProfiles(String gender, String countryId, String ageGroup) {
        // 1. Fetch all profiles
        List<Profiles> allProfiles = profileRepository.findAll();
        log.info("Total records in DB: {}", allProfiles.size());

        // 2. Apply optional filters
        List<Profiles> filtered = allProfiles.stream()
                .filter(p -> gender == null || gender.isBlank() || p.getGender().equalsIgnoreCase(gender.trim()))
                .filter(p -> countryId == null || countryId.isBlank() || p.getCountryId().equalsIgnoreCase(countryId.trim()))
                .filter(p -> ageGroup == null || ageGroup.isBlank() || p.getAgeGroup().equalsIgnoreCase(ageGroup.trim()))
                .toList();

        log.info("Filtered records count: {}", filtered.size());

        // 3. Map to DTO List (Ensuring UUID is converted to String for standard JSON)
        List<ProfileResponse2Dto.Data> dataList = filtered.stream()
                .map(p -> ProfileResponse2Dto.Data.builder()
                        .id(p.getId()) // Ensure your DTO field is UUID or String
                        .name(p.getName())
                        .gender(p.getGender())
                        .age(p.getAge())
                        .ageGroup(p.getAgeGroup())
                        .countryId(p.getCountryId())
                        .build())
                .collect(Collectors.toList());

        // 4. Build Response (REMOVED the hardcoded data.get(1) index which caused errors)
        ProfileResponse2Dto response = ProfileResponse2Dto.builder()
                .status("success")
                .count(dataList.size())
                .data(dataList) // Passing the whole list correctly
                .build();
        System.out.println("response: " + response);

        return ResponseEntity.ok(response);
    }

//    // Helper to prevent the "Empty String" filter trap
//    private boolean isNullOrBlank(String str) {
//        return str == null || str.trim().isEmpty();
//    }
}