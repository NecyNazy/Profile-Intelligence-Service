package com.backend_torch.Data.Persistence.API.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// AllProfileResponseDto.java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllProfileResponseDto {

    private String status;
    private Integer page;
    private Integer limit;
    private Long total;
    private List<Data> data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private UUID id;
        private String name;
        private String gender;

        @JsonProperty("gender_probability")
        private Float genderProbability;

        private Integer age;

        @JsonProperty("age_group")
        private String ageGroup;

        @JsonProperty("country_id")
        private String countryId;

        @JsonProperty("country_name")
        private String countryName;

        @JsonProperty("country_probability")
        private Float countryProbability;

        @JsonProperty("created_at")
        private Instant createdAt;
    }
}