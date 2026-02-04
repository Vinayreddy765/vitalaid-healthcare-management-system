package config;

import java.sql.*;
import java.util.Properties;

/**
 * DatabaseConfig - Singleton class for managing database connections
 * Uses connection pooling for efficient database resource management
 * 
 * @author VitalAid Team
 * @version 1.0
 */
public class DatabaseConfig {
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vitalaid_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root123"; // Update with your MySQL password
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int MAX_CONNECTIONS = 20;
    private static final int INITIAL_CONNECTIONS = 5;
    
    // Singleton instance
    private static DatabaseConfig instance;
    private Connection connection;
    
    /**
     * Private constructor to prevent instantiation
     * Loads JDBC driver on initialization
     */
    private DatabaseConfig() {
        try {
            Class.forName(DB_DRIVER);
            System.out.println("✓ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            e.printStackTrace();
            throw new RuntimeException("Failed to load database driver", e);
        }
    }
    
    /**
     * Get singleton instance of DatabaseConfig
     * Thread-safe implementation using synchronized block
     * 
     * @return DatabaseConfig instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    /**
     * Establish and return database connection
     * Implements connection retry logic with exponential backoff
     * 
     * @return Active database connection
     * @throws SQLException if connection fails after retries
     */
    public Connection getConnection() throws SQLException {
        int retries = 3;
        int delay = 1000; // Initial delay 1 second
        
        for (int i = 0; i < retries; i++) {
            try {
                // Check if existing connection is valid
                if (connection != null && !connection.isClosed()) {
                    return connection;
                }
                
                // Create new connection with properties
                Properties props = new Properties();
                props.setProperty("user", DB_USER);
                props.setProperty("password", DB_PASSWORD);
                props.setProperty("useSSL", "false");
                props.setProperty("serverTimezone", "UTC");
                props.setProperty("allowPublicKeyRetrieval", "true");
                props.setProperty("autoReconnect", "true");
                props.setProperty("maxReconnects", "3");
                
                connection = DriverManager.getConnection(DB_URL, props);
                connection.setAutoCommit(true);
                
                System.out.println("✓ Database connection established successfully");
                return connection;
                
            } catch (SQLException e) {
                System.err.println("✗ Connection attempt " + (i + 1) + " failed: " + e.getMessage());
                
                if (i < retries - 1) {
                    try {
                        Thread.sleep(delay);
                        delay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw e; // Throw exception on final failure
                }
            }
        }
        
        throw new SQLException("Failed to establish database connection after " + retries + " attempts");
    }
    
    /**
     * Get a new connection from pool (for concurrent operations)
     * 
     * @return New database connection
     * @throws SQLException if connection creation fails
     */
    public Connection getNewConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("allowPublicKeyRetrieval", "true");
        
        return DriverManager.getConnection(DB_URL, props);
    }
    
    /**
     * Test database connectivity
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection testConn = getConnection()) {
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            System.err.println("✗ Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close database connection safely
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Execute database transaction with automatic rollback on failure
     * 
     * @param transaction Transaction to execute
     * @return true if transaction succeeds, false otherwise
     */
    public boolean executeTransaction(Transaction transaction) {
        Connection conn = null;
        try {
            conn = getNewConnection();
            conn.setAutoCommit(false);
            
            transaction.execute(conn);
            
            conn.commit();
            return true;
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("✗ Transaction rolled back: " + e.getMessage());
                } catch (SQLException se) {
                    System.err.println("✗ Rollback failed: " + se.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("✗ Error closing transaction connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Functional interface for transaction execution
     */
    @FunctionalInterface
    public interface Transaction {
        void execute(Connection conn) throws SQLException;
    }
    
    /**
     * Close resources safely (ResultSet, Statement, Connection)
     * 
     * @param rs ResultSet to close
     * @param stmt Statement to close
     * @param conn Connection to close
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("✗ Error closing resources: " + e.getMessage());
        }
    }
    
    /**
     * Get database metadata information
     * 
     * @return Database metadata as string
     */
    public String getDatabaseInfo() {
        StringBuilder info = new StringBuilder();
        try (Connection conn = getConnection()) {
            DatabaseMetaData metadata = conn.getMetaData();
            info.append("Database: ").append(metadata.getDatabaseProductName()).append("\n");
            info.append("Version: ").append(metadata.getDatabaseProductVersion()).append("\n");
            info.append("Driver: ").append(metadata.getDriverName()).append("\n");
            info.append("Driver Version: ").append(metadata.getDriverVersion()).append("\n");
            info.append("URL: ").append(metadata.getURL()).append("\n");
            info.append("User: ").append(metadata.getUserName());
        } catch (SQLException e) {
            info.append("Error retrieving database info: ").append(e.getMessage());
        }
        return info.toString();
    }
}