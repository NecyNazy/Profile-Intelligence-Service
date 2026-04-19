package com.backend_torch.Data.Persistence.API.service.contracts;

import com.backend_torch.Data.Persistence.API.dtos.CreateProfileRequest;
import com.backend_torch.Data.Persistence.API.dtos.IdempotencyResponseDto;
import org.springframework.http.ResponseEntity;

public interface CreateProfileService {
    ResponseEntity<?> createProfile(CreateProfileRequest request);
}
