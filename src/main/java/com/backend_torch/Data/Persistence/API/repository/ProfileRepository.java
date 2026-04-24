package com.backend_torch.Data.Persistence.API.repository;

import com.backend_torch.Data.Persistence.API.model.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profiles, UUID>,
        JpaSpecificationExecutor<Profiles> {

    Optional<Profiles> findByName(String name);

    Boolean existsByName(String name);

    Optional<Profiles> findByGenderAndCountryIdAndAgeGroup(String gender, String countryId, String ageGroup);

}