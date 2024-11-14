package Demo;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Website")
public class Website extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/jfs";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "sai@2002";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        String password = request.getParameter("password");
        PrintWriter p=response.getWriter();
        p.print(websiteName);
        try 
        {
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            String query = "SELECT password FROM user_details WHERE website_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, websiteName);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        response.sendRedirect("chat.html");
                    } else {
                        // Display error if password is incorrect
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();
                        out.println("<html><body><p>Incorrect password. Please try again.</p></body></html>");
                    }
                } else {
                    // Display error if website name does not exist
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<html><body><p>Website name not found. Please try again.</p></body></html>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }
}
