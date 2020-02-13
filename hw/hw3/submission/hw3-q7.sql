--hw3-q7

SELECT C.name as carrier
FROM Carriers as c, Flights as f
WHERE f.origin_city = 'Seattle WA'
AND f.dest_city = 'San Francisco CA'
AND c.cid = f.carrier_id
GROUP BY c.name
ORDER BY c.name asc;

-- Returns 4 rows
-- Took 11s

-- CARRIER
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America
