package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.LoginReply;
import com.soumyajit.jharkhand_project.dto.SignupRequest;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.UserRepository;
import com.soumyajit.jharkhand_project.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private final Map<String, PasswordResetEntry> passwordResetCache = new ConcurrentHashMap<>();

    @Value("${app.otp.expiration}")
    private Long otpExpirationMs;

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.password-reset.expiration:300000}") // 5 minutes default
    private Long passwordResetExpirationMs;

    // ========== EXISTING OTP & SIGNUP METHODS (UNCHANGED) ==========

    public void sendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already registered with email");
        }

        String otp = generateOtp();
        otpCache.put(email, new OtpEntry(otp, System.currentTimeMillis() + otpExpirationMs));
        emailService.sendOtpEmail(email, otp);
    }

    public void verifyOtp(String email, String otp) {
        OtpEntry entry = otpCache.get(email);
        if (entry == null || !entry.getOtp().equals(otp) || System.currentTimeMillis() > entry.getExpiry()) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        entry.setVerified(true);
    }

    public void signup(SignupRequest request) {
        OtpEntry entry = otpCache.get(request.getEmail());
        if (entry == null || !entry.isVerified()) {
            throw new RuntimeException("OTP not verified");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(true)
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        otpCache.remove(request.getEmail());

        // Send welcome email after successful signup
        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        log.info("User successfully registered: {}", user.getEmail());
    }

    public LoginReply login(String email, String password, String device, String ip, String loginTime) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("USER");

        emailService.sendLoginAlertEmail(email, device, ip, loginTime);

        String token = jwtUtils.generateJwtToken(userDetails);

        return new LoginReply(token, role);
    }


//public String login(String email, String password) {
//    Authentication authentication = authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(email, password)
//    );
//
//    Object principal = authentication.getPrincipal();
//    if (!(principal instanceof UserDetails)) {
//        throw new RuntimeException("Authentication principal is not of type UserDetails");
//    }
//
//    UserDetails userDetails = (UserDetails) principal;
//
//    return jwtUtils.generateJwtToken(userDetails);
//}


    // ========== NEW PASSWORD RESET METHODS (6-DIGIT CODES) ==========

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        log.info("Password reset request received for email: {}", email);

        if (user != null) {
            // Generate 6-digit numeric code
            String resetCode = generatePasswordResetCode();

            // Store in cache with expiration
            passwordResetCache.put(email, new PasswordResetEntry(
                    resetCode,
                    user.getId(),
                    System.currentTimeMillis() + passwordResetExpirationMs
            ));

            // Send email with short code
            emailService.sendPasswordResetEmail(email, resetCode);
            log.info("Password reset code generated for user ID: {}", user.getId());
        }

        // Always show success message (prevents account enumeration)
    }

    public void resetPassword(String email, String resetCode, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate the reset code
        validatePasswordResetCode(email, resetCode, user.getId());

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove from cache after successful reset
        passwordResetCache.remove(email);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(
                email,
                user.getFirstName(),
                user.getLastName()
        );

        log.info("Password successfully reset for user: {} (ID: {})", email, user.getId());
    }

    private String generatePasswordResetCode() {
        // Generate cryptographically secure 6-digit code
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Ensures 6 digits (100000-999999)
        return String.valueOf(code);
    }

    private void validatePasswordResetCode(String email, String resetCode, Long userId) {
        PasswordResetEntry entry = passwordResetCache.get(email);

        if (entry == null) {
            throw new RuntimeException("No active password reset request found");
        }

        // Check if code matches
        if (!entry.getCode().equals(resetCode)) {
            throw new RuntimeException("Invalid reset code");
        }

        // Check if expired
        if (System.currentTimeMillis() > entry.getExpiry()) {
            passwordResetCache.remove(email);
            throw new RuntimeException("Reset code expired");
        }

        // Check if user ID matches (extra security)
        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Invalid reset request");
        }
    }

    // ========== PRIVATE HELPER METHODS (UNCHANGED) ==========

    private String generateOtp() {
        Random random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private static class OtpEntry {
        private final String otp;
        private final long expiry;
        private boolean verified;

        public OtpEntry(String otp, long expiry) {
            this.otp = otp;
            this.expiry = expiry;
            this.verified = false;
        }

        public String getOtp() { return otp; }
        public long getExpiry() { return expiry; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean v) { this.verified = v; }
    }

    // Password Reset Cache Entry
    private static class PasswordResetEntry {
        private final String code;
        private final Long userId;
        private final long expiry;

        public PasswordResetEntry(String code, Long userId, long expiry) {
            this.code = code;
            this.userId = userId;
            this.expiry = expiry;
        }

        public String getCode() { return code; }
        public Long getUserId() { return userId; }
        public long getExpiry() { return expiry; }
    }
}
