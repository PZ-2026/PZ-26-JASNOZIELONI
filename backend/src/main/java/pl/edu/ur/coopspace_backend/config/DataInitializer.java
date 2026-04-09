package pl.edu.ur.coopspace_backend.config;

import pl.edu.ur.coopspace_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {
    
    @Bean
    @Transactional
    public CommandLineRunner initializeData(UserRepository userRepository, 
                                           JdbcTemplate jdbcTemplate,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            // Sprawdzamy czy dane już istnieją
            if (userRepository.count() > 0) {
                return;
            }
            
            // BUILDINGS
            jdbcTemplate.update(
                "INSERT INTO building (name, address) VALUES (?, ?)",
                "Tulipan", "ul. Kwiatowa 1"
            );
            jdbcTemplate.update(
                "INSERT INTO building (name, address) VALUES (?, ?)",
                "Mlecz", "ul. Słoneczna 5"
            );
            
            // LOCALS
            jdbcTemplate.update(
                "INSERT INTO local (building_id, number, staircase, number_of_residents) VALUES (?, ?, ?, ?)",
                1, "1", "A", 2
            );
            jdbcTemplate.update(
                "INSERT INTO local (building_id, number, staircase, number_of_residents) VALUES (?, ?, ?, ?)",
                1, "2", "A", 3
            );
            jdbcTemplate.update(
                "INSERT INTO local (building_id, number, staircase, number_of_residents) VALUES (?, ?, ?, ?)",
                1, "3", "B", 1
            );
            jdbcTemplate.update(
                "INSERT INTO local (building_id, number, staircase, number_of_residents) VALUES (?, ?, ?, ?)",
                2, "1", "A", 4
            );
            jdbcTemplate.update(
                "INSERT INTO local (building_id, number, staircase, number_of_residents) VALUES (?, ?, ?, ?)",
                2, "2", "B", 2
            );
            
            // ISSUE CATEGORIES
            jdbcTemplate.update("INSERT INTO issue_category (name) VALUES (?)", "Hydraulika");
            jdbcTemplate.update("INSERT INTO issue_category (name) VALUES (?)", "Elektryka");
            jdbcTemplate.update("INSERT INTO issue_category (name) VALUES (?)", "Porządek");
            jdbcTemplate.update("INSERT INTO issue_category (name) VALUES (?)", "Inne");
            
            // CHARGE ITEM TYPES
            jdbcTemplate.update("INSERT INTO charge_item_type (name) VALUES (?)", "Woda");
            jdbcTemplate.update("INSERT INTO charge_item_type (name) VALUES (?)", "Prąd");
            jdbcTemplate.update("INSERT INTO charge_item_type (name) VALUES (?)", "Czynsz");
            jdbcTemplate.update("INSERT INTO charge_item_type (name) VALUES (?)", "Gaz");
            
            // Haszujemy hasła
            String adminPasswordHash = passwordEncoder.encode("admin123");
            String maintainerPasswordHash = passwordEncoder.encode("maintainer123");
            
            // USERS z haszowanymi hasłami
            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "admin@test.com", adminPasswordHash, "Jan", "Admin", "123456789", "ADMIN", null, true
            );
            
            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "maintainer@test.com", maintainerPasswordHash, "Anna", "Serwis", "987654321", "MAINTAINER", null, true
            );
            
            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user1@test.com", passwordEncoder.encode("piotrek123"), "Piotr", "Kowalski", "111222333", "RESIDENT", 1, true
            );
            
            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user2@test.com", passwordEncoder.encode("mariannnaa121"), "Maria", "Nowak", "444555666", "RESIDENT", 2, true
            );
            
            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user3@test.com", passwordEncoder.encode("tomeczek123"), "Tomasz", "Wiśniewski", "777888999", "RESIDENT", 3, true
            );

            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user4@test.com", passwordEncoder.encode("karolina123"), "Karolina", "Mazur", "700100200", "RESIDENT", 4, true
            );

            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user5@test.com", passwordEncoder.encode("michal123"), "Michał", "Zieliński", "700100201", "RESIDENT", 5, true
            );

            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "user6@test.com", passwordEncoder.encode("aleksandra123"), "Aleksandra", "Witkowska", "700100202", "RESIDENT", 1, true
            );

            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "maintainer2@test.com", maintainerPasswordHash, "Paweł", "Serwis", "700100203", "MAINTAINER", null, true
            );

            jdbcTemplate.update(
                "INSERT INTO \"user\" (email, password_hash, first_name, last_name, phone_number, role, local_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::user_role, ?, ?, NOW(), NOW())",
                "admin2@test.com", adminPasswordHash, "Ewa", "Admin", "700100204", "ADMIN", null, true
            );
            
            // ISSUES
            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Cieknący kran", "Kran w kuchni przecieka", 1, 1, 3, 2, "IN_PROGRESS"
            );
            
            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Brak światła na klatce", "Żarówka przepalona", 2, 2, 4, 2, "OPEN"
            );
            
            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Śmieci na korytarzu", "Ktoś zostawił worki", 3, 3, 5, null, "OPEN"
            );

            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Awaria domofonu", "Domofon nie działa od rana", 2, 4, 6, 9, "IN_PROGRESS"
            );

            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Nieszczelny kaloryfer", "Kaloryfer w salonie przecieka", 1, 5, 7, 2, "OPEN"
            );

            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Zepsuta lampa na parkingu", "Brak oswietlenia przy wejsciu", 2, 1, 8, 9, "OPEN"
            );

            jdbcTemplate.update(
                "INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::issue_status, NOW(), NOW())",
                "Sprzatanie klatki", "Prosba o dodatkowe sprzatanie w weekend", 3, 2, 10, null, "OPEN"
            );
            
            // ISSUE ASSIGNMENTS
            jdbcTemplate.update(
                "INSERT INTO issue_assignment (issue_id, user_id, assigned_by, assigned_at) VALUES (?, ?, ?, NOW())",
                1, 2, 1
            );
            jdbcTemplate.update(
                "INSERT INTO issue_assignment (issue_id, user_id, assigned_by, assigned_at) VALUES (?, ?, ?, NOW())",
                2, 2, 1
            );
            jdbcTemplate.update(
                "INSERT INTO issue_assignment (issue_id, user_id, assigned_by, assigned_at) VALUES (?, ?, ?, NOW())",
                4, 9, 1
            );
            jdbcTemplate.update(
                "INSERT INTO issue_assignment (issue_id, user_id, assigned_by, assigned_at) VALUES (?, ?, ?, NOW())",
                5, 2, 10
            );
            jdbcTemplate.update(
                "INSERT INTO issue_assignment (issue_id, user_id, assigned_by, assigned_at) VALUES (?, ?, ?, NOW())",
                6, 9, 1
            );
            
            // ISSUE STATUS HISTORY
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                1, "OPEN", 3
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                1, "IN_PROGRESS", 2
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                2, "OPEN", 4
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                4, "OPEN", 6
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                4, "IN_PROGRESS", 9
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                5, "OPEN", 7
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                6, "OPEN", 8
            );
            jdbcTemplate.update(
                "INSERT INTO issue_status_history (issue_id, status, changed_by, changed_at) VALUES (?, ?::issue_status, ?, NOW())",
                7, "OPEN", 10
            );
            
            // ISSUE COMMENTS
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                1, 3, "Problem pojawił się wczoraj"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                1, 2, "Zajmuję się tym"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                2, 4, "Proszę o szybką naprawę"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                4, 6, "Problem wystepuje codziennie po 18:00"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                4, 9, "Zamowiono nowy modul domofonu"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                5, 7, "Wyciek nasila sie wieczorem"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_comment (issue_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                6, 8, "Lampa miga i czasem gasnie"
            );
            
            // ISSUE IMAGES
            jdbcTemplate.update(
                "INSERT INTO issue_image (issue_id, file_path) VALUES (?, ?)",
                1, "issue1.png"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_image (issue_id, file_path) VALUES (?, ?)",
                2, "issue2.png"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_image (issue_id, file_path) VALUES (?, ?)",
                4, "issue4.png"
            );
            jdbcTemplate.update(
                "INSERT INTO issue_image (issue_id, file_path) VALUES (?, ?)",
                5, "issue5.png"
            );
            
            // CHARGES
            jdbcTemplate.update(
                "INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?::charge_status, ?)",
                1, java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-31"), 500, "UNPAID", 1
            );
            jdbcTemplate.update(
                "INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?::charge_status, ?)",
                2, java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-31"), 650, "PARTIALLY_PAID", 1
            );
            jdbcTemplate.update(
                "INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?::charge_status, ?)",
                3, java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-31"), 430, "PAID", 10
            );
            jdbcTemplate.update(
                "INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?::charge_status, ?)",
                4, java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-31"), 720, "UNPAID", 1
            );
            jdbcTemplate.update(
                "INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by) VALUES (?, ?, ?, ?, ?::charge_status, ?)",
                5, java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2025-01-31"), 590, "PARTIALLY_PAID", 10
            );
            
            // CHARGE ITEMS
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                1, 1, 10, "m3", 10, 100
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                1, 2, 50, "kWh", 1, 50
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                1, 3, 1, "month", 350, 350
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                2, 3, 1, "month", 500, 500
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                2, 2, 100, "kWh", 1.5, 150
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                3, 3, 1, "month", 300, 300
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                3, 2, 70, "kWh", 1.2, 84
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                3, 1, 4, "m3", 11.5, 46
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                4, 3, 1, "month", 500, 500
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                4, 2, 120, "kWh", 1.5, 180
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                4, 1, 4, "m3", 10, 40
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                5, 3, 1, "month", 450, 450
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                5, 2, 80, "kWh", 1.5, 120
            );
            jdbcTemplate.update(
                "INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total) VALUES (?, ?, ?, ?, ?, ?)",
                5, 1, 2, "m3", 10, 20
            );
            
            // PAYMENTS
            jdbcTemplate.update(
                "INSERT INTO payment (charge_id, amount, payment_date) VALUES (?, ?, ?)",
                2, 300, java.sql.Date.valueOf("2025-01-15")
            );
            jdbcTemplate.update(
                "INSERT INTO payment (charge_id, amount, payment_date) VALUES (?, ?, ?)",
                3, 430, java.sql.Date.valueOf("2025-01-12")
            );
            jdbcTemplate.update(
                "INSERT INTO payment (charge_id, amount, payment_date) VALUES (?, ?, ?)",
                5, 300, java.sql.Date.valueOf("2025-01-20")
            );
            
            // ANNOUNCEMENTS
            jdbcTemplate.update(
                "INSERT INTO announcement (title, content, created_by) VALUES (?, ?, ?)",
                "Przegląd instalacji", "W dniu 10 lutego odbędzie się przegląd instalacji gazowej.", 1
            );
            jdbcTemplate.update(
                "INSERT INTO announcement (title, content, created_by) VALUES (?, ?, ?)",
                "Planowane czyszczenie klatek", "W sobotę od 8:00 odbędzie się mycie klatek schodowych.", 10
            );
            
            // DOCUMENTS
            jdbcTemplate.update(
                "INSERT INTO document (title, file_path, uploaded_by) VALUES (?, ?, ?)",
                "Regulamin", "/docs/regulamin.pdf", 1
            );
            jdbcTemplate.update(
                "INSERT INTO document (title, file_path, uploaded_by) VALUES (?, ?, ?)",
                "Harmonogram opłat", "/docs/harmonogram-oplat.pdf", 10
            );
            
            // AUDIT LOG
            jdbcTemplate.update(
                "INSERT INTO audit_log (user_id, action_text, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                1, "Utworzenie zgłoszenia", "issue", 1
            );
            jdbcTemplate.update(
                "INSERT INTO audit_log (user_id, action_text, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                2, "Zmiana statusu", "issue", 1
            );
            jdbcTemplate.update(
                "INSERT INTO audit_log (user_id, action_text, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                9, "Przypisanie zgłoszenia", "issue", 4
            );
            jdbcTemplate.update(
                "INSERT INTO audit_log (user_id, action_text, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                10, "Dodanie ogłoszenia", "announcement", 2
            );
        };
    }
}
