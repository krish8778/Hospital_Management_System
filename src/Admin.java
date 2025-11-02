import java.sql.*;
import java.util.Scanner;

public class Admin {
    Connection connection;
    Scanner scan;

    public Admin(Connection connection, Scanner scan){
        this.connection = connection;
        this.scan =scan;
    }

    public void adminMenu(){
        int choice;
        do{
            System.out.println("👑 Admin Menu:");
            System.out.println("1. Add new user");
            System.out.println("2. Doctor's list");
            System.out.println("3. Remove a doctor");
            System.out.println("4. View all appointments");
            System.out.println("5. Logout");
            System.out.print("Enter your choice : ");

            while(!scan.hasNextInt()){
                System.out.print("Please enter a valid number : ");
                scan.next();
            }
            choice = scan.nextInt();
            scan.nextLine();
            switch (choice){
                case 1:
                    addUser();
                    break;
                case 2:
                    viewDoctor();
                    break;
                case 3:
                    removeDoctor();
                    break;
                case 4:
                    viewAppointment();
                    break;
                case 5 :
                    DbConnection.closeConnection(connection);
                    System.out.println("Thank you:)");
                    break;
                default :
                    System.out.println("❌ Invalid choice");
            }
        }while(choice != 5);
    }


    private void addUser() {
        System.out.println("Creating new user");
        System.out.println("-----------------");
        System.out.print("Enter the name : ");
        String name = scan.nextLine();
        System.out.print("Enter the username : ");
        String userName = scan.nextLine();
        System.out.print("Set the password : ");
        String password = scan.nextLine();
        System.out.print("Enter the role('ADMIN','RECEPTIONIST','DOCTOR') : ");
        String role = scan.nextLine().toUpperCase();
        String query = "INSERT INTO user(name,user_name,password,role) VALUES(?,?,?,?)";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,name);
            ps.setString(2,userName);
            ps.setString(3,password);
            ps.setString(4,role);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                if(role.equals("DOCTOR") || role.equals("3")){
                    try{
                    String queryForId = """
                            SELECT user_id
                            FROM user
                            WHERE user_name = ?;
                            """;
                        PreparedStatement ps2 = connection.prepareStatement(queryForId);
                        ps2.setString(1,userName);
                        ResultSet rs = ps2.executeQuery();
                        if(rs.next()) {
                            int userId = rs.getInt("user_id");
                            addDoctor(name, userId);
                        }
                        else{
                            System.out.println("❌ Failed  to add Doctor");
                        }
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("✅ User added successfully!");
                }
            }else {
                System.out.println("❌ Failed");
            }
        }catch(SQLIntegrityConstraintViolationException e){
            System.out.println("❌ User name is already taken");
//            String resetQuery = """
//                    ALTER TABLE user AUTO
//                    """
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    private void addDoctor(String name, int userId) {
        System.out.println("Doctor Details");
        System.out.println("--------------");
        System.out.print("Specialization : ");
        String special = scan.nextLine();
        System.out.print("Experience : ");
        int experience = scan.nextInt();
        scan.nextLine();
        System.out.print("Contact : ");
        String contact = scan.nextLine();
        String query = "INSERT INTO doctor(name,specialization,experience,contact,user_id) VALUES(?,?,?,?,?)";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,name);
            ps.setString(2,special.toUpperCase());
            ps.setInt(3,experience);
            ps.setString(4,contact);
            ps.setInt(5,userId);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                System.out.println("✅ Doctor added successfully!");
            }else {
                System.out.println("❌ Failed");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    private void viewDoctor() {
        String query = "SELECT * FROM doctor";
        System.out.println("Doctor's list");
        System.out.println("-------------");
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            System.out.printf("|%-10s|%-15s|%-15s|%-10s|%-10s|%n","Doctor id","Name","specialization","Experience","Contact");
            while (rs.next()){
                System.out.printf("|%-10s|%-15s|%-15s|%-10s|%-10s|%n",rs.getInt(1),rs.getString(2),
                        rs.getString(3),rs.getInt(4),rs.getString(5));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    private void removeDoctor() {
        System.out.println("Delete doctor data");
        System.out.println("-------------------");
        System.out.print("Doctor id : ");
        int id = scan.nextInt();
        scan.nextLine();
        String query = "DELETE FROM doctor WHERE doctor_id = ?";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                System.out.println("✅ Doctor removed successfully!");
            }else{
                System.out.println("❌ Failed");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    private void viewAppointment() {
        Appointment appointment = new Appointment(connection,scan);
        appointment.showAppointments();
    }
}
