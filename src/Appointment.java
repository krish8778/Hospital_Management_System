import java.sql.*;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Appointment {
    Connection connection;
    Scanner scan;

    public Appointment(Connection connection, Scanner scan){
        this.connection = connection;
        this.scan = scan;
    }

    public void book(){
        System.out.println("Booking appointment");
        System.out.println("-------------------");
        System.out.print("Patient id : ");
        int patientId = scan.nextInt();
        if(!isPatientAvailable(patientId)){
            System.out.println("❌ Patient ID is invalid");
            return;
        }
        System.out.print("Doctor id : ");
        int doctorId = scan.nextInt();
        scan.nextLine();
        if(!isDoctorAvailable(doctorId)){
            System.out.println("❌ Doctor ID is invalid");
            return;
        }
        System.out.print("Appointment date(YYYY-MM-DD) : ");
        String inputDate = scan.nextLine();
        LocalDate appointmentDate = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.print("Appointment time(HH:MM) : ");
        String inputTime = scan.nextLine();
        LocalTime appointmentTime = LocalTime.parse(inputTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime appointmentStart = LocalDateTime.of(appointmentDate,appointmentTime);

        if(!isWithInWorkingHours(appointmentTime)){
            System.out.println("❌ Appointment not allowed! Doctor is available only between :");
            System.out.println("   Morning: 09:00–13:00  |  Evening: 19:00–23:00");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if(appointmentStart.isBefore(now)){
            System.out.println("❌ Invalid! Appointment time is in the past.");
            return;
        }

        String checkQuery = """
                SELECT * FROM appointment
                WHERE doctor_id = ?
                AND (
                    (appointment_date_time <= ?
                    AND DATE_ADD(appointment_date_time, INTERVAL duration MINUTE) > ?)
                    )""";
        try{
            PreparedStatement ps = connection.prepareStatement(checkQuery);
            ps.setInt(1,doctorId);
            ps.setTimestamp(2, Timestamp.valueOf(appointmentStart));
            ps.setTimestamp(3,Timestamp.valueOf(appointmentStart));
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                System.out.println("❌ Appointment booking failed");
                System.out.println("Doctor is already booked during this time. Please choose another slot.");
            }else {
                String query = """
                        INSERT INTO
                        appointment(patient_id,doctor_id,appointment_date_time)
                        VALUES(?,?,?)
                        """;
                ps = connection.prepareStatement(query);
                ps.setInt(1,patientId);
                ps.setInt(2,doctorId);
                ps.setTimestamp(3,Timestamp.valueOf(appointmentStart));
                int affectedRows = ps.executeUpdate();
                if(affectedRows > 0){
                    LocalDateTime appointmentEnd = appointmentStart.plusMinutes(30);
                    LocalTime appointmentEndTime = appointmentEnd.toLocalTime();
                    System.out.println("✅ Appointment booked successfully");
                    System.out.println("-------------------------------------------");
                    System.out.println("Date : " + appointmentDate);
                    System.out.println("From : " + appointmentTime + " | To : " + appointmentEndTime);
                    System.out.println("-------------------------------------------");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private boolean isDoctorAvailable(int doctorId) {
        String query = """
                SELECT name, specialization,experience FROM doctor
                WHERE doctor_id = ?
                """;
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Doctor Details");
                System.out.println("-------------------------------------------");
                System.out.println("Name           : " + rs.getString("name"));
                System.out.println("Specialization : " + rs.getString("specialization"));
                System.out.println("Experience     : " + rs.getString("experience"));
                System.out.println("-------------------------------------------");
                return true;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean isPatientAvailable(int patientId) {
        String query = """
                SELECT name, age,gender FROM patient
                WHERE patient_id = ?
                """;
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,patientId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Patient Details");
                System.out.println("-------------------------------------------");
                System.out.println("Name   : " + rs.getString("name"));
                System.out.println("Age    : " + rs.getInt("age"));
                System.out.println("Gender : " + rs.getString("gender"));
                System.out.println("-------------------------------------------");
                return true;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean isWithInWorkingHours(LocalTime time){
        LocalTime morningStart = LocalTime.of(9,0);
        LocalTime morningEnd = LocalTime.of(13,0);
        LocalTime eveningStart = LocalTime.of(19,0);
        LocalTime eveningEnd = LocalTime.of(23,0);

        return (time.equals(morningStart) || (time.isAfter(morningStart) && time.isBefore(morningEnd))) ||
                (time.equals(eveningStart) || (time.isAfter(eveningStart) && time.isBefore(eveningEnd)));
    }

    public void showAppointments(){
        checkStatus();
        System.out.println("Appointments list");
        System.out.println("-----------------");
        String query = """
                SELECT
                    appointment.id,
                    patient.name,
                    doctor.name,
                    doctor.specialization,
                    appointment.`status`,
                    appointment.duration,
                    DATE(appointment.appointment_date_time) AS date,
                    TIME(appointment.appointment_date_time) AS time
                FROM appointment
                JOIN patient ON appointment.patient_id = patient.patient_id
                JOIN doctor ON appointment.doctor_id = doctor.doctor_id
                ORDER BY appointment.appointment_date_time ASC;
                """;
        try{
            PreparedStatement ps =connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            System.out.printf("|%-3s|%-12s|%-12s|%-10s|%-10s|%-11s|%-11s|%-8s|","Id","Patient Name","Doctor Name",
                    "Speciality","Status","Date","Time","Duration");
            System.out.println();
            while(rs.next()){
                System.out.printf("|%-3s|%-12s|%-12s|%-10s|%-10s|%-11s|%-11s|%-8s|",rs.getInt(1),rs.getString(2),
                        rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(7),rs.getString(8),rs.getString(6));
                System.out.println();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void showDoctorAppointment(int doctorId){
        checkStatus();
        System.out.println("Appointments list by Doctor");
        System.out.println("---------------------------");
        String query = """
                SELECT
                    appointment.id,
                    patient.name,
                    doctor.name,
                    doctor.specialization,
                    appointment.`status`,
                    appointment.duration,
                    DATE(appointment.appointment_date_time) AS date,
                    TIME(appointment.appointment_date_time) AS time
                FROM appointment
                JOIN patient ON appointment.patient_id = patient.patient_id
                JOIN doctor ON appointment.doctor_id = doctor.doctor_id
                Where doctor.doctor_id = ?
                    AND DATE(appointment.appointment_date_time) >= CURDATE()
                    AND appointment.appointment_date_time > NOW()
                ORDER BY appointment.appointment_date_time ASC;
                """;
        try{
            PreparedStatement ps =connection.prepareStatement(query);
            ps.setInt(1,doctorId);
            ResultSet rs = ps.executeQuery();
            System.out.printf("|%-3s|%-12s|%-12s|%-10s|%-10s|%-11s|%-11s|%-8s|","Id","Patient Name","Doctor Name",
                    "Speciality","Status","Date","Time","Duration");
            System.out.println();
            while(rs.next()){
                System.out.printf("|%-3d|%-12s|%-12s|%-10s|%-10s|%-11s|%-11s|%-8s|",rs.getInt(1),rs.getString(2),
                        rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(7),rs.getString(8),rs.getString(6));
                System.out.println();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void viewPatient() {
        String query = "SELECT * FROM patient";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            System.out.printf("|%-10s|%-15s|%-5s|%-6s|%-10s|%n","Patient id","Name","Age","Gender","Contact");
            while(rs.next()){
                System.out.printf("|%-10s|%-15s|%-5s|%-6s|%-10s|%n",rs.getInt(1),rs.getString(2),
                        rs.getInt(3),rs.getString(4),rs.getString(5));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void checkStatus(){
        String query = """
                UPDATE appointment
                    SET `status` = 'COMPLETED'
                WHERE
                    DATE_ADD(appointment_date_time,INTERVAL 30 MINUTE) <= NOW()
                    AND
                    `status` <> 'CANCELLED';
                """;
        try{
            PreparedStatement ps= connection.prepareStatement(query);
            ps.executeUpdate();
            System.out.println("successfull");
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void cancelAppointment(){
        System.out.println("Cancelling appointment");
        System.out.println("---------------------");
        System.out.print("Appointment ID : ");
        int Id = scan.nextInt();
        scan.nextLine();
        String query = """
                UPDATE appointment
                    SET `status` = 'CANCELLED'
                WHERE
                    id = ?
                AND
                    appointment_date_time > NOW()
                AND
                    `status` <> 'CANCELLED';
                """;
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,Id);
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                System.out.println("✅ Appointment ID \"" + Id + "\" has been cancelled successfully.");
            }
            else{
                System.out.println("⚠️ No appointment found with ID " + Id + ".");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}
