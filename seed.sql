-- Mobile frontend smoke-test seed for Docker / local MySQL
--
-- Purpose:
-- - Creates two organizations with enough data to test all 4 mobile tabs
--   and the organization switcher:
--   1. Rutiner
--   2. Logging
--   3. Kartlegging og tiltak
--   4. Avvik
-- - Creates one primary worker user you can log in as
-- - Seeds a mix of pending/completed routines and CCPs in the first org
-- - Seeds pending routines and CCPs in the second org
-- - Seeds mapping points in both orgs
-- - Leaves deviations empty so new submissions are easy to verify
--
-- Primary login for mobile testing:
--   Email: worker.mobiletest@grimni.local
--   Password: MobileTest123!
--
-- Secondary users:
--   owner.mobiletest@grimni.local   / OwnerTest123!
--   manager.mobiletest@grimni.local / ManagerTest123!
--   deputy.mobiletest@grimni.local  / DeputyTest123!
--
-- Suggested manual mobile test flow:
-- - Log in as worker.mobiletest@grimni.local
-- - Confirm you start in "Grimni Mobile QA Oslo"
-- - Rutiner: complete the pending routines
-- - Logging:
--   - submit "3.2" on "Kjølerom temperatur" for a normal in-range measurement
--   - submit "7.5" on "Varmholding buffet" to trigger "Registrer avvik"
-- - Avvik: submit the deviation from the out-of-range CCP
-- - Kartlegging og tiltak: verify the mapping points load from DB
-- - Switch organization to "Grimni Mobile QA Bergen"
-- - Confirm all four tabs now show the Bergen seed data

START TRANSACTION;

SET NAMES utf8mb4;

SET @seed_org_number = 910500321;
SET @seed_org_name = 'Grimni Mobile QA Oslo';
SET @seed_org_number_2 = 910500322;
SET @seed_org_name_2 = 'Grimni Mobile QA Bergen';

SET @seed_owner_email = 'owner.mobiletest@grimni.local';
SET @seed_manager_email = 'manager.mobiletest@grimni.local';
SET @seed_worker_email = 'worker.mobiletest@grimni.local';
SET @seed_deputy_email = 'deputy.mobiletest@grimni.local';

SET @seed_owner_hash = '$2y$10$E53k8Mk8VX0jJWNT4D7oGegtIJMmgQC9CcxfQ6YTsnS5FqBb8JLKi';
SET @seed_manager_hash = '$2y$10$p4VDD7l5tsqtARTOecPjcepEdsFKYuwEY1/8WUgEyfPXbEbYjI/rC';
SET @seed_worker_hash = '$2y$10$TL1Gs7siEA3aUKiap/qTPuIQRNZopdJVsDmMAhM6ztq0KLSonzMyC';
SET @seed_deputy_hash = '$2y$10$W6SekPGzVf4jWPFf1IJUp.tBvCwCZ7tiEJeN2h1nfUdt9ECNXhh6C';

DROP TEMPORARY TABLE IF EXISTS seed_org_ids;
DROP TEMPORARY TABLE IF EXISTS seed_interval_ids;

CREATE TEMPORARY TABLE seed_org_ids (
    id INT PRIMARY KEY
);

CREATE TEMPORARY TABLE seed_interval_ids (
    id INT PRIMARY KEY
);

INSERT IGNORE INTO seed_org_ids (id)
SELECT id
FROM organization
WHERE org_number IN (@seed_org_number, @seed_org_number_2);

INSERT IGNORE INTO seed_interval_ids (id)
SELECT interval_id
FROM prerequisite_routine
WHERE org_id IN (SELECT id FROM seed_org_ids)
  AND interval_id IS NOT NULL;

INSERT IGNORE INTO seed_interval_ids (id)
SELECT interval_id
FROM ccp
WHERE org_id IN (SELECT id FROM seed_org_ids)
  AND interval_id IS NOT NULL;

DELETE FROM deviation
WHERE org_id IN (SELECT id FROM seed_org_ids);

DELETE FROM prerequisite_routine_record
WHERE org_id IN (SELECT id FROM seed_org_ids);

DELETE FROM ccp_record
WHERE org_id IN (SELECT id FROM seed_org_ids);

DELETE FROM refresh_token
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE email IN (
        @seed_owner_email,
        @seed_manager_email,
        @seed_worker_email,
        @seed_deputy_email
    )
);

DELETE FROM organization
WHERE id IN (SELECT id FROM seed_org_ids);

DELETE FROM interval_rule
WHERE id IN (SELECT id FROM seed_interval_ids);

DELETE FROM users
WHERE email IN (
    @seed_owner_email,
    @seed_manager_email,
    @seed_worker_email,
    @seed_deputy_email
);

INSERT INTO organization (
    org_address,
    org_name,
    alcohol_enabled,
    food_enabled,
    org_number,
    created_at
) VALUES (
    'Storgata 18, 0184 Oslo',
    @seed_org_name,
    TRUE,
    TRUE,
    @seed_org_number,
    CURRENT_TIMESTAMP
);

SET @org_id = LAST_INSERT_ID();

INSERT INTO organization (
    org_address,
    org_name,
    alcohol_enabled,
    food_enabled,
    org_number,
    created_at
) VALUES (
    'Bryggen 42, 5003 Bergen',
    @seed_org_name_2,
    TRUE,
    TRUE,
    @seed_org_number_2,
    CURRENT_TIMESTAMP
);

SET @org_id_2 = LAST_INSERT_ID();

INSERT INTO users (password_data, legal_name, email, created_at) VALUES
(@seed_owner_hash, 'Olivia Owner', @seed_owner_email, CURRENT_TIMESTAMP),
(@seed_manager_hash, 'Mikael Manager', @seed_manager_email, CURRENT_TIMESTAMP),
(@seed_worker_hash, 'Wanda Worker', @seed_worker_email, CURRENT_TIMESTAMP),
(@seed_deputy_hash, 'Daniel Deputy', @seed_deputy_email, CURRENT_TIMESTAMP);

SET @owner_user_id = (SELECT id FROM users WHERE email = @seed_owner_email LIMIT 1);
SET @manager_user_id = (SELECT id FROM users WHERE email = @seed_manager_email LIMIT 1);
SET @worker_user_id = (SELECT id FROM users WHERE email = @seed_worker_email LIMIT 1);
SET @deputy_user_id = (SELECT id FROM users WHERE email = @seed_deputy_email LIMIT 1);

INSERT INTO org_user_bridge (org_id, user_id, user_role, created_at) VALUES
(@org_id, @owner_user_id, 'OWNER', CURRENT_TIMESTAMP),
(@org_id, @manager_user_id, 'MANAGER', CURRENT_TIMESTAMP),
(@org_id, @worker_user_id, 'WORKER', CURRENT_TIMESTAMP),
(@org_id, @deputy_user_id, 'WORKER', CURRENT_TIMESTAMP),
(@org_id_2, @owner_user_id, 'OWNER', CURRENT_TIMESTAMP),
(@org_id_2, @manager_user_id, 'MANAGER', CURRENT_TIMESTAMP),
(@org_id_2, @worker_user_id, 'WORKER', CURRENT_TIMESTAMP),
(@org_id_2, @deputy_user_id, 'WORKER', CURRENT_TIMESTAMP);

INSERT INTO prerequisite_category (category_name, org_id, created_at) VALUES
('Mottak og åpning', @org_id, CURRENT_TIMESTAMP),
('Hygiene og renhold', @org_id, CURRENT_TIMESTAMP),
('Stenging', @org_id, CURRENT_TIMESTAMP),
('Mottak Bergen', @org_id_2, CURRENT_TIMESTAMP),
('Servering Bergen', @org_id_2, CURRENT_TIMESTAMP);

SET @category_opening_id = (
    SELECT id FROM prerequisite_category
    WHERE org_id = @org_id AND category_name = 'Mottak og åpning'
    LIMIT 1
);
SET @category_hygiene_id = (
    SELECT id FROM prerequisite_category
    WHERE org_id = @org_id AND category_name = 'Hygiene og renhold'
    LIMIT 1
);
SET @category_closing_id = (
    SELECT id FROM prerequisite_category
    WHERE org_id = @org_id AND category_name = 'Stenging'
    LIMIT 1
);
SET @category_bergen_opening_id = (
    SELECT id FROM prerequisite_category
    WHERE org_id = @org_id_2 AND category_name = 'Mottak Bergen'
    LIMIT 1
);
SET @category_bergen_service_id = (
    SELECT id FROM prerequisite_category
    WHERE org_id = @org_id_2 AND category_name = 'Servering Bergen'
    LIMIT 1
);

SET @now_epoch = UNIX_TIMESTAMP(UTC_TIMESTAMP());

INSERT INTO interval_rule (interval_start, interval_repeat_time, created_at) VALUES
(@now_epoch - 7200, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 7500, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 7800, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 8100, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 8400, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 8700, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 9000, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 9300, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 9600, 86400, CURRENT_TIMESTAMP),
(@now_epoch - 9900, 86400, CURRENT_TIMESTAMP);

SET @routine_interval_1 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 7200 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @routine_interval_2 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 7500 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @routine_interval_3 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 7800 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @ccp_interval_1 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 8100 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @ccp_interval_2 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 8400 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @ccp_interval_3 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 8700 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @routine_interval_4 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 9000 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @routine_interval_5 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 9300 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @ccp_interval_4 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 9600 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);
SET @ccp_interval_5 = (
    SELECT id FROM interval_rule
    WHERE interval_start = @now_epoch - 9900 AND interval_repeat_time = 86400
    ORDER BY id DESC LIMIT 1
);

INSERT INTO prerequisite_routine (
    immediate_corrective_action,
    title,
    prerequisite_category_id,
    prerequisite_description,
    org_id,
    interval_id,
    created_at
) VALUES
(
    'Hold varene adskilt og varsle skiftleder dersom temperaturen er for høy.',
    'Mottakskontroll kjølerom',
    @category_opening_id,
    'Kontroller kjølerommet før åpning og bekreft at temperatur og vareplassering er riktig.',
    @org_id,
    @routine_interval_1,
    CURRENT_TIMESTAMP
),
(
    'Desinfiser håndvaskstasjonen på nytt og fyll opp forbruksmateriell før drift.',
    'Sjekk håndvaskstasjon',
    @category_hygiene_id,
    'Bekreft at såpe, papir og ren sone rundt håndvasken er klar før skiftstart.',
    @org_id,
    @routine_interval_2,
    CURRENT_TIMESTAMP
),
(
    'Fullfør renholdet før lokalet stenges og noter eventuelle mangler.',
    'Stengekontroll renhold',
    @category_closing_id,
    'Kontroller kontaktflater, gulv og avfallshåndtering før lokalet låses.',
    @org_id,
    @routine_interval_3,
    CURRENT_TIMESTAMP
),
(
    'Stans vareinnsett og varsle skiftleder dersom mottaket ikke kan godkjennes.',
    'Mottakskontroll fiskedisk',
    @category_bergen_opening_id,
    'Kontroller temperatur, lukt og merking på sjømatleveransen før den settes på lager.',
    @org_id_2,
    @routine_interval_4,
    CURRENT_TIMESTAMP
),
(
    'Hold buffet lukket til kontrollen er fullført og noter årsaken dersom den utsettes.',
    'Sjekk allergenmerking buffet',
    @category_bergen_service_id,
    'Bekreft at allergener og dagens merking stemmer før serveringen åpnes.',
    @org_id_2,
    @routine_interval_5,
    CURRENT_TIMESTAMP
);

SET @routine_1_id = (
    SELECT id FROM prerequisite_routine
    WHERE org_id = @org_id AND title = 'Mottakskontroll kjølerom'
    ORDER BY id DESC LIMIT 1
);
SET @routine_2_id = (
    SELECT id FROM prerequisite_routine
    WHERE org_id = @org_id AND title = 'Sjekk håndvaskstasjon'
    ORDER BY id DESC LIMIT 1
);
SET @routine_3_id = (
    SELECT id FROM prerequisite_routine
    WHERE org_id = @org_id AND title = 'Stengekontroll renhold'
    ORDER BY id DESC LIMIT 1
);
SET @routine_4_id = (
    SELECT id FROM prerequisite_routine
    WHERE org_id = @org_id_2 AND title = 'Mottakskontroll fiskedisk'
    ORDER BY id DESC LIMIT 1
);
SET @routine_5_id = (
    SELECT id FROM prerequisite_routine
    WHERE org_id = @org_id_2 AND title = 'Sjekk allergenmerking buffet'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO routine_user_bridge (user_id, routine_id, user_role, created_at) VALUES
(@worker_user_id, @routine_1_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@manager_user_id, @routine_1_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@worker_user_id, @routine_2_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@owner_user_id, @routine_2_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@worker_user_id, @routine_3_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@deputy_user_id, @routine_3_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@worker_user_id, @routine_4_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@manager_user_id, @routine_4_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@worker_user_id, @routine_5_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@owner_user_id, @routine_5_id, 'DEPUTY', CURRENT_TIMESTAMP);

INSERT INTO ccp (
    how,
    equipment,
    instructions_and_calibration,
    immediate_corrective_action,
    critical_min,
    critical_max,
    unit,
    ccp_name,
    monitored_description,
    org_id,
    interval_id,
    created_at
) VALUES
(
    'Les av digitalt display og bekreft mot intern sensor.',
    'Kjølerom hovedlinje',
    'Kontroller displayet og noter faktisk temperatur som ett desimalpunkt.',
    'Flytt varer til reservekjøl og varsle ansvarlig leder.',
    0.00,
    4.00,
    'C',
    'Kjølerom temperatur',
    'Temperatur i hovedkjølerom',
    @org_id,
    @ccp_interval_1,
    CURRENT_TIMESTAMP
),
(
    'Les av termometeret i buffetlinjen før servering.',
    'Buffet varmebenk',
    'Kontroller at proben står riktig plassert før målingen logges.',
    'Stans servering, sett maten tilbake til sikker temperatur og registrer avvik.',
    60.00,
    75.00,
    'C',
    'Varmholding buffet',
    'Temperatur i varmholding før lunsjservering',
    @org_id,
    @ccp_interval_2,
    CURRENT_TIMESTAMP
),
(
    'Les av hygrometeret og registrer høyeste målte verdi.',
    'Tørrlager',
    'Vent 10 sekunder før avlesning etter at døren er lukket.',
    'Fjern utsatte varer og vurder behov for avfukting.',
    30.00,
    55.00,
    '%',
    'Tørrlager luftfuktighet',
    'Luftfuktighet i tørrlager',
    @org_id,
    @ccp_interval_3,
    CURRENT_TIMESTAMP
),
(
    'Les av displayet på kjølebenken før åpning.',
    'Kjølebenk sjømat',
    'Bekreft målingen mot intern probe og registrer én desimal.',
    'Flytt produktene til reservekjøl og varsle ansvarlig leder umiddelbart.',
    0.00,
    4.00,
    'C',
    'Kjølebenk temperatur Bergen',
    'Temperatur i sjømatkjølebenk',
    @org_id_2,
    @ccp_interval_4,
    CURRENT_TIMESTAMP
),
(
    'Mål kjernetemperaturen i suppekjelen før servering.',
    'Suppekjele',
    'Kalibrer termometeret ved skiftstart og registrer målingen med én desimal.',
    'Stans servering, varm opp produktet på nytt og registrer avvik ved for lav temperatur.',
    65.00,
    82.00,
    'C',
    'Suppekjele temperatur Bergen',
    'Temperatur i varm suppe før servering',
    @org_id_2,
    @ccp_interval_5,
    CURRENT_TIMESTAMP
);

SET @ccp_1_id = (
    SELECT id FROM ccp
    WHERE org_id = @org_id AND ccp_name = 'Kjølerom temperatur'
    ORDER BY id DESC LIMIT 1
);
SET @ccp_2_id = (
    SELECT id FROM ccp
    WHERE org_id = @org_id AND ccp_name = 'Varmholding buffet'
    ORDER BY id DESC LIMIT 1
);
SET @ccp_3_id = (
    SELECT id FROM ccp
    WHERE org_id = @org_id AND ccp_name = 'Tørrlager luftfuktighet'
    ORDER BY id DESC LIMIT 1
);
SET @ccp_4_id = (
    SELECT id FROM ccp
    WHERE org_id = @org_id_2 AND ccp_name = 'Kjølebenk temperatur Bergen'
    ORDER BY id DESC LIMIT 1
);
SET @ccp_5_id = (
    SELECT id FROM ccp
    WHERE org_id = @org_id_2 AND ccp_name = 'Suppekjele temperatur Bergen'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO ccp_user_bridge (user_id, ccp_id, user_role, created_at) VALUES
(@worker_user_id, @ccp_1_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_1_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_1_id, 'DEVIATION_RECEIVER', CURRENT_TIMESTAMP),
(@worker_user_id, @ccp_2_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@owner_user_id, @ccp_2_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_2_id, 'DEVIATION_RECEIVER', CURRENT_TIMESTAMP),
(@worker_user_id, @ccp_3_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@deputy_user_id, @ccp_3_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@worker_user_id, @ccp_4_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_4_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_4_id, 'DEVIATION_RECEIVER', CURRENT_TIMESTAMP),
(@worker_user_id, @ccp_5_id, 'PERFORMER', CURRENT_TIMESTAMP),
(@owner_user_id, @ccp_5_id, 'DEPUTY', CURRENT_TIMESTAMP),
(@manager_user_id, @ccp_5_id, 'DEVIATION_RECEIVER', CURRENT_TIMESTAMP);

INSERT INTO prerequisite_routine_record (
    org_id,
    routine_id,
    performed_by,
    result_status,
    comment,
    created_at
) VALUES
(
    @org_id,
    @routine_2_id,
    @worker_user_id,
    'COMPLETED',
    'Håndvaskstasjonen ble kontrollert ved skiftstart.',
    FROM_UNIXTIME(@now_epoch - 900)
),
(
    @org_id,
    @routine_1_id,
    @worker_user_id,
    'COMPLETED',
    'Historisk registrering fra forrige periode.',
    FROM_UNIXTIME(@now_epoch - 172800)
),
(
    @org_id,
    @routine_3_id,
    @worker_user_id,
    'COMPLETED',
    'Historisk stengekontroll fra forrige periode.',
    FROM_UNIXTIME(@now_epoch - 259200)
),
(
    @org_id_2,
    @routine_4_id,
    @worker_user_id,
    'COMPLETED',
    'Historisk mottakskontroll fra forrige skift i Bergen.',
    FROM_UNIXTIME(@now_epoch - 172800)
);

INSERT INTO ccp_record (
    org_id,
    ccp_id,
    performed_by,
    comment,
    measured_value,
    critical_min,
    critical_max,
    unit,
    ccp_name,
    created_at
) VALUES
(
    @org_id,
    @ccp_3_id,
    @worker_user_id,
    'Fuktigheten er kontrollert for denne perioden.',
    44.00,
    30.00,
    55.00,
    '%',
    'Tørrlager luftfuktighet',
    FROM_UNIXTIME(@now_epoch - 1200)
),
(
    @org_id,
    @ccp_1_id,
    @worker_user_id,
    'Historisk normalverdi fra forrige periode.',
    3.40,
    0.00,
    4.00,
    'C',
    'Kjølerom temperatur',
    FROM_UNIXTIME(@now_epoch - 172800)
),
(
    @org_id,
    @ccp_2_id,
    @worker_user_id,
    'Historisk høy verdi fra tidligere test.',
    71.00,
    60.00,
    75.00,
    'C',
    'Varmholding buffet',
    FROM_UNIXTIME(@now_epoch - 259200)
),
(
    @org_id_2,
    @ccp_4_id,
    @worker_user_id,
    'Historisk bergensk kjølebenkmåling fra forrige periode.',
    2.80,
    0.00,
    4.00,
    'C',
    'Kjølebenk temperatur Bergen',
    FROM_UNIXTIME(@now_epoch - 172800)
);

INSERT INTO mapping_point (
    title,
    challenges,
    measures,
    responsible_for_point,
    law,
    severity_dots,
    org_id,
    created_at
) VALUES
(
    'Salg eller utlevering til personer under 18 år',
    'Mindreårige kan bruke lånt legitimasjon eller forsøke kjøp via eldre venner.',
    'Be om legitimasjon av alle som ser ut til å være under 25 år, og nekt salg ved tvil.',
    'Den som står i kassen',
    'AL § 1-5',
    8,
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Salg til åpenbart påvirket person',
    'Høyt tempo og kø kan gjøre vurderingene utydelige for ansatte.',
    'Bruk tydelig intern rutine for observasjon, tilkall kollega ved tvil og nekt salg ved behov.',
    'Skiftleder og serveringsansvarlig',
    'AL § 8-11',
    6,
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Mangelfull intern opplæring i alkoholregelverket',
    'Nye ansatte kjenner ikke alle kontrollpunkter ved legitimasjon og salgsnekt.',
    'Gjennomfør fast opplæringsløp ved oppstart og repeter regelverk på personalmøter.',
    'Daglig leder',
    'AL § 1-9',
    4,
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Manglende kontroll ved uteservering',
    'Høyt tempo i uteserveringen kan gjøre legitimasjonskontrollen svakere.',
    'Bruk fast dørvakt ved behov og krev legitimasjon ved tvil i alle skift.',
    'Skiftleder uteområde',
    'AL § 1-5',
    7,
    @org_id_2,
    CURRENT_TIMESTAMP
),
(
    'Utilstrekkelig intern kommunikasjon om salgsnekt',
    'Ansatte kan være usikre på når kollega skal tilkalles ved salgsnekt.',
    'Repeter rutinen i oppstartsmøtet og bruk en tydelig eskaleringsplan i travle skift.',
    'Restaurantsjef',
    'AL § 8-11',
    5,
    @org_id_2,
    CURRENT_TIMESTAMP
);

INSERT INTO product_category (
    product_name,
    product_description,
    org_id,
    created_at
) VALUES
(
    'Kjølevarer',
    'Ferske meieriprodukter, kjøtt og fisk som krever kjølekjede.',
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Varmretter',
    'Ferdiglagede varmretter til buffet og servering.',
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Tørrvarer',
    'Lagerførte tørrvarer som mel, ris og krydder.',
    @org_id,
    CURRENT_TIMESTAMP
),
(
    'Sjømat Bergen',
    'Fersk sjømat mottatt daglig i Bergen.',
    @org_id_2,
    CURRENT_TIMESTAMP
),
(
    'Varme supper Bergen',
    'Varme supper laget på stedet for servering.',
    @org_id_2,
    CURRENT_TIMESTAMP
);

COMMIT;

-- =========================================================
-- Verification queries
-- Run these after testing in the mobile frontend.
-- =========================================================

-- 1. Confirm seeded users and orgs
SELECT
    o.id AS org_id,
    o.org_name,
    o.org_number,
    u.id AS user_id,
    u.legal_name,
    u.email,
    oub.user_role
FROM organization o
JOIN org_user_bridge oub ON oub.org_id = o.id
JOIN users u ON u.id = oub.user_id
WHERE o.org_number IN (@seed_org_number, @seed_org_number_2)
ORDER BY o.id, u.id;

-- 2. Check routines assigned to the primary worker across both orgs
SELECT
    pr.org_id,
    pr.id AS routine_id,
    pr.title,
    pc.category_name,
    rub.user_role,
    ir.interval_start,
    ir.interval_repeat_time
FROM prerequisite_routine pr
JOIN prerequisite_category pc ON pc.id = pr.prerequisite_category_id
JOIN routine_user_bridge rub ON rub.routine_id = pr.id
LEFT JOIN interval_rule ir ON ir.id = pr.interval_id
WHERE pr.org_id IN (@org_id, @org_id_2)
  AND rub.user_id = @worker_user_id
ORDER BY pr.org_id, pr.id, rub.user_role;

-- 3. Check routine records created for the worker
SELECT
    prr.org_id,
    prr.id AS routine_record_id,
    pr.title,
    u.legal_name AS performed_by,
    prr.result_status,
    prr.comment,
    prr.created_at
FROM prerequisite_routine_record prr
LEFT JOIN prerequisite_routine pr ON pr.id = prr.routine_id
LEFT JOIN users u ON u.id = prr.performed_by
WHERE prr.org_id IN (@org_id, @org_id_2)
ORDER BY prr.org_id, prr.created_at DESC;

-- 4. Check CCP assignments for the primary worker across both orgs
SELECT
    c.org_id,
    c.id AS ccp_id,
    c.ccp_name,
    c.monitored_description,
    cub.user_role,
    c.critical_min,
    c.critical_max,
    c.unit
FROM ccp c
JOIN ccp_user_bridge cub ON cub.ccp_id = c.id
WHERE c.org_id IN (@org_id, @org_id_2)
  AND cub.user_id = @worker_user_id
ORDER BY c.org_id, c.id, cub.user_role;

-- 5. Check logged CCP measurements
SELECT
    cr.org_id,
    cr.id AS ccp_record_id,
    cr.ccp_name,
    u.legal_name AS performed_by,
    cr.measured_value,
    cr.unit,
    cr.comment,
    cr.created_at
FROM ccp_record cr
LEFT JOIN users u ON u.id = cr.performed_by
WHERE cr.org_id IN (@org_id, @org_id_2)
ORDER BY cr.org_id, cr.created_at DESC;

-- 6. Check mapping points shown on "Kartlegging og tiltak"
SELECT
    mp.org_id,
    mp.id,
    mp.law,
    mp.severity_dots,
    mp.title,
    mp.responsible_for_point
FROM mapping_point mp
WHERE mp.org_id IN (@org_id, @org_id_2)
ORDER BY mp.org_id, mp.created_at, mp.id;

-- 7. Main deviation verification query after you submit from the mobile Avvik page
SELECT
    d.org_id,
    d.id AS deviation_id,
    d.created_at,
    d.category,
    d.review_status,
    reporter.legal_name AS reported_by,
    d.what_went_wrong,
    d.immediate_action_taken,
    d.potential_cause,
    d.potential_preventative_measure,
    cr.id AS ccp_record_id,
    cr.ccp_name,
    cr.measured_value,
    cr.unit
FROM deviation d
LEFT JOIN users reporter ON reporter.id = d.reported_by
LEFT JOIN ccp_record cr ON cr.id = d.ccp_record_id
WHERE d.org_id IN (@org_id, @org_id_2)
ORDER BY d.org_id, d.created_at DESC;

-- 8. Narrow check for deviations submitted by the worker user
SELECT
    d.org_id,
    d.id,
    d.category,
    d.review_status,
    d.created_at,
    d.ccp_record_id,
    d.what_went_wrong
FROM deviation d
WHERE d.org_id IN (@org_id, @org_id_2)
  AND d.reported_by = @worker_user_id
ORDER BY d.org_id, d.created_at DESC;
