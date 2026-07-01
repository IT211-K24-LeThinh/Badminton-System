package com.re.badmintonsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BadmintonSystemApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(BadmintonSystemApplication.class, args);
    }

    private static void loadDotenv() {
        Path envFile = null;
        String[] searchPaths = {".env", "../.env", "Backend/.env"};
        for (String p : searchPaths) {
            Path candidate = Paths.get(p);
            if (Files.exists(candidate)) {
                envFile = candidate;
                break;
            }
        }
        if (envFile == null) return;

        try {
            for (String line : Files.readAllLines(envFile)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim().replaceAll("^[\"']|[\"']$", "");
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
