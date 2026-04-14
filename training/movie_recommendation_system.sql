-- settings
SET SERVEROUTPUT ON
WHENEVER SQLERROR CONTINUE;

-- drop everything first
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Watch_History CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Ratings CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Movies CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Users CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- tables

CREATE TABLE Users (
    user_id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    age NUMBER
);

CREATE TABLE Movies (
    movie_id NUMBER PRIMARY KEY,
    title VARCHAR2(100),
    genre VARCHAR2(50)
);

CREATE TABLE Ratings (
    user_id NUMBER,
    movie_id NUMBER,
    rating NUMBER(2,1),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_movie FOREIGN KEY (movie_id) REFERENCES Movies(movie_id)
);

-- who watched what and when
CREATE TABLE Watch_History (
    user_id NUMBER,
    movie_id NUMBER,
    watch_date DATE,
    CONSTRAINT fk_wh_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_wh_movie FOREIGN KEY (movie_id) REFERENCES Movies(movie_id)
);

-- sample data

INSERT INTO Users VALUES (1, 'Alice', 22);
INSERT INTO Users VALUES (2, 'Bob', 25);
INSERT INTO Users VALUES (3, 'Charlie', 30);
INSERT INTO Users VALUES (4, 'David', 28);

INSERT INTO Movies VALUES (101, 'Inception', 'Sci-Fi');
INSERT INTO Movies VALUES (102, 'Interstellar', 'Sci-Fi');
INSERT INTO Movies VALUES (103, 'Avengers', 'Action');
INSERT INTO Movies VALUES (104, 'Titanic', 'Romance');
INSERT INTO Movies VALUES (105, 'Joker', 'Drama');

INSERT INTO Ratings VALUES (1, 101, 4.5);
INSERT INTO Ratings VALUES (1, 102, 5.0);
INSERT INTO Ratings VALUES (2, 101, 4.0);
INSERT INTO Ratings VALUES (2, 103, 4.5);
INSERT INTO Ratings VALUES (3, 104, 3.5);
INSERT INTO Ratings VALUES (3, 105, 4.0);
INSERT INTO Ratings VALUES (4, 101, 5.0);
INSERT INTO Ratings VALUES (4, 102, 4.5);

-- watch history
INSERT INTO Watch_History VALUES (1, 101, SYSDATE - 2);
INSERT INTO Watch_History VALUES (1, 102, SYSDATE - 1);
INSERT INTO Watch_History VALUES (2, 103, SYSDATE - 3);
INSERT INTO Watch_History VALUES (3, 104, SYSDATE - 10);
INSERT INTO Watch_History VALUES (4, 101, SYSDATE - 1);
INSERT INTO Watch_History VALUES (4, 102, SYSDATE - 2);

COMMIT;

-- formatting
SET LINESIZE 200
SET PAGESIZE 50
SET WRAP OFF
SET TRIMSPOOL ON

ALTER SESSION SET NLS_DATE_FORMAT = 'DD-MON-YYYY';

COLUMN title FORMAT A25
COLUMN genre FORMAT A15
COLUMN name FORMAT A15

COLUMN avg_rating FORMAT 999.9
COLUMN watch_count FORMAT 999
COLUMN movies_watched FORMAT 999
COLUMN views FORMAT 999

--- queries ---

-- top rated movies
PROMPT ===== Top Rated Movies =====

SELECT m.title, AVG(r.rating) AS avg_rating
FROM Ratings r
JOIN Movies m ON r.movie_id = m.movie_id
GROUP BY m.title
ORDER BY avg_rating DESC;

-- which genres are most watched
PROMPT ===== Most Popular Genres =====

select m.genre, count(*) as watch_count
from Watch_History w
join Movies m on w.movie_id = m.movie_id
group by m.genre
order by watch_count desc;

-- recommend movies for user 1
-- find what ppl who watched similar stuff also liked
PROMPT ===== Movie Recommendations =====

SELECT DISTINCT m.title
FROM Ratings r1
JOIN Ratings r2 ON r1.movie_id = r2.movie_id
JOIN Movies m ON r2.movie_id = m.movie_id
WHERE r1.user_id = 1
AND r2.user_id != 1
AND r2.rating >= 4
AND m.movie_id NOT IN (
    SELECT movie_id FROM Ratings WHERE user_id = 1
);

-- how active is each user
PROMPT ===== User Watch Frequency =====

SELECT u.name, COUNT(w.movie_id) AS movies_watched
FROM Users u
LEFT JOIN Watch_History w ON u.user_id = w.user_id
GROUP BY u.name
ORDER BY movies_watched DESC;

-- whats trending in the last week
PROMPT ===== Trending Movies =====

-- tried a subquery first but this is simpler
SELECT m.title, COUNT(*) AS views FROM Watch_History w JOIN Movies m ON w.movie_id = m.movie_id WHERE w.watch_date >= SYSDATE - 7 GROUP BY m.title ORDER BY views DESC;
