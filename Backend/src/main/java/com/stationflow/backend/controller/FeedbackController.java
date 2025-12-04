package com.stationflow.backend.controller;

import com.stationflow.backend.model.Feedback;
import com.stationflow.backend.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*") // Allow frontend access
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> payload, jakarta.servlet.http.HttpServletRequest request) {
        String message = payload.get("message");
        String email = payload.get("email");
        String ipAddress = request.getRemoteAddr();

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mesaj zorunludur."));
        }
        
        if (message.length() < 10) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mesaj en az 10 karakter olmalıdır."));
        }
        
        if (message.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mesaj en fazla 500 karakter olabilir."));
        }

        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!email.matches(emailRegex)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Geçersiz e-posta adresi."));
            }
        }

        // Rate Limiting: Max 5 requests per 24 hours
        java.time.LocalDateTime oneDayAgo = java.time.LocalDateTime.now().minusDays(1);
        long count = feedbackRepository.countByIpAddressAndCreatedAtAfter(ipAddress, oneDayAgo);

        if (count >= 5) {
            return ResponseEntity.status(429).body(Map.of("message", "Çok fazla geri bildirim gönderdiniz. Lütfen yarın tekrar deneyin."));
        }

        Feedback feedback = new Feedback(message, email, ipAddress);
        feedbackRepository.save(feedback);

        return ResponseEntity.ok().body(Map.of("message", "Feedback received"));
    }
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public java.util.List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFeedback(@PathVariable String id) {
        if (!feedbackRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok().body(Map.of("message", "Geri bildirim silindi."));
    }
}
