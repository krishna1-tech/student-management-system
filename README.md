# Student Management System (Java + MySQL, Desktop)

A Swing desktop application for managing student, course, and attendance
records with persistent storage in MySQL — add, update, search, and delete
operations, backed by a normalized schema and input-validated forms.

## Tech stack
- **Java** (Swing for the UI, JDBC for the database layer) — JDK 11+
- **MySQL 8.0+**
- **mysql-connector-j** (MySQL's JDBC driver)

## Project structure
```
StudentManagementSystem/
├── sql/
│   └── schema.sql              -- database schema + sample data
├── src/com/sms/
│   ├── Main.java                -- entry point
│   ├── db/DBConnection.java     -- JDBC connection manager
│   ├── model/                   -- Student, Course, AttendanceRecord
│   ├── dao/                     -- StudentDAO, CourseDAO, AttendanceDAO (SQL lives here)
│   ├── util/ValidationUtil.java -- input validation helpers
│   └── ui/                      -- MainFrame, StudentPanel, CoursePanel, AttendancePanel
└── lib/                          -- put mysql-connector-j-x.x.x.jar here
```

## 1. Set up the database
Make sure MySQL is running, then load the schema (creates the database,
tables, foreign keys, and a few sample rows):

```bash
mysql -u root -p < sql/schema.sql
```

This creates three tables:
- **courses** — `course_id`, `course_code` (unique), `course_name`, `instructor`
- **students** — `student_id`, `roll_number` (unique), `full_name`, `email` (unique),
  `phone`, `course_id` (FK → courses), `date_joined`
- **attendance** — `attendance_id`, `student_id` (FK), `course_id` (FK),
  `attendance_date`, `status` (`PRESENT`/`ABSENT`), with a unique key on
  `(student_id, course_id, attendance_date)` so each student gets one
  attendance entry per course per day.

Deleting a course unassigns its students (`ON DELETE SET NULL`) and removes
its attendance rows (`ON DELETE CASCADE`). Deleting a student removes their
attendance history.

## 2. Add the MySQL JDBC driver
Download `mysql-connector-j` (the MySQL Connector/J `.jar`) from MySQL's
official site and place it in the `lib/` folder.

## 3. Configure credentials
Open `src/com/sms/db/DBConnection.java` and update:
```java
private static final String URL = "jdbc:mysql://localhost:3306/student_management?...";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

## 4. Compile and run

**Linux / macOS:**
```bash
javac -d bin -cp "lib/*" $(find src -name "*.java")
java -cp "bin:lib/*" com.sms.Main
```

**Windows (PowerShell/cmd):**
```bat
javac -d bin -cp "lib/*" src\com\sms\*.java src\com\sms\db\*.java src\com\sms\model\*.java src\com\sms\dao\*.java src\com\sms\util\*.java src\com\sms\ui\*.java
java -cp "bin;lib/*" com.sms.Main
```

If you'd rather use an IDE (IntelliJ IDEA, Eclipse, NetBeans), just import
the `src/` folder as a source root and add the connector jar as a library
dependency, then run `com.sms.Main`.

## What each requirement maps to
- **Add / update / search / delete, persistent storage in MySQL** —
  `StudentDAO`, `CourseDAO`, and `AttendanceDAO` use `PreparedStatement`s for
  every operation; `StudentPanel` / `CoursePanel` / `AttendancePanel` wire
  those into the Swing forms and tables.
- **Database schema and SQL for student, course, and attendance data** —
  `sql/schema.sql`, with foreign keys tying attendance back to both students
  and courses, and a join used in `StudentDAO.getAllStudents()` to show each
  student's course name.
- **Input validation and basic error handling** — `ValidationUtil` checks
  required fields, email format, 10-digit phone format, and date format
  before any SQL runs; DAO calls validate roll number/email/course code
  uniqueness; every database call is wrapped so `SQLException`s surface as a
  readable status message instead of crashing the app.

## Notes
- This was built and reviewed without a live MySQL instance available in
  this environment (no network access to install a JDK/driver here), so
  it hasn't been compiled or run end-to-end on this machine. The SQL and
  JDBC code follow standard, well-tested patterns, but **please test
  locally** before relying on it, and let me know if you hit any errors —
  happy to debug.
