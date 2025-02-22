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
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/DisposalLogServlet")
public class DisposalLogServlet extends HttpServlet {

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
        JSONArray disposalLogs = getDisposalLogs(username);

        try (PrintWriter out = response.getWriter()) {
            out.print(disposalLogs);
            out.flush();
        }
    }

    private JSONArray getDisposalLogs(String username) {
        JSONArray logs = new JSONArray();
        String sql = "SELECT waste_item, disposal_item, disposal_event, time_stamp FROM DISPOSAL_LOG WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject logEntry = new JSONObject();
                    logEntry.put("wasteItem", rs.getString("waste_item"));
                    logEntry.put("disposalItem", rs.getString("disposal_item"));
                    logEntry.put("disposalEvent", rs.getString("disposal_event"));
                    logEntry.put("timeStamp", rs.getString("time_stamp"));
                    logs.put(logEntry);
                }
            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", "Database error: " + e.getMessage());
            logs.put(error);
        }
        return logs;
    }
}
