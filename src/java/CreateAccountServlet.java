import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/CreateAccountServlet")
public class CreateAccountServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        if (name == null || password == null || email == null || 
            name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            response.sendRedirect("create_account.html?error=All fields are required");
            return;
        }

        // Hash the password before storing
        String hashedPassword = hashPassword(password);

        // Insert into the database
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO USERDB (name, password, email, point, created) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, hashedPassword);
                statement.setString(3, email);
                statement.setInt(4, 0); // Default point = 0
                statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now())); // Current date-time

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("User registered successfully!");
                    response.sendRedirect("login.html?success=Account created");
                } else {
                    System.out.println("User registration failed.");
                    response.sendRedirect("create_account.html?error=Registration failed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("create_account.html?error=Database error");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
