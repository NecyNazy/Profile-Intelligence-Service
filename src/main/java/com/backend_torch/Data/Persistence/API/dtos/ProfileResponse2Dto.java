package com.backend_torch.Data.Persistence.API.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse2Dto {
    private String status;
    private int count;
    private List<Data> data;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private UUID id;
        private String name;
        private String gender;
        private Integer age;

        @JsonProperty("age_group") // Requirement: snake_case
        private String ageGroup;

        @JsonProperty("country_id") // Requirement: snake_case
        private String countryId;

        @Override
        public String toString() {
            return "Data{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", gender='" + gender + '\'' +
                    ", age=" + age +
                    ", ageGroup='" + ageGroup + '\'' +
                    ", countryId='" + countryId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ProfileResponse2Dto{" +
                "status='" + status + '\'' +
                ", count=" + count +
                ", data=" + data +
                '}';
    }
}
