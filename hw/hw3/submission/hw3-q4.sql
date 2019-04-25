--hw3-q4
-- 1. Obtain list of cities that has Seattle as origin city
-- 2. Obtain list of cities that can be reached from Seattle with one stop (refer to hw2-q2)
-- Make sure the destination city is not Seattle.
-- 3. Obtain list where the dest_city is not in first table but is in second table

SELECT f.dest_city as city
FROM Flights as f
WHERE NOT EXISTS (SELECT f1.dest_city
                    FROM Flights as f1
                    WHERE f1.origin_city = 'Seattle WA'
                    AND f1.dest_city = f.dest_city
                    GROUP BY f1.dest_city)
      AND EXISTS (SELECT f2.dest_city as dest_city
      FROM Flights as f1, Flights as f2
      WHERE f1.dest_city = f2.origin_city
      AND f1.origin_city = 'Seattle WA'
      AND f2.dest_city != 'Seattle WA'
      AND f2.dest_city = f.dest_city
      GROUP BY f2.dest_city)
GROUP BY f.dest_city
ORDER BY f.dest_city asc;

-- Returns 256 rows
-- Took 10 m 16 s 466 ms in DataGrip

-- city
-- Aberdeen SD
-- Abilene TX
-- Adak Island AK
-- Aguadilla PR
-- Akron OH
-- Albany GA
-- Albany NY
-- Alexandria LA
-- Allentown/Bethlehem/Easton PA
-- Alpena MI
-- Amarillo TX
-- Appleton WI
-- Arcata/Eureka CA
-- Asheville NC
-- Ashland WV
-- Aspen CO
-- Atlantic City NJ
-- Augusta GA
-- Bakersfield CA
-- Bangor ME
