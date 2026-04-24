package com.backend_torch.Data.Persistence.API.dtos;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyResponseDto {
      private  String status;
      private String message;
        private Data data;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
       public static class Data {
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