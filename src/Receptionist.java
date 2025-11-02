import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.sql.ResultSet;

public class Receptionist {
    Connection connection;
    Scanner scan;
    Appointment appointment;

    public Receptionist(Connection connection, Scanner scan){
        this.connection = connection;
        this.scan = scan;
        appointment = new Appointment(connection, scan);
    }

    public void receptionistMenu(){
        int choice;

        do {
            System.out.println("\uD83C\uDFE5Receptionist Menu:");
            System.out.println("1. Add Patient");
            System.out.println("2. View Patients");
            System.out.println("3. View Doctors");
            System.out.println("4. Book Appointment");
            System.out.println("5. View All Appointments");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            while(!scan.hasNextInt()){
                System.out.print("Please enter a valid number : ");
                scan.next();
            }
            choice = scan.nextInt();
            scan.nextLine();
            switch(choice){
                case 1:
                    addPatient();
                    break;
                case 2:
                    appointment.viewPatient();
                    break;
                case 3:
                    viewDoctor();
                    break;
                case 4:
                    appointment.book();
                    break;
                case 5:
                    appointment.showAppointments();
                    break;
                case 6:
                    DbConnection.closeConnection(connection);
                    System.out.println("Thank you:)");
                    break;
                default :
                    System.out.println("❌ Invalid choice");
            }
        }while(choice != 6);
    }

    private void addPatient() {
        System.out.println("Adding new patients");
        System.out.println("-------------------");
        System.out.print("Patient name : ");
        String name = scan.nextLine();
        System.out.print("Age : ");
        int age = scan.nextInt();
        scan.nextLine();
        System.out.print("Gender ('M','F','O') : ");
        String gender = scan.nextLine();
        System.out.print("Contact : ");
        String contact = scan.nextLine();

        String query = "INSERT INTO patient(name,age,gender,contact) VALUES(?,?,?,?)";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,name);
            ps.setInt(2,age);
            ps.setString(3,gender);
            ps.setString(4,contact);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                System.out.println("✅ Patient added successfully!");
            }else{
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
}
