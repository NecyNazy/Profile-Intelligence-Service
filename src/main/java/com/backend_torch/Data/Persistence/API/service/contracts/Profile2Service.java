package com.backend_torch.Data.Persistence.API.service.contracts;

import com.backend_torch.Data.Persistence.API.dtos.ProfileResponse2Dto;
import org.springframework.http.ResponseEntity;

public interface Profile2Service {
    ResponseEntity<ProfileResponse2Dto> getProfiles(String gender, String countryId, String ageGroup);
}
