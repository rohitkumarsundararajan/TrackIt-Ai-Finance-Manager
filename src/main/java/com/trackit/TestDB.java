package com.trackit;

import com.trackit.config.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Minimal JDBC connectivity test utility.
 * <p>
 * This class loads database credentials from config.properties (or env vars as fallback).
 * </p>
 */
public final class TestDB {

    private TestDB() {
    }

    public static void main(String[] args) {
        // Force driver loading explicitly for debug clarity.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC driver loaded.");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found.");
            e.printStackTrace();
            return;
        }

        DBConfig.DBDetails config;
        try {
            config = DBConfig.resolve();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid database configuration: " + e.getMessage());
            return;
        }

        System.out.println("Trying URL: " + config.getUrl());
        System.out.println("Using USER: " + config.getUsername());

        try (Connection connection = DriverManager.getConnection(
                config.getUrl(), config.getUsername(), config.getPassword())) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("DB CONNECTED SUCCESSFULLY");
            } else {
                System.out.println("Connection object returned but appears closed.");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed for URL: " + config.getUrl());
            e.printStackTrace();
        }
    }
}
