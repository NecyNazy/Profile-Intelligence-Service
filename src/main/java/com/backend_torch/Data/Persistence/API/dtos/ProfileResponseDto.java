package com.backend_torch.Data.Persistence.API.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
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
        private Double genderProbability;
        private Long sampleSize;
        private Integer age;
        private String ageGroup;
        private String countryId;
        private Double countryProbability;
        private Instant createdAt;

    }
}
