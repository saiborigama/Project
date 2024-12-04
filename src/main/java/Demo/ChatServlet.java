package Demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/ChatServlet")
@MultipartConfig(maxFileSize = 16177215) // Limit upload size to ~16MB
public class ChatServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jfs", "root", "sai@2002")) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (websiteName == null || websiteName.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Website name is missing.");
                return;
            }

            response.setContentType("text/html");
            PrintWriter p = response.getWriter();

            // Start HTML
            p.print("<html><head><style>");
            p.print("body { font-family: 'Arial', sans-serif; background-color: #f2f2f2; padding: 20px; }");
            p.print(".chat-container { max-width: 600px; margin: auto; background: #ffffff; border-radius: 10px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); padding: 20px; }");
            p.print("h1 { text-align: center; color: #4CAF50; }");
            p.print("#messages { max-height: 400px; overflow-y: auto; padding: 15px; border: 1px solid #ddd; background: #fafafa; border-radius: 10px; margin-bottom: 20px; }");
            p.print(".message { margin-bottom: 15px; padding: 10px; border-radius: 8px; background: #e6e6e6; }");
            p.print(".message img { max-width: 100%; border-radius: 8px; margin-top: 10px; }");
            p.print(".message .timestamp { font-size: 12px; color: #555; margin-top: 5px; }");
            p.print(".location-link { color: #007BFF; text-decoration: none; }");
            p.print(".location-link:hover { text-decoration: underline; }");
            p.print("input[type='text'], input[type='file'] { width: calc(100% - 20px); padding: 10px; margin: 10px 10px 20px 0; border: 1px solid #ccc; border-radius: 5px; }");
            p.print("button { background: #4CAF50; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }");
            p.print("button:hover { background: #45a049; }");
            p.print(".location-button { background: #2196F3; margin-top: 10px; }");
            p.print(".location-button:hover { background: #0b7dda; }");
            p.print("</style></head><body>");

            p.print("<div class='chat-container'>");
            p.print("<h1>"+websiteName+" Let's chat, share laughs, and create memories that last a lifetime!</h1>");
            p.print("<div id='messages'>");

            // Retrieve messages from the database
            String getMessagesSQL = "SELECT msg, image_path, location, type, timestamp FROM msg WHERE websaitename = ? ORDER BY timestamp ASC";
            PreparedStatement getMessagesStmt = conn.prepareStatement(getMessagesSQL);
            getMessagesStmt.setString(1, websiteName);
            ResultSet rs = getMessagesStmt.executeQuery();

            if (!rs.next()) {
                p.print("<p>No messages found for this website.</p>");
            }

            // Display messages
            do {
                String message = rs.getString("msg");
                String imagePath = rs.getString("image_path");
                String location = rs.getString("location");
                String type = rs.getString("type");
                java.sql.Timestamp timestamp = rs.getTimestamp("timestamp");

                String formattedTime = new java.text.SimpleDateFormat("HH:mm, MMM dd").format(timestamp);

                p.print("<div class='message'>");
                p.print("<p>" + message + "</p>");

                if ("image".equals(type) && imagePath != null) {
                    p.print("<img src='" + imagePath + "' alt='Image' />");
                }

                if ("location".equals(type) && location != null) {
                    p.print("<p><a class='location-link' href='" + location + "' target='_blank'>View Location</a></p>");
                }

                p.print("<p class='timestamp'>" + formattedTime + "</p>");
                p.print("</div>");
            } while (rs.next());

            p.print("</div>");

            // Chat form
            p.print("<form method='post' action='ChatServlet' enctype='multipart/form-data'>");
            p.print("<input type='hidden' name='websiteName' value='" + websiteName + "'>");
            p.print("<input type='text' name='newMessage' placeholder='Type your message' >");
            p.print("<input type='file' name='image'>");
            p.print("<input type='hidden' id='latitude' name='latitude'>");
            p.print("<input type='hidden' id='longitude' name='longitude'>");
            p.print("<button type='submit'>Send</button>");
            p.print("<button type='button' class='location-button' onclick='getLocation()'>Share Location</button>");
            p.print("</form>");

            // JavaScript for location sharing
            p.print("<script>");
            p.print("function getLocation() {");
            p.print("  if (navigator.geolocation) {");
            p.print("    navigator.geolocation.getCurrentPosition(function(position) {");
            p.print("      document.getElementById('latitude').value = position.coords.latitude;");
            p.print("      document.getElementById('longitude').value = position.coords.longitude;");
            p.print("    }, function() { alert('Unable to retrieve location.'); });");
            p.print("  } else { alert('Geolocation is not supported by your browser.'); }");
            p.print("}");
            p.print("</script>");

            p.print("</div>");
            p.print("</body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading messages.");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        String newMessage = request.getParameter("newMessage");
        String latitudeStr = request.getParameter("latitude");
        String longitudeStr = request.getParameter("longitude");
        Part imagePart = request.getPart("image");

        String uploadDir = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jfs", "root", "sai@2002")) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String insertSQL = "INSERT INTO msg (websaitename, msg, image_path, location, type, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(insertSQL);
            stmt.setString(1, websiteName);
            stmt.setString(2, newMessage);

            String imagePath = null;
            if (imagePart != null && imagePart.getSize() > 0) {
                String fileName = imagePart.getSubmittedFileName();
                String filePath = uploadDir + File.separator + fileName;
                imagePart.write(filePath);
                imagePath = "uploads/" + fileName;
            }
            stmt.setString(3, imagePath);

            String location = null;
            if (latitudeStr != null && longitudeStr != null && !latitudeStr.isEmpty() && !longitudeStr.isEmpty()) {
                location = "https://maps.google.com/?q=" + latitudeStr + "," + longitudeStr;
            }
            stmt.setString(4, location);

            String type = "text";
            if (imagePath != null) type = "image";
            else if (location != null) type = "location";

            stmt.setString(5, type);

            stmt.executeUpdate();

            response.sendRedirect("ChatServlet?websiteName=" + websiteName);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<html><body><h3>Error: Unable to save the message.</h3></body></html>");
        }
    }
}
