-- add all your SQL setup statements here.
DROP TABLE IF EXISTS Reservations;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Itineraries;

CREATE TABLE Users (
  username VARCHAR(20) PRIMARY KEY,
  password VARCHAR(20),
  balance int
)

CREATE TABLE Itineraries (
  itineraryId int PRIMARY KEY,
  fid1 int,
  fid2 int
)

CREATE TABLE Reservations(
  reservationId int IDENTITY(1,1) PRIMARY KEY,
  paid int, -- 1 means paid
  username VARCHAR(20) FOREIGN KEY REFERENCES Users(username),
  fid1 int,
  fid2 int
)
-- You can assume that the following base table has been created with data loaded for you when we test your submission
-- (you still need to create and populate it in your instance however),
-- although you are free to insert extra ALTER COLUMN ... statements to change the column
-- names / types if you like.

--FLIGHTS (fid int,
--         month_id int,        -- 1-12
--         day_of_month int,    -- 1-31
--         day_of_week_id int,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
--         carrier_id varchar(7),
--         flight_num int,
--         origin_city varchar(34),
--         origin_state varchar(47),
--         dest_city varchar(34),
--         dest_state varchar(46),
--         departure_delay int, -- in mins
--         taxi_out int,        -- in mins
--         arrival_delay int,   -- in mins
--         canceled int,        -- 1 means canceled
--         actual_time int,     -- in mins
--         distance int,        -- in miles
--         capacity int,
--         price int            -- in $
--         )
