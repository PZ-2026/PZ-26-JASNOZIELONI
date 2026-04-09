# Frontend do projektu "System do obsługi spółdzielni mieszkaniowej"

Aplikacja mobilna w Kotlin.

**Uwaga:**

W kliencie Android używany jest adres http://10.0.2.2:8080 (działa dla emulatora Android i backendu uruchomionego lokalnie na komputerze) do łączenia z backendem. Dla fizycznego telefonu trzeba podmienić BASE_URL na IP komputera w tej samej sieci. Adres ip backendu ustawia się w pliku [build.grandle.kts](./app/build.gradle.kts).

Po poprawnym logowaniu:
ADMINISTRATOR trafia na admin_home.
MIESZKANIEC trafia na resident_tickets.
KONSERWATOR trafia na service_tickets.

