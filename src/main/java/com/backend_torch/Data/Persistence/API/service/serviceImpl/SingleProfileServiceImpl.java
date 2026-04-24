package com.backend_torch.Data.Persistence.API.service.serviceImpl;

import com.backend_torch.Data.Persistence.API.dtos.ProfileResponseDto;
import com.backend_torch.Data.Persistence.API.exception.ApiException;
import com.backend_torch.Data.Persistence.API.model.Profiles;
import com.backend_torch.Data.Persistence.API.repository.ProfileRepository;
import com.backend_torch.Data.Persistence.API.service.contracts.SingleProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SingleProfileServiceImpl implements SingleProfileService {
    private final ProfileRepository profileRepository;

    @Override
    public ResponseEntity<ProfileResponseDto> getSingleProfile(UUID id) {

        //1.Validate Input
        if (id == null) {
            throw new ApiException("error", "Profile id is required");
        }
        //2. Validate id
        Profiles profile = profileRepository.findById(id)
                .orElseThrow(() -> new ApiException("error", "Profile not found", 404));

        //3. return response
        ProfileResponseDto response = ProfileResponseDto.builder()
                .status("success")
                .data(ProfileResponseDto.Data.builder()
                        .id(profile.getId())
                        .name(profile.getName())
                        .gender(profile.getGender())
                        .genderProbability(profile.getGenderProbability())
                        .age(profile.getAge())
                        .ageGroup(profile.getAgeGroup())
                        .countryId(profile.getCountryId())
                        .countryProbability(profile.getCountryProbability())
                        .createdAt(profile.getCreatedAt())
                        .build())
                .build();
        return ResponseEntity.ok(response);

    }
}
