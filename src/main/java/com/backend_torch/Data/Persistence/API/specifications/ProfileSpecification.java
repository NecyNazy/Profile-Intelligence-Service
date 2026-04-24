package com.backend_torch.Data.Persistence.API.specifications;

import com.backend_torch.Data.Persistence.API.model.Profiles;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProfileSpecification implements Specification<Profiles> {

    private final String gender;
    private final String ageGroup;
    private final String countryId;
    private final Integer minAge;
    private final Integer maxAge;
    private final Double minGenderProbability;
    private final Double minCountryProbability;

    @Override
    public Predicate toPredicate(Root<Profiles> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (gender != null)
            predicates.add(cb.equal(cb.lower(root.get("gender").as(String.class)), gender.toLowerCase()));

        if (ageGroup != null)
            predicates.add(cb.equal(cb.lower(root.get("ageGroup").as(String.class)), ageGroup.toLowerCase()));

        if (countryId != null)
            predicates.add(cb.equal(cb.lower(root.get("countryId").as(String.class)), countryId.toLowerCase()));

        if (minAge != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));

        if (maxAge != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("age"), maxAge));

        if (minGenderProbability != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("genderProbability"), minGenderProbability));

        if (minCountryProbability != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("countryProbability"), minCountryProbability));

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}