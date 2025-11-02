import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection{

    private static final String url = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String dbName = "root";
    private static final String dbPassword = "root";

    public static Connection getConnection(){
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, dbName, dbPassword);
            System.out.println("✅ Database connected successfully!");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection connection){
        try{
            connection.close();
            System.out.println("✅ Database disconnected successfully!");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}
