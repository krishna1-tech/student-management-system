package com.sms.dao;

import com.sms.db.DBConnection;
import com.sms.model.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer for the STUDENTS table. Every method uses prepared statements. */
public class StudentDAO {

    public boolean isRollNumberTaken(String rollNumber, int excludeId) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE LOWER(roll_number) = LOWER(?) AND student_id <> ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isEmailTaken(String email, int excludeId) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE LOWER(email) = LOWER(?) AND student_id <> ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_number, full_name, email, phone, course_id, date_joined) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindStudent(ps, s);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET roll_number = ?, full_name = ?, email = ?, phone = ?, " +
                "course_id = ?, date_joined = ? WHERE student_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            bindStudent(ps, s);
            ps.setInt(7, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        // Attendance rows for this student are removed via ON DELETE CASCADE in the schema.
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name FROM students s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY s.roll_number";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Student> searchStudents(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name FROM students s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "WHERE s.full_name LIKE ? OR s.roll_number LIKE ? OR s.email LIKE ? " +
                "ORDER BY s.roll_number";
        String like = "%" + keyword + "%";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Student> getStudentsByCourse(int courseId) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name FROM students s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "WHERE s.course_id = ? ORDER BY s.roll_number";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private void bindStudent(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getRollNumber());
        ps.setString(2, s.getFullName());
        ps.setString(3, s.getEmail());
        ps.setString(4, s.getPhone());
        if (s.getCourseId() == null) {
            ps.setNull(5, Types.INTEGER);
        } else {
            ps.setInt(5, s.getCourseId());
        }
        ps.setDate(6, Date.valueOf(s.getDateJoined()));
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("student_id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        int courseId = rs.getInt("course_id");
        s.setCourseId(rs.wasNull() ? null : courseId);
        s.setCourseName(rs.getString("course_name"));
        Date joined = rs.getDate("date_joined");
        s.setDateJoined(joined != null ? joined.toLocalDate() : null);
        return s;
    }
}
