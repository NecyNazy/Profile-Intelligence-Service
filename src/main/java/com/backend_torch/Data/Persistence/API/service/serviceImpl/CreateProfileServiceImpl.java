package com.backend_torch.Data.Persistence.API.service.serviceImpl;

import com.backend_torch.Data.Persistence.API.dtos.*;
import com.backend_torch.Data.Persistence.API.exception.ApiException;
import com.backend_torch.Data.Persistence.API.helper.HttpShooter;
import com.backend_torch.Data.Persistence.API.model.Profiles;
import com.backend_torch.Data.Persistence.API.repository.ProfileRepository;
import com.backend_torch.Data.Persistence.API.service.contracts.CreateProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CreateProfileServiceImpl implements CreateProfileService {

    private final ProfileRepository profileRepository;
    private final HttpShooter httpShooter;
    private final AppProperties appProperties;

    @Override
    public ResponseEntity<?> createProfile(CreateProfileRequest request) {

        // 1. Validate input
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ApiException("error", "Name is required");
        }

        if (!request.getName().matches("^[a-zA-Z ]+$")|| request.getName().trim().length() < 2) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).build();

        }

        String name = request.getName().trim().toLowerCase();
        System.out.println("got here");
        // 2. Idempotency check
        Optional<Profiles> existing = profileRepository.findByName(name);
        if (existing.isPresent()) {
            return ResponseEntity.ok(mapIdempotencyResponse(existing.get()));
        }
        System.out.println("passed idempotency check");
        // 3. Fetch aggregated data
        AggregatedResponse data = fetchAndValidate(name);
        System.out.println(data.getGender() + " " + data.getAge() + " " + data.getCountryId());

        // 4. Persist entity
        Profiles profile = Profiles.builder()
                .name(data.getName())
                .gender(data.getGender())
                .genderProbability(data.getProbability())
                .age(data.getAge())
                .ageGroup(data.getAgeGroup())
                .countryId(data.getCountryId())
                .countryProbability(data.getProbability())
                .countryName(data.getCountryName())
                .createdAt(Instant.now())
                .build();
        System.out.println("profile built");
        Profiles saved = profileRepository.save(profile);
        System.out.println("saved");

        return new ResponseEntity<>(mapToResponse(profile), HttpStatus.CREATED);
   }

    private AggregatedResponse fetchAndValidate(String name) {

        String genderUrl = appProperties.getGenderizeApiUrl() + "?name=" + name.trim();
        String agifyUrl = appProperties.getAgifyApiUrl() + "?name=" + name.trim();
        String nationalizeUrl = appProperties.getNationalizeApiUrl() + "?name=" + name.trim();


        Mono<GenderizeResponse> gMono =
                httpShooter.getRequest(genderUrl, GenderizeResponse.class, "Genderize");
        System.out.println("reached genderized api");

        Mono<AgifyResponse> aMono =
                httpShooter.getRequest(agifyUrl, AgifyResponse.class,"Agify");
        System.out.println("Reached agify api");
        Mono<NationalizeResponse> cMono =
                httpShooter.getRequest(nationalizeUrl, NationalizeResponse.class, "Nationalize");
        System.out.println("reached nationalize api");
        return Mono.zip(gMono, aMono, cMono)
                .<AggregatedResponse>handle((tuple, sink) -> {
                    // ADD THESE LOGS TO SEE THE RAW DATA
                    log.info("Genderize Result: {}", tuple.getT1());
                    log.info("Agify Result: {}", tuple.getT2());
                    log.info("Nationalize Result: {}", tuple.getT3());
                    GenderizeResponse g = tuple.getT1();
                    AgifyResponse a = tuple.getT2();
                    NationalizeResponse c = tuple.getT3();

                    // Validation
                    final String GENDERIZE = "Genderize";
                    final String AGIFY = "Agify";
                    final String NATIONALIZE = "Nationalize";
                    if (g.getGender() == null || g.getCount() == 0) {
                        sink.error(new ApiException("502", GENDERIZE + " returned invalid response"));
                        return;

                    }
                    System.out.println("Genderize returned invalid response");
                    if (a.getAge() == null) {
                        sink.error(new ApiException("502", AGIFY  + " returned invalid response"));
                        return;
                    }
                    System.out.println("Agify returned invalid response");
                    if (c.getCountry() == null || c.getCountry().isEmpty()) {
                        sink.error(new ApiException("502", NATIONALIZE + " returned invalid response"));
                        return;
                    }
                    System.out.println("Nationalize returned invalid response");
                    sink.next(new AggregatedResponse(g, a, c));
                })
                .block();
    }

    private ProfileResponseDto mapToResponse(Profiles p) {
        System.out.println("mapped response");
        return ProfileResponseDto.builder()
                .status("success")
                .data(ProfileResponseDto.Data.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .gender(p.getGender())
                        .genderProbability(p.getGenderProbability())
                        .age(p.getAge())
                        .ageGroup(p.getAgeGroup())
                        .countryId(p.getCountryId())
                        .countryProbability(p.getCountryProbability())
                        .countryName(p.getCountryName())
                        .createdAt(p.getCreatedAt())
                        .build())
                .build();
    }
    private IdempotencyResponseDto mapIdempotencyResponse(Profiles p) {
        System.out.println("mapped response");
        return IdempotencyResponseDto.builder()
                .status("success")
                .message("Profile already exists")
                .data(IdempotencyResponseDto.Data.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .gender(p.getGender())
                        .genderProbability(p.getGenderProbability())
                        .age(p.getAge())
                        .ageGroup(p.getAgeGroup())
                        .countryId(p.getCountryId())
                        .countryProbability(p.getCountryProbability())
                        .countryName(p.getCountryName())
                        .createdAt(p.getCreatedAt())
                        .build())
                .build();
    }
}