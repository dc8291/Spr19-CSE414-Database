--hw2-q5
SELECT DISTINCT c.name as name,
COUNT(case f.cancelled when 1 then 1 else null end)*100.0/COUNT(c.name) as percentage
FROM Flights as f INNER JOIN Carriers as c ON f.carrier_id = c.cid
WHERE f.origin_city = "Seattle WA"
GROUP BY f.carrier_id
HAVING COUNT(case f.cancelled when 1 then 1 else null end)*1.0/COUNT(c.name) > 0.005
ORDER BY percentage asc
;
-- output: "name", "Percentage"
