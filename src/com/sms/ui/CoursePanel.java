package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.model.Course;
import com.sms.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/** CRUD + search screen for the course catalog. */
public class CoursePanel extends JPanel {

    private final CourseDAO courseDAO = new CourseDAO();

    private final JTextField codeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField instructorField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Code", "Course Name", "Instructor", "Enrolled"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable table = new JTable(tableModel);

    private int editingCourseId = -1;

    /** Notified after add/update/delete so other tabs can refresh their course lists. */
    private Consumer<Void> onChangedListener;

    public CoursePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildFormArea(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRowIntoForm());
        loadAllCourses();
    }

    public void setOnChangedListener(Consumer<Void> listener) {
        this.onChangedListener = listener;
    }

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
        clearBtn.addActionListener(e -> { searchField.setText(""); loadAllCourses(); });

        return bar;
    }

    private JScrollPane buildTableArea() {
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        return new JScrollPane(table);
    }

    private JPanel buildFormArea() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Course Details"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, gc, 0, "Course Code:", codeField);
        addFormRow(form, gc, 1, "Course Name:", nameField);
        addFormRow(form, gc, 2, "Instructor:", instructorField);

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

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        form.add(btnRow, gc);

        statusLabel.setForeground(new Color(0x9B4636));
        gc.gridy = 4;
        form.add(statusLabel, gc);

        return form;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1;
        form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(field, gc);
    }

    private void loadAllCourses() {
        try {
            populateTable(courseDAO.getAllCourses());
            setStatus(" ");
        } catch (SQLException e) {
            showError("Could not load courses: " + e.getMessage());
        }
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllCourses();
            return;
        }
        try {
            List<Course> results = courseDAO.searchCourses(keyword);
            populateTable(results);
            setStatus(results.isEmpty() ? "No matching courses." : results.size() + " result(s) found.");
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void populateTable(List<Course> courses) {
        tableModel.setRowCount(0);
        for (Course c : courses) {
            int enrolled = 0;
            try {
                enrolled = courseDAO.countEnrolled(c.getId());
            } catch (SQLException ignored) {
                // Enrollment count is informational; a failed lookup shouldn't break the table.
            }
            tableModel.addRow(new Object[]{c.getId(), c.getCourseCode(), c.getCourseName(), c.getInstructor(), enrolled});
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        editingCourseId = (int) tableModel.getValueAt(modelRow, 0);
        codeField.setText((String) tableModel.getValueAt(modelRow, 1));
        nameField.setText((String) tableModel.getValueAt(modelRow, 2));
        Object instructor = tableModel.getValueAt(modelRow, 3);
        instructorField.setText(instructor == null ? "" : instructor.toString());
        setStatus(" ");
    }

    private void onAdd() {
        Course c = readFormOrNull(-1);
        if (c == null) return;
        try {
            courseDAO.addCourse(c);
            setStatus("Course added.");
            clearForm();
            loadAllCourses();
            notifyChanged();
        } catch (SQLException e) {
            showError("Could not add course: " + e.getMessage());
        }
    }

    private void onUpdate() {
        if (editingCourseId == -1) {
            showError("Select a course in the table to update.");
            return;
        }
        Course c = readFormOrNull(editingCourseId);
        if (c == null) return;
        try {
            courseDAO.updateCourse(c);
            setStatus("Course updated.");
            clearForm();
            loadAllCourses();
            notifyChanged();
        } catch (SQLException e) {
            showError("Could not update course: " + e.getMessage());
        }
    }

    private void onDelete() {
        if (editingCourseId == -1) {
            showError("Select a course in the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this course? Enrolled students will be unassigned and related attendance removed.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            courseDAO.deleteCourse(editingCourseId);
            setStatus("Course deleted.");
            clearForm();
            loadAllCourses();
            notifyChanged();
        } catch (SQLException e) {
            showError("Could not delete course: " + e.getMessage());
        }
    }

    private Course readFormOrNull(int existingId) {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String instructor = instructorField.getText().trim();

        String err = ValidationUtil.validateRequired(code, "Course code");
        if (err == null) err = ValidationUtil.validateRequired(name, "Course name");

        try {
            if (err == null && courseDAO.isCourseCodeTaken(code, existingId)) {
                err = "This course code is already in use.";
            }
        } catch (SQLException e) {
            showError("Validation check failed: " + e.getMessage());
            return null;
        }

        if (err != null) {
            showError(err);
            return null;
        }

        Course c = new Course();
        c.setId(existingId);
        c.setCourseCode(code);
        c.setCourseName(name);
        c.setInstructor(instructor.isEmpty() ? null : instructor);
        return c;
    }

    private void clearForm() {
        editingCourseId = -1;
        codeField.setText("");
        nameField.setText("");
        instructorField.setText("");
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

    private void notifyChanged() {
        if (onChangedListener != null) onChangedListener.accept(null);
    }
}
