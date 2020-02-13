--hw3-q2
-- 1. Find a list of origin_city with flights longer than 3 hours.
-- 2. Get the list of origin_city that aren't in the above list.

SELECT DISTINCT f.origin_city as city
FROM Flights as f
WHERE f.origin_city NOT IN (SELECT f1.origin_city
                    FROM Flights as f1
                    WHERE f1.actual_time >= 180)
ORDER BY f.origin_city;

-- Returns 109 rows
-- Took 114s in Azure Query Editor

-- CITY
-- Aberdeen SD
-- Abilene TX
-- Alpena MI
-- Ashland WV
-- Augusta GA
-- Barrow AK
-- Beaumont/Port Arthur TX
-- Bemidji MN
-- Bethel AK
-- Binghamton NY
-- Brainerd MN
-- Bristol/Johnson City/Kingsport TN
-- Butte MT
-- Carlsbad CA
-- Casper WY
-- Cedar City UT
-- Chico CA
-- College Station/Bryan TX
-- Columbia MO
-- Columbus GA
