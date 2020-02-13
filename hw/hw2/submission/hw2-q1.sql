--hw2-q1
SELECT DISTINCT flight_num
FROM Flights as f, Carriers as c, Weekdays as w
WHERE f.carrier_id = c.cid and
f.day_of_week_id = w.did and
f.origin_city = "Seattle WA" and
f.dest_city = "Boston MA" and
c.name = "Alaska Airlines Inc." and
w.day_of_week = "Monday"
;
--"Name the output column flight_num"
