import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendRedirect("login.html?error=Missing username or password");
            return;
        }

        // Hash the password before checking
        String hashedPassword = hashPassword(password);

        if (checkLogin(username, hashedPassword)) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            response.sendRedirect("user.html");
        } else {
            response.sendRedirect("login.html?error=Invalid username or password");
        }
    }

    private boolean checkLogin(String username, String hashedPassword) {
        boolean isValid = false;
        String sql = "SELECT username FROM USERDB WHERE username = ? AND password = ?";

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement statement = conn.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, hashedPassword);

                try (ResultSet resultSet = statement.executeQuery()) {
                    isValid = resultSet.next();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database error: ", e);
        }

        return isValid;
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
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            return null;
        }
    }
}
