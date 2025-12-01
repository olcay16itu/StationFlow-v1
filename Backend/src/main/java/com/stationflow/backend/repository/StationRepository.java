package com.stationflow.backend.repository;

import com.stationflow.backend.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, String> {
    List<Station> findByOwnerId(String ownerId);
}
