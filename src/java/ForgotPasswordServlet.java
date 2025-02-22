import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        if ("sendCode".equals(action)) {
            String email = request.getParameter("email");
            HttpSession session = request.getSession();

            if (isEmailRegistered(email)) {
                int verificationCode = generateVerificationCode();
                session.setAttribute("verificationCode", verificationCode);
                session.setAttribute("email", email);
                
                // Simulate email sending (Replace this with actual email API)
                System.out.println("Verification code sent to email: " + verificationCode);

                out.print("CodeSent");
            } else {
                out.print("EmailNotFound");
            }

        } else if ("verifyCode".equals(action)) {
            int inputCode = Integer.parseInt(request.getParameter("code"));
            HttpSession session = request.getSession();
            int storedCode = (int) session.getAttribute("verificationCode");

            if (inputCode == storedCode) {
                out.print("CodeVerified");
            } else {
                out.print("InvalidCode");
            }

        } else if ("resetPassword".equals(action)) {
            String newPassword = request.getParameter("newPassword");
            HttpSession session = request.getSession();
            String email = (String) session.getAttribute("email");

            if (updatePassword(email, newPassword)) {
                out.print("PasswordUpdated");
            } else {
                out.print("ErrorUpdatingPassword");
            }
        }
    }

    private boolean isEmailRegistered(String email) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE USERS SET password = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int generateVerificationCode() {
        Random random = new Random();
        return 1000 + random.nextInt(9000); // Generates a 4-digit code
    }
}
