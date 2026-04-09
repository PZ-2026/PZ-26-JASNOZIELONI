-- Skrypt wypelniajacy baze danych przykladowymi danymi

-- BUILDINGS
INSERT INTO building (name, address)
VALUES 
('Tulipan', 'ul. Kwiatowa 1'),
('Mlecz', 'ul. Słoneczna 5');

-- LOCALS
INSERT INTO local (building_id, number, staircase, number_of_residents)
VALUES
(1, '1', 'A', 2),
(1, '2', 'A', 3),
(1, '3', 'B', 1),
(2, '1', 'A', 4),
(2, '2', 'B', 2);

-- USERS
INSERT INTO "user" (email, password_hash, first_name, last_name, phone_number, role, local_id)
VALUES
('admin@test.com', '$2a$10$H8upDe4/1Z2wcd4d5H.7kuR6jrrt2WBKl74EJDxUGC87OKQMKrBcm', 'Jan', 'Admin', '123456789', 'ADMIN', NULL),
('maintainer@test.com', '$2a$10$TLwzF2W4zq1ET31W0m7F1O5TK7c7RhAVsSSoqCSWIG8gSp55nO7Um', 'Anna', 'Serwis', '987654321', 'MAINTAINER', NULL),
('user1@test.com', '$2a$10$sokOPTW47X11dyxudxJQfeXysH8l3pOotGGNKmhFILen1PvMBZb4C', 'Piotr', 'Kowalski', '111222333', 'RESIDENT', 1),
('user2@test.com', '$2a$10$dvAf36OWdB0f6mjsWz3FQONqX/YWIQbTOUl2nztzLzwM3OOeOPD3y', 'Maria', 'Nowak', '444555666', 'RESIDENT', 2),
('user3@test.com', '$2a$10$L9s0PBv2OUZeW.7n8vef7.lLY2Das1kpMFf1cXrf.o9CQXCkSSZvy', 'Tomasz', 'Wiśniewski', '777888999', 'RESIDENT', 3),
('user4@test.com', '$2a$10$ic23kmAl6VoBIZGXk5Xv0eXA3TfllVgQV5Cj9p/8LpH/lYlZV4rZu', 'Karolina', 'Mazur', '700100200', 'RESIDENT', 4),
('user5@test.com', '$2a$10$83vcEUkmQTTDzqxMaMTzTOqFrDmFVr..d9x9HIu7e7LiVZcFxA02K', 'Michał', 'Zieliński', '700100201', 'RESIDENT', 5),
('user6@test.com', '$2a$10$zNiQGQhaRn45/qc6z/iwSezFnD1flraa9GrsUyUrBX1k/F/SncAA6', 'Aleksandra', 'Witkowska', '700100202', 'RESIDENT', 1),
('maintainer2@test.com', '$2a$10$TLwzF2W4zq1ET31W0m7F1O5TK7c7RhAVsSSoqCSWIG8gSp55nO7Um', 'Paweł', 'Serwis', '700100203', 'MAINTAINER', NULL),
('admin2@test.com', '$2a$10$H8upDe4/1Z2wcd4d5H.7kuR6jrrt2WBKl74EJDxUGC87OKQMKrBcm', 'Ewa', 'Admin', '700100204', 'ADMIN', NULL);

-- ISSUE CATEGORIES
INSERT INTO issue_category (name)
VALUES
('Hydraulika'),
('Elektryka'),
('Porządek'),
('Inne');

-- ISSUES
INSERT INTO issue (title, description, category_id, local_id, created_by_user_id, main_assignee_id, status)
VALUES
('Cieknący kran', 'Kran w kuchni przecieka', 1, 1, 3, 2, 'IN_PROGRESS'),
('Brak światła na klatce', 'Żarówka przepalona', 2, 2, 4, 2, 'OPEN'),
('Śmieci na korytarzu', 'Ktoś zostawił worki', 3, 3, 5, NULL, 'OPEN'),
('Awaria domofonu', 'Domofon nie działa od rana', 2, 4, 6, 9, 'IN_PROGRESS'),
('Nieszczelny kaloryfer', 'Kaloryfer w salonie przecieka', 1, 5, 7, 2, 'OPEN'),
('Zepsuta lampa na parkingu', 'Brak oswietlenia przy wejsciu', 2, 1, 8, 9, 'OPEN'),
('Sprzatanie klatki', 'Prosba o dodatkowe sprzatanie w weekend', 3, 2, 10, NULL, 'OPEN');

-- ISSUE ASSIGNMENTS
INSERT INTO issue_assignment (issue_id, user_id, assigned_by)
VALUES
(1, 2, 1),
(2, 2, 1),
(4, 9, 1),
(5, 2, 10),
(6, 9, 1);

-- ISSUE STATUS HISTORY
INSERT INTO issue_status_history (issue_id, status, changed_by)
VALUES
(1, 'OPEN', 3),
(1, 'IN_PROGRESS', 2),
(2, 'OPEN', 4),
(4, 'OPEN', 6),
(4, 'IN_PROGRESS', 9),
(5, 'OPEN', 7),
(6, 'OPEN', 8),
(7, 'OPEN', 10);

-- ISSUE COMMENTS
INSERT INTO issue_comment (issue_id, user_id, content)
VALUES
(1, 3, 'Problem pojawił się wczoraj'),
(1, 2, 'Zajmuję się tym'),
(2, 4, 'Proszę o szybką naprawę'),
(4, 6, 'Problem wystepuje codziennie po 18:00'),
(4, 9, 'Zamowiono nowy modul domofonu'),
(5, 7, 'Wyciek nasila sie wieczorem'),
(6, 8, 'Lampa miga i czasem gasnie');

-- ISSUE IMAGES
INSERT INTO issue_image (issue_id, file_path)
VALUES
(1, 'issue1.png'),
(2, 'issue2.png'),
(4, 'issue4.png'),
(5, 'issue5.png');

-- CHARGE ITEM TYPES
INSERT INTO charge_item_type (name)
VALUES
('Woda'),
('Prąd'),
('Czynsz'),
('Gaz');

-- CHARGES
INSERT INTO charge (local_id, period_start, period_end, total_amount, status, created_by)
VALUES
(1, '2025-01-01', '2025-01-31', 500, 'UNPAID', 1),
(2, '2025-01-01', '2025-01-31', 650, 'PARTIALLY_PAID', 1),
(3, '2025-01-01', '2025-01-31', 430, 'PAID', 10),
(4, '2025-01-01', '2025-01-31', 720, 'UNPAID', 1),
(5, '2025-01-01', '2025-01-31', 590, 'PARTIALLY_PAID', 10);

-- CHARGE ITEMS
INSERT INTO charge_item (charge_id, type_id, quantity, unit, unit_price, total)
VALUES
(1, 1, 10, 'm3', 10, 100),
(1, 2, 50, 'kWh', 1, 50),
(1, 3, 1, 'month', 350, 350),
(2, 3, 1, 'month', 500, 500),
(2, 2, 100, 'kWh', 1.5, 150),
(3, 3, 1, 'month', 300, 300),
(3, 2, 70, 'kWh', 1.2, 84),
(3, 1, 4, 'm3', 11.5, 46),
(4, 3, 1, 'month', 500, 500),
(4, 2, 120, 'kWh', 1.5, 180),
(4, 1, 4, 'm3', 10, 40),
(5, 3, 1, 'month', 450, 450),
(5, 2, 80, 'kWh', 1.5, 120),
(5, 1, 2, 'm3', 10, 20);

-- PAYMENTS
INSERT INTO payment (charge_id, amount, payment_date)
VALUES
(2, 300, '2025-01-15'),
(3, 430, '2025-01-12'),
(5, 300, '2025-01-20');

-- ANNOUNCEMENTS
INSERT INTO announcement (title, content, created_by)
VALUES
('Przegląd instalacji', 'W dniu 10 lutego odbędzie się przegląd instalacji gazowej.', 1),
('Planowane czyszczenie klatek', 'W sobotę od 8:00 odbędzie się mycie klatek schodowych.', 10);

-- DOCUMENTS
INSERT INTO document (title, file_path, uploaded_by)
VALUES
('Regulamin', '/docs/regulamin.pdf', 1),
('Harmonogram opłat', '/docs/harmonogram-oplat.pdf', 10);

-- AUDIT LOG
INSERT INTO audit_log (user_id, action_text, entity_type, entity_id)
VALUES
(1, 'Utworzenie zgłoszenia', 'issue', 1),
(2, 'Zmiana statusu', 'issue', 1),
(9, 'Przypisanie zgłoszenia', 'issue', 4),
(10, 'Dodanie ogłoszenia', 'announcement', 2);
