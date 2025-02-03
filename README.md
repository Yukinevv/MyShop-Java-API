# MyShop – API sklepu internetowego

> Projekt zawiera przykładową implementację REST API sklepu internetowego przy użyciu **Java 17**, **Spring Boot**, **Hibernate/JPA** i bazy danych **MSSQL**.  
> Funkcjonalność obejmuje m.in. rejestrację i logowanie użytkowników (z użyciem **JWT**), składanie zamówień, zarządzanie produktami, obsługę ról i walidację danych.

## 📌 Spis treści

- [Funkcjonalności](#funkcjonalności)
- [Wymagania](#wymagania)
- [Struktura projektu](#struktura-projektu)
- [Konfiguracja bazy danych](#konfiguracja-bazy-danych)
- [Instrukcja uruchomienia](#instrukcja-uruchomienia)
- [Testowanie API w Postmanie](#testowanie-api-w-postmanie)
- [Opis wybranych endpointów](#opis-wybranych-endpointów)
  - [Autentykacja i autoryzacja](#autentykacja-i-autoryzacja)
  - [Produkty](#produkty)
  - [Zamówienia](#zamówienia)
  - [Płatności (opcjonalnie)](#płatności-opcjonalnie)
- [Rozszerzenia i pomysły](#rozszerzenia-i-pomysły)
- [Licencja](#licencja)
- [Autorzy / Kontakt](#autorzy--kontakt)

---

## 🛠 Funkcjonalności

1. **Rejestracja i logowanie użytkowników**  
   - Uwierzytelnianie przy pomocy JWT (token Access + refresh token).
   - Role (`ROLE_USER`, `ROLE_ADMIN`).

2. **Zarządzanie produktami**  
   - Tworzenie, pobieranie, usuwanie produktów.
   - Pola: nazwa, cena (opcjonalnie stan magazynowy `stockQuantity`).

3. **Składanie zamówień**  
   - Każdy zalogowany użytkownik może dodać zamówienie.
   - Walidacja ilości produktów i obsługa błędów.

4. **Mechanizm Refresh Token**  
   - Odnawianie tokena dostępowego (endpoint `/api/auth/refresh`).
   - Wylogowywanie i unieważnianie refresh tokenu (`/api/auth/logout`).

5. **Walidacja danych (JSR-303)**  
   - Adnotacje typu `@NotBlank`, `@Size`, `@Valid` itp.

6. **Globalna obsługa wyjątków**  
   - Klasa `GlobalExceptionHandler` z `@ExceptionHandler` dla najczęstszych błędów (np. `MethodArgumentNotValidException`, `RuntimeException`).

7. **Obsługa płatności (opcjonalnie)**  
   - Przykładowe endpointy do inicjowania płatności i odbioru callbacku.

---

## 🔧 Wymagania

- **Java 17** (lub wyższa)
- **Maven** (lub Gradle, ale w przykładzie korzystam z Mavena)
- **Baza danych MSSQL** (lokalnie lub w kontenerze Docker)
- (Opcjonalnie) **Postman** do testowania endpointów

---

## 📁 Struktura projektu
```
src
└── main
  └── java
    └── com.example.shop
      ├── ShopApplication.java # Klasa startowa Spring Boot
      ├── config/ # Konfiguracje (np. SecurityConfig, AppConfig)
      ├── controller/ # Kontrolery REST
      ├── dto/ # Obiekty transferu danych (Request/Response)
      ├── entity/ # Encje JPA (User, Product, Order itp.)
      ├── exception/ # GlobalExceptionHandler, klasy błędów
      ├── mapper/ # Mapowanie encja <-> DTO (opcjonalnie)
      ├── repository/ # Interfejsy Spring Data JPA
      ├── security/ # Filtry JWT, UserDetailsService itp.
      └── service/ # Logika biznesowa
```

---

## 🗄 Konfiguracja bazy danych

1. Uruchom bazę MSSQL lokalnie lub w kontenerze (np. Docker).
2. Utwórz bazę, np. `simple_shop`, oraz użytkownika z uprawnieniami.
3. W pliku `application.properties` (lub `.yml`) ustaw parametry połączenia:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=simple_shop;encrypt=true;trustServerCertificate=true
spring.datasource.username=test_user
spring.datasource.password=test_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
```
(Opcjonalnie) W application-test.properties użyj innej bazy testowej (np. simple_shop_test) i ddl-auto=create-drop.

## 🚀 Instrukcja uruchomienia

**1. Sklonuj repozytorium:**
```
git clone https://github.com/nazwa-uzytkownika/nazwaprojektu.git
```

**2. Wejdź do katalogu projektu:**
```
cd nazwaprojektu
```

**3. Zbuduj projekt (pobierze zależności):**
```
mvn clean install
```

**4. Uruchom aplikację:**
```
mvn spring-boot:run
```
lub:
```
java -jar target/shop-0.0.1-SNAPSHOT.jar
```

**5. Domyślna ścieżka aplikacji:**
```
http://localhost:8080
```

## 🧪 Testowanie API w Postmanie
1. Zaimportuj plik kolekcji Postmana (np. NaukaShop.postman_collection.json).

2. Sprawdź, czy {{base_url}} to http://localhost:8080.

3. Wykonaj kolejno requesty:

- Rejestracja (POST /api/auth/register)
- Logowanie (POST /api/auth/login) – pobierz accessToken
- Tworzenie produktu (role ADMIN, nagłówek Authorization: Bearer <token>)
- Składanie zamówienia (POST /api/orders)
- Odświeżanie tokenu (POST /api/auth/refresh) z użyciem refreshToken
- Wylogowanie (POST /api/auth/logout)

## 🔗 Opis wybranych endpointów

### Autentykacja i autoryzacja
🔹 Rejestracja (POST /api/auth/register)
Body (JSON):
```
{
  "username": "testuser",
  "password": "secret123",
  "isAdmin": false
}
```

🔹 Logowanie (POST /api/auth/login)
Body (JSON):
```
{
  "username": "testuser",
  "password": "secret123"
}
```
**Response:**
```
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

### Produkty
🔹 Dodanie produktu

**POST /api/products** (Wymaga roli ADMIN)

Body (JSON):
```
{
  "name": "Laptop Dell",
  "price": 3500
}
```

🔹 Pobranie listy produktów

**GET /api/products** (Wymaga zalogowania)

### Zamówienia
🔹 Składanie zamówienia (POST /api/orders)

Body (JSON):
```
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 5 }
  ]
}
```

### Płatności

🔹 Inicjalizacja płatności (POST /api/payments/init)

Podaj orderId w body.

🔹 Odbiór callbacku z bramki (POST /api/payments/notify)

Zmienia paymentStatus w zamówieniu.

### 📜 Licencja
Projekt jest dostępny na licencji MIT.
Możesz używać, modyfikować i rozpowszechniać kod w dowolny sposób – proszę jedynie o zachowanie informacji o autorze i licencji.

### ✉️ Autor / Kontakt
Adrian Rodzic
adrianrodzic33@gmail.com
