package org.oplearn.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class OpLearnApplication {

  public static void main(String[] args) {
    loadDotEnv();
    SpringApplication.run(OpLearnApplication.class, args);
  }

  private static void loadDotEnv() {
    try {
      Path envPath = Paths.get(".env");
      if (Files.exists(envPath)) {
        List<String> lines = Files.readAllLines(envPath);
        for (String line : lines) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) {
            continue;
          }
          int equalsIndex = line.indexOf('=');
          if (equalsIndex > 0) {
            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1).trim();
            if (System.getProperty(key) == null && System.getenv(key) == null) {
              System.setProperty(key, value);
            }
          }
        }
      }
    } catch (Exception ignored) {
    }
  }

}
