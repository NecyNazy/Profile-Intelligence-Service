package com.backend_torch.Data.Persistence.API.controller;

import com.backend_torch.Data.Persistence.API.dtos.AllProfileResponseDto;
import com.backend_torch.Data.Persistence.API.dtos.CreateProfileRequest;
import com.backend_torch.Data.Persistence.API.dtos.ProfileResponseDto;
import com.backend_torch.Data.Persistence.API.service.contracts.AllProfileService;
import com.backend_torch.Data.Persistence.API.service.contracts.CreateProfileService;
import com.backend_torch.Data.Persistence.API.service.contracts.DeleteProfileService;
import com.backend_torch.Data.Persistence.API.service.contracts.SingleProfileService;
import com.backend_torch.Data.Persistence.API.service.serviceImpl.AllProfileServiceImpl;
import com.backend_torch.Data.Persistence.API.service.serviceImpl.CreateProfileServiceImpl;
import com.backend_torch.Data.Persistence.API.service.serviceImpl.DeleteProfileServiceImpl;
import com.backend_torch.Data.Persistence.API.service.serviceImpl.SingleProfileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class ProfileController {
    private final CreateProfileServiceImpl createProfileService;
    private final SingleProfileServiceImpl singleProfileService;
    private final AllProfileServiceImpl allProfileService;
    private final DeleteProfileServiceImpl deleteProfileService;

    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest request) {
        return createProfileService.createProfile(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDto> getSingleProfile(@PathVariable UUID id) {
        return singleProfileService.getSingleProfile(id);
    }

    @GetMapping
    public ResponseEntity<AllProfileResponseDto> getAllProfiles(@RequestParam(value = "gender", required = false) String gender,
                                                                @RequestParam(value = "country_id", required = false) String countryId, // Maps country_id to countryId
                                                                @RequestParam(value = "age_group", required = false) String ageGroup) {
        return allProfileService.getProfiles(gender,countryId,ageGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID id){
        return deleteProfileService.deleteProfile(id);
    }
}