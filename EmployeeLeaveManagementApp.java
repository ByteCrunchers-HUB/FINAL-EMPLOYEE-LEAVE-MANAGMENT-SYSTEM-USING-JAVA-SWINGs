
/*
   EMPLOYEE LEAVE MANAGEMENT SYSTEM
    GUI BASED (SWING)
    DB: MYSQL
    AUTHOR: TEAM 15(CSE ADITYA UNIVERSITY)
    FOR RUN: javac -cp ".;lib/mysql-connector-java-8.0.30.jar" EmployeeLeaveManagementApp.java
*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EmployeeLeaveManagementApp extends JFrame {

    static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    static final String DB_URL = "jdbc:mysql://localhost:3306/EMPLOYEE_LEAVE_DB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "root";

    public static void main(String[] args) {
        initDB();
        SwingUtilities.invokeLater(() -> new EmployeeLeaveManagementApp().setVisible(true));
    }

    public EmployeeLeaveManagementApp() {
        setTitle("Employee Leave Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        showHome();
    }

    void showHome() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 20, 20));

        JButton adminBtn = new JButton("Admin Login");
        JButton empBtn = new JButton("Employee Login");
        JButton exitBtn = new JButton("Exit");

        adminBtn.addActionListener(e -> adminLogin());
        empBtn.addActionListener(e -> employeeLogin());
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(adminBtn);
        panel.add(empBtn);
        panel.add(exitBtn);

        setContentPane(panel);
        revalidate();
    }

    void adminLogin() {
        String user = JOptionPane.showInputDialog(this, "Admin Username:");
        String pass = JOptionPane.showInputDialog(this, "Admin Password:");

        if ("admin".equals(user) && "admin".equals(pass)) {
            showAdminMenu();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Credentials");
        }
    }

    void employeeLogin() {
        String user = JOptionPane.showInputDialog(this, "Employee Username:");
        String pass = JOptionPane.showInputDialog(this, "Employee Password:");

        if ("emp".equals(user) && "emp123".equals(pass)) {
            showEmployeeMenu();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Employee Credentials");
        }
    }

    void showAdminMenu() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 15, 15));

        JButton insertEmp = new JButton("Insert Employee");
        JButton viewEmp = new JButton("View Employees");
        JButton viewStack = new JButton("View Leave Stack");
        JButton approveRejectLeave = new JButton("Approve/Reject Leave");
        JButton revertDecision = new JButton("Revert Decision");
        JButton back = new JButton("Back");

        insertEmp.addActionListener(e -> insertEmployee());
        viewEmp.addActionListener(e -> viewEmployees());
        viewStack.addActionListener(e -> viewLeaveStack());
        approveRejectLeave.addActionListener(e -> approveRejectLeave());
        revertDecision.addActionListener(e -> revertDecision());
        back.addActionListener(e -> showHome());

        panel.add(insertEmp);
        panel.add(viewEmp);
        panel.add(viewStack);
        panel.add(approveRejectLeave);
        panel.add(revertDecision);
        panel.add(back);

        setContentPane(panel);
        revalidate();
    }

    void showEmployeeMenu() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 15, 15));

        JButton viewDetails = new JButton("View Details");
        JButton applyLeave = new JButton("Apply Leave");
        JButton viewStatus = new JButton("View Leave Status");
        JButton revertLeave = new JButton("Revert Leave");
        JButton back = new JButton("Back");

        viewDetails.addActionListener(e -> viewEmployeeDetails());
        applyLeave.addActionListener(e -> applyLeave());
        viewStatus.addActionListener(e -> viewLeaveStatus());
        revertLeave.addActionListener(e -> revertLeaveApplication());
        back.addActionListener(e -> showHome());

        panel.add(viewDetails);
        panel.add(applyLeave);
        panel.add(viewStatus);
        panel.add(revertLeave);
        panel.add(back);

        setContentPane(panel);
        revalidate();
    }

    void insertEmployee() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField dept = new JTextField();
        JTextField des = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();

        Object[] fields = {
                "ID", id, "Name", name, "Department", dept,
                "Designation", des, "Email", email, "Phone", phone
        };

        if (JOptionPane.showConfirmDialog(this, fields,
                "Insert Employee", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try (Connection con = getConnection()) {
                Statement st = con.createStatement();

                st.executeUpdate(
                        "INSERT INTO employee VALUES('" + id.getText() + "','" +
                                name.getText() + "','" + dept.getText() + "','" +
                                des.getText() + "','" + email.getText() + "','" +
                                phone.getText() + "')");

                st.executeUpdate(
                        "INSERT INTO leave_balance VALUES('" + id.getText() + "',20,0,20)");

                JOptionPane.showMessageDialog(this, "Employee Inserted");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    void viewEmployees() {
        try (Connection con = getConnection()) {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT * FROM employee");

            StringBuilder sb = new StringBuilder("EMPLOYEES:\n\n");
            while (rs.next()) {
                sb.append(rs.getString("employee_id"))
                        .append(" | ")
                        .append(rs.getString("name"))
                        .append(" | ")
                        .append(rs.getString("department"))
                        .append("\n");
            }

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void approveRejectLeave() {
        String leaveId = JOptionPane.showInputDialog(this, "Enter Leave ID");
        if (leaveId == null || leaveId.trim().isEmpty())
            return;

        Object[] options = { "Approve", "Reject", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this, "Select action for Leave ID: " + leaveId,
                "Process Leave", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION)
            return; // Cancel

        String status = choice == 0 ? "APPROVED" : "REJECTED";

        try (Connection con = getConnection()) {
            int updated = con.createStatement().executeUpdate(
                    "UPDATE leave_application SET status='" + status + "' WHERE leave_id='" + leaveId + "'");
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Leave " + status);
            } else {
                JOptionPane.showMessageDialog(this, "Leave ID not found");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void revertDecision() {
        String leaveId = JOptionPane.showInputDialog(this, "Enter Leave ID to Revert Decision");
        if (leaveId == null || leaveId.trim().isEmpty())
            return;

        try (Connection con = getConnection()) {
            int updated = con.createStatement().executeUpdate(
                    "UPDATE leave_application SET status='NOT APPROVED' WHERE leave_id='" + leaveId + "'");
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Leave decision reverted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Leave ID not found");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void viewEmployeeDetails() {
        String empId = JOptionPane.showInputDialog(this, "Employee ID");

        try (Connection con = getConnection()) {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT * FROM employee WHERE employee_id='" + empId + "'");

            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "ID: " + rs.getString("employee_id") + "\n" +
                                "Name: " + rs.getString("name") + "\n" +
                                "Dept: " + rs.getString("department") + "\n" +
                                "Role: " + rs.getString("designation") + "\n" +
                                "Email: " + rs.getString("email"));
            } else {
                JOptionPane.showMessageDialog(this, "Employee Not Found");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void applyLeave() {
        JTextField empId = new JTextField();
        JTextField type = new JTextField();
        JTextField start = new JTextField();
        JTextField end = new JTextField();
        JTextField reason = new JTextField();

        Object[] fields = {
                "Employee ID", empId,
                "Leave Type", type,
                "Start Date (YYYY-MM-DD)", start,
                "End Date (YYYY-MM-DD)", end,
                "Reason", reason
        };

        if (JOptionPane.showConfirmDialog(this, fields,
                "Apply Leave", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try (Connection con = getConnection()) {
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery(
                        "SELECT remaining_leaves FROM leave_balance WHERE employee_id='" + empId.getText() + "'");

                if (rs.next() && rs.getInt(1) <= 0) {
                    JOptionPane.showMessageDialog(this, "No Leave Balance - Salary will be deducted if approved!");
                }

                String leaveId = "LEAVE" + System.currentTimeMillis();

                st.executeUpdate(
                        "INSERT INTO leave_application VALUES('" + leaveId + "','" +
                                empId.getText() + "','" + type.getText() + "','" +
                                start.getText() + "','" + end.getText() + "','" +
                                reason.getText() + "','NOT APPROVED')");

                JOptionPane.showMessageDialog(this, "Leave Applied\nID: " + leaveId);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    void viewLeaveStatus() {
        String empId = JOptionPane.showInputDialog(this, "Employee ID");

        try (Connection con = getConnection()) {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT * FROM leave_application WHERE employee_id='" + empId + "'");

            StringBuilder sb = new StringBuilder("LEAVE STATUS:\n\n");
            while (rs.next()) {
                sb.append(rs.getString("leave_id"))
                        .append(" | ")
                        .append(rs.getString("leave_type"))
                        .append(" | ")
                        .append(rs.getString("status"))
                        .append("\n");
            }

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void revertLeaveApplication() {
        String leaveId = JOptionPane.showInputDialog(this, "Enter Leave ID to Revert");
        if (leaveId == null || leaveId.trim().isEmpty())
            return;

        try (Connection con = getConnection()) {
            int updated = con.createStatement().executeUpdate(
                    "UPDATE leave_application SET status='REVERTED' WHERE leave_id='" + leaveId
                            + "' AND status='NOT APPROVED'");
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Leave application reverted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Leave ID not found or already processed.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    static void initDB() {
        try {
            Connection con = DriverManager.getConnection(BASE_URL, USER, PASS);
            Statement st = con.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS EMPLOYEE_LEAVE_DB");
            con.close();

            con = getConnection();
            st = con.createStatement();

            st.executeUpdate("CREATE TABLE IF NOT EXISTS employee(" +
                    "employee_id VARCHAR(50) PRIMARY KEY," +
                    "name VARCHAR(100)," +
                    "department VARCHAR(100)," +
                    "designation VARCHAR(100)," +
                    "email VARCHAR(100)," +
                    "phone VARCHAR(20))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS leave_balance(" +
                    "employee_id VARCHAR(50)," +
                    "total_leaves INT," +
                    "used_leaves INT," +
                    "remaining_leaves INT," +
                    "FOREIGN KEY(employee_id) REFERENCES employee(employee_id))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS leave_application(" +
                    "leave_id VARCHAR(50) PRIMARY KEY," +
                    "employee_id VARCHAR(50)," +
                    "leave_type VARCHAR(50)," +
                    "start_date DATE," +
                    "end_date DATE," +
                    "reason VARCHAR(200)," +
                    "status VARCHAR(50)," +
                    "FOREIGN KEY(employee_id) REFERENCES employee(employee_id))");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void viewLeaveStack() {
        try (Connection con = getConnection()) {

            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT * FROM leave_application ORDER BY leave_id DESC");

            StringBuilder sb = new StringBuilder();
            sb.append("LEAVE APPLICATION STACK (LATEST FIRST)\n\n");

            while (rs.next()) {
                sb.append("Leave ID : ").append(rs.getString("leave_id")).append("\n");
                sb.append("Emp ID   : ").append(rs.getString("employee_id")).append("\n");
                sb.append("Type     : ").append(rs.getString("leave_type")).append("\n");
                sb.append("From     : ").append(rs.getDate("start_date")).append("\n");
                sb.append("To       : ").append(rs.getDate("end_date")).append("\n");
                sb.append("Reason   : ").append(rs.getString("reason")).append("\n");
                sb.append("Status   : ").append(rs.getString("status")).append("\n");
                sb.append("--------------------------------------\n");
            }

            JTextArea area = new JTextArea(sb.toString(), 20, 40);
            area.setEditable(false);

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(area),
                    "Leave Application Stack",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
