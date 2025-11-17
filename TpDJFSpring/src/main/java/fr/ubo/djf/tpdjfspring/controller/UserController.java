package fr.ubo.djf.tpdjfspring.controller;

import fr.ubo.djf.tpdjfspring.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fr.ubo.djf.tpdjfspring.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;
    public UserController(UserRepository repo) { this.repo = repo; }

    @GetMapping
    public List<User> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User u) {
        if (repo.existsByEmail(u.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User saved = repo.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User payload) {
        return repo.findById(id).map(existing -> {
            existing.setNom(payload.getNom());
            existing.setEmail(payload.getEmail());
            existing.setRole(payload.getRole());
            existing.setPassword(payload.getPassword());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}