--hw3-q5
-- 1. List of all cities that has Seattle as origin_city
-- 2. List of all cities that can be reach from Seattle with one-stop
-- 3. Not in both lists

SELECT f.dest_city as city
FROM Flights as f
WHERE NOT EXISTS (SELECT f1.dest_city
                    FROM Flights as f1
                    WHERE f1.origin_city = 'Seattle WA'
                    AND f1.dest_city = f.dest_city
                    GROUP BY f1.dest_city)
      AND NOT EXISTS (SELECT f2.dest_city as dest_city
      FROM Flights as f1, Flights as f2
      WHERE f1.dest_city = f2.origin_city
      AND f1.origin_city = 'Seattle WA'
      AND f2.dest_city != 'Seattle WA'
      AND f2.dest_city = f.dest_city
      GROUP BY f2.dest_city)
GROUP BY f.dest_city
ORDER BY f.dest_city asc;

-- Returns 3 rows
-- Took 10 m 49 s 886 ms in DataGrip

-- city
-- Devils Lake ND
-- Hattiesburg/Laurel MS
-- St. Augustine FL
