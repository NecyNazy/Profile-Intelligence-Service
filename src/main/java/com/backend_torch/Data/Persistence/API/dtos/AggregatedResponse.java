package com.backend_torch.Data.Persistence.API.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Comparator;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedResponse {

    private String name;

    private String gender;
    private Float probability;

    private Long count;

    private Integer age;
    private String ageGroup;

    private String countryId;
    private String countryName;

    /**
     * Aggregation constructor
     */
    public AggregatedResponse(GenderizeResponse genderRes,
                              AgifyResponse ageRes,
                              NationalizeResponse countryRes) {


        // Gender
        this.name = genderRes.getName();
        this.gender = genderRes.getGender();
        this.probability = genderRes.getProbability();
        this.count = genderRes.getCount();

        // Age
        this.age = ageRes.getAge();
        this.ageGroup = mapAgeToGroup(ageRes.getAge());

        // Country (pick the highest probability)
        if (countryRes.getCountry() != null && !countryRes.getCountry().isEmpty()) {
            NationalizeResponse.Country topCountry = countryRes.getCountry()
                    .stream()
                    .max(Comparator.comparing(NationalizeResponse.Country::getCountry_probability))
                    .orElse(null);

            this.countryId = topCountry.getCountry_id();
            this.probability = topCountry.getCountry_probability();
            this.countryName = topCountry.getCountry_name();
        }
    }

    /**
     * Simple age grouping logic
     */
    private String mapAgeToGroup(Integer age) {
        if (age == null) return null;

        if (age < 13) return "child";
        if (age < 20) return "teen";
        if (age < 40) return "adult";
        return "elder";
    }
}