package com.backend_torch.Data.Persistence.API.controller;

import com.backend_torch.Data.Persistence.API.dtos.ProfileFilterDto;
import com.backend_torch.Data.Persistence.API.dtos.ProfileResponse2Dto;
import com.backend_torch.Data.Persistence.API.dtos.CreateProfileRequest;
import com.backend_torch.Data.Persistence.API.dtos.ProfileResponseDto;
import com.backend_torch.Data.Persistence.API.helper.NaturalLanguageQueryParser;
import com.backend_torch.Data.Persistence.API.service.serviceImpl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProfileController {
    private final CreateProfileServiceImpl createProfileService;
    private final SingleProfileServiceImpl singleProfileService;
    private final ProfileService2Impl allProfileService;
    private final DeleteProfileServiceImpl deleteProfileService;
    private final AllProfileServiceImpl allProfileServiceImpl;
    private final NaturalLanguageQueryParser nlqParser;

    // GET /api/profiles
    @GetMapping
    public ResponseEntity<?> getAllProfiles(
            @RequestParam(required = false) String gender,
            @RequestParam(value = "age_group", required = false) String ageGroup,
            @RequestParam(value = "country_id", required = false) String countryId,
            @RequestParam(value = "min_age", required = false) Integer minAge,
            @RequestParam(value = "max_age", required = false) Integer maxAge,
            @RequestParam(value = "min_gender_probability", required = false) Double minGenderProbability,
            @RequestParam(value = "min_country_probability", required = false) Double minCountryProbability,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        return allProfileServiceImpl.getProfiles(
                gender, ageGroup, countryId,
                minAge, maxAge,
                minGenderProbability, minCountryProbability,
                sortBy, order,
                page, limit
        );
    }

    // GET /api/profiles/search?q=...
    @GetMapping("/search")
    public ResponseEntity<?> naturalLanguageSearch(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "Invalid query parameters")
            );
        }

        ProfileFilterDto filters = nlqParser.parse(q);

        if (filters == null) {
            return ResponseEntity.status(422).body(
                    Map.of("status", "error", "message", "Unable to interpret query")
            );
        }

        return allProfileServiceImpl.getProfiles(
                filters.getGender(),
                filters.getAgeGroup(),
                filters.getCountryId(),
                filters.getMinAge(),
                filters.getMaxAge(),
                null, null,
                null, null,
                page, limit
        );
    }
}

//    // POST /api/profiles
//    @PostMapping
//    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequestDto request) {
//        return createProfileService.createProfile(request);
//    }
//
//    // GET /api/profiles/{id}
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getProfileById(@PathVariable String id) {
//        return getProfileByIdService.getProfile(id);
//    }
//}
//
//    @PostMapping
//    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest request) {
//        return createProfileService.createProfile(request);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ProfileResponseDto> getSingleProfile(@PathVariable UUID id) {
//        return singleProfileService.getSingleProfile(id);
//    }
//
//    @GetMapping
//    public ResponseEntity<ProfileResponse2Dto> retrieveAllProfiles(@RequestParam(value = "gender", required = false) String gender,
//                                                              @RequestParam(value = "country_id", required = false) String countryId, // Maps country_id to countryId
//                                                              @RequestParam(value = "age_group", required = false) String ageGroup) {
//        return allProfileService.getProfiles(gender,countryId,ageGroup);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteProfile(@PathVariable UUID id){
//        return deleteProfileService.deleteProfile(id);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ProfileResponseDto>> getProfiles(
//            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
//            @RequestParam(required = false, defaultValue = "desc") String order) {
//
//        return ResponseEntity.ok(profileService.sortProfiles(sortBy, order));
//    }
//}
//
