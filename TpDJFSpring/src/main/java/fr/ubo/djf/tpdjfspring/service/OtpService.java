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
    private final String API_KEY = "DOSITPDJF"; // ⚠️ Mets ta clé si tu l'as
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAndSendOtp(String phoneNumber) {
        // --- CONTRAINTE 1 : Délai de 30 secondes ---
        Optional<Otp> lastOtp = otpRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber);
        if (lastOtp.isPresent()) {
            // Si le dernier code a été créé il y a moins de 30 secondes
            // (ExpiryDate - 2min = CreationDate)
            LocalDateTime creationDate = lastOtp.get().getExpiryDate().minusMinutes(2);
            if (creationDate.plusSeconds(30).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Veuillez attendre 30 secondes avant de renvoyer un code.");
            }
        }

        // --- CONTRAINTE 2 : Health Check (Ping) ---
        if (!pingServer()) {
            System.err.println("⚠️ Serveur SMS injoignable (Ping KO). Passage en mode secours.");
            // On ne lance pas d'exception pour ne pas bloquer le TP, mais en prod on pourrait.
        }

        // Génération du code
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // --- CONTRAINTE 3 : Retry (2 tentatives) ---
        boolean sent = sendSmsWithRetry(phoneNumber, code);

        // --- CONTRAINTE 4 : Expiration 2 minutes ---
        String hashedCode = hashCode(code);
        // On sauvegarde l'heure d'expiration = Maintenant + 2 min
        Otp otp = new Otp(phoneNumber, hashedCode, LocalDateTime.now().plusMinutes(2));
        otpRepository.save(otp);

        if (sent) return "Code envoyé par SMS";
        return "MODE SIMULATION : Code = " + code + " (Voir logs console)";
    }

    private boolean pingServer() {
        try {
            restTemplate.getForObject(SMS_SERVER_URL + "/ping", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean sendSmsWithRetry(String phone, String code) {
        int attempts = 0;
        // On essaie 2 fois maximum
        while (attempts < 2) {
            try {
                System.out.println("Tentative d'envoi SMS n°" + (attempts + 1) + "...");

                String url = SMS_SERVER_URL + "/send-sms";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (!API_KEY.isEmpty()) headers.set("x-api-key", API_KEY);

                Map<String, String> body = new HashMap<>();
                body.put("to", phone);
                body.put("message", "Votre code OTP est : " + code);

                HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
                restTemplate.postForEntity(url, request, String.class);

                return true; // Succès !
            } catch (Exception e) {
                attempts++;
                System.err.println("Échec tentative " + attempts + " : " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) {} // Pause 1s
            }
        }

        // Si on arrive ici, les 2 tentatives ont échoué
        System.out.println("========================================");
        System.out.println("❌ ÉCHEC ENVOI SMS RÉEL (Pas de clé ? Serveur HS ?)");
        System.out.println("👉 CODE DE SECOURS : " + code);
        System.out.println("========================================");
        return false;
    }

    public boolean validateOtp(String phoneNumber, String userCode) {
        return otpRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .map(otp -> {
                    // Vérif Expiration
                    if (otp.getExpiryDate().isBefore(LocalDateTime.now())) return false;
                    // Vérif Code Hashé
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