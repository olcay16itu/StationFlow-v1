package com.stationflow.backend.config;

import com.stationflow.backend.model.Location;
import com.stationflow.backend.model.Station;
import com.stationflow.backend.model.StationStatus;
import com.stationflow.backend.model.TransportType;
import com.stationflow.backend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StationRepository stationRepository;

    private static final List<SafeZone> SAFE_ZONES = Arrays.asList(
            new SafeZone("Beşiktaş Çarşı", 41.0428, 29.0075),
            new SafeZone("Kadıköy Boğa", 40.9905, 29.0292),
            new SafeZone("Sultanahmet", 41.0054, 28.9768),
            new SafeZone("Şişli Camii", 41.0632, 28.9931),
            new SafeZone("Levent Metro", 41.0766, 29.0135),
            new SafeZone("Üsküdar Meydan", 41.0268, 29.0160),
            new SafeZone("Fatih Camii", 41.0195, 28.9497),
            new SafeZone("Bakırköy Özgürlük", 40.9801, 28.8724),
            new SafeZone("Maslak İTÜ", 41.1070, 29.0250),
            new SafeZone("Moda Sahil", 40.9830, 29.0270)
    );

    private static final String[] BUS_NAMES = {"Merkez Durağı", "Meydan Hattı", "Okul Yolu", "Hastane Önü"};
    private static final String[] METRO_NAMES = {"M2 Çıkışı", "M4 Girişi", "M1 Bağlantı", "M7 İstasyonu"};
    private static final String[] BIKE_NAMES = {"Park Noktası", "Kampüs İstasyonu", "AVM Yanı", "Sahil Yolu"};
    private static final String[] SCOOTER_NAMES = {"Köşe Başı", "Durak Yanı", "Kütüphane Önü", "Spor Salonu"};

    @Override
    public void run(String... args) throws Exception {
        if (stationRepository.count() == 0) {
            System.out.println("Veritabanı boş, örnek istasyonlar ekleniyor...");
            seedStations();
            System.out.println("Örnek istasyonlar başarıyla eklendi.");
        } else {
            System.out.println("Veritabanı dolu, durum kontrolü yapılıyor...");
            fixStationStatuses();
        }
    }

    private void fixStationStatuses() {
        List<Station> stations = stationRepository.findAll();
        boolean changed = false;
        
        for (Station station : stations) {
            StationStatus oldStatus = station.getStatus();
            StationStatus newStatus = oldStatus;
            
            if (station.getType() == TransportType.BUS || station.getType() == TransportType.METRO) {
                if (station.getAvailable() == 0) {
                    newStatus = StationStatus.FULL;
                } else {
                    newStatus = StationStatus.ACTIVE;
                }
            } else {
                if (station.getAvailable() == 0) {
                    newStatus = StationStatus.EMPTY;
                } else {
                    // Full or partial capacity is ACTIVE (Good for renting)
                    // Ensure we overwrite any existing 'FULL' status
                    newStatus = StationStatus.ACTIVE;
                }
            }
            
            if (oldStatus != newStatus) {
                station.setStatus(newStatus);
                stationRepository.save(station);
                changed = true;
            }
        }
        
        if (changed) {
            System.out.println("İstasyon durumları güncellendi.");
        } else {
            System.out.println("İstasyon durumları güncel.");
        }
    }

    private void seedStations() {
        Random random = new Random();
        TransportType[] types = TransportType.values();

        for (int i = 0; i < 10; i++) {
            SafeZone zone = SAFE_ZONES.get(i % SAFE_ZONES.size());
            TransportType type = types[random.nextInt(types.length)];
            
            String baseName = getRandomName(type, random);
            
            // Add slight jitter
            double jitterLat = (random.nextDouble() - 0.5) * 0.002;
            double jitterLng = (random.nextDouble() - 0.5) * 0.002;

            Location location = new Location(zone.lat + jitterLat, zone.lng + jitterLng);

            int capacity = (type == TransportType.BUS || type == TransportType.METRO) ? 100 : (random.nextInt(11) + 5);
            int available = random.nextInt(capacity + 1);

            StationStatus status = StationStatus.ACTIVE;
            
            if (type == TransportType.BUS || type == TransportType.METRO) {
                // For public transport: 0 available means FULL (no seats)
                if (available == 0) status = StationStatus.FULL;
            } else {
                // For shared vehicles: 0 available means EMPTY (no vehicles)
                if (available == 0) status = StationStatus.EMPTY;
                // Full capacity is actually GOOD for renting, so keep it ACTIVE
                else status = StationStatus.ACTIVE;
            }

            Station station = new Station();
            station.setName(zone.name + " - " + baseName);
            station.setType(type);
            station.setLocation(location);
            station.setCapacity(capacity);
            station.setAvailable(available);
            station.setStatus(status);
            station.setLastUpdate(LocalDateTime.now());
            station.setCustom(false);
            station.setOwnerId("system");

            stationRepository.save(station);
        }
    }

    private String getRandomName(TransportType type, Random random) {
        String[] names;
        switch (type) {
            case BUS: names = BUS_NAMES; break;
            case METRO: names = METRO_NAMES; break;
            case BIKE: names = BIKE_NAMES; break;
            case SCOOTER: names = SCOOTER_NAMES; break;
            default: names = BUS_NAMES;
        }
        return names[random.nextInt(names.length)];
    }

    private static class SafeZone {
        String name;
        double lat;
        double lng;

        public SafeZone(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }
}
