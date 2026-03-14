# DentFlow - System do zarzadzania gabinetem stomatologicznym

## Struktura repozytorium

```
PZ-26-BORDOWI/
|-- DentFlow-PZ/           # Backend (Spring Boot, Maven multi-module)
|   |-- identity-service/  # Uwierzytelnianie, JWT, role
|   |-- core-service/      # Logika biznesowa
|   |   |-- core-app/      # Glowna aplikacja (laczy moduly)
|   |   |-- clinic/        # Gabinety, lokalizacje, personel
|   |   |-- patient/       # Pacjenci
|   |   |-- scheduling/    # Grafiki pracy, blokady
|   |   |-- reservation/   # Rezerwacje wizyt
|   |   |-- catalog/       # Cennik uslug
|   |   |-- notification/  # Powiadomienia in-app
|   |   └── file/          # Pliki (Supabase Storage)
|   |-- docker-compose.yml
|   └── .env.example
|-- DentFlowAndroid/       # Aplikacja mobilna (Kotlin, Jetpack Compose)
|-- Doc/                   # Dokumentacja techniczna
|-- .github/workflows/     # CI/CD (GitHub Actions)
|-- shell.nix              # Srodowisko deweloperskie (NixOS)
```

## Quick start (backend)

### Wymagania
- Java 21
- Maven 3.9+
- Docker + Docker Compose
- (dla NixOS) `nix-shell` w katalogu repozytorium zainstaluje wszystko automatycznie

### Uruchomienie

```bash
# 1. Uruchomienie bazy danych
cd DentFlow-PZ
docker-compose up -d postgres

# 2. Zainstaluj wszystkie moduly w lokalnym Maven repo (z katalogu DentFlow-PZ/)
mvn install -DskipTests

# 3. Uruchomienie serwisow (osobne terminale, z katalogu DentFlow-PZ/)
#    Profil dev ustawia domyslne wartosci (DB, JWT, itp.)
mvn spring-boot:run -pl identity-service -Dspring-boot.run.profiles=dev
mvn spring-boot:run -pl core-service/core-app -Dspring-boot.run.profiles=dev
```

> **Produkcja / Docker**: zamiast profilu `dev` uzyj pliku `.env` (skopiuj z `.env.example` i uzupelnij).

### Swagger UI
- Identity Service: http://localhost:8081/swagger-ui.html
- Core Service: http://localhost:8080/swagger-ui.html

## CI/CD

Pipeline GitHub Actions uruchamia sie automatycznie na push/PR do `main` i `develop` (backend i aplikacja mobilna):

- **Backend - Build & Test** - kompilacja + testy z PostgreSQL
- **Backend - Code Style** - sprawdzenie kompilacji
- **Android - Build & Test** - kompilacja + testy
- **Android - Lint** - sprawdzenie kodu

CI waliduje tylko nowe commity

## Konwencje commitow

Uzywamy formatu [Conventional Commits](https://www.conventionalcommits.org/):

```
<typ>(zakres): krotki opis

[opcjonalnie: dluzszy opis]
[opcjonalnie: SCRUM-XX]
```

### Typy

| Typ | Kiedy |
|---|---|
| `feat` | Nowa funkcjonalnosc |
| `fix` | Naprawa bledu |
| `refactor` | Refaktoryzacja (bez zmiany funkcjonalnosci) |
| `docs` | Dokumentacja |
| `test` | Testy |
| `chore` | Konfiguracja, CI, zaleznosci |
| `style` | Formatowanie kodu |

### Zakresy

| Zakres | Obszar |
|---|---|
| `identity` | identity-service |
| `core` | core-service (ogolne) |
| `clinic` | modul clinic |
| `patient` | modul patient |
| `scheduling` | modul scheduling |
| `reservation` | modul reservation |
| `catalog` | modul catalog |
| `notification` | modul notification |
| `file` | modul file |
| `android` | aplikacja Android |
| `ci` | CI/CD, GitHub Actions |
| `docs` | dokumentacja |

### Przyklady

```bash
feat(identity): dodanie endpointu POST /auth/login
fix(reservation): walidacja kolizji wizyt tego samego dentysty
chore(ci): konfiguracja GitHub Actions
docs: aktualizacja README
test(patient): testy jednostkowe PatientService
refactor(core): wydzielenie wspolnego BaseEntity
```

## Praca z branchami

```
main              <- produkcja (tylko merge z develop)
  |-- develop     <- branch integracyjny
      |-- feat/SCRUM-33-identity-auth
      |-- feat/SCRUM-45-clinic-crud
      |-- fix/SCRUM-XX-opis-bledu
```

### Zaczynanie pracy nad taskiem

```bash
# 1. Przełącz się na develop i pobierz najnowsze zmiany
git checkout develop
git pull origin develop

# 2. Utwórz branch od develop (nazwa: typ/SCRUM-XX-krotki-opis)
git checkout -b feat/SCRUM-45-clinic-crud develop

# 3. Często commituj zmiany
git add -A
git commit -m "feat(clinic): CRUD lokalizacji - entity + repository"
git commit -m "feat(clinic): CRUD lokalizacji - REST controller"

# 4. Pushuj branch na remote
git push -u origin feat/SCRUM-45-clinic-crud

# 5. Otwórz Pull Request do develop na GitHubie
#    - Tytuł: "feat(clinic): SCRUM-45 CRUD gabinet, lokalizacje, personel"
#    - Opis: co zrobione, link do taska SCRUM-45
#    - Poczekaj aż CI przejdzie (zielony checkmark)
#    - Poproś o review

# 6. Po ukonczeniu sprintu - merge develop -> main (leader)
```

### Nazewnictwo branchy

| Prefiks | Kiedy | Przyklad |
|---|---|---|
| `feat/` | Nowa funkcjonalnosc | `feat/SCRUM-43-jwt-auth` |
| `fix/` | Naprawa bledu | `fix/SCRUM-50-appointment-conflict` |
| `refactor/` | Refaktoryzacja | `refactor/SCRUM-42-code-style` |
| `chore/` | Konfiguracja, CI | `chore/SCRUM-36-supabase-config` |
| `docs/` | Dokumentacja | `docs/SCRUM-77-uzupelnienie-doc` |

### Zasady

1. **Nigdy nie pushuj bezposrednio do `main`** - tylko PR z `develop`
2. **Jeden branch - jeden task SCRUM** (lub kilka powiazanych)
3. **Tworzysz branch od `develop`**: `git checkout -b feat/SCRUM-XX-opis develop`
4. **Commitujesz czesto**, male zmiany z opisowymi komunikatami
5. **Przed PR** - upewnij sie ze `mvn compile` (backend) lub `./gradlew assembleDebug` (Android) przechodzi lokalnie
6. **Pull Request** - opis co zostalo zrobione, link do SCRUM task
7. **CI musi przejsc** zanim PR moze byc zmergowany
8. **Nie commituj plikow `.env`** - tylko `.env.example`
9. **Konflikty** - rozwiazujesz u siebie na branchu (`git merge develop` lub `git rebase develop`)

## Zmienne srodowiskowe

Wszystkie wrazliwe dane sa w `.env` (nie w kodzie). Przed uruchomieniem:

```bash
cp DentFlow-PZ/.env.example DentFlow-PZ/.env
```

Opis zmiennych w `.env.example`. Generowanie JWT_SECRET:

```bash
openssl rand -hex 32
```
