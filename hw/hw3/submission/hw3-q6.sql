--hw3-q6

SELECT C.name as carrier
FROM Carriers as c, (SELECT f.carrier_id as id
                      FROM Flights as f
                      WHERE f.origin_city = 'Seattle WA'
                      AND f.dest_city = 'San Francisco CA') as f
WHERE c.cid = f.id
GROUP BY c.name
ORDER BY c.name asc;

-- Returns 4 rows
-- Took 12s

-- CARRIER
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America
