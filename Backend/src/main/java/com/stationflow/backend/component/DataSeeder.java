package com.stationflow.backend.component;

import com.stationflow.backend.model.TransportType;
import com.stationflow.backend.service.GeoJsonImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private GeoJsonImportService importService;

    @Autowired
    private com.stationflow.backend.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedStations("Minibüs Durakları Verisi.geojson", TransportType.MINIBUS);
        seedStations("Taksi Dolmuş Durakları Verisi.geojson", TransportType.DOLMUS);
        seedStations("Raylı Sistem İstasyon Noktaları Verisi.geojson", TransportType.METRO);
        seedAdminUser();
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("adminmemo").isEmpty()) {
            com.stationflow.backend.model.User admin = new com.stationflow.backend.model.User();
            admin.setUsername("adminmemo");
            admin.setEmail("admin@stationflow.com");
            admin.setPassword(passwordEncoder.encode("can123"));
            admin.setRole(com.stationflow.backend.model.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user 'adminmemo' created successfully.");
        } else {
            System.out.println("Admin user 'adminmemo' already exists.");
        }
    }

    @Autowired
    private com.stationflow.backend.service.StationService stationService; // Inject StationService

    private void seedStations(String fileName, TransportType type) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName + ". Skipping import.");
            return;
        }

        // Clear existing non-custom stations of this type to prevent duplicates
        try {
            stationService.deleteStationsByType(type);
            System.out.println("Cleared existing " + type + " stations.");
        } catch (Exception e) {
            System.err.println("Failed to clear existing stations: " + e.getMessage());
        }

        try (FileInputStream input = new FileInputStream(file)) {
            var stations = importService.importStations(input, type);
            if (!stations.isEmpty()) {
                System.out.println("Imported " + stations.size() + " stations from " + fileName + " as " + type);
            }
            
        } catch (IOException e) {
            System.err.println("Error importing " + fileName + ": " + e.getMessage());
        }
    }
}
