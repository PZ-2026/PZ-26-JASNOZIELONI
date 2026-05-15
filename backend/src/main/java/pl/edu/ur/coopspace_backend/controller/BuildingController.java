package pl.edu.ur.coopspace_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pl.edu.ur.coopspace_backend.dto.BuildingSummaryResponse;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.BuildingRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@CrossOrigin(origins = "*")
/**
 * Tylko-do-odczytu API administracyjnego dla slownika budynkow.
 * Zwraca liste budynkow do raportu zgloszen konserwatorskich.
 */
public class BuildingController {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public BuildingController(BuildingRepository buildingRepository, UserRepository userRepository) {
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Zwraca liste aktywnych budynkow posortowanych po adresie alfabetycznie.
     */
    @GetMapping
    public ResponseEntity<List<BuildingSummaryResponse>> getBuildings(Authentication authentication) {
        requireAdmin(authentication);

        List<BuildingSummaryResponse> buildings = buildingRepository.findAll().stream()
                .filter(building -> building.getDeletedAt() == null)
                .sorted(Comparator.comparing(building -> building.getAddress() == null ? "" : building.getAddress(), String.CASE_INSENSITIVE_ORDER))
                .map(building -> new BuildingSummaryResponse(
                        building.getId(),
                        building.getName(),
                        building.getAddress()
                ))
                .toList();

        return ResponseEntity.ok(buildings);
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
