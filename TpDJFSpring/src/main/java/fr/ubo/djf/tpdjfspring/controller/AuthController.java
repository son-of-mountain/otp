package fr.ubo.djf.tpdjfspring.controller;

import fr.ubo.djf.tpdjfspring.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    // Endpoint 1 : Le Front demande d'envoyer le SMS
    @PostMapping("/send-sms")
    public ResponseEntity<?> sendSms(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) return ResponseEntity.badRequest().body("Numéro requis");

        try {
            String msg = otpService.generateAndSendOtp(phone);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint 2 : Le Front demande de vérifier le code
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");

        if (otpService.validateOtp(phone, code)) {
            return ResponseEntity.ok("Succès");
        } else {
            return ResponseEntity.status(401).body("Code invalide ou expiré");
        }
    }
}