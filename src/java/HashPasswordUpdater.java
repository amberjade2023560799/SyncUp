import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class HashPasswordUpdater {
    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    public static void main(String[] args) {
        try {
            // Load Derby database driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            
            // Connect to the database
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            
            // Query to get all users and their plaintext passwords
            String selectQuery = "SELECT username, password FROM USERDB";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String plainPassword = rs.getString("password"); // Assuming stored in plaintext
                
                // Convert plaintext password to SHA-256 hash
                String hashedPassword = hashPassword(plainPassword);
                
                // Update the password with the hashed version
                String updateQuery = "UPDATE USERDB SET password = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, username);
                updateStmt.executeUpdate();
                updateStmt.close();

                System.out.println("Updated password for user: " + username);
            }

            // Close resources
            rs.close();
            selectStmt.close();
            conn.close();
            
            System.out.println("Password update completed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
