-- setup
SET SERVEROUTPUT ON
WHENEVER SQLERROR CONTINUE;

-- drop old tables first
BEGIN EXECUTE IMMEDIATE 'DROP TABLE IssuedBooks CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Books CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Students CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- create the tables

CREATE TABLE Books (
    BookID NUMBER PRIMARY KEY,
    Title VARCHAR2(100),
    Category VARCHAR2(50)
);

CREATE TABLE Students (
    StudentID NUMBER PRIMARY KEY,
    StudentName VARCHAR2(100),
    JoinDate DATE,
    IsActive CHAR(1) DEFAULT 'Y',
    CONSTRAINT chk_active CHECK (IsActive IN ('Y','N'))
);

-- tracks which book is given to which student
CREATE TABLE IssuedBooks (
    IssueID NUMBER PRIMARY KEY,
    BookID NUMBER,
    StudentID NUMBER,
    IssueDate DATE,
    ReturnDate DATE,
    CONSTRAINT fk_book FOREIGN KEY (BookID) REFERENCES Books(BookID),
    CONSTRAINT fk_student FOREIGN KEY (StudentID) REFERENCES Students(StudentID)
);

-- insert test data

INSERT INTO Books VALUES (1, 'Data Structures', 'Science');
INSERT INTO Books VALUES (2, 'World History', 'History');
INSERT INTO Books VALUES (3, 'Java Programming', 'Science');
INSERT INTO Books VALUES (4, 'Harry Potter', 'Fiction');
INSERT INTO Books VALUES (5, 'AI Basics', 'Science');

-- students
INSERT INTO Students (StudentID, StudentName, JoinDate) VALUES (101, 'Rahul', SYSDATE - 1000);
INSERT INTO Students (StudentID, StudentName, JoinDate) VALUES (102, 'Priya', SYSDATE - 200);
INSERT INTO Students (StudentID, StudentName, JoinDate) VALUES (103, 'Amit', SYSDATE - 1500);
INSERT INTO Students (StudentID, StudentName, JoinDate) VALUES (104, 'Sneha', SYSDATE - 50);

-- book issue records
INSERT INTO IssuedBooks VALUES (1, 1, 101, SYSDATE - 20, NULL);
INSERT INTO IssuedBooks VALUES (2, 2, 102, SYSDATE - 10, SYSDATE - 2);
-- INSERT INTO IssuedBooks VALUES (3, 3, 103, SYSDATE - 30, NULL);
INSERT INTO IssuedBooks VALUES (4, 4, 104, SYSDATE - 5, NULL);
INSERT INTO IssuedBooks VALUES (5, 5, 102, SYSDATE - 40, SYSDATE - 10);

COMMIT;

-- output formatting stuff
SET LINESIZE 200
SET PAGESIZE 50
ALTER SESSION SET NLS_DATE_FORMAT = 'DD-MON-YYYY';

COLUMN StudentName FORMAT A15
COLUMN Title FORMAT A20
COLUMN Category FORMAT A12
COLUMN IsActive FORMAT A8

-- overdue books - more then 14 days without return
PROMPT ===== Overdue Books (More than 14 days) =====

select s.StudentName, b.Title, i.IssueDate
from IssuedBooks i
join Students s on i.StudentID = s.StudentID
join Books b on i.BookID = b.BookID
where i.ReturnDate IS NULL
and i.IssueDate < SYSDATE - 14;

-- which category ppl borrow the most
PROMPT ===== Most Popular Book Categories =====

SELECT b.Category, COUNT(*) AS BorrowCount
FROM IssuedBooks i
JOIN Books b ON i.BookID = b.BookID
GROUP BY b.Category
ORDER BY BorrowCount DESC;

-- deactivate students who havent borrowed anything in 3 yrs
-- 1095 days = 3 years roughly
PROMPT ===== Soft Deactivating Inactive Students (>3 years) =====

UPDATE Students s
SET IsActive = 'N'
WHERE NOT EXISTS (
    SELECT 1 FROM IssuedBooks i
    WHERE i.StudentID = s.StudentID
    AND i.IssueDate >= SYSDATE - 1095
);

COMMIT;

-- show results
PROMPT ===== All Students (with Active Status) =====
SELECT * FROM Students;

PROMPT ===== Active Students Only =====
select * from Students where IsActive = 'Y';
