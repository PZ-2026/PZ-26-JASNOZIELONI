# Dokumentacja korzystania z API biblioteki do generowania raportów PDF

Biblioteka Java służąca do generowania raportów w formacie PDF.
Jest ona wykorzystywana w projekcie z przedmiotu `Programowanie zespołowe` na Uniwersytecie Rzeszowskim.
Zawiera dostosowane funkcje do generowania raportów PDF związanych z tematyką projektu.

Raporty PDF generowane przez bibliotekę:
- Protokół Naprawy - szczegółowy opis prac konserwatorskich wraz z komentarzem.
- Raport Finansowy Mieszkańca - zestawienie opłat (czynsz, media) wraz ze statusem płatności.
- Raport Statystyczny Spółdzielni - zbiorcze zestawienie przychodów, usterek i danych o mieszkańcach.
- Raport Zgłoszeń Konserwatorskich - ewidencja prac technicznych i czasu napraw.

Technologie wymagane do działania biblioteki:
- Java 25
- iText 7 Core - zewnętrzna biblioteka do programistycznego tworzenia i manipulacji dokumentami PDF.
- Gradle - system zarządzania pakietami i budową projektu.

## Korzystanie z biblioteki

Aby korzystać z biblioteki musi być ona wgrana jako plik .jar.
Ważne jest również zainstalowanie następujących zależności poprzez Gradle.
Plik `build.gradle`:
```groovy
dependencies {
    implementation(files("libs/TwojaBiblioteka.jar")) // gdy w glownym katalogu projektu jest folder libs z plikami jar
    implementation ("com.itextpdf:itext7-core:8.0.3") // iText dla generowania PDF
    implementation("org.slf4j:slf4j-api:2.0.7") // do logow dla iTexta
}
```

Przykłady użycia poszczególnych funkcji do generowania raportów:
- Raport zgłoszeń konserwatorskich:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class Main {
    // Tworzymy statyczną instancję loggera dla tej klasy
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // przykaldowe wywolanie generatora raportu zgloszen konserwatorskich
    public static void main(String[] args) {
        try {
            MaintenanceReportGenerator generator = new MaintenanceReportGenerator();
            MaintenanceReportData data = new MaintenanceReportData();

            data.dataStworzenia = "08.05.2026";
            data.numerRaportu = "RK/2026/05/88";
            data.dataOd = "01.05.2026";
            data.dataDo = "08.05.2026";

            data.pozycje = new ArrayList<>();
            data.pozycje.add(new MaintenanceReportData.MaintenanceRow(
                    "1", "02.05.2026", "Awarie elektryczne", "ul. Polna 12/4", "Jan Kowalski", "2h 30m", "ZAKOŃCZONE"
            ));
            data.pozycje.add(new MaintenanceReportData.MaintenanceRow(
                    "2", "04.05.2026", "Hydraulika", "ul. Nowa 5/10", "Adam Nowak", "1h 15m", "W TRAKCIE"
            ));
            data.pozycje.add(new MaintenanceReportData.MaintenanceRow(
                    "3", "04.05.2026", "Hydraulika", "ul. Stara 5/10", "Adam Nowak", "1h 15m", "W TRAKCIE"
            ));

            String filePath = "Raport_Zgloszen_Konserwatorskich_001.pdf";
            generator.generateMaintenanceReportPdf(filePath, data);
        } catch (Exception e) {
            logger.error("Wystąpił krytyczny błąd podczas generowania raportu PDF!", e);
        }
    }
}
```

- Raport Statystyczny Spółdzielni:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class Main {
    // Tworzymy statyczną instancję loggera dla tej klasy
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // przkyladowe wywolanie jako wzorzec dla api
    public static void main() {
        try {
            StatisticReportGenerator gen = new StatisticReportGenerator();
            StatsReportData d = new StatsReportData();

            d.dataStworzenia = "08.05.2026";
            d.numerRaportu = "STAT/05/2026";
            d.dataOd = "01.05.2026";
            d.dataDo = "08.05.2026";

            d.sumaPrzychodow = "254,300.50 PLN";
            d.ogolnaLiczbaUsterek = "14";
            d.sredniCzasRozwiazania = "2d 4h";
            d.iloscMieszkancow = "452";
            d.mieszkancyDopisani = "3";
            d.mieszkancyUsunieci = "1";

            d.pozycje = new ArrayList<>();
            d.pozycje.add(new StatsReportData.StatsRow("1", "02.05.2026", "Kwiatowa 5/1", "1200", "80", "120", "30", "1430", "1430"));
            d.pozycje.add(new StatsReportData.StatsRow("2", "03.05.2026", "Leśna 10/2", "1100", "95", "150", "40", "1385", "0"));
            d.pozycje.add(new StatsReportData.StatsRow("3", "03.05.2026", "Leśna 10/2", "1100", "95", "150", "40", "1385", "0"));
            d.pozycje.add(new StatsReportData.StatsRow("4", "03.05.2026", "Leśna 10/2", "1100", "95", "150", "40", "1385", "0"));
            d.pozycje.add(new StatsReportData.StatsRow("5", "03.05.2026", "Leśna 12/2", "1100", "95", "150", "40", "1385", "0"));

            String sciezkaPliku = "Raport_Statystyczny_001.pdf";
            gen.generateStatsReportPdf(sciezkaPliku, d);
        } catch (Exception e) {
            logger.error("Wystąpił krytyczny błąd podczas generowania raportu PDF!", e);
        }
    }
}
```

- Protokół Naprawy:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    // Tworzymy statyczną instancję loggera dla tej klasy
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // przkyladowe wywolanie jako wzorzec dla api
    public static void main() {
        try{
            RepairProtocolGenerator generator = new RepairProtocolGenerator();

            RepairProtocolData data = new RepairProtocolData();
            data.dataStworzenia = "08.05.2026";
            data.numerProtokolu = "PR/2026/05/001";
            data.dataRozpoczecia = "07.05.2026 08:00";
            data.dataZakonczenia = "08.05.2026 10:00";
            data.konserwator = "Andrzej Naprawski";
            data.adres = "ul. Techniczna 15/4, Poznań";
            data.status = "ZAKOŃCZONE";
            data.osobaTworzaca = "Anna Nowakowska";
            data.kategoria = "Hydraulika";
            data.tytul = "Naprawa pękniętej rury w łazience";
            data.opis = "Po przybyciu na miejsce stwierdzono pęknięcie rury doprowadzającej wodę do pralki. " +
                    "Zakręcono główny zawór, wymieniono uszkodzony odcinek rury (ok. 30 cm) oraz " +
                    "zamontowano nowy zawór kulowy. Przeprowadzono próbę szczelności - wynik pozytywny.";

            // 3. Dodajemy listę komentarzy
            data.komentarze = new ArrayList<>();
            data.komentarze.add(new RepairProtocolData.CommentRow(
                    "1", "07.05.2026", "Zgłoszenie przyjęte, brak dostępu do mieszkania rano.", "System"
            ));
            data.komentarze.add(new RepairProtocolData.CommentRow(
                    "2", "08.05.2026", "Naprawa wykonana, klient potwierdził odbiór.", "A. Naprawski"
            ));

            // 4. Wywołujemy generowanie pliku
            String sciezkaPliku = "Protokol_Naprawy_001.pdf";
            generator.generateRepairPdf(sciezkaPliku, data);
        } catch(IOException e) {
            logger.error("Wystąpił krytyczny błąd podczas generowania raportu PDF!", e);
        }
    }
}
```

- Raport Finansowy Mieszkańca:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Main {
    // Tworzymy statyczną instancję loggera dla tej klasy
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // przkyladowe wywolanie jako wzorzec dla api
    public static void main() {
        try{
            FinancialReportGenerator generator = new FinancialReportGenerator();
            FinancialReportData data = new FinancialReportData();

            data.dataStworzenia = "08.05.2026";
            data.numerRaportu = "RF/2026/05/12";
            data.dataOd = "01.04.2026";
            data.dataDo = "30.04.2026";
            data.imieNazwisko = "Marek Nowakowski";
            data.adresMieszkania = "ul. Kwiatowa 4/12";
            data.email = "marek.n@example.com";

            data.pozycje = new ArrayList<>();
            data.pozycje.add(new FinancialReportData.FinanceRow(
                    "1", "05.04.2026", "1200.00", "150.50", "210.00", "45.00", "1605.50", "0.00", "OPŁACONE"
            ));
            data.pozycje.add(new FinancialReportData.FinanceRow(
                    "2", "02.05.2026", "1200.00", "140.00", "195.00", "42.00", "1577.00", "1577.00", "NIEOPŁACONE"
            ));

            String sciezkaPliku = "Raport_Finansowy_001.pdf";
            generator.generateFinancialReportPdf(sciezkaPliku, data);
        } catch (Exception e) {
            logger.error("Wystąpił krytyczny błąd podczas generowania raportu PDF!", e);
        }
    }
}
```


## Licencja

Projekt biblioteki w formie .jar został stworzony na potrzeby wewnętrzne projektu.
Wykorzystuje bibliotekę iText na licencji AGPL.
