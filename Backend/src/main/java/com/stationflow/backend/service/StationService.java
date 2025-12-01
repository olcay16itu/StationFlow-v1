package com.stationflow.backend.service;

import com.stationflow.backend.model.Station;
import com.stationflow.backend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public Optional<Station> getStationById(String id) {
        return stationRepository.findById(id);
    }

    public void deleteStation(String id) {
        stationRepository.deleteById(id);
    }
}
