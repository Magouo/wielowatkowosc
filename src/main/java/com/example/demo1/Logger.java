package com.example.demo1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

enum LogLevel {
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR"),
    DEBUG("DEBUG");

    private final String name;

    LogLevel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

public class Logger {
    private final File logFile;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Logger() {
        // Ustalenie ścieżki do pliku
        String userHome = System.getProperty("user.home");
        File logDir = new File(userHome, "ImageProcessorLogs");

        // Utworzenie katalogu jeśli nie istnieje
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        logFile = new File(logDir, "image_processor.log");
    }

    public void log(LogLevel level, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(dateFormatter);
            writer.println(timestamp + " [" + level + "] " + message);
        } catch (IOException e) {
            System.err.println("Nie można zapisać do pliku logów: " + e.getMessage());
        }
    }
}
