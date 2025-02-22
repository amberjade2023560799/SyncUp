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

@WebServlet("/HubServlet")
public class HubServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:derby://localhost:1527/EcoDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String disposalBinInfo = getDisposalBinInfo();
        String kioskInfo = getKioskInfo();

        out.print(disposalBinInfo + "|" + kioskInfo); // Send data as a formatted string
    }

    private String getDisposalBinInfo() {
        String sql = "SELECT location FROM DisposalBins ORDER BY id DESC FETCH FIRST 1 ROW ONLY";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("location");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Data";
    }

    private String getKioskInfo() {
        String sql = "SELECT name FROM Kiosks ORDER BY id DESC FETCH FIRST 1 ROW ONLY";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Data";
    }
}
