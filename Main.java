import com.google.gson.Gson;

import java.sql.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Main {
    public static boolean addNewSpeaker(Connection confConn, String name,
                                        String email, String phone, String alternatePhone) throws SQLException {
        String sql = "INSERT INTO Speaker (Name, Email, PhoneNumber, AlternateNumber) VALUES ( ? ,  ? ,  ? ,  ? )";
        PreparedStatement stmt = confConn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, phone);
        stmt.setString(4, alternatePhone);
        int rs = stmt.executeUpdate();
        stmt.close();
        return rs > 0;
    }

    public static int getSpeakerID(Connection confConn, String name) throws SQLException {
        String sql = "SELECT SpeakerID FROM Speaker WHERE Name =  ? ";
        PreparedStatement stmt = confConn.prepareStatement(sql);
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("SpeakerID");
            rs.close();
            return id;
        }
        rs.close();
        return -1;
    }

    public static boolean createSession(Connection nonConfConn,
                                        String SpeakerID, LocalDate date, String sessionTitle) throws SQLException {
        String sql = "INSERT INTO Session (SpeakerID, SessionTime, SessionTitle) VALUES ( ? ,  ? ,  ? )";
        PreparedStatement stmt = nonConfConn.prepareStatement(sql);
        stmt.setString(1, SpeakerID);
        stmt.setDate(2, Date.valueOf(date));
        stmt.setString(3, sessionTitle);
        int rs = stmt.executeUpdate();
        stmt.close();
        return rs > 0;
    }

    public static boolean removeSpeaker(MyPojo gdc, String name) throws SQLException, ClassNotFoundException {
        // get all tables from gdc (global database catalog) where speakerID is used
        String url = "jdbc:mysql://" + gdc.getDatabases().get("Event_management_confidential").getServerURL() + ":3306/Event_management_confidential";
        System.out.println(url);
        Connection conn = DriverManager.getConnection(url, "5408", "3T%MA?4q9^6aR?ak");
        int speakerID = getSpeakerID(conn, name);
        conn.close();
        if (speakerID == -1) {
            return false;
        }

        for (Map.Entry<String, MyPojo.Database> entry : gdc.getDatabases().entrySet()) {
            MyPojo.Database db = entry.getValue();
            url = "jdbc:mysql://" + db.getServerURL() + ":3306/" + entry.getKey();
            conn = DriverManager.getConnection(url, "5408", "3T%MA?4q9^6aR?ak");
            for (Map.Entry<String, List<String>> table : db.getTables().entrySet()) {
                List<String> columns = table.getValue();
                for (String column : columns) {
                    if (column.equals("SpeakerID")) {
                        String sql = "DELETE FROM " + table.getKey() + " WHERE SpeakerID =  ? ";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, speakerID);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                }
            }
            conn.close();
        }
        return true;
    }

    public static void main(String[] args) {
        String url1 = "jdbc:mysql://{Ipaddress}/Event_management";
        String url2 = "jdbc:mysql://{Ipaddress}/Event_management_confidential";

        String user = "{user}";
        String password = "{password}";

        try {
            // Load and register the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open a connection
            Connection conn1 = DriverManager.getConnection(url1, user, password);
            Connection conn2 = DriverManager.getConnection(url2, user, password);

            // Execute a query
            Statement stmt2 = conn2.createStatement();
            String sql = "SELECT FileContent FROM GDC WHERE Id=1";
            ResultSet rs2 = stmt2.executeQuery(sql);

            // Extract data from result set
            MyPojo gdc = null;
            if(rs2.next()){
                String gdc_json_64 = rs2.getString("FileContent");
//                System.out.println("FileContent from Event_management_confidential: " + gdc_json_64);

                byte[] decodedBytes = Base64.getDecoder().decode(gdc_json_64);
                String jsonStr = new String(decodedBytes);

                Gson gson = new Gson();

                gdc = gson.fromJson(jsonStr, MyPojo.class);
            }

            /* This represent a business logic that is implemented in the application
                * and it is using the data from the database to perform some operation
                * in this case, it is adding a new speaker and creating a session for the speaker
            * */
            try {
                addNewSpeaker(conn2, "John Doe", "abc@mail.com", "1234567890", "0987654321");
                int speakerID = getSpeakerID(conn2, "John Doe");
                createSession(conn1, Integer.toString(speakerID), LocalDate.of(2021, 10, 10), "Introduction to Java");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            /* This represent a business logic that is implemented in the application
                * and it is using the data from the database to perform some operation
                * in this case, it is removing a speaker from all the databases
                * And it is using the data from the global database catalog to perform the operation
                * It removes all the records from all the tables where the speakerID is used
            * */
            try {
                System.out.println(removeSpeaker(gdc, "John Doe"));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }


            // Clean-up environment
            rs2.close();
            stmt2.close();
            conn1.close();
            conn2.close();
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }
}