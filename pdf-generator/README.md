# DentFlow PDF Generator

Biblioteka Java do generowania raportów PDF dla systemu DentFlow.  
Zbudowana z użyciem **iText 8** (AGPL).

## Struktura

```
pdf-generator/
├── pom.xml
└── src/
    └── main/java/com/dentflow/pdf/
        ├── DentFlowPdfGenerator.java       ← publiczne API (fasada)
        ├── generator/
        │   ├── AppointmentListPdfGenerator.java    (Raport 1)
        │   ├── RoomOccupancyPdfGenerator.java      (Raport 2)
        │   └── PatientVisitHistoryPdfGenerator.java (Raport 3)
        ├── model/
        │   ├── AppointmentListReportData.java
        │   ├── RoomOccupancyReportData.java
        │   └── PatientVisitHistoryReportData.java
        └── util/
            └── PdfStyles.java                      (kolory, fonty, tabele)
```

## Raporty

| # | Raport | Parametry |
|---|--------|-----------|
| 1 | Lista wizyt | zakres dat, lekarz (opcj.), lokalizacja (opcj.), status (opcj.) |
| 2 | Obłożenie gabinetu | miesiąc, rok, lokalizacja (opcj.) |
| 3 | Historia wizyt pacjenta | patientId, zakres dat (opcj.) |

## Budowanie JAR

```bash
mvn clean package -DskipTests
# JAR: target/pdf-generator-1.0.0.jar
```

## Integracja z backendem (core-service)

Dodaj JAR do `pom.xml` backendu jako zależność systemową (lub zainstaluj do lokalnego Maven):

```bash
# Instalacja do lokalnego repo Maven
mvn install:install-file \
  -Dfile=pdf-generator-1.0.0.jar \
  -DgroupId=com.dentflow \
  -DartifactId=pdf-generator \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

Następnie w `pom.xml` core-app:
```xml
<dependency>
    <groupId>com.dentflow</groupId>
    <artifactId>pdf-generator</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Użycie w kontrolerze Spring Boot

```java
@GetMapping(value = "/reports/patient-history/{patientId}", produces = "application/pdf")
public ResponseEntity<byte[]> downloadPatientHistory(...) {
    PatientVisitHistoryReportData data = reportService.buildPatientHistoryData(...);
    byte[] pdf = new DentFlowPdfGenerator().generatePatientHistory(data);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", "attachment; filename=\"historia-pacjenta.pdf\"")
        .body(pdf);
}
```
