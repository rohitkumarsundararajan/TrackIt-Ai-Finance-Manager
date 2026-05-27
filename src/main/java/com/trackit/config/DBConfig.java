package com.trackit.config;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

/**
 * Resolves database configuration from an external config.properties file.
 * <p>
 * Priority:
 * <ol>
 *     <li>Use config.properties values if present.</li>
 *     <li>If file is absent, use environment variables.</li>
 *     <li>If values are still missing, prompt the user for only missing fields.</li>
 * </ol>
 * Values are kept in memory only for the current runtime.
 * </p>
 */
public final class DBConfig {

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String PROPERTY_URL = "database.url";
    private static final String PROPERTY_USER = "database.username";
    private static final String PROPERTY_PASSWORD = "database.password";
    private static final String ENV_URL = "FINANCE_DB_URL";
    private static final String ENV_USER = "FINANCE_DB_USER";
    private static final String ENV_PASSWORD = "FINANCE_DB_PASSWORD";

    private static volatile DBDetails cachedDetails;

    private DBConfig() {
    }

    /**
     * Returns DB details resolved from config.properties, environment variables, or prompt fallback.
     *
     * @return immutable DB details
     */
    public static DBDetails resolve() {
        DBDetails existing = cachedDetails;
        if (existing != null) {
            return existing;
        }

        synchronized (DBConfig.class) {
            if (cachedDetails != null) {
                return cachedDetails;
            }

            DBDetails resolved = loadFromConfigFile();
            if (resolved == null) {
                resolved = loadFromEnvironment();
            }

            if (resolved == null) {
                System.out.println("Database configuration was not found in config.properties or environment variables.");
                System.out.println("Please enter missing database details:");
                Console console = System.console();
                String url = trimToNull(System.getenv(ENV_URL));
                String user = trimToNull(System.getenv(ENV_USER));
                String password = trimToNull(System.getenv(ENV_PASSWORD));

                if (console != null) {
                    url = url == null ? promptNonBlank(console, "Database URL: ") : url;
                    user = user == null ? promptNonBlank(console, "Database username: ") : user;
                    password = password == null ? promptPasswordNonBlank(console, "Database password: ") : password;
                } else {
                    Scanner scanner = new Scanner(System.in);
                    url = url == null ? promptNonBlank(scanner, "Database URL: ") : url;
                    user = user == null ? promptNonBlank(scanner, "Database username: ") : user;
                    password = password == null ? promptNonBlank(scanner, "Database password (input visible): ") : password;
                }
                resolved = new DBDetails(url, user, password);
            }

            cachedDetails = resolved;
            return cachedDetails;
        }
    }

    private static DBDetails loadFromConfigFile() {
        File configFile = new File(System.getProperty("user.dir"), CONFIG_FILE_NAME);
        if (!configFile.isFile()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFile);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load " + CONFIG_FILE_NAME + ": " + ex.getMessage(), ex);
        }

        String url = trimToNull(props.getProperty(PROPERTY_URL));
        String user = trimToNull(props.getProperty(PROPERTY_USER));
        String password = trimToNull(props.getProperty(PROPERTY_PASSWORD));

        if (url == null || user == null || password == null) {
            throw new IllegalArgumentException("config.properties must contain "
                    + PROPERTY_URL + ", "
                    + PROPERTY_USER + ", and "
                    + PROPERTY_PASSWORD + " values.");
        }

        return new DBDetails(url, user, password);
    }

    private static DBDetails loadFromEnvironment() {
        String url = trimToNull(System.getenv(ENV_URL));
        String user = trimToNull(System.getenv(ENV_USER));
        String password = trimToNull(System.getenv(ENV_PASSWORD));
        if (url == null || user == null || password == null) {
            return null;
        }
        return new DBDetails(url, user, password);
    }

    private static String promptNonBlank(Console console, String prompt) {
        while (true) {
            String value = trimToNull(console.readLine(prompt));
            if (value != null) {
                return value;
            }
            System.out.println("Value cannot be empty. Please try again.");
        }
    }

    private static String promptPasswordNonBlank(Console console, String prompt) {
        while (true) {
            char[] chars = console.readPassword(prompt);
            if (chars == null) {
                System.out.println("Password input failed. Please try again.");
                continue;
            }
            String value = trimToNull(new String(chars));
            if (value != null) {
                return value;
            }
            System.out.println("Value cannot be empty. Please try again.");
        }
    }

    private static String promptNonBlank(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = trimToNull(scanner.nextLine());
            if (value != null) {
                return value;
            }
            System.out.println("Value cannot be empty. Please try again.");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * In-memory immutable DB credentials holder.
     */
    public static final class DBDetails {
        private final String url;
        private final String username;
        private final String password;

        public DBDetails(String url, String username, String password) {
            this.url = requireNonBlank(url, "Database URL");
            this.username = requireNonBlank(username, "Database username");
            this.password = requireNonBlank(password, "Database password");
        }

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        private static String requireNonBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " must not be empty.");
            }
            return value.trim();
        }
    }
}
