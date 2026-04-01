# Autentykacja - Instrukcja testowania

## Endpointy API

### Logowanie
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "admin123"
}
```

**Odpowiedź:**
```json
{
  "id": 1,
  "email": "admin@test.com",
  "firstName": "Jan",
  "lastName": "Admin",
  "phoneNumber": "123456789",
  "role": "ADMIN",
  "localId": null
}
```

### Rejestracja
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "NewPassword123",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phoneNumber": "555666777"
}
```
