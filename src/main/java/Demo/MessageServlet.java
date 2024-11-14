package Demo;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/MessageServlet")
public class MessageServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jfs";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        String message = request.getParameter("message");

        if (websiteName != null && message != null) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO chats (websiteName, message) VALUES (?, ?)")) {
                
                stmt.setString(1, websiteName);
                stmt.setString(2, message);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        List<Map<String, String>> messages = new ArrayList<>();

        if (websiteName != null) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("SELECT message, timestamp FROM chats WHERE websiteName = ? ORDER BY timestamp")) {
                
                stmt.setString(1, websiteName);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> msg = new HashMap<>();
                    msg.put("message", rs.getString("message"));
                    msg.put("timestamp", rs.getString("timestamp"));
                    messages.add(msg);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(new Gson().toJson(messages));
    }
}
