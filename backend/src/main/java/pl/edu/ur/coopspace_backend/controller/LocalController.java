package pl.edu.ur.coopspace_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.coopspace_backend.dto.LocalSummaryResponse;
import pl.edu.ur.coopspace_backend.entity.Local;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/locals")
@CrossOrigin(origins = "*")
public class LocalController {

    private final LocalRepository localRepository;
    private final UserRepository userRepository;

    public LocalController(LocalRepository localRepository, UserRepository userRepository) {
        this.localRepository = localRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<LocalSummaryResponse>> getLocals(Authentication authentication) {
        requireAdmin(authentication);

        List<LocalSummaryResponse> locals = localRepository.findAll().stream()
                .filter(local -> local.getDeletedAt() == null)
                .sorted(Comparator.comparing(Local::getBuildingId)
                        .thenComparing(Local::getNumber, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(local -> local.getStaircase() == null ? "" : local.getStaircase(), String.CASE_INSENSITIVE_ORDER))
                .map(local -> new LocalSummaryResponse(
                        local.getId(),
                        local.getBuildingId(),
                        local.getNumber(),
                        local.getStaircase()
                ))
                .toList();

        return ResponseEntity.ok(locals);
    }

    private User requireAdmin(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator ma dostep do tego endpointu");
        }

        return currentUser;
    }
}