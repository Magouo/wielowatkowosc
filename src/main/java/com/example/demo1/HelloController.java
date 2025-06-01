package com.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.stage.StageStyle;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

public class HelloController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView processedImageView;
    @FXML
    private ComboBox<String> operationComboBox;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Button rotateLeftButton;
    @FXML
    private Button rotateRightButton;
    @FXML
    private Button scaleButton;
    @FXML
    private ListView<String> historyListView;

    private Image originalImage;
    private Image processedImage;
    private ExecutorService executorService;
    private Logger logger;
    private ObservableList<String> operationHistory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inicjalizacja loggera
        logger = new Logger();
        logger.log(LogLevel.INFO, "Aplikacja została uruchomiona");

        // Inicjalizacja wątków do przetwarzania obrazów
        executorService = Executors.newFixedThreadPool(4);

        // Inicjalizacja listy operacji
        operationComboBox.getItems().addAll(
                "Brak operacji",
                "Negatyw",
                "Progowanie",
                "Konturowanie"
        );
        operationComboBox.setValue(null);

        // Inicjalizacja historii operacji
        historyListView.setItems(operationHistory);

        // logo
        try {
            Image logo = new Image(getClass().getResourceAsStream("logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            logger.log(LogLevel.ERROR, "Nie można załadować logo: " + e.getMessage());
            System.err.println("Nie można załadować logo: " + e.getMessage());
        }

        // przycisk wymaga załadowanego obrazu
        updateButtonsState(false);

        Platform.runLater(() -> {
            originalImageView.getScene().getWindow().setOnCloseRequest(event -> {
                logger.log(LogLevel.INFO, "Aplikacja została zamknięta");
                executorService.shutdown();
            });
        });
    }

    private void updateButtonsState(boolean enabled) {
        rotateLeftButton.setDisable(!enabled);
        rotateRightButton.setDisable(!enabled);
        scaleButton.setDisable(!enabled);
    }

    public void onLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Obrazy JPG", "*.jpg"));
        File file = chooser.showOpenDialog(null);
        if (file != null && file.getName().endsWith(".jpg")) {
            try {
                originalImage = new Image(file.toURI().toString());
                originalImageView.setImage(originalImage);
                processedImage = originalImage; // Początkowa kopia
                processedImageView.setImage(processedImage);
                showToast("Pomyślnie załadowano plik");
                logger.log(LogLevel.INFO, "Załadowano obraz: " + file.getAbsolutePath());

                // Dodanie wpisu do historii
                addToHistory("Załadowano obraz: " + file.getName());

                // Aktywacja przycisków po załadowaniu obrazu
                updateButtonsState(true);
            } catch (Exception e) {
                showToast("Nie udało się załadować pliku");
                logger.log(LogLevel.ERROR, "Błąd ładowania obrazu: " + e.getMessage());
            }
        } else if (file != null) {
            showToast("Niedozwolony format pliku");
            logger.log(LogLevel.WARNING, "Próba załadowania pliku w niedozwolonym formacie");
        }
    }

    public void onExecute() {
        if (originalImage == null) {
            showToast("Najpierw wczytaj obraz");
            return;
        }

        String selectedOperation = operationComboBox.getValue();
        if (selectedOperation == null) {
            showToast("Nie wybrano operacji do wykonania");
            return;
        }

        switch (selectedOperation) {
            case "Brak operacji":
                processedImage = originalImage;
                processedImageView.setImage(processedImage);
                addToHistory("Przywrócono oryginalny obraz");
                logger.log(LogLevel.INFO, "Przywrócono oryginalny obraz");
                break;
            case "Negatyw":
                applyNegative();
                break;
            case "Progowanie":
                showThresholdDialog();
                break;
            case "Konturowanie":
                applyContour();
                break;
            default:
                showToast("Nieznana operacja");
                logger.log(LogLevel.WARNING, "Próba wykonania nieznanej operacji: " + selectedOperation);
                break;
        }
    }

    // Obrót obrazu w lewo
    public void onRotateLeft() {
        if (processedImage == null) return;

        PixelReader pixelReader = processedImage.getPixelReader();
        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();

        WritableImage rotatedImage = new WritableImage(height, width);
        PixelWriter pixelWriter = rotatedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(y, width - 1 - x, color);
            }
        }

        processedImage = rotatedImage;
        processedImageView.setImage(processedImage);
        showToast("Obraz został obrócony w lewo");

        // Dodanie do historii i logu
        addToHistory("Obrót obrazu w lewo");
        logger.log(LogLevel.INFO, "Wykonano obrót obrazu w lewo");
    }

    // Obrót obrazu w prawo
    public void onRotateRight() {
        if (processedImage == null) return;

        PixelReader pixelReader = processedImage.getPixelReader();
        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();

        WritableImage rotatedImage = new WritableImage(height, width);
        PixelWriter pixelWriter = rotatedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(height - 1 - y, x, color);
            }
        }

        processedImage = rotatedImage;
        processedImageView.setImage(processedImage);
        showToast("Obraz został obrócony w prawo");

        // Dodanie do historii i logu
        addToHistory("Obrót obrazu w prawo");
        logger.log(LogLevel.INFO, "Wykonano obrót obrazu w prawo");
    }

    // Skalowanie obrazu
    public void onScale() {
        if (processedImage == null) return;

        // Tworzenie dialogu z polami do wprowadzenia nowych wymiarów
        Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");
        dialog.setHeaderText("Podaj nowe wymiary obrazu");

        // Przyciski
        ButtonType scaleButtonType = new ButtonType("Zmień rozmiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scaleButtonType, ButtonType.CANCEL);

        // Pola dla szerokości i wysokości
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField widthField = new TextField();
        widthField.setPromptText("Szerokość");
        widthField.setText(String.valueOf((int) processedImage.getWidth()));

        TextField heightField = new TextField();
        heightField.setPromptText("Wysokość");
        heightField.setText(String.valueOf((int) processedImage.getHeight()));

        grid.add(new Label("Szerokość (px):"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Wysokość (px):"), 0, 1);
        grid.add(heightField, 1, 1);

        // Przycisk przywrócenia oryginalnych wymiarów
        Button resetButton = new Button("Przywróć oryginalne wymiary");
        resetButton.setOnAction(e -> {
            widthField.setText(String.valueOf((int) originalImage.getWidth()));
            heightField.setText(String.valueOf((int) originalImage.getHeight()));
        });
        grid.add(resetButton, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Ustawienie focusu na pierwszym polu
        widthField.requestFocus();

        // Walidacja wprowadzonych danych
        dialog.getDialogPane().lookupButton(scaleButtonType).setDisable(true);

        // Listener do walidacji
        widthField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDimensions(widthField, heightField, dialog, scaleButtonType);
        });

        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDimensions(widthField, heightField, dialog, scaleButtonType);
        });

        // Konwersja wyniku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scaleButtonType) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    return new Pair<>(width, height);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<Integer, Integer>> result = dialog.showAndWait();

        result.ifPresent(dimensions -> {
            int width = dimensions.getKey();
            int height = dimensions.getValue();

            // Tworzenie przeskalowanego obrazu
            ImageView tempView = new ImageView(processedImage);
            tempView.setFitWidth(width);
            tempView.setFitHeight(height);
            tempView.setPreserveRatio(false);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            WritableImage scaledImage = tempView.snapshot(params, null);

            processedImage = scaledImage;
            processedImageView.setImage(processedImage);

            String message = "Skalowanie obrazu do " + width + "x" + height + " pikseli";
            showToast(message);
            addToHistory(message);
            logger.log(LogLevel.INFO, message);
        });
    }

    private void validateDimensions(TextField widthField, TextField heightField, Dialog<?> dialog, ButtonType scaleButtonType) {
        String widthText = widthField.getText();
        String heightText = heightField.getText();

        boolean valid = true;

        // Usunięcie poprzednich stylów błędów
        widthField.setStyle("");
        heightField.setStyle("");

        try {
            int width = Integer.parseInt(widthText);
            if (width <= 0 || width > 3000) {
                widthField.setStyle("-fx-border-color: red;");
                valid = false;
            }
        } catch (NumberFormatException e) {
            widthField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        try {
            int height = Integer.parseInt(heightText);
            if (height <= 0 || height > 3000) {
                heightField.setStyle("-fx-border-color: red;");
                valid = false;
            }
        } catch (NumberFormatException e) {
            heightField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        dialog.getDialogPane().lookupButton(scaleButtonType).setDisable(!valid);
    }

    // Negatyw obrazu z wielowątkowością
    private void applyNegative() {
        if (processedImage == null) return;

        logger.log(LogLevel.INFO, "Rozpoczęto generowanie negatywu");

        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();

        WritableImage negativeImage = new WritableImage(width, height);

        // Podział obrazu na części dla wątków
        int processors = Math.min(4, Runtime.getRuntime().availableProcessors());
        int rowsPerThread = height / processors;

        List<Future<?>> futures = new ArrayList<>();

        for (int threadId = 0; threadId < processors; threadId++) {
            final int startRow = threadId * rowsPerThread;
            final int endRow = (threadId == processors - 1) ? height : (threadId + 1) * rowsPerThread;

            futures.add(executorService.submit(() -> {
                processNegativeChunk(processedImage, negativeImage, startRow, endRow, width);
            }));
        }

        // Czekanie na koniec wszystkich wątków
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.log(LogLevel.ERROR, "Błąd podczas generowania negatywu: " + e.getMessage());
                showToast("Błąd podczas generowania negatywu");
                return;
            }
        }

        processedImage = negativeImage;
        processedImageView.setImage(processedImage);

        showToast("Negatyw został wygenerowany pomyślnie!");
        addToHistory("Wygenerowano negatyw obrazu");
        logger.log(LogLevel.INFO, "Zakończono generowanie negatywu");
    }

    private void processNegativeChunk(Image source, WritableImage target, int startRow, int endRow, int width) {
        PixelReader pixelReader = source.getPixelReader();
        PixelWriter pixelWriter = target.getPixelWriter();

        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                Color negativeColor = Color.rgb(
                        255 - (int) (color.getRed() * 255),
                        255 - (int) (color.getGreen() * 255),
                        255 - (int) (color.getBlue() * 255)
                );
                pixelWriter.setColor(x, y, negativeColor);
            }
        }
    }

    // Dialog do progu
    private void showThresholdDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Progowanie");
        dialog.setHeaderText("Podaj wartość progu (0-255)");

        // Przyciski
        ButtonType applyButtonType = new ButtonType("Wykonaj progowanie", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        // Pole dla rogu
        Slider thresholdSlider = new Slider(0, 255, 128);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(50);
        thresholdSlider.setMinorTickCount(5);
        thresholdSlider.setBlockIncrement(10);

        Label thresholdValue = new Label("Próg: 128");
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            thresholdValue.setText("Próg: " + newVal.intValue());
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(thresholdSlider, 0, 0);
        grid.add(thresholdValue, 0, 1);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                return (int) thresholdSlider.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(threshold -> {
            applyThreshold(threshold);
        });
    }

    // Progowanie obrazu z wielowątkowością
    private void applyThreshold(int threshold) {
        if (processedImage == null) return;

        logger.log(LogLevel.INFO, "Rozpoczęto progowanie z wartością progu: " + threshold);

        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();

        WritableImage thresholdedImage = new WritableImage(width, height);

        // Podział obrazu na części dla wątków
        int processors = Math.min(4, Runtime.getRuntime().availableProcessors());
        int rowsPerThread = height / processors;

        List<Future<?>> futures = new ArrayList<>();

        for (int threadId = 0; threadId < processors; threadId++) {
            final int startRow = threadId * rowsPerThread;
            final int endRow = (threadId == processors - 1) ? height : (threadId + 1) * rowsPerThread;

            futures.add(executorService.submit(() -> {
                processThresholdChunk(processedImage, thresholdedImage, startRow, endRow, width, threshold);
            }));
        }

        // Czekanie na koniec wszystkich wątków
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.log(LogLevel.ERROR, "Błąd podczas progowania: " + e.getMessage());
                showToast("Błąd podczas progowania");
                return;
            }
        }

        processedImage = thresholdedImage;
        processedImageView.setImage(processedImage);

        showToast("Progowanie zostało przeprowadzone pomyślnie!");
        addToHistory("Progowanie obrazu z progiem: " + threshold);
        logger.log(LogLevel.INFO, "Zakończono progowanie z wartością progu: " + threshold);
    }

    private void processThresholdChunk(Image source, WritableImage target, int startRow, int endRow, int width, int threshold) {
        PixelReader pixelReader = source.getPixelReader();
        PixelWriter pixelWriter = target.getPixelWriter();

        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                // Konwersja na odcienie szarości i progowanie
                double gray = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                Color newColor = gray * 255 < threshold ? Color.BLACK : Color.WHITE;
                pixelWriter.setColor(x, y, newColor);
            }
        }
    }

    // Konturowanie obrazu z wielowątkowością
    private void applyContour() {
        if (processedImage == null) return;

        logger.log(LogLevel.INFO, "Rozpoczęto konturowanie obrazu");

        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();

        double[][] grayImage = new double[height][width];

        // Podział obrazu na części dla różnych wątków
        int processors = Math.min(4, Runtime.getRuntime().availableProcessors());
        int rowsPerThread = height / processors;

        List<Future<?>> futures = new ArrayList<>();

        // Konwersja na odcienie szarości
        for (int threadId = 0; threadId < processors; threadId++) {
            final int startRow = threadId * rowsPerThread;
            final int endRow = (threadId == processors - 1) ? height : (threadId + 1) * rowsPerThread;

            futures.add(executorService.submit(() -> {
                PixelReader pixelReader = processedImage.getPixelReader();
                for (int y = startRow; y < endRow; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        grayImage[y][x] = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                    }
                }
            }));
        }

        // Czekamy na koniec konwersji
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.log(LogLevel.ERROR, "Błąd podczas konwersji na odcienie szarości: " + e.getMessage());
                showToast("Błąd podczas konturowania");
                return;
            }
        }

        futures.clear();

        // konturowanie
        WritableImage contouredImage = new WritableImage(width, height);

        for (int threadId = 0; threadId < processors; threadId++) {
            final int startRow = threadId * rowsPerThread;
            final int endRow = (threadId == processors - 1) ? height : (threadId + 1) * rowsPerThread;

            futures.add(executorService.submit(() -> {
                processContourChunk(contouredImage, grayImage, startRow, endRow, width, height);
            }));
        }

        // Czekamy na koniec konturowania
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.log(LogLevel.ERROR, "Błąd podczas konturowania: " + e.getMessage());
                showToast("Błąd podczas konturowania");
                return;
            }
        }

        processedImage = contouredImage;
        processedImageView.setImage(processedImage);

        showToast("Konturowanie zostało przeprowadzone pomyślnie!");
        addToHistory("Wykonano konturowanie obrazu");
        logger.log(LogLevel.INFO, "Zakończono konturowanie obrazu");
    }

    private void processContourChunk(WritableImage target, double[][] grayImage, int startRow, int endRow, int width, int height) {
        PixelWriter pixelWriter = target.getPixelWriter();

        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < width; x++) {
                // Dla krawędzi obrazu - ustaw czarny
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    pixelWriter.setColor(x, y, Color.BLACK);
                    continue;
                }

                // Obliczanie gradientu
                double gx = grayImage[y][x + 1] - grayImage[y][x - 1];
                double gy = grayImage[y + 1][x] - grayImage[y - 1][x];
                double gradient = Math.sqrt(gx * gx + gy * gy);

                // Normalizacja i ustawienie koloru
                double normalizedGradient = Math.min(1.0, gradient);
                Color newColor = Color.gray(1.0 - normalizedGradient);
                pixelWriter.setColor(x, y, newColor);
            }
        }
    }

    public void onSaveImage() {
        if (processedImageView.getImage() == null) {
            showToast("Na pliku nie zostały wykonane żadne operacje!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapis obrazu");
        dialog.setHeaderText("Podaj nazwę pliku:");
        dialog.setContentText("Nazwa:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.length() < 3 || name.length() > 100) {
                showToast("Wpisz co najmniej 3 znaki");
                logger.log(LogLevel.WARNING, "Próba zapisu z nieprawidłową nazwą pliku (zbyt krótka/długa)");
                return;
            }
            File output = new File(System.getProperty("user.home") + "/Pictures/" + name + ".jpg");
            if (output.exists()) {
                showToast("Plik " + name + ".jpg już istnieje w systemie.");
                logger.log(LogLevel.WARNING, "Plik o podanej nazwie już istnieje: " + output.getAbsolutePath());
            } else {
                boolean saved = saveImage(processedImageView.getImage(), output);
                if (saved) {
                    String message = "Zapisano obraz w pliku " + name + ".jpg";
                    showToast(message);
                    addToHistory(message);
                    logger.log(LogLevel.INFO, "Zapisano obraz: " + output.getAbsolutePath());
                } else {
                    showToast("Nie udało się zapisać pliku " + name + ".jpg");
                    logger.log(LogLevel.ERROR, "Błąd zapisu obrazu: " + output.getAbsolutePath());
                }
            }
        });
    }
    // Wyświetlenie okna
    private void showToast(String message) {
        Stage toastStage = new Stage();
        toastStage.initOwner(originalImageView.getScene().getWindow());
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        Label toastLabel = new Label(message);
        toastLabel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.8); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: 12px;"
        );

        StackPane root = new StackPane(toastLabel);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // prawy dolny rog
        Stage ownerStage = (Stage) originalImageView.getScene().getWindow();
        toastStage.setX(ownerStage.getX() + ownerStage.getWidth() - 250);
        toastStage.setY(ownerStage.getY() + ownerStage.getHeight() - 100);

        toastStage.show();

        // zamknięcie po 1.5 sekundzie
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(1.5),
                e -> toastStage.close()
        ));
        timeline.play();
    }

    // Dodanie operacji do historii
    private void addToHistory(String operation) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = now.format(formatter);

        operationHistory.add(0, timestamp + " - " + operation);
    }

    // Zapis

    private boolean saveImage(Image image, File file) {
        try {
            // Wymuszenie formatu RGB
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);

            SwingFXUtils.fromFXImage(image, bufferedImage);

            return ImageIO.write(bufferedImage, "jpg", file);

        } catch (IOException e) {
            logger.log(LogLevel.ERROR, "Błąd zapisu obrazu: " + e.getMessage());
            return false;
        }
    }
}
