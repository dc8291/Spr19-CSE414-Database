--hw3-q1
-- 1. Obtain list of max time for each city
-- 2. Find dest_city and origin_city from that list.

SELECT f.origin_city as origin_city, f.dest_city as dest_city, max(f.actual_time) as time
FROM Flights as f, (SELECT f1.origin_city as origin_city, max(f1.actual_time) as maxtime
                    FROM Flights as f1
                    GROUP BY f1.origin_city) as longf
WHERE f.origin_city = longf.origin_city
AND f.actual_time = longf.maxtime
GROUP BY f.origin_city, f.dest_city
ORDER BY f.origin_city, f.dest_city;

-- Returns 334 rows
-- Took 31s in Azure Query Editor

-- ORIGIN_CITY	DEST_CITY	TIME
-- Aberdeen SD	Minneapolis MN	106
-- Abilene TX	Dallas/Fort Worth TX	111
-- Adak Island AK	Anchorage AK	471
-- Aguadilla PR	New York NY	368
-- Akron OH	Atlanta GA	408
-- Albany GA	Atlanta GA	243
-- Albany NY	Atlanta GA	390
-- Albuquerque NM	Houston TX	492
-- Alexandria LA	Atlanta GA	391
-- Allentown/Bethlehem/Easton PA	Atlanta GA	456
-- Alpena MI	Detroit MI	80
-- Amarillo TX	Houston TX	390
-- Anchorage AK	Barrow AK	490
-- Appleton WI	Atlanta GA	405
-- Arcata/Eureka CA	San Francisco CA	476
-- Asheville NC	Chicago IL	279
-- Ashland WV	Cincinnati OH	84
-- Aspen CO	Los Angeles CA	304
-- Atlanta GA	Honolulu HI	649
-- Atlantic City NJ	Fort Lauderdale FL	212
