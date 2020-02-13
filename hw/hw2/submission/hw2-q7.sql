--hw2-q7
SELECT SUM(f.capacity) as capacity
FROM Flights as f, Months as m
WHERE f.month_id = m.mid and
m.month = "July" and
f.day_of_month = 10 and
((f.origin_city = "San Francisco CA" and f.dest_city = "Seattle WA") or
(f.origin_city = "Seattle WA" and f.dest_city = "San Francisco CA"))
;
