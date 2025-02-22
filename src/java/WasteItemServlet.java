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

@WebServlet("/WasteItemServlet")
public class WasteItemServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String wasteType = request.getParameter("type");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String[] wasteDetails = getWasteDetails(wasteType);
        if (wasteDetails != null) {
            out.print(wasteDetails[0] + "|" + wasteDetails[1] + "|" + wasteDetails[2]);
        } else {
            out.print("No Data|No Description|No Image");
        }
    }

    private String[] getWasteDetails(String type) {
        String sql = "SELECT type, description, image FROM WasteItems WHERE type = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{rs.getString("type"), rs.getString("description"), rs.getString("image")};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
