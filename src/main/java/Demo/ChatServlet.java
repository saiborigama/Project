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

            // Check if websiteName is null or empty
            if (websiteName == null || websiteName.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Website name is missing.");
                return;
            }

            response.setContentType("text/html");
            PrintWriter p = response.getWriter();

            p.print("<html><head><style>");
            p.print("body { font-family: 'Arial', sans-serif; background-color: #f7f7f7; padding: 30px; }");
            p.print(".chat-container { max-width: 600px; margin: 0 auto; background-color: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }");
            p.print("h1 { color: #4CAF50; font-size: 28px; margin-bottom: 20px; text-align: center; }");
            p.print("#messages { border: 1px solid #ddd; background-color: #f1f1f1; border-radius: 8px; padding: 15px; max-height: 400px; overflow-y: auto; margin-bottom: 20px; }");
            p.print(".message { margin-bottom: 15px; padding: 10px; background-color: #e9e9e9; border-radius: 8px; }");
            p.print(".message img { max-width: 200px; margin-top: 10px; border-radius: 8px; }");
            p.print(".location-link { color: #4CAF50; text-decoration: none; }");
            p.print(".location-link:hover { text-decoration: underline; }");
            p.print("input[type='text'], input[type='file'] { width: 100%; padding: 10px; margin-top: 10px; border: 1px solid #ccc; border-radius: 8px; box-sizing: border-box; }");
            p.print("button { background-color: #4CAF50; color: white; padding: 12px 25px; border: none; border-radius: 5px; cursor: pointer; width: 100%; margin-top: 15px; font-size: 16px; }");
            p.print("button:hover { background-color: #45a049; }");
            p.print(".location-button { background-color: #2196F3; }");
            p.print(".location-button:hover { background-color: #0b7dda; }");
            p.print("</style></head><body>");

            p.print("<div class='chat-container'>");
            p.print("<h1>Chat Room</h1>");
            p.print("<div id='messages'>");

            // SQL query to get messages from the database
            String getMessagesSQL = "SELECT msg, image_path, location, type FROM msg WHERE websaitename = ?";
            PreparedStatement getMessagesStmt = conn.prepareStatement(getMessagesSQL);
            getMessagesStmt.setString(1, websiteName);
            ResultSet rs = getMessagesStmt.executeQuery();

            if (!rs.next()) {
                p.print("<p>No messages found for the website: " + websiteName + "</p>");
            }

            // Process and display messages
            do {
                String message = rs.getString("msg");
                String imagePath = rs.getString("image_path");
                String location = rs.getString("location");
                String type = rs.getString("type");

                p.print("<div class='message'><p>" + message + "</p>");

                // Show image if it exists and type is 'image'
                if ("image".equals(type) && imagePath != null) {
                    p.print("<img src='" + imagePath + "' alt='Image'/>");
                }

                // Show location link if it exists and type is 'location'
                if ("location".equals(type) && location != null) {
                    p.print("<p><a class='location-link' href='" + location + "' target='_blank'>View Location</a></p>");
                }

                p.print("</div>");
            } while (rs.next());

            p.print("</div>");

            // Chat input form
            p.print("<form method='post' action='ChatServlet' enctype='multipart/form-data'>");
            p.print("<input type='hidden' name='websiteName' value='" + websiteName + "'>");
            p.print("<input type='text' name='newMessage' placeholder='Type your message' required>");
            p.print("<input type='file' name='image'>");
            p.print("<input type='hidden' id='latitude' name='latitude'>");
            p.print("<input type='hidden' id='longitude' name='longitude'>");
            p.print("<button type='submit'>Send Message</button>");
            p.print("<button type='button' class='location-button' onclick='getLocation()'>Share Location</button>");
            p.print("</form>");

            // JavaScript for capturing location
            p.print("<script>");
            p.print("function getLocation() {");
            p.print("  if (navigator.geolocation) {");
            p.print("    navigator.geolocation.getCurrentPosition(function(position) {");
            p.print("      document.getElementById('latitude').value = position.coords.latitude;");
            p.print("      document.getElementById('longitude').value = position.coords.longitude;");
            p.print("    }, function() { alert('Geolocation failed.'); });");
            p.print("  } else { alert('Geolocation is not supported by this browser.'); }");
            p.print("}");
            p.print("</script>");

            p.print("</div>");
            p.print("</body></html>");
        } catch (Exception e) {
            e.printStackTrace();  // Print stack trace for debugging
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading messages.");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String websiteName = request.getParameter("websiteName");
        String newMessage = request.getParameter("newMessage");
        String latitudeStr = request.getParameter("latitude");
        String longitudeStr = request.getParameter("longitude");
        Part imagePart = request.getPart("image");

        // Check if uploads directory exists, if not create it
        String uploadDir = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jfs", "root", "sai@2002")) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String insertSQL = "INSERT INTO msg (websaitename, msg, image_path, location, type) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertSQL);
            stmt.setString(1, websiteName);
            stmt.setString(2, newMessage);

            // Store image if it was uploaded
            String imagePath = null;
            if (imagePart != null && imagePart.getSize() > 0) {
                String fileName = imagePart.getSubmittedFileName();
                String filePath = uploadDir + File.separator + fileName; // Save the file to the uploads directory
                imagePart.write(filePath);
                imagePath = "uploads/" + fileName; // Save the relative path to the database
            }

            stmt.setString(3, imagePath);

            // Add location if available
            String location = null;
            if (latitudeStr != null && longitudeStr != null && !latitudeStr.isEmpty() && !longitudeStr.isEmpty()) {
                location = "https://maps.google.com/?q=" + latitudeStr + "," + longitudeStr;
            }

            stmt.setString(4, location);

            // Set message type (text, image, location)
            String type = "text";
            if (imagePath != null) {
                type = "image";
            } else if (location != null) {
                type = "location";
            }
            stmt.setString(5, type);

            stmt.executeUpdate();

            // Redirect back to the chat page after posting the message
            response.sendRedirect("ChatServlet?websiteName=" + websiteName);
        } catch (Exception e) {
            e.printStackTrace();  // Print stack trace for debugging
            response.getWriter().println("<html><body><h3>Error: Unable to save the message.</h3></body></html>");
        }
    }
}
