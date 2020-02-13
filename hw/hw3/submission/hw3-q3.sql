-- --hw3-q3
-- -- 1. obtain list of short flights with count
-- -- 2. obtain list of long flights with count
-- -- 3. Outer join the two lists and do math to find percentage

SELECT allf.city as city, shortf.count * 100.0 / allf.count AS percentage
FROM  (SELECT f2.origin_city as city, COUNT(*) as count
      FROM Flights as f2
      GROUP BY f2.origin_city) as allf
      LEFT OUTER JOIN
      (SELECT f1.origin_city as city, COUNT(*) as count
      FROM Flights as f1
      WHERE f1.actual_time < 180
      GROUP BY f1.origin_city) as shortf
      ON shortf.city = allf.city
ORDER BY percentage asc;

-- Returns 327 rows
-- Took 46s in Azure Query Editor

-- CITY	PERCENTAGE
-- Guam TT
-- Pago Pago TT
-- Aguadilla PR	29.43396226
-- Anchorage AK	32.1460374
-- San Juan PR	33.89036071
-- Charlotte Amalie VI	40
-- Ponce PR	41.93548387
-- Fairbanks AK	50.69124424
-- Kahului HI	53.66499853
-- Honolulu HI	54.90880869
-- San Francisco CA	56.30765683
-- Los Angeles CA	56.60410765
-- Seattle WA	57.75541655
-- Long Beach CA	62.45411641
-- Kona HI	63.28210757
-- New York NY	63.48151977
-- Las Vegas NV	65.16300929
-- Christiansted VI	65.33333333
-- Newark NJ	67.13735558
-- Worcester MA	67.74193548
