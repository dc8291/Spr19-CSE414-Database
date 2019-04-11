--hw2-q8
SELECT DISTINCT c.name as name, SUM(f.departure_delay) as delay
FROM Flights as f, Carriers as c
WHERE f.carrier_id = c.cid
GROUP BY f.carrier_id
;
