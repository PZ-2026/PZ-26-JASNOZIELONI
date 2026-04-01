# Baza danych

Wykorzystywaną bazą danych jest PostgreSQL w wersji >= 16.

W tym folderze znajdują się dwa skrypty w SQL.

- [`db_init`](./db_init.sql) - służy do inicjalizacji struktury bazy danych.
- [`db_data`](./db_data.sql) - służy do zainicjowania bazy danych danymi.

Skrypty można uruchamiać poprzez wklejenie do konsoli bazy danych.
Wykonywanie tej czynności nie jest jednak niezbędne, ponieważ projekt posiada na backendzie migracje i seedery wykonywane automatycznie.

Aby ręcznie zaimportować bazę danych:
1. Utwórz baze o nazwie `projekt_pz`.
```sql
CREATE DATABASE projekt_pz;
```
2. Możesz opcjonalnie skonfigurować nowego użytkownika z odpowiednimi uprawnieniami tylko do tej bazy.
Przykład:
```sql
CREATE USER admin WITH PASSWORD 'student';
GRANT ALL PRIVILEGES ON DATABASE projekt_pz TO admin;
```
3. Następnie należy wkleić do konsoli lub wykonać w inny sposób skrypt o naziwe [`db_init.sql`](./db_init.sql).
4. Potem wykonaj skrypt wypełniający bazę danych danymi, czyli [`db_data.sql`](./db_data.sql).
5. Baza danych jest gotowa.
