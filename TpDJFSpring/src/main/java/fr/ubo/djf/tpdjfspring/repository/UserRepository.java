package fr.ubo.djf.tpdjfspring.repository;

import fr.ubo.djf.tpdjfspring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}