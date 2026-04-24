package com.backend_torch.Data.Persistence.API.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ProfileFilterDto {
    private String gender;
    private String ageGroup;
    private String countryId;
    private Integer minAge;
    private Integer maxAge;
    private Float minGenderProbability;
    private Float minCountryProbability;
    private String sortBy;
    private String order;
    private int page;
    private int limit;
}