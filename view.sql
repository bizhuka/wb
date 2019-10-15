-- CREATE BTREE INDEX idx_req_wb ON REQHEADER(WAYBILL_ID);
-- CREATE BTREE INDEX idx_sch_wb ON SCHEDULE(WAYBILL_ID);


CREATE VIEW "v_count_wb" AS
SELECT w."werks", w."status", count(*) as "cnt"
FROM "wb.db::pack.waybill" as w
GROUP BY w."werks", w."status"
ORDER BY w."werks", w."status";

CREATE VIEW "v_count_req" AS
SELECT r."iwerk" as "werks", r."statusreason" as "status", count(*) as "cnt"
FROM "wb.db::pack.reqheader" as r
GROUP BY r."iwerk", r."statusreason"
ORDER BY r."iwerk", r."statusreason";

CREATE VIEW "v_waybill" AS
SELECT w.*, dr."fio",
       e."eqktx", e."point", e."imei", e."mptyp", e."wialonid", e."license_num", e."tooname", e."petrolmode", e."anln1", e."ktschtxt",
       (SELECT COUNT (*) FROM "wb.db::pack.reqheader" as r WHERE r."waybill_id" = w."id") AS "req_cnt",
       (SELECT COUNT (*) FROM "wb.db::pack.schedule" as s WHERE s."waybill_id" = w."id") AS "sch_cnt",
       (SELECT COUNT (*) FROM "wb.db::pack.reqhistory" as h WHERE h."waybill_id" = w."id") AS "hist_cnt",
       (SELECT COUNT (*) FROM "wb.db::pack.gasspent" as g WHERE g."waybill_id" = w."id") AS "gas_cnt"
FROM "wb.db::pack.waybill" as w
         LEFT OUTER JOIN "wb.db::pack.driver" as dr ON w."bukrs" = dr."bukrs" AND w."driver" = dr."pernr"
         LEFT OUTER JOIN "wb.db::pack.equipment" as e ON w."equnr" = e."equnr";

CREATE VIEW "v_reqheader" AS
SELECT
    r.*,
    w."status",
    w."description",
    s."kz" as "statusreason_kz",
    s."ru" as "statusreason_ru",
    to_char(r."gstrp", 'YYYYMMDD') as "gstrpchar",
    to_char(r."gstrp", 'YYYYMMDD') as "gltrpchar"
FROM "wb.db::pack.reqheader" as r
         LEFT OUTER JOIN "wb.db::pack.waybill" as w ON w."id" = r."waybill_id"
         LEFT OUTER JOIN "wb.db::pack.statustext" as s ON s."id" = r."statusreason";


CREATE VIEW "v_driver" AS
SELECT dr.*
FROM "wb.db::pack.driver" as dr;
-- WHERE EXTRACT(YEAR FROM dr."validdate") = EXTRACT(YEAR FROM CURRENT_DATE) AND
--       EXTRACT(MONTH FROM dr."validdate") = EXTRACT(MONTH FROM CURRENT_DATE) AND
--       EXTRACT(DAY FROM dr."validdate") = EXTRACT(DAY FROM CURRENT_DATE);

CREATE VIEW "v_gasspent" AS
SELECT g.*,
       t."maktx",
       e."equnr",
       e."eqktx",
       e."point",
       e."imei",
       e."mptyp",
       e."wialonid",
       e."license_num",
       e."tooname",
       e."petrolmode",
       e."anln1",
       e."ktschtxt",
       w."id",
       w."werks",

       w."createdate",
       w."fromdate",
       w."todate",
       w."garagedepdate",
       w."garagearrdate",

       w."ododiff",
       w."motohour",
       w."description",
       w."status",
       w."spent1",
       w."spent2",
       w."spent4",
       CASE
           WHEN g."pttype" = 1 THEN 'Негізгі бак'
           WHEN g."pttype" = 2 THEN 'Жоғары жабдықтау'
           WHEN g."pttype" = 4 THEN 'Күрке'
           END as "pttype_kz",

       CASE
           WHEN g."pttype" = 1 THEN 'Основной бак'
           WHEN g."pttype" = 2 THEN 'Верхнее оборудование'
           WHEN g."pttype" = 4 THEN 'Будка'
           END as "pttype_ru",

       CASE
           WHEN g."pttype" = 1 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent1" THEN w."spent1"
                                         ELSE g."gasbefore" + g."gasgiven" END)
           WHEN g."pttype" = 2 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent2" THEN w."spent2"
                                         ELSE g."gasbefore" + g."gasgiven" END)
           WHEN g."pttype" = 4 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent4" THEN w."spent4"
                                         ELSE g."gasbefore" + g."gasgiven" END)
           END as "gasspent",

       CASE
           WHEN g."pttype" = 1 THEN (g."gasbefore" + g."gasgiven" - w."spent1")
           WHEN g."pttype" = 2 THEN (g."gasbefore" + g."gasgiven" - w."spent2")
           WHEN g."pttype" = 4 THEN (g."gasbefore" + g."gasgiven" - w."spent4")
           END as "gasafter_next",

       CASE
           WHEN g."pttype" = 1 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent1"
                                             THEN g."gasbefore" + g."gasgiven" - w."spent1"
                                         ELSE 0 END)
           WHEN g."pttype" = 2 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent2"
                                             THEN g."gasbefore" + g."gasgiven" - w."spent2"
                                         ELSE 0 END)
           WHEN g."pttype" = 4 THEN (CASE
                                         WHEN g."gasbefore" + g."gasgiven" >= w."spent4"
                                             THEN g."gasbefore" + g."gasgiven" - w."spent4"
                                         ELSE 0 END)
           END as "gasafter"
FROM "wb.db::pack.gasspent" as g
         LEFT OUTER JOIN "wb.db::pack.waybill" as w ON w."id" = g."waybill_id"
         LEFT OUTER JOIN "wb.db::pack.equipment" as e ON w."equnr" = e."equnr"
         LEFT OUTER JOIN "wb.db::pack.gastype" as t ON g."gasmatnr" = t."matnr"
ORDER BY g."waybill_id", g."pttype", g."pos";