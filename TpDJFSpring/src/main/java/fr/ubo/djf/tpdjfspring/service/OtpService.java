package fr.ubo.djf.tpdjfspring.service;

import fr.ubo.djf.tpdjfspring.entity.Otp;
import fr.ubo.djf.tpdjfspring.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    private final String SMS_SERVER_URL = "http://dosipa.univ-brest.fr";
    private final String API_KEY = "DOSITPDJF"; // Mets ta clé ici si le prof t'en donne une
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAndSendOtp(String phoneNumber) {
        // 1. Règle des 30 secondes (Anti-spam)
        Optional<Otp> lastOtp = otpRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber);
        if (lastOtp.isPresent()) {
            if (lastOtp.get().getExpiryDate().minusMinutes(2).plusSeconds(30).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Veuillez attendre 30 secondes entre deux demandes.");
            }
        }

        // 2. VERIFICATION DU SERVEUR (PING)
        if (!pingServer()) {
            // Si le ping échoue, on ne bloque pas le TP, on passe en simulation direct
            System.err.println("Le serveur SMS ne répond pas au PING.");
        }

        // 3. Génération du code
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // 4. Tentative d'envoi (SEND-SMS)
        boolean sent = trySendSms(phoneNumber, code);

        // 5. Sauvegarde en BDD
        String hashedCode = hashCode(code);
        Otp otp = new Otp(phoneNumber, hashedCode, LocalDateTime.now().plusMinutes(2));
        otpRepository.save(otp);

        if (sent) return "Code envoyé par SMS";
        return "Mode Simulation : Code généré (voir logs)";
    }

    // Appelle http://dosipa.univ-brest.fr/ping
    private boolean pingServer() {
        try {
            restTemplate.getForObject(SMS_SERVER_URL + "/ping", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Appelle http://dosipa.univ-brest.fr/send-sms
    private boolean trySendSms(String phone, String code) {
        try {
            System.out.println("Appel API send-sms vers " + phone + "...");
            String url = SMS_SERVER_URL + "/send-sms";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!API_KEY.isEmpty()) headers.set("x-api-key", API_KEY);

            Map<String, String> body = new HashMap<>();
            body.put("to", phone);
            body.put("message", "Votre code OTP est : " + code);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

            return true; // Succès réel
        } catch (Exception e) {
            // SI ERREUR (401 Unauthorized ou autre), ON SIMULE
            System.err.println("API 'send-sms' a échoué (" + e.getMessage() + ") -> MODE SIMULATION ACTIVÉ");
            System.out.println("========================================");
            System.out.println("⚠️  CODE OTP POUR " + phone + " : " + code + "  ⚠️");
            System.out.println("========================================");
            return false; // Retourne faux mais l'appli continue
        }
    }

    public boolean validateOtp(String phoneNumber, String userCode) {
        return otpRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .map(otp -> {
                    if (otp.getExpiryDate().isBefore(LocalDateTime.now())) return false;
                    return otp.getCode().equals(hashCode(userCode));
                })
                .orElse(false);
    }

    private String hashCode(String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(code.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) { throw new RuntimeException("Erreur Hash"); }
    }
}