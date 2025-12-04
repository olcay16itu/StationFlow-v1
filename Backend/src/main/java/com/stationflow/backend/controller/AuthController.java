package com.stationflow.backend.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stationflow.backend.model.Role;
import com.stationflow.backend.model.User;
import com.stationflow.backend.payload.request.LoginRequest;
import com.stationflow.backend.payload.request.SignupRequest;
import com.stationflow.backend.payload.response.JwtResponse;
import com.stationflow.backend.payload.response.MessageResponse;
import com.stationflow.backend.repository.UserRepository;
import com.stationflow.backend.security.jwt.JwtUtils;
import com.stationflow.backend.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  @Autowired
  com.stationflow.backend.security.services.RateLimitService rateLimitService;

  @Autowired
  com.stationflow.backend.security.services.RecaptchaService recaptchaService;

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, jakarta.servlet.http.HttpServletRequest request) {
    String ipAddress = request.getRemoteAddr();
    if (!rateLimitService.isAllowed(ipAddress)) {
        return ResponseEntity.status(429).body(new MessageResponse("Hata: Çok fazla kayıt denemesi. Lütfen daha sonra tekrar deneyiniz."));
    }

    // Verify reCAPTCHA
    if (!recaptchaService.verify(signUpRequest.getRecaptchaToken())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Hata: Robot doğrulaması başarısız!"));
    }

    System.out.println("Register request received for: " + signUpRequest.getUsername());

    if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Hata: Bu e-posta adresi zaten kullanımda!"));
    }

    if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
        return ResponseEntity
            .badRequest()
            .body(new MessageResponse("Hata: Bu kullanıcı adı zaten kullanımda!"));
    }

    // Create new user's account
    User user = new User();
    user.setUsername(signUpRequest.getUsername());
    user.setEmail(signUpRequest.getEmail());
    user.setPassword(encoder.encode(signUpRequest.getPassword()));
    
    // Default role
    user.setRole(Role.USER);
    
    // Handle specific roles if needed (simplified for now)
    if (signUpRequest.getRole() != null && signUpRequest.getRole().contains("admin")) {
        user.setRole(Role.ADMIN);
    }

    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("Kullanıcı başarıyla kaydedildi!"));
  }

  @PostMapping("/change-password")
  @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<?> changePassword(@Valid @RequestBody com.stationflow.backend.payload.request.ChangePasswordRequest changePasswordRequest) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      User user = userRepository.findById(userDetails.getId())
              .orElseThrow(() -> new RuntimeException("Error: User is not found."));

      if (!encoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
          return ResponseEntity.badRequest().body(new MessageResponse("Hata: Mevcut şifre yanlış!"));
      }

      user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
      userRepository.save(user);

      return ResponseEntity.ok(new MessageResponse("Şifre başarıyla değiştirildi!"));
  }
}
