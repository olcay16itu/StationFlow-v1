package com.stationflow.backend.controller;

import com.stationflow.backend.model.Station;
import com.stationflow.backend.model.TransportType;
import com.stationflow.backend.service.GeoJsonImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/stations/import")
public class GeoJsonImportController {

    @Autowired
    private GeoJsonImportService importService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importGeoJson(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) TransportType type) {
        try {
            List<Station> stations = importService.importStations(file, type);
            return ResponseEntity.ok(new com.stationflow.backend.payload.response.MessageResponse(stations.size() + " istasyon başarıyla eklendi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.stationflow.backend.payload.response.MessageResponse("Hata: " + e.getMessage()));
        }
    }
}
