-- 일간 집계 쿼리
SELECT
    agg.Daily,
    agg.Steps,
    agg.calories,
    agg.distance,
    s.record_key AS recordkey
FROM (
         SELECT
             period_date AS Daily,
             source_id,
             SUM(steps) AS Steps,
             SUM(calories_value) AS calories,
             SUM(distance_value) AS distance
         FROM kb_healthcare.record
         GROUP BY period_date, source_id
     ) agg
         INNER JOIN kb_healthcare.source s ON agg.source_id = s.id
ORDER BY agg.Daily ASC
;

-- 월간 집계 쿼리
SELECT
    agg.Monthly,
    agg.Steps,
    agg.calories,
    agg.distance,
    s.record_key AS recordkey
FROM (
         SELECT
             DATE_FORMAT(period_date, '%Y-%m') AS Monthly,
             source_id,
             SUM(steps) AS Steps,
             SUM(calories_value) AS calories,
             SUM(distance_value) AS distance
         FROM kb_healthcare.record
         GROUP BY DATE_FORMAT(period_date, '%Y-%m'), source_id
     ) agg
         INNER JOIN kb_healthcare.source s ON agg.source_id = s.id
ORDER BY agg.Monthly ASC
;