package com.stationflow.backend.controller;

import com.stationflow.backend.model.Station;
import com.stationflow.backend.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @PostMapping
    public Station createStation(@jakarta.validation.Valid @RequestBody Station station) {
        return stationService.createStation(station);
    }

    // --- Station Update Requests ---

    @Autowired
    private com.stationflow.backend.repository.StationUpdateRequestRepository requestRepository;
    @Autowired
    private com.stationflow.backend.repository.StationRepository stationRepository; // Added this import for stationRepository
    
    @PostMapping("/{id}/request-update")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> requestUpdate(@PathVariable String id, @RequestBody java.util.Map<String, Integer> payload) {
        Station station = stationRepository.findById(id).orElse(null);
        if (station == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Get current user
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.stationflow.backend.security.services.UserDetailsImpl userDetails = (com.stationflow.backend.security.services.UserDetailsImpl) auth.getPrincipal();

        System.out.println("DEBUG: requestUpdate - UserID: " + userDetails.getId());

        int requestedAvailable = payload.get("available");

        // 1. Capacity Check
        if (requestedAvailable > station.getCapacity()) {
            return ResponseEntity.badRequest().body(new com.stationflow.backend.payload.response.MessageResponse("Hata: Girilen sayı istasyon kapasitesini (" + station.getCapacity() + ") aşamaz!"));
        }
        
        // 2. Daily Limit Check (for non-admins)
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
            long dailyCount = requestRepository.countByUserIdAndCreatedAtAfter(userDetails.getId(), startOfDay);
            if (dailyCount >= 5) {
                return ResponseEntity.badRequest().body(new com.stationflow.backend.payload.response.MessageResponse("Hata: Günlük 5 güncelleme isteği hakkınız doldu."));
            }
        }
        
        com.stationflow.backend.model.StationUpdateRequest request = new com.stationflow.backend.model.StationUpdateRequest(
            id, 
            userDetails.getId(), 
            requestedAvailable
        );
        
        requestRepository.save(request);
        System.out.println("DEBUG: requestUpdate - Saved request with ID: " + request.getId());
        
        return ResponseEntity.ok(new com.stationflow.backend.payload.response.MessageResponse("Güncelleme isteği gönderildi, admin onayı bekleniyor."));
    }

    @Autowired
    private com.stationflow.backend.repository.UserRepository userRepository; // Added UserRepository

    @GetMapping("/requests")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<com.stationflow.backend.payload.response.StationUpdateRequestDto> getPendingRequests() {
        List<com.stationflow.backend.model.StationUpdateRequest> requests = requestRepository.findByStatus(com.stationflow.backend.model.StationUpdateRequest.RequestStatus.PENDING);
        
        return requests.stream().map(request -> {
            String stationName = stationRepository.findById(request.getStationId())
                    .map(Station::getName)
                    .orElse("Unknown Station");
            
            String username = userRepository.findById(request.getUserId())
                    .map(com.stationflow.backend.model.User::getUsername)
                    .orElse("Unknown User");
            
            return new com.stationflow.backend.payload.response.StationUpdateRequestDto(request, stationName, username);
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/my-requests")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<com.stationflow.backend.payload.response.StationUpdateRequestDto> getMyRequests() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.stationflow.backend.security.services.UserDetailsImpl userDetails = (com.stationflow.backend.security.services.UserDetailsImpl) auth.getPrincipal();
        
        System.out.println("DEBUG: getMyRequests - UserID: " + userDetails.getId());

        List<com.stationflow.backend.model.StationUpdateRequest> requests = requestRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getId());
        
        System.out.println("DEBUG: getMyRequests - Found " + requests.size() + " requests");

        return requests.stream().map(request -> {
            String stationName = stationRepository.findById(request.getStationId())
                    .map(Station::getName)
                    .orElse("Unknown Station");
            
            String username = userDetails.getUsername();
            
            return new com.stationflow.backend.payload.response.StationUpdateRequestDto(request, stationName, username);
        }).collect(java.util.stream.Collectors.toList());
    }

    @Autowired
    private com.stationflow.backend.service.NotificationService notificationService;

    @PostMapping("/requests/{requestId}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") // Added full path for PreAuthorize
    public ResponseEntity<?> approveRequest(@PathVariable String requestId) {
        return requestRepository.findById(requestId).map(request -> {
            if (request.getStatus() != com.stationflow.backend.model.StationUpdateRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(new com.stationflow.backend.payload.response.MessageResponse("İstek zaten işlenmiş."));
            }

            Station station = stationRepository.findById(request.getStationId()).orElse(null);
            if (station == null) {
                return ResponseEntity.notFound().build();
            }

            // Update station
            station.setAvailable(request.getRequestedAvailable());
            
            if (station.getType() == com.stationflow.backend.model.TransportType.BUS || station.getType() == com.stationflow.backend.model.TransportType.METRO) {
                 if (station.getAvailable() == 0) station.setStatus(com.stationflow.backend.model.StationStatus.FULL);
                 else station.setStatus(com.stationflow.backend.model.StationStatus.ACTIVE);
            } else {
                 if (station.getAvailable() == 0) station.setStatus(com.stationflow.backend.model.StationStatus.EMPTY);
                 // Full capacity is GOOD for renting, so keep it ACTIVE
                 else station.setStatus(com.stationflow.backend.model.StationStatus.ACTIVE);
            }
            
            stationRepository.save(station);

            // Update request status
            request.setStatus(com.stationflow.backend.model.StationUpdateRequest.RequestStatus.APPROVED);
            requestRepository.save(request);

            // Send real-time update
            notificationService.sendStationUpdate(station);

            return ResponseEntity.ok(new com.stationflow.backend.payload.response.MessageResponse("İstek onaylandı ve istasyon güncellendi."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/requests/{requestId}/reject")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") // Added full path for PreAuthorize
    public ResponseEntity<?> rejectRequest(@PathVariable String requestId) {
        return requestRepository.findById(requestId).map(request -> {
             if (request.getStatus() != com.stationflow.backend.model.StationUpdateRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(new com.stationflow.backend.payload.response.MessageResponse("İstek zaten işlenmiş."));
            }
            
            request.setStatus(com.stationflow.backend.model.StationUpdateRequest.RequestStatus.REJECTED);
            requestRepository.save(request);
            return ResponseEntity.ok(new com.stationflow.backend.payload.response.MessageResponse("İstek reddedildi."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable String id) {
        stationService.deleteStation(id);
        return ResponseEntity.ok().build();
    }
}
