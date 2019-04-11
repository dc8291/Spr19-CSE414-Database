--hw2-q4
SELECT DISTINCT c.name as name
FROM Flights as f, Carriers as c
WHERE f.carrier_id = c.cid
GROUP BY c.name, f.month_id, f.day_of_month
HAVING count(c.name) > 1000
;

-- output: "name"
