# Aplikacja do Obróbki Obrazów w JavaFX

## Wstęp

Projekt został stworzony w języku Java z wykorzystaniem biblioteki JavaFX i implementuje desktopową aplikację do podstawowej obróbki obrazów. Aplikacja umożliwia:

*   Wczytywanie obrazów w formacie JPG.
*   Podgląd obrazu oryginalnego oraz przetworzonego.
*   Wykonywanie operacji na obrazach, takich jak:
    *   Negatyw
    *   Progowanie (z możliwością wyboru progu)
    *   Konturowanie
*   Transformacje obrazu:
    *   Obrót w lewo i w prawo
    *   Skalowanie
*   Zapisywanie przetworzonego obrazu do pliku JPG.
*   Śledzenie historii wykonanych operacji.
*   Logowanie zdarzeń aplikacji do pliku.
*   Wykorzystanie wielowątkowości do przyspieszenia niektórych operacji przetwarzania obrazu.

## Opis funkcjonalności

*   **Wczytywanie obrazu:**
    *   Użytkownik może wybrać obraz w formacie `.jpg` z lokalnego systemu plików.
    *   Po wczytaniu, obraz oryginalny jest wyświetlany w dedykowanym polu.
    *   Kopia obrazu jest tworzona jako obraz do przetwarzania.
*   **Przetwarzanie obrazu:**
    *   **Negatyw:** Tworzy negatyw kolorystyczny obrazu. Operacja jest zoptymalizowana przy użyciu wielowątkowości.
    *   **Progowanie:** Konwertuje obraz na czarno-biały na podstawie zdefiniowanego przez użytkownika progu (0-255). Użytkownik wybiera próg za pomocą suwaka w oknie dialogowym. Operacja jest zoptymalizowana przy użyciu wielowątkowości.
    *   **Konturowanie:** Wykrywa i zaznacza krawędzie na obrazie. Operacja jest zoptymalizowana przy użyciu wielowątkowości.
    *   **Brak operacji:** Przywraca obraz przetworzony do stanu obrazu oryginalnego.
*   **Transformacje obrazu:**
    *   **Obrót w lewo/prawo:** Obraca obraz o 90 stopni w wybranym kierunku.
    *   **Skalowanie:** Umożliwia zmianę wymiarów obrazu.
*   **Zapisywanie obrazu:**
    *   Użytkownik może zapisać przetworzony obraz.
    *   Aplikacja prosi o podanie nazwy pliku
    *   Nazwa pliku jest walidowana pod kątem długości.
    *   Obrazy zapisywane są w katalogu `Pictures` w folderze domowym użytkownika.
    *   Aplikacja sprawdza, czy plik o podanej nazwie już istnieje.
*   **Historia operacji:**
    *   Każda wykonana operacja (wczytanie, przetwarzanie, zapis) jest dodawana do listy historii wraz ze znacznikiem czasowym (godzina:minuta:sekunda).
    *   Historia jest wyświetlana w interfejsie użytkownika.
*   **Logowanie zdarzeń:**
    *   Aplikacja loguje kluczowe zdarzenia do pliku `image_processor.log`.
    *   Plik logów znajduje się w katalogu `ImageProcessorLogs` w folderze domowym użytkownika.
    *   Logi zawierają poziom (INFO, WARNING, ERROR), znacznik czasowy oraz komunikat.
*   **Interfejs użytkownika:**
    *   Zdefiniowany w pliku FXML (`hello-view.fxml`).
    *   Wyświetla logo aplikacji.
    *   Zawiera dwa pola `ImageView` do wyświetlania obrazu oryginalnego i przetworzonego.
    *   Panel z przyciskami do wczytywania, zapisywania, obracania, skalowania obrazu oraz wyboru i wykonania operacji.
    *   Lista (`ListView`) wyświetlająca historię operacji.
    *   Wyświetla powiadomienia "toast" informujące o wyniku operacji lub błędach.
    *   Przyciski transformacji są aktywne tylko po załadowaniu obrazu.
    *   Stopka zawiera informację o autorze.

## Pliki i struktura projektu oraz ich przeznaczenie

*   **`com.example.demo1.HelloApplication.java`**:
    *   Główna klasa aplikacji, dziedzicząca po `javafx.application.Application`.
    *   Odpowiada za uruchomienie aplikacji, załadowanie interfejsu z pliku FXML i wyświetlenie głównego okna.
*   **`com.example.demo1.HelloController.java`**:
    *   Kontroler dla interfejsu zdefiniowanego w `hello-view.fxml`.
    *   Zawiera logikę obsługi zdarzeń (np. kliknięcia przycisków), przetwarzania obrazów, zarządzania historią i logowaniem.
    *   Inicjalizuje `ExecutorService` do wielowątkowego przetwarzania.
    *   Metody:
        *   `initialize()`: Inicjalizacja komponentów, loggera, puli wątków, listy operacji.
        *   `onLoadImage()`: Obsługa wczytywania obrazu.
        *   `onExecute()`: Wykonanie wybranej operacji (negatyw, progowanie, konturowanie).
        *   `onRotateLeft()`, `onRotateRight()`: Obrót obrazu.
        *   `onScale()`: Wyświetlenie dialogu i skalowanie obrazu.
        *   `applyNegative()`, `processNegativeChunk()`: Logika negatywu (wielowątkowo).
        *   `showThresholdDialog()`, `applyThreshold()`, `processThresholdChunk()`: Logika progowania (wielowątkowo).
        *   `applyContour()`, `processContourChunk()`: Logika konturowania (wielowątkowo).
        *   `onSaveImage()`, `saveImage()`: Obsługa zapisywania obrazu.
        *   `showToast()`: Wyświetlanie powiadomień.
        *   `addToHistory()`: Dodawanie wpisów do historii.
        *   `updateButtonsState()`: Zarządzanie aktywnością przycisków.
        *   `validateDimensions()`: Walidacja wymiarów przy skalowaniu.
*   **`com.example.demo1.hello-view.fxml`**:
    *   Plik XML definiujący strukturę i wygląd interfejsu użytkownika aplikacji przy użyciu JavaFX Scene Builder.
    *   Określa rozmieszczenie kontrolek (przyciski, etykiety, pola obrazów, lista).
*   **`com.example.demo1.Logger.java`**:
    *   Klasa odpowiedzialna za logowanie zdarzeń aplikacji do pliku.
    *   Definiuje enum `LogLevel` (INFO, WARNING, ERROR, DEBUG).
    *   Zapisuje logi w formacie: `data godzina [POZIOM] komunikat`.
*   **`com.example.demo1.module-info.java`**:
    *   Plik modułu Javy, definiujący zależności (`requires`) oraz eksportowane pakiety (`exports`) i otwarte pakiety (`opens`) dla JavaFX.
*   **`com.example.demo1.logo.png`** (zasób w projekcie):
    *   Plik graficzny używany jako logo aplikacji, wyświetlany w górnej części interfejsu.

## Działanie programu

1.  **Uruchomienie:**
    *   Aplikacja jest uruchamiana poprzez wykonanie metody `main` w klasie `HelloApplication`.
    *   Wyświetlane jest główne okno aplikacji.
2.  **Interakcja:**
    *   Użytkownik klika przycisk "Wczytaj Obraz", aby wybrać plik `.jpg`.
    *   Obraz pojawia się w polach "Oryginalny Obraz" i "Przetworzony Obraz". Przyciski transformacji stają się aktywne.
    *   Użytkownik może wybrać operację z listy rozwijanej (Negatyw, Progowanie, Konturowanie) i kliknąć "Wykonaj".
        *   Dla progowania pojawi się dodatkowe okno dialogowe do ustawienia wartości progu.
    *   Użytkownik może użyć przycisków "⟲" (obrót w lewo), "⟳" (obrót w prawo) lub "Skaluj obraz".
        *   Dla skalowania pojawi się okno dialogowe do wprowadzenia nowych wymiarów.
    *   Wynik operacji jest widoczny w polu "Przetworzony Obraz".
    *   Każda operacja jest dodawana do "Historii operacji".
    *   Użytkownik może zapisać przetworzony obraz klikając "Zapisz Obraz" i podając nazwę pliku.
    *   Informacje o przebiegu działania i ewentualnych błędach są logowane do pliku.
    *   Powiadomienia "toast" informują o statusie operacji.

## Wykorzystane technologie

*   **Java** OpenJDK 21
*   **JavaFX**: Biblioteka do tworzenia graficznych interfejsów użytkownika.
*   **FXML**: Język XML do deklaratywnego budowania interfejsu JavaFX.
*   **SwingFXUtils**: Do konwersji między obrazami JavaFX a `BufferedImage` (na potrzeby zapisu).
*   **ImageIO**: Do zapisu obrazów.
*   **Wielowątkowość (`java.util.concurrent.ExecutorService`)**: Do optymalizacji czasochłonnych operacji przetwarzania obrazów.
