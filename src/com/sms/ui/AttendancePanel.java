package com.sms.ui;

import com.sms.dao.AttendanceDAO;
import com.sms.dao.CourseDAO;
import com.sms.dao.StudentDAO;
import com.sms.model.AttendanceRecord;
import com.sms.model.Course;
import com.sms.model.Student;
import com.sms.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Screen for taking attendance per course/date, and reviewing the attendance log. */
public class AttendancePanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private final JComboBox<Course> courseCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel rosterModel =
            new DefaultTableModel(new Object[]{"Present", "Roll No.", "Name"}, 0) {
                @Override public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
                @Override public boolean isCellEditable(int row, int col) { return col == 0; }
            };
    private final JTable rosterTable = new JTable(rosterModel);
    private final List<Integer> rosterStudentIds = new ArrayList<>();

    private final DefaultTableModel logModel =
            new DefaultTableModel(new Object[]{"Date", "Student", "Course", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable logTable = new JTable(logModel);

    public AttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildRosterArea(), buildLogArea());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        refreshCourseCombo();
        loadLog();
    }

    private JPanel buildRosterArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Take Attendance"));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(new JLabel("Course:"));
        courseCombo.setPreferredSize(new Dimension(240, 26));
        controls.add(courseCombo);
        controls.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField.setPreferredSize(new Dimension(120, 26));
        controls.add(dateField);

        JButton loadBtn = new JButton("Load Roster");
        JButton allPresentBtn = new JButton("Mark All Present");
        JButton saveBtn = new JButton("Save Attendance");
        controls.add(loadBtn);
        controls.add(allPresentBtn);
        controls.add(saveBtn);

        loadBtn.addActionListener(e -> loadRoster());
        allPresentBtn.addActionListener(e -> markAllPresent());
        saveBtn.addActionListener(e -> saveAttendance());

        rosterTable.setRowHeight(24);
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(rosterTable), BorderLayout.CENTER);

        statusLabel.setForeground(new Color(0x9B4636));
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildLogArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Attendance Log (most recent 200)"));
        logTable.setRowHeight(22);
        logTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(logTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Log");
        refreshBtn.addActionListener(e -> loadLog());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCourseCombo() {
        courseCombo.removeAllItems();
        try {
            for (Course c : courseDAO.getAllCourses()) {
                courseCombo.addItem(c);
            }
        } catch (SQLException e) {
            showError("Could not load courses: " + e.getMessage());
        }
    }

    private void loadRoster() {
        Course course = (Course) courseCombo.getSelectedItem();
        String dateText = dateField.getText().trim();

        if (course == null) {
            showError("Choose a course first.");
            return;
        }
        String dateErr = ValidationUtil.validateDate(dateText, "Date");
        if (dateErr != null) {
            showError(dateErr);
            return;
        }
        LocalDate date = ValidationUtil.parseDate(dateText);

        try {
            List<Student> roster = studentDAO.getStudentsByCourse(course.getId());
            List<AttendanceRecord> existing = attendanceDAO.getByCourseAndDate(course.getId(), date);

            rosterModel.setRowCount(0);
            rosterStudentIds.clear();
            if (roster.isEmpty()) {
                setStatus("No students are enrolled in this course.");
                return;
            }
            for (Student s : roster) {
                boolean present = existing.stream()
                        .filter(a -> a.getStudentId() == s.getId())
                        .findFirst()
                        .map(a -> a.getStatus() == AttendanceRecord.Status.PRESENT)
                        .orElse(true); // default to present for a fresh roster
                rosterModel.addRow(new Object[]{present, s.getRollNumber(), s.getFullName()});
                rosterStudentIds.add(s.getId());
            }
            setStatus("Loaded " + roster.size() + " student(s) for " + date + ".");
        } catch (SQLException e) {
            showError("Could not load roster: " + e.getMessage());
        }
    }

    private void markAllPresent() {
        for (int i = 0; i < rosterModel.getRowCount(); i++) {
            rosterModel.setValueAt(true, i, 0);
        }
    }

    private void saveAttendance() {
        Course course = (Course) courseCombo.getSelectedItem();
        String dateText = dateField.getText().trim();
        if (course == null || rosterStudentIds.isEmpty()) {
            showError("Load a roster before saving attendance.");
            return;
        }
        String dateErr = ValidationUtil.validateDate(dateText, "Date");
        if (dateErr != null) {
            showError(dateErr);
            return;
        }
        LocalDate date = ValidationUtil.parseDate(dateText);

        try {
            for (int i = 0; i < rosterStudentIds.size(); i++) {
                boolean present = (Boolean) rosterModel.getValueAt(i, 0);
                AttendanceRecord r = new AttendanceRecord(
                        rosterStudentIds.get(i), course.getId(), date,
                        present ? AttendanceRecord.Status.PRESENT : AttendanceRecord.Status.ABSENT);
                attendanceDAO.markAttendance(r);
            }
            setStatus("Attendance saved for " + date + ".");
            loadLog();
        } catch (SQLException e) {
            showError("Could not save attendance: " + e.getMessage());
        }
    }

    private void loadLog() {
        try {
            logModel.setRowCount(0);
            for (AttendanceRecord r : attendanceDAO.getLog(200)) {
                logModel.addRow(new Object[]{
                        r.getAttendanceDate(),
                        r.getStudentName() + " (" + r.getRollNumber() + ")",
                        r.getCourseName(),
                        r.getStatus()
                });
            }
        } catch (SQLException e) {
            showError("Could not load attendance log: " + e.getMessage());
        }
    }

    private void setStatus(String message) {
        statusLabel.setForeground(new Color(0x2F6F6B));
        statusLabel.setText(message);
    }

    private void showError(String message) {
        statusLabel.setForeground(new Color(0x9B4636));
        statusLabel.setText(message);
    }

    /** Called when courses change elsewhere, so the dropdown stays current. */
    public void onCoursesChanged() {
        Course previous = (Course) courseCombo.getSelectedItem();
        refreshCourseCombo();
        if (previous != null) {
            for (int i = 0; i < courseCombo.getItemCount(); i++) {
                if (courseCombo.getItemAt(i).getId() == previous.getId()) {
                    courseCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
}
