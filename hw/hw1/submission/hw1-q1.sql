--hw1-q1
.headers on
.mode col
create table Edge (Source int, Destination int);
insert into Edge values(10,5);
insert into Edge values(6, 25);
insert into Edge values(1,3);
insert into Edge values(4, 4);
select * from Edge;
select Source from Edge;
select * from Edge where Source > Destination;
insert into Edge values('-1', '2000');
-- no error due to Type Affinity. SQLite automatically assigns types when required.
