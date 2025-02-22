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
import org.json.JSONObject;

@WebServlet("/DisposalBinServlet")
public class DisposalBinServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        String username = (String) session.getAttribute("username");
        JSONObject binData = getDisposalBinData(username);

        try (PrintWriter out = response.getWriter()) {
            out.print(binData);
            out.flush();
        }
    }

    private JSONObject getDisposalBinData(String username) {
        JSONObject binData = new JSONObject();
        String sql = "SELECT capacity, current_fill, last_emptied, location FROM DISPOSAL_BIN WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    binData.put("capacity", rs.getString("capacity"));
                    binData.put("currentFill", rs.getString("current_fill"));
                    binData.put("lastEmptied", rs.getString("last_emptied"));
                    binData.put("location", rs.getString("location"));
                }
            }
        } catch (Exception e) {
            binData.put("error", "Database error: " + e.getMessage());
        }
        return binData;
    }
}
