package com.backend_torch.Data.Persistence.API.service.contracts;

import com.backend_torch.Data.Persistence.API.dtos.AllProfileResponseDto;
import org.springframework.http.ResponseEntity;

public interface AllProfileService {
    public ResponseEntity<AllProfileResponseDto> getProfiles(
            String gender, String ageGroup, String countryId,
            Integer minAge, Integer maxAge,
            Double minGenderProbability, Double minCountryProbability,
            String sortBy, String order,
            int page, int limit);


}
