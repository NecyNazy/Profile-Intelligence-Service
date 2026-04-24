package com.backend_torch.Data.Persistence.API.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProfileResponseDto {
    private String status;
    private Data data;

    @Getter
    @Setter
    @Builder
   public static class Data{
        private UUID id;
        private String name;
        private String gender;
        private Float genderProbability;
        private Integer age;
        private String ageGroup;
        private String countryId;
        private Float countryProbability;
        private String countryName;
        private Instant createdAt;

    }
}
