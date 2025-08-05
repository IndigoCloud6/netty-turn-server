package com.example.turnserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database initialization service that runs on application startup
 * Validates database schema and logs initialization status
 */
@Service
@Order(1)
public class DatabaseInitializationService implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializationService.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting database initialization check...");
        
        try (Connection connection = dataSource.getConnection()) {
            validateDatabaseSchema(connection);
            logDatabaseInfo(connection);
            validateInitialData(connection);
            logger.info("Database initialization completed successfully");
        } catch (SQLException e) {
            logger.error("Database initialization failed - SQL Error: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        } catch (Exception e) {
            logger.error("Database initialization failed - Unexpected Error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validateDatabaseSchema(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        
        // Check if required tables exist
        String[] requiredTables = {"users", "turn_sessions", "allocations"};
        
        for (String tableName : requiredTables) {
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    logger.info("Table '{}' exists", tableName);
                } else {
                    logger.warn("Table '{}' not found", tableName);
                }
            }
        }
        
        // Check if indexes exist
        checkIndexExists(metaData, "users", "idx_username");
        checkIndexExists(metaData, "turn_sessions", "idx_session_id");
        checkIndexExists(metaData, "allocations", "idx_allocation_id");
    }
    
    private void checkIndexExists(DatabaseMetaData metaData, String tableName, String indexName) throws SQLException {
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            boolean indexFound = false;
            while (rs.next()) {
                String idxName = rs.getString("INDEX_NAME");
                if (indexName.equals(idxName)) {
                    indexFound = true;
                    break;
                }
            }
            if (indexFound) {
                logger.info("Index '{}' exists on table '{}'", indexName, tableName);
            } else {
                logger.warn("Index '{}' not found on table '{}'", indexName, tableName);
            }
        }
    }
    
    private void logDatabaseInfo(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        logger.info("Database: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
        logger.info("Driver: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
        logger.info("Database URL: {}", connection.getMetaData().getURL());
    }
    
    private void validateInitialData(Connection connection) throws SQLException {
        // Check if initial admin user exists
        try (var stmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            stmt.setString(1, "admin");
            try (var rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.info("Default admin user exists in database");
                } else {
                    logger.warn("Default admin user not found - this may be expected if custom initialization is used");
                }
            }
        }
        
        // Check total user count
        try (var stmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userCount = rs.getInt(1);
                    logger.info("Total users in database: {}", userCount);
                }
            }
        }
    }
}