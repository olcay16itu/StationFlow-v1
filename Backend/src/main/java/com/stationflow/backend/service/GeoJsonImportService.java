package com.stationflow.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stationflow.backend.model.Location;
import com.stationflow.backend.model.Station;
import com.stationflow.backend.model.StationStatus;
import com.stationflow.backend.model.TransportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeoJsonImportService {

    @Autowired
    private StationService stationService;

    public List<Station> importStations(java.io.InputStream inputStream, TransportType defaultType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);
        JsonNode features = root.path("features");

        List<Station> stations = new ArrayList<>();

        if (features.isArray()) {
            for (JsonNode feature : features) {
                Station station = parseFeature(feature, defaultType);
                if (station != null) {
                    stations.add(stationService.createStation(station));
                }
            }
        }
        return stations;
    }

    public List<Station> importStations(MultipartFile file, TransportType defaultType) throws IOException {
        return importStations(file.getInputStream(), defaultType);
    }

    private Station parseFeature(JsonNode feature, TransportType defaultType) {
        try {
            JsonNode geometry = feature.path("geometry");
            JsonNode properties = feature.path("properties");

            String type = geometry.path("type").asText();
            if (!"Point".equalsIgnoreCase(type)) {
                return null; // Only support Point for now
            }

            JsonNode coordinates = geometry.path("coordinates");
            double lng = coordinates.get(0).asDouble();
            double lat = coordinates.get(1).asDouble();

            Station station = new Station();
            station.setLocation(new Location(lat, lng));
            
            // Try to find name
            String name = "Unknown Station";
            
            // Metro specific logic
            if (properties.has("ISTASYON")) {
                if (properties.has("PROJE_ASAMA") && "İnşaat Aşamasında".equals(properties.get("PROJE_ASAMA").asText())) {
                    return null; // Skip under construction
                }
                
                String stationName = properties.get("ISTASYON").asText();
                String projectName = properties.has("PROJE_ADI") ? properties.get("PROJE_ADI").asText() : "";
                name = stationName + (projectName.isEmpty() ? "" : " / " + projectName);
            }
            else if (properties.has("name")) name = properties.get("name").asText();
            else if (properties.has("DURAK_ADI")) name = properties.get("DURAK_ADI").asText(); // Common Turkish field
            else if (properties.has("ADI")) name = properties.get("ADI").asText();
            
            if ("Unknown Station".equals(name)) {
                return null; // Skip stations with no name
            }

            station.setName(name);

            // Determine type
            if (defaultType != null) {
                station.setType(defaultType);
            } else {
                 // Try to guess or default to BUS
                 station.setType(TransportType.BUS);
            }

            station.setCapacity(100); // Default capacity updated to 100
            // Random availability between 0 and 100
            station.setAvailable((int) (Math.random() * 101));
            station.setStatus(StationStatus.ACTIVE);
            station.setCustom(false);

            return station;
        } catch (Exception e) {
            // Log error or skip
            return null;
        }
    }
}
