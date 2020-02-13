--hw2-q6
SELECT DISTINCT c.name as carrier, MAX(f.price) as price
FROM Flights as f, Carriers as c
WHERE f.carrier_id = c.cid and
((f.dest_city = "Seattle WA" and f.origin_city = "New York NY") or
(f.dest_city = "New York NY" and f.origin_city = "Seattle WA"))
GROUP BY f.carrier_id
;
--output: carrier, price
