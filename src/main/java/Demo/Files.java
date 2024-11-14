package Demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Files")
public class Files extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/jfs";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "sai@2002";
    private static final String INSERT_QUERY = "INSERT INTO user_details (website_name, password, relationship_type) VALUES (?, ?, ?)";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        String password = request.getParameter("password");
        String relationshipType = request.getParameter("relationship");
        String name = "bs." + websiteName + ".com";
        response.setContentType("text/html");

        try (Connection con = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
             PrintWriter p = response.getWriter()) {

            Class.forName("com.mysql.cj.jdbc.Driver");
            PreparedStatement preparedStatement = con.prepareStatement(INSERT_QUERY);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, relationshipType);

            int row = preparedStatement.executeUpdate();

            if (row > 0) {
                p.print("<html><body>");
                p.print("<h1>Your Relationship Was Created! Let's Chat and Make Memories</h1>");
                p.print("<a href='index.html'>Go to Home Page</a>");
                p.print("</body></html>");
            } else {
                p.print("<h3>Error saving details. Please try again.</h3>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
