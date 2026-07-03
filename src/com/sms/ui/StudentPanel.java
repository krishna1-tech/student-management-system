package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.dao.StudentDAO;
import com.sms.model.Course;
import com.sms.model.Student;
import com.sms.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** CRUD + search screen for student records. */
public class StudentPanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    private final JTextField rollField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<Course> courseCombo = new JComboBox<>();
    private final JTextField joinedField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Roll No.", "Name", "Email", "Phone", "Course", "Joined"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable table = new JTable(tableModel);

    private int editingStudentId = -1; // -1 means "adding new"

    public StudentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildFormArea(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRowIntoForm());

        refreshCourseCombo();
        loadAllStudents();
    }

    // ---------------- UI builders ----------------

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.add(new JLabel("Search:"), BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Show All");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(searchBtn);
        btns.add(clearBtn);
        bar.add(btns, BorderLayout.EAST);

        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        clearBtn.addActionListener(e -> { searchField.setText(""); loadAllStudents(); });

        return bar;
    }

    private JScrollPane buildTableArea() {
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        return new JScrollPane(table);
    }

    private JPanel buildFormArea() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, gc, 0, "Roll Number:", rollField);
        addFormRow(form, gc, 1, "Full Name:", nameField);
        addFormRow(form, gc, 2, "Email:", emailField);
        addFormRow(form, gc, 3, "Phone:", phoneField);
        addFormRow(form, gc, 4, "Course:", courseCombo);
        addFormRow(form, gc, 5, "Date Joined (YYYY-MM-DD):", joinedField);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear Form");

        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.add(addBtn);
        btnRow.add(updateBtn);
        btnRow.add(deleteBtn);
        btnRow.add(clearBtn);

        gc.gridx = 0; gc.gridy = 6; gc.gridwidth = 2;
        form.add(btnRow, gc);

        statusLabel.setForeground(new Color(0x9B4636));
        gc.gridy = 7;
        form.add(statusLabel, gc);

        return form;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1;
        form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(field, gc);
    }

    // ---------------- Data operations ----------------

    private void refreshCourseCombo() {
        courseCombo.removeAllItems();
        courseCombo.addItem(null); // represents "no course assigned"
        try {
            for (Course c : courseDAO.getAllCourses()) {
                courseCombo.addItem(c);
            }
        } catch (SQLException e) {
            showError("Could not load courses: " + e.getMessage());
        }
    }

    private void loadAllStudents() {
        try {
            populateTable(studentDAO.getAllStudents());
            setStatus(" ");
        } catch (SQLException e) {
            showError("Could not load students: " + e.getMessage());
        }
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllStudents();
            return;
        }
        try {
            List<Student> results = studentDAO.searchStudents(keyword);
            populateTable(results);
            setStatus(results.isEmpty() ? "No matching students." : results.size() + " result(s) found.");
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getRollNumber(), s.getFullName(), s.getEmail(),
                    s.getPhone() == null ? "" : s.getPhone(),
                    s.getCourseName() == null ? "Unassigned" : s.getCourseName(),
                    s.getDateJoined()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        editingStudentId = (int) tableModel.getValueAt(modelRow, 0);
        rollField.setText((String) tableModel.getValueAt(modelRow, 1));
        nameField.setText((String) tableModel.getValueAt(modelRow, 2));
        emailField.setText((String) tableModel.getValueAt(modelRow, 3));
        phoneField.setText((String) tableModel.getValueAt(modelRow, 4));
        joinedField.setText(String.valueOf(tableModel.getValueAt(modelRow, 6)));

        String courseName = (String) tableModel.getValueAt(modelRow, 5);
        courseCombo.setSelectedIndex(0); // default to "unassigned" unless a match is found below
        if (!"Unassigned".equals(courseName)) {
            for (int i = 0; i < courseCombo.getItemCount(); i++) {
                Course c = courseCombo.getItemAt(i);
                if (c != null && c.getCourseName().equals(courseName)) {
                    courseCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        setStatus(" ");
    }

    private void onAdd() {
        Student s = readFormOrNull(-1);
        if (s == null) return;
        try {
            studentDAO.addStudent(s);
            setStatus("Student added.");
            clearForm();
            loadAllStudents();
        } catch (SQLException e) {
            showError("Could not add student: " + e.getMessage());
        }
    }

    private void onUpdate() {
        if (editingStudentId == -1) {
            showError("Select a student in the table to update.");
            return;
        }
        Student s = readFormOrNull(editingStudentId);
        if (s == null) return;
        try {
            studentDAO.updateStudent(s);
            setStatus("Student updated.");
            clearForm();
            loadAllStudents();
        } catch (SQLException e) {
            showError("Could not update student: " + e.getMessage());
        }
    }

    private void onDelete() {
        if (editingStudentId == -1) {
            showError("Select a student in the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this student and their attendance history? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            studentDAO.deleteStudent(editingStudentId);
            setStatus("Student deleted.");
            clearForm();
            loadAllStudents();
        } catch (SQLException e) {
            showError("Could not delete student: " + e.getMessage());
        }
    }

    /** Validates the form; returns a populated Student, or null (with an error shown) if invalid. */
    private Student readFormOrNull(int existingId) {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String joinedText = joinedField.getText().trim();

        String err = ValidationUtil.validateRequired(roll, "Roll number");
        if (err == null) err = ValidationUtil.validateRequired(name, "Full name");
        if (err == null) err = ValidationUtil.validateEmail(email);
        if (err == null) err = ValidationUtil.validatePhone(phone);
        if (err == null) err = ValidationUtil.validateDate(joinedText, "Date joined");

        try {
            if (err == null && studentDAO.isRollNumberTaken(roll, existingId)) {
                err = "This roll number is already in use.";
            }
            if (err == null && studentDAO.isEmailTaken(email, existingId)) {
                err = "This email is already in use.";
            }
        } catch (SQLException e) {
            showError("Validation check failed: " + e.getMessage());
            return null;
        }

        if (err != null) {
            showError(err);
            return null;
        }

        Student s = new Student();
        s.setId(existingId);
        s.setRollNumber(roll);
        s.setFullName(name);
        s.setEmail(email);
        s.setPhone(phone.isEmpty() ? null : phone);
        Course selected = (Course) courseCombo.getSelectedItem();
        s.setCourseId(selected == null ? null : selected.getId());
        s.setDateJoined(ValidationUtil.parseDate(joinedText));
        return s;
    }

    private void clearForm() {
        editingStudentId = -1;
        rollField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        joinedField.setText("");
        courseCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private void setStatus(String message) {
        statusLabel.setForeground(new Color(0x2F6F6B));
        statusLabel.setText(message);
    }

    private void showError(String message) {
        statusLabel.setForeground(new Color(0x9B4636));
        statusLabel.setText(message);
    }

    /** Called by other tabs (e.g. Courses) when course data changes. */
    public void onCoursesChanged() {
        refreshCourseCombo();
        loadAllStudents();
    }
}
