import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Doctor {
    Connection connection;
    Scanner scan;
    int doctorId;
    Appointment appointment;


    public Doctor(Connection connection, Scanner scan, String userName){
        this.connection = connection;
        this.scan = scan;
        try{
            String query = """
                    SELECT
                        doctor_id
                    FROM
                        doctor
                    JOIN
                        user on doctor.user_id = user.user_id
                    WHERE
                        user.user_name = ?;
                    """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,userName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.doctorId = rs.getInt(1);
            }
            else{
                System.out.println("❌Doctor not found");
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        this.appointment = new Appointment(connection,scan);
    }

    public void doctorMenu(){
        int choice;
        do{
            System.out.println("===== Doctor Menu =====");
            System.out.println("1. View Patients");
            System.out.println("2. View Schedule");
            System.out.println("3. Cancel Schedule");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            while(!scan.hasNextInt()){
                System.out.print("Please enter a valid number : ");
                scan.next();
            }
            choice = scan.nextInt();
            scan.nextLine();
            switch(choice){
                case 1 :
                    appointment.viewPatient();
                    break;
                case 2 :
                    appointment.showDoctorAppointment(doctorId);
                    break;
                case 3 :
                    appointment.cancelAppointment();
                    break;
                case 4 :
                    DbConnection.closeConnection(connection);
                    System.out.println("Thank you:)");
                    break;
                default :
                    System.out.println("❌ Invalid choice");
            }
        }while(choice != 4);
    }
}
