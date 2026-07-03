-- ============================================================
-- Student Management System — Database Schema
-- MySQL 8.0+
-- ============================================================

DROP DATABASE IF EXISTS student_management;
CREATE DATABASE student_management;
USE student_management;

-- ------------------------------------------------------------
-- COURSES
-- ------------------------------------------------------------
CREATE TABLE courses (
    course_id     INT AUTO_INCREMENT PRIMARY KEY,
    course_code   VARCHAR(20)  NOT NULL UNIQUE,
    course_name   VARCHAR(100) NOT NULL,
    instructor    VARCHAR(100),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- STUDENTS
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id    INT AUTO_INCREMENT PRIMARY KEY,
    roll_number   VARCHAR(20)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(15),
    course_id     INT,
    date_joined   DATE NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- ATTENDANCE
-- One row per student, per course, per calendar date.
-- ------------------------------------------------------------
CREATE TABLE attendance (
    attendance_id    INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    course_id         INT NOT NULL,
    attendance_date   DATE NOT NULL,
    status             ENUM('PRESENT', 'ABSENT') NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_attendance_unique_entry
        UNIQUE (student_id, course_id, attendance_date)
) ENGINE=InnoDB;

-- Helpful indexes for common lookups
CREATE INDEX idx_students_name  ON students(full_name);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);

-- ------------------------------------------------------------
-- SAMPLE DATA (optional — comment out if not needed)
-- ------------------------------------------------------------
INSERT INTO courses (course_code, course_name, instructor) VALUES
    ('CS101', 'Introduction to Computer Science', 'Dr. R. Patnaik'),
    ('CS204', 'Database Management Systems',       'Dr. S. Mohanty'),
    ('MA110', 'Discrete Mathematics',               'Prof. A. Das');

INSERT INTO students (roll_number, full_name, email, phone, course_id, date_joined) VALUES
    ('24CS001', 'Asha Verma',     'asha.verma@example.com',     '9876543210', 1, '2024-07-01'),
    ('24CS002', 'Rohan Mehta',    'rohan.mehta@example.com',    '9876543211', 1, '2024-07-01'),
    ('24CS003', 'Priya Nair',     'priya.nair@example.com',     '9876543212', 2, '2024-07-02');
