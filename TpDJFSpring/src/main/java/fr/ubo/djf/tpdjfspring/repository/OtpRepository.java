package fr.ubo.djf.tpdjfspring.repository;

import fr.ubo.djf.tpdjfspring.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByPhoneNumberOrderByIdDesc(String phoneNumber);
}