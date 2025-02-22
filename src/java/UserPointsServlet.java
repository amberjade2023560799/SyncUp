import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/UserPointsServlet")
public class UserPointsServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.html"); // Redirect if not logged in
            return;
        }

        int userId = (int) session.getAttribute("user_id");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String pointsHistory = getPointsHistory(userId);
        String redeemHistory = getRedeemHistory(userId);
        String rewards = getRewards(userId);

        out.print(pointsHistory + "|" + redeemHistory + "|" + rewards);
    }

    private String getPointsHistory(int userId) {
        String sql = "SELECT SUM(points) AS total_points FROM UserPoints WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("total_points");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Data";
    }

    private String getRedeemHistory(int userId) {
        String sql = "SELECT COUNT(*) AS total_redeems FROM RedeemHistory WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("total_redeems");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Data";
    }

    private String getRewards(int userId) {
        String sql = "SELECT reward_name FROM Rewards WHERE user_id = ?";
        StringBuilder rewardsList = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rewardsList.append(rs.getString("reward_name")).append(", ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rewardsList.length() > 0 ? rewardsList.toString() : "No Rewards";
    }
}
