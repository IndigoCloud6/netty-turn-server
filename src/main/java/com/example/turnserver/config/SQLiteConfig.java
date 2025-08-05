package com.example.turnserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SQLite database configuration
 * Ensures the data directory exists and handles SQLite-specific settings
 */
@Configuration
public class SQLiteConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLiteConfig.class);
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    
    @PostConstruct
    public void initializeDatabase() {
        try {
            logger.info("Initializing SQLite database configuration...");
            
            // Extract database file path from JDBC URL
            if (databaseUrl != null && databaseUrl.startsWith("jdbc:sqlite:")) {
                String dbPath = databaseUrl.substring("jdbc:sqlite:".length());
                // Remove any additional parameters from the path
                if (dbPath.contains("?")) {
                    dbPath = dbPath.substring(0, dbPath.indexOf("?"));
                }
                
                Path dbFilePath = Paths.get(dbPath);
                Path dbDirectory = dbFilePath.getParent();
                
                // Ensure the directory exists
                if (dbDirectory != null) {
                    if (!Files.exists(dbDirectory)) {
                        Files.createDirectories(dbDirectory);
                        logger.info("Created database directory: {}", dbDirectory.toAbsolutePath());
                    } else {
                        logger.info("Database directory already exists: {}", dbDirectory.toAbsolutePath());
                    }
                }
                
                logger.info("SQLite database will be stored at: {}", dbFilePath.toAbsolutePath());
                
                // Set SQLite-specific system properties for better performance
                System.setProperty("sqlite.purejava", "false");
                System.setProperty("sqlite.tmpdir", System.getProperty("java.io.tmpdir"));
                
                // Check if database file exists
                if (Files.exists(dbFilePath)) {
                    logger.info("Using existing SQLite database file");
                } else {
                    logger.info("SQLite database file will be created on first connection");
                }
                
            } else {
                throw new IllegalArgumentException("Invalid SQLite database URL: " + databaseUrl);
            }
            
            logger.info("SQLite database configuration completed successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing SQLite database configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }
}