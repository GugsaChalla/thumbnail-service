package com.api.imageIngestion.repository;

import com.api.imageIngestion.entity.ImageSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageSetRepository extends JpaRepository<ImageSet, Long> {
    // Allow lookup by name if needed; returns all matches since names are not unique
    List<ImageSet> findBySetName(String setName);
}
