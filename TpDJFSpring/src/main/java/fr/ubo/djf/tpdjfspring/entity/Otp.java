package fr.ubo.djf.tpdjfspring.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber; // L'identifiant de l'utilisateur

    private String code; // Sera stocké hashé

    private LocalDateTime expiryDate; // Pour vérifier si le code est encore valide

    // Constructeurs, Getters et Setters
    public Otp() {}

    public Otp(String phoneNumber, String code, LocalDateTime expiryDate) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.expiryDate = expiryDate;
    }

    // Ajoute les getters/setters ici...
    public String getCode() { return code; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
}