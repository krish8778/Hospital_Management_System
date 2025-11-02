import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.PreparedStatement;

public class HospitalManagementSystem {

    public static void main(String[] args) throws SQLException {
        Connection connection = DbConnection.getConnection();
        if(connection == null){
            System.out.println("Unable to connect database :(");
            System.out.println("try again!!!");
            System.exit(0);
        }

        Scanner scan = new Scanner(System.in);

        for(int i = 1; i <= 3;i++) {
            System.out.println("Hospital Management System");
            System.out.println("--------------------------");
            System.out.println("          Login:)         ");
            System.out.print("User name : ");
            String userName = scan.nextLine();
            System.out.print("Password  : ");
            String password = scan.nextLine();
            String query = "SELECT name, role FROM user WHERE user_name = ? AND password = ?";
            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, userName);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name").toUpperCase();
                    String role = rs.getString("role").toLowerCase();

                    System.out.println("✅ Login successful!\nWelcome " + name +",\nRole : " + role.toUpperCase());
                    switch (role) {
                        case "admin":
                            Admin admin = new Admin(connection, scan);
                            admin.adminMenu();
                            scan.close();
                            System.exit(0);
                        case "doctor":
                            Doctor doctor = new Doctor(connection,scan,userName);
                            doctor.doctorMenu();
                            scan.close();
                            System.exit(0);
                        case "receptionist":
                            Receptionist receptionist = new Receptionist(connection,scan);
                            receptionist.receptionistMenu();
                            scan.close();
                            System.exit(0);
                    }
                } else {
                    System.out.println("❌ Invalid username or password.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
