--hw2-q3
SELECT w.day_of_week as day_of_week, AVG(f.arrival_delay) as delay
FROM flights as f, Weekdays as w
WHERE f.day_of_week_id = w.did
GROUP BY f.day_of_week_id
ORDER BY AVG(f.arrival_delay) desc
Limit 1;

--output: day_of_week, delay
