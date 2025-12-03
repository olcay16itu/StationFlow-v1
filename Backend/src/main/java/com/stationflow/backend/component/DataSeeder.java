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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Auto-fix for Enum constraint issues to avoid manual table drop
        try {
            jdbcTemplate.execute("ALTER TABLE stations DROP CONSTRAINT IF EXISTS stations_type_check");
            jdbcTemplate.execute("ALTER TABLE stations DROP CONSTRAINT IF EXISTS stations_status_check");
            System.out.println("Database constraints updated automatically.");
        } catch (Exception e) {
            System.out.println("Could not update constraints (might not exist): " + e.getMessage());
        }

        seedStations("minibus.geojson", TransportType.MINIBUS);
        seedStations("dolmus.geojson", TransportType.DOLMUS);
        seedStations("metro.geojson", TransportType.METRO);
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
            
        } catch (Exception e) {
            System.err.println("Error importing " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
