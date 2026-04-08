# Backend do projektu "System do obsługi spółdzielni mieszkaniowej"

Backend w Java Spring Boot. Wykorzystuje on bazę danych PostgreSQL.
Wymagana jest Java w wersji 25.

## Kontener Dockera z bazą danych

Aby uruchomić bazę danych w kontenerze dockera, należy
przejśc do folderu głownego repozytorium. Zbudować kontener poleceniem:
```bash
docker-compose build
```

**Kontener budujemy tylko raz lub gdy wystapiły jakieś poważniejsze zmiany w projekcie.**
Kolejnym krokiem jest uruchomienie kontenera za pomoca polecenia:
```bash
docker compose up --detach
```

Działający kontener można zatrzymać z poziomu GUI (tam też można go uruchamiać po zbudowaniu) lub poleceniem:
```bash
docker-compose down
```

Migracje wykonują się automatycznie przy starcie backednu.

## Uruchamianie backendu

Backend można uruchamiać z poziomu Inteliij, poprzez uruchomienie głównego pliku, czyli
`CoopspaceBackendApplication.java`.
Drugim sposobem jest uruchomienie z poziomu linii poleceń. Robi się to poleceniem:
- Czysty start (rebuild): Jeśli masz problemy ze starymi plikami buildu, wyczyść je przed uruchomieniem:
```bash
.\gradlew clean bootRun
```
- Standardowe uruchomienie:
```bash
.\grandlew bootRun
```
- Tryb Debug: Jeśli chcesz, aby Gradle pokazywał więcej szczegółów w razie błędów:
```bash
.\gradlew bootRun --info
```

*Aby uruchomić na Linux należy zamienić `.\` na `./`*

Backend uruchamia się pod adresem [localhost:8080](http:localhost:8080).
Po przejściu pod adres [localhost:8080](http:localhost:8080) ukaże się panel WWW. Należy się do niego zalogować tymi danymi:
```
Login: admin
Hasło: ala
```

Przy pierwszym uruchomieniu backendu są automatycznie tworzone tabele. Następnie nastepuje uruchomienie seederów.
Wszystkie hasła z seederów i w projekcie są zabezpieczone BCrypt.

## Struktura katalogów backendu

```
src/main/java/pl/edu/ur/coopspace_backend/
├── entity/
│   ├── User.java              # Encja użytkownika z JPA
│   └── UserRole.java          # Enum ról
├── repository/
│   └── UserRepository.java    # Dostęp do bazy danych
├── service/
│   └── AuthService.java       # Logika autentykacji
├── controller/
│   └── AuthController.java    # REST endpointy
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── AuthResponse.java
└── config/
    ├── SecurityConfig.java    # BCrypt Password Encoder
    └── DataInitializer.java   # Inicjalizacja danych testowych
        
        
backend/uploads/
└── issue-images/
    ├── 1          # folder z zdjeciami zgloszenia 1
    └── 2          # folder z zdjeciami zgloszenia 2
```

