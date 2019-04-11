--hw2 create-tables.sql
create table Flights (
  fid int primary key,
  month_id int, --1-12
  day_of_month int,    -- 1-31
  day_of_week_id int,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
  carrier_id varchar(7),
  flight_num int,
  origin_city varchar(34),
  origin_state varchar(47),
  dest_city varchar(34),
  dest_state varchar(46),
  departure_delay int, -- in mins
  taxi_out int,        -- in mins
  arrival_delay int,   -- in mins
  cancelled int,        -- 1 means canceled
  actual_time int,     -- in mins
  distance int,        -- in miles
  capacity int,
  price int            -- in $
);

create table Carriers (
  cid varchar(7) primary key, --carrier_id
  name varchar(83)
);

create table Months (
  mid int primary key, --month_id
  month varchar (9)
);

create table Weekdays(
  did int primary key, --day_of_week_id
  day_of_week varchar(9)
);

PRAGMA foreign_key=ON;
.mode csv
.import C:/Users/dc829/OneDrive/School/CSE414/cse414-dc8291/hw/hw2/starter-code/flights-small.csv Flights
.import C:/Users/dc829/OneDrive/School/CSE414/cse414-dc8291/hw/hw2/starter-code/carriers.csv Carriers
.import C:/Users/dc829/OneDrive/School/CSE414/cse414-dc8291/hw/hw2/starter-code/months.csv Months
.import C:/Users/dc829/OneDrive/School/CSE414/cse414-dc8291/hw/hw2/starter-code/weekdays.csv Weekdays
