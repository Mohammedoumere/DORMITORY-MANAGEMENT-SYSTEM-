package com.dormitory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static HikariDataSource dataSource;
    
    public static void connect(String username, String password) throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/dormitory_db");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(5000);
            
            dataSource = new HikariDataSource(config);
            
            try (Connection conn = dataSource.getConnection()) {
                // Success
            }
        } catch (Exception e) {
            if (dataSource != null) {
                dataSource.close();
            }
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection is not initialized.");
        }
        return dataSource.getConnection();
    }
    
    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}