package com.backend_torch.Data.Persistence.API.repository;

import com.backend_torch.Data.Persistence.API.model.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profiles,UUID> {
    Optional<Profiles> findByName(String name);

    Boolean existsByName(String name);

    Optional<Profiles> findByGenderAndCountryIdAndAgeGroup(String gender, String countryId , String ageGroup);

    @Query("""
        SELECT p FROM Profiles p
        WHERE (:gender IS NULL OR LOWER(p.gender) = LOWER(:gender))
        AND (:ageGroup IS NULL OR LOWER(p.ageGroup) = LOWER(:ageGroup))
        AND (:countryId IS NULL OR LOWER(p.countryId) = LOWER(:countryId))
        AND (:minAge IS NULL OR p.age >= :minAge)
        AND (:maxAge IS NULL OR p.age <= :maxAge)
        AND (:minGenderProbability IS NULL OR p.genderProbability >= :minGenderProbability)
        AND (:minCountryProbability IS NULL OR p.countryProbability >= :minCountryProbability)
    """)
    Page<Profiles> findWithFilters(
            @Param("gender") String gender,
            @Param("ageGroup") String ageGroup,
            @Param("countryId") String countryId,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("minGenderProbability") Double minGenderProbability,
            @Param("minCountryProbability") Double minCountryProbability,
            Pageable pageable
    );
}
