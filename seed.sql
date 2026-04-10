-- Demo seed data for teacher/evaluator use
-- Every seeded account uses the same password:
--   teacherdemo123
--
-- Recommended accounts:
--   teacher.owner@demo.local   -> OWNER in Fjord Bistro, can also switch to North Taproom and Harbor Bakery
--   anna.manager@demo.local    -> MANAGER in Fjord Bistro
--   mia.worker@demo.local      -> WORKER in Fjord Bistro, has mobile routine and CCP assignments
--   ole.tapowner@demo.local    -> OWNER in North Taproom
--   lars.bartender@demo.local  -> WORKER in North Taproom, has mobile routine assignments
--   ida.bakeryowner@demo.local -> OWNER in Harbor Bakery
--   eva.baker@demo.local       -> WORKER in Harbor Bakery, has mobile routine and CCP assignments

INSERT INTO users (id, password_data, legal_name, email, created_at) VALUES
(1, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Teacher Owner', 'teacher.owner@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 90 DAY)),
(2, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Anna Manager', 'anna.manager@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 85 DAY)),
(3, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Mia Worker', 'mia.worker@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 80 DAY)),
(4, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Jonas Worker', 'jonas.worker@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 78 DAY)),
(5, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Sara Quality', 'sara.quality@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 76 DAY)),
(6, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Ole Tapowner', 'ole.tapowner@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 74 DAY)),
(7, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Lars Bartender', 'lars.bartender@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 72 DAY)),
(8, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Emma Shiftlead', 'emma.shiftlead@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 70 DAY)),
(9, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Ida Bakeryowner', 'ida.bakeryowner@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 68 DAY)),
(10, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Noah Bakerymanager', 'noah.bakerymanager@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 66 DAY)),
(11, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Eva Baker', 'eva.baker@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 64 DAY)),
(12, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Liam Auditor', 'liam.auditor@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 62 DAY)),
(13, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Sofie Newhire', 'sofie.newhire@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 45 DAY)),
(14, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Henrik Newhire', 'henrik.newhire@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(15, '$2a$10$bc6qQTDUEuXSX5f6worOe.aI.ryvEu9tbdQYrUbfBjwTOwm9k0f1C', 'Nora Flex', 'nora.flex@demo.local', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY));

INSERT INTO organization (id, org_name, org_address, org_number, alcohol_enabled, food_enabled, created_at) VALUES
(1, 'Fjord Bistro and Bar', 'Bryggekanten 12, Oslo', 910001001, TRUE, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 120 DAY)),
(2, 'North Taproom', 'Torvgata 8, Trondheim', 910001002, TRUE, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 110 DAY)),
(3, 'Harbor Bakery', 'Kaiveien 4, Bergen', 910001003, FALSE, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 100 DAY));

INSERT INTO org_user_bridge (org_id, user_id, user_role, created_at) VALUES
(1, 1, 'OWNER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 120 DAY)),
(1, 2, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 118 DAY)),
(1, 3, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 117 DAY)),
(1, 4, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 116 DAY)),
(1, 5, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 115 DAY)),
(1, 10, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 100 DAY)),
(1, 11, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 96 DAY)),
(1, 12, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 92 DAY)),
(1, 13, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 44 DAY)),
(2, 1, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 109 DAY)),
(2, 6, 'OWNER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 108 DAY)),
(2, 7, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 107 DAY)),
(2, 8, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 106 DAY)),
(2, 14, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(2, 15, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(3, 1, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 99 DAY)),
(3, 9, 'OWNER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 98 DAY)),
(3, 10, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 97 DAY)),
(3, 11, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 95 DAY)),
(3, 12, 'MANAGER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 91 DAY)),
(3, 15, 'WORKER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY));

INSERT INTO org_user_bridge_danger_analysis_collaborator (org_id, user_id, created_at) VALUES
(1, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 119 DAY)),
(1, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 118 DAY)),
(1, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 115 DAY)),
(1, 10, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 100 DAY)),
(2, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 109 DAY)),
(2, 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 108 DAY)),
(2, 8, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 106 DAY)),
(3, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 99 DAY)),
(3, 9, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 98 DAY)),
(3, 10, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 97 DAY)),
(3, 12, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 91 DAY));

INSERT INTO todo (id, task, assigned_to, org_id, created_at) VALUES
(1, 'Review supplier approval list before next audit.', 2, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY)),
(2, 'Update bar incident escalation contacts.', 8, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 4 DAY)),
(3, 'Replace damaged cooling labels in pastry prep area.', 10, 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 DAY)),
(4, 'Confirm allergy matrix posted on prep wall.', 5, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY)),
(5, 'Verify age-check poster placement at both tills.', 6, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 DAY));

INSERT INTO interval_rule (id, interval_start, interval_repeat_time, created_at) VALUES
(1, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)), 86400, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(2, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)), 28800, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)),
(3, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)), 604800, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(4, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY)), 14400, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY)),
(5, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY)), 7200, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY)),
(6, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY)), 604800, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY)),
(7, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 28 DAY)), 86400, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 28 DAY)),
(8, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY)), 21600, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY)),
(9, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY)), 86400, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY)),
(10, UNIX_TIMESTAMP(DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY)), 86400, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY));

INSERT INTO course (id, title, course_description, org_id, created_at) VALUES
(1, 'Food Safety Onboarding', 'Starter training for HACCP basics, reporting, and daily hygiene expectations.', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(2, 'Allergen Control', 'How Fjord Bistro separates allergens, labels prep, and handles guest communication.', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(3, 'Closing Checklist Refresher', 'Short refresher for shift handover, cleaning closeout, and documentation.', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(4, 'Responsible Alcohol Service', 'Core training for age checks, refusal, and incident documentation.', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(5, 'Conflict De-escalation', 'Guest communication and escalation flow for difficult service situations.', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(6, 'Bakery Hygiene Basics', 'Sanitation, allergen handling, and line separation for Harbor Bakery.', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(7, 'Cooling and Labeling', 'Cooling limits, batch labeling, and chilled storage routines for fillings.', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY));

INSERT INTO course_link (id, course_id, link, created_at) VALUES
(1, 1, 'https://demo.local/docs/fjord-bistro/haccp-onboarding', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(2, 2, 'https://demo.local/docs/fjord-bistro/allergen-control', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(3, 3, 'https://demo.local/docs/fjord-bistro/closing-checklist', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(4, 4, 'https://demo.local/docs/north-taproom/responsible-service', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(5, 5, 'https://demo.local/docs/north-taproom/conflict-deescalation', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(6, 6, 'https://demo.local/docs/harbor-bakery/hygiene-basics', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(7, 7, 'https://demo.local/docs/harbor-bakery/cooling-and-labeling', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY));

INSERT INTO user_course_bridge_responsible (course_id, user_id, created_at) VALUES
(1, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(2, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(3, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(4, 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(4, 8, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(5, 8, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(6, 9, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 10, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(7, 10, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY));

INSERT INTO course_user_bridge_progress (course_id, user_id, is_completed, last_updated, created_at) VALUES
(1, 1, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 2, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 3, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 4, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 5, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 13 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 10, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 11, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 11 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 12, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 DAY)),
(1, 13, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(2, 1, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 19 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 2, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 19 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 3, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 4, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 5, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 17 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 10, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 15 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 11, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 7 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 12, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 7 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 58 DAY)),
(2, 13, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(3, 1, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 9 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 2, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 9 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 3, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 9 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 4, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 5, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 10, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 11, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 7 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 12, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 7 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 13, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(4, 1, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(4, 6, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(4, 7, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(4, 8, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 56 DAY)),
(4, 14, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(4, 15, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(5, 1, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(5, 6, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 11 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(5, 7, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(5, 8, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 11 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(5, 14, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(5, 15, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(6, 1, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 9, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 10, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 15 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 11, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 12, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 13 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(6, 15, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(7, 1, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 6 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(7, 9, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(7, 10, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 11 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(7, 11, TRUE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(7, 12, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 6 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(7, 15, FALSE, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY));

INSERT INTO prerequisite_category (id, category_name, org_id, created_at) VALUES
(1, 'Cleaning and sanitation', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 55 DAY)),
(2, 'Receiving and storage', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 54 DAY)),
(3, 'Personal hygiene', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 53 DAY)),
(4, 'Opening procedures', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 50 DAY)),
(5, 'Incident prevention', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 49 DAY)),
(6, 'Production hygiene', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 48 DAY)),
(7, 'Cooling and storage', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 47 DAY));

INSERT INTO prerequisite_standard (id, standard_name, standard_description, prerequisite_category_id, created_at) VALUES
(1, 'Hand wash sink stocked at start of shift', 'Soap, paper, and sanitizer must be checked before service opens.', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 53 DAY)),
(2, 'Separate raw and ready-to-eat storage', 'Raw poultry and seafood must stay below ready-to-eat items in cold storage.', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 52 DAY)),
(3, 'Use color coded sanitation tools', 'Prep and floor tools must stay separated to avoid cross-contamination.', 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 51 DAY)),
(4, 'Verify age-check signage is visible', 'Signs must be visible from both the entrance and both service positions.', 4, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 48 DAY)),
(5, 'Incident log completed before close', 'Any guest conflict, refusal, or intervention must be written up before leaving.', 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 47 DAY)),
(6, 'Cream filling cooled in shallow trays', 'Fresh pastry cream must be spread into shallow trays before chilling.', 7, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 46 DAY)),
(7, 'Gloves changed between allergen tasks', 'Staff must change gloves and sanitize hands when moving between allergen groups.', 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 45 DAY)),
(8, 'Mixer attachments air-dried before reuse', 'Attachments must be fully air-dried to avoid residue and microbial growth.', 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 44 DAY)),
(9, 'Incoming chilled goods are 4C or below', 'Receiving staff must reject or isolate chilled goods above the limit.', 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 43 DAY)),
(10, 'Beer line cleaning documented weekly', 'North Taproom stores the date and performer for every tap line clean.', 4, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 42 DAY));

INSERT INTO prerequisite_routine (id, immediate_corrective_action, title, prerequisite_category_id, prerequisite_description, org_id, interval_id, created_at) VALUES
(1, 'Re-sanitize all touched surfaces and replace any exposed food or utensils.', 'Sanitize prep surfaces', 1, 'Sanitize the prep bench, slicer, and high-touch handles before lunch service.', 1, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(2, 'Quarantine the affected delivery, contact the supplier, and log the temperature issue.', 'Verify chilled deliveries', 2, 'Check that incoming chilled deliveries meet receiving temperature limits.', 1, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(3, 'Repeat the deep clean before next service and escalate any damaged equipment.', 'Weekly line deep clean', 1, 'Deep clean fry station surfaces, hood filters, and surrounding splash zones.', 1, 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(4, 'Pause service, brief the team again, and confirm refusals are handled by the shift lead.', 'Bar opening age control briefing', 4, 'Brief the bar team on age checks, refusal flow, and escalation before opening.', 2, 10, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(5, 'Complete the missing handoff, notify the owner, and review escalation notes before next shift.', 'Incident handoff log review', 5, 'Review the incident log and verify that any previous shift issues are handed over.', 2, 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(6, 'Stop production, re-clean the area, and discard any exposed fillings or toppings.', 'Mixer and bench sanitation', 6, 'Clean the mixer station and surrounding prep bench before pastry production starts.', 3, 7, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(7, 'Relabel the batch, isolate unclear items, and review cooling times with the team.', 'Cooling label verification', 7, 'Confirm that cooled fillings have batch labels, production time, and use-by information.', 3, 9, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 34 DAY));

INSERT INTO routine_user_bridge (user_id, routine_id, user_role, created_at) VALUES
(2, 1, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(5, 1, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(5, 1, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(3, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(4, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(13, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(11, 1, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(2, 2, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(5, 2, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(3, 2, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(4, 2, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(13, 2, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(5, 3, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(5, 3, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(4, 3, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(3, 3, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(8, 4, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(6, 4, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(8, 4, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(7, 4, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(14, 4, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(15, 4, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(6, 5, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(6, 5, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(8, 5, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(15, 5, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(10, 6, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(12, 6, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(11, 6, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(15, 6, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(1, 6, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY)),
(9, 7, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 34 DAY)),
(10, 7, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 34 DAY)),
(10, 7, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 34 DAY)),
(11, 7, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 34 DAY)),
(15, 7, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY));

INSERT INTO product_category (id, product_description, org_id, flowchart, created_at) VALUES
(1, 'Ready-to-eat seafood', 1, '["Receive","Cold store","Prep","Service"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 41 DAY)),
(2, 'Chicken dishes', 1, '["Receive","Cold store","Cook","Hot hold","Service"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 40 DAY)),
(3, 'Dairy desserts', 1, '["Receive","Cold store","Plate","Service"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 39 DAY)),
(4, 'Draught beer and taps', 2, '["Open bar","Purge taps","Serve","Close line"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 38 DAY)),
(5, 'Spirits and cocktails', 2, '["Open spirits","Check measures","Serve","Secure storage"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 37 DAY)),
(6, 'Cream pastries', 3, '["Mix filling","Cool","Assemble","Chill display"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 36 DAY)),
(7, 'Sandwich fillings', 3, '["Prep","Cool","Label","Cold store"]', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 35 DAY));

INSERT INTO danger_risk_combo (id, danger, danger_corrective_measure, severity_score, likelihood_score, product_category_id, created_at) VALUES
(1, 'Temperature abuse during cold holding.', 'Move product to backup cold storage and review door-opening practice.', 5, 3, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(2, 'Cross contamination from raw shellfish handling.', 'Separate tools immediately and retrain on zone separation.', 5, 2, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 29 DAY)),
(3, 'Undercooked chicken in service.', 'Continue cooking, verify core temperature, and hold the batch from service.', 5, 2, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 28 DAY)),
(4, 'Raw chicken stored above ready-to-eat items.', 'Reorganize cold room and discard any exposed product.', 4, 3, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 27 DAY)),
(5, 'Desserts held too warm on the pass.', 'Return trays to cold storage and reduce counter batch size.', 4, 3, 3, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 26 DAY)),
(6, 'Dirty tap line affects beer quality.', 'Clean the line before service and log the maintenance date.', 3, 3, 4, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(7, 'Glass fragments near bar garnish area.', 'Stop service, isolate the area, and reset garnish stock.', 4, 2, 4, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 24 DAY)),
(8, 'Unattended spirit service to a visibly intoxicated guest.', 'Refuse service, document the incident, and support the bartender.', 5, 2, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 23 DAY)),
(9, 'Incorrect ABV communication to staff during promotion.', 'Brief the team again and remove any unclear promo material.', 3, 2, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 22 DAY)),
(10, 'Pastry cream cooled too slowly.', 'Split the batch into shallow trays and chill immediately.', 5, 3, 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 21 DAY)),
(11, 'Nut cross-contact in pastry finishing area.', 'Re-clean the station and segregate allergen tools.', 5, 2, 6, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)),
(12, 'Filling tubs mislabeled or unlabeled.', 'Relabel all tubs, isolate unclear items, and retrain staff.', 4, 3, 7, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 19 DAY));

INSERT INTO mapping_point (id, title, challenges, measures, responsible_for_point, law, severity_dots, org_id, created_at) VALUES
(1, 'Age verification at entry', 'Busy nights make it easy to wave guests through without checking ID.', 'Use a door host, require ID on doubt, and back refusals immediately.', 'Shift lead at the entrance', '4-1', 5, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY)),
(2, 'Alcohol storage after close', 'Open bottles were once left unsecured on the back counter.', 'Lock premium stock, count open bottles, and sign off the close.', 'Closing bartender', '4-6', 3, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 17 DAY)),
(3, 'Incident logging after refusal', 'Team members sometimes rely on memory and forget details by end of shift.', 'Write incidents before handoff and review them in the next manager brief.', 'Manager on duty', '4-7', 4, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 16 DAY)),
(4, 'Prevent overserving during events', 'Large tables can spread orders across multiple staff members.', 'Tag one server per table and pause service when behavior changes.', 'Service manager', '4-3', 5, 1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 15 DAY)),
(5, 'Visible age-check signage', 'Guests challenge refusals more often when signage is missing or hidden.', 'Keep signs at both tills and entrance sightlines.', 'Bar owner', '4-1', 3, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 14 DAY)),
(6, 'Refusal support for bartenders', 'New bartenders hesitate when guests push back on refusals.', 'Shift lead takes over the conversation and logs the incident.', 'Shift lead', '4-3', 5, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 13 DAY)),
(7, 'Secure spirits at close', 'Late clean-down can leave spirits unattended while cash-up happens.', 'Close one station at a time and lock spirits before drawer count.', 'Closing bartender', '4-6', 4, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 DAY)),
(8, 'Guest conflict handoff', 'Incidents were not always passed to the next day team.', 'Use the written handoff log and review it at opening briefing.', 'Owner or opening manager', '4-7', 4, 2, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 11 DAY));

INSERT INTO ccp (id, how, equipment, instructions_and_calibration, immediate_corrective_action, critical_min, critical_max, unit, ccp_name, monitored_description, org_id, interval_id, created_at) VALUES
(1, 'Measure the warmest point in the walk-in cooler with a calibrated probe.', 'Digital probe thermometer', 'Calibrate weekly in ice water and sanitize before and after each reading.', 'Move product to backup cooling, keep the door closed, and isolate exposed batches until reviewed.', 0.00, 4.00, 'C', 'Cold storage walk-in temperature', 'Main walk-in temperature for ready-to-eat and chilled prep items.', 1, 4, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(2, 'Check the hottest tray in the steam well during service.', 'Probe thermometer and hot-holding well display', 'Verify the probe weekly and compare with the well display at shift start.', 'Reheat to above 75C, adjust the well, and discard food that cannot be recovered safely.', 60.00, 90.00, 'C', 'Hot holding soup well', 'Service temperature for hot-held soups and sauces.', 1, 5, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 32 DAY)),
(3, 'Measure the center temperature of the cooling tray after blast chilling.', 'Sanitized cooling probe', 'Calibrate weekly, sanitize between batches, and record after the chill step.', 'Split the batch, extend chilling, and hold the item from assembly until within limit.', 0.00, 4.00, 'C', 'Cooling of pastry cream', 'Cooling limit for pastry cream before assembly.', 3, 8, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 31 DAY)),
(4, 'Read the freezer probe and confirm the display matches.', 'Freezer probe and display panel', 'Compare probe against the display once per week and report any drift.', 'Move high-risk products to backup freezer and request maintenance immediately.', -25.00, -18.00, 'C', 'Freezer temperature', 'Freezer temperature for cream pastries and frozen fillings.', 3, 9, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY));

INSERT INTO ccp_user_bridge (user_id, ccp_id, user_role, created_at) VALUES
(2, 1, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(5, 1, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(5, 1, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(3, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(4, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(13, 1, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 29 DAY)),
(11, 1, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 33 DAY)),
(2, 2, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 32 DAY)),
(5, 2, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 32 DAY)),
(3, 2, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 32 DAY)),
(4, 2, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 32 DAY)),
(13, 2, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 29 DAY)),
(10, 3, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 31 DAY)),
(12, 3, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 31 DAY)),
(11, 3, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 31 DAY)),
(15, 3, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY)),
(1, 3, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 31 DAY)),
(9, 4, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(10, 4, 'VERIFIER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(10, 4, 'DEVIATION_RECEIVER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(11, 4, 'PERFORMER', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)),
(15, 4, 'DEPUTY', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 DAY));

INSERT INTO ccp_corrective_measure (id, product_category_id, ccp_id, measure_description, created_at) VALUES
(1, 1, 1, 'Move seafood trays to backup cooling and assess exposure time before returning to service.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)),
(2, 2, 1, 'Transfer chicken to the prep cooler, quarantine any warm trays, and review cooler loading.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)),
(3, 3, 1, 'Return desserts to cold storage and reduce how many portions are held on the pass.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 20 DAY)),
(4, 2, 2, 'Reheat chicken-based dishes above 75C within 30 minutes or discard them.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 19 DAY)),
(5, 6, 3, 'Split pastry trays into smaller portions and repeat blast chilling immediately.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY)),
(6, 7, 3, 'Relabel fillings, use shallow pans, and verify chill times before release.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 18 DAY)),
(7, 6, 4, 'Isolate cream pastries, move stock to the backup freezer, and request maintenance.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 17 DAY));

INSERT INTO ccp_record (id, org_id, ccp_id, last_verifier, verification_status, verified_at, performed_by, comment, measured_value, critical_min, critical_max, unit, ccp_name, created_at) VALUES
(1, 1, 1, 2, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 MINUTE), 3, 'Walk-in stable during lunch prep.', 3.20, 0.00, 4.00, 'C', 'Cold storage walk-in temperature', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 HOUR)),
(2, 1, 1, NULL, 'WAITING', NULL, 4, 'Top shelf seafood pan ran warm after repeated door opening.', 5.60, 0.00, 4.00, 'C', 'Cold storage walk-in temperature', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 HOUR)),
(3, 1, 2, 2, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 4 HOUR), 4, 'Soup well checked before dinner rush.', 62.00, 60.00, 90.00, 'C', 'Hot holding soup well', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 HOUR)),
(4, 1, 2, NULL, 'WAITING', NULL, 3, 'Steam well dipped below target before refill.', 58.00, 60.00, 90.00, 'C', 'Hot holding soup well', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 HOUR)),
(5, 3, 3, NULL, 'WAITING', NULL, 11, 'Pastry cream cooled within target after blast chill.', 3.80, 0.00, 4.00, 'C', 'Cooling of pastry cream', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 MINUTE)),
(6, 3, 4, 10, 'REJECTED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 24 HOUR), 15, 'Freezer rose during delivery unload and did not recover fast enough.', -17.00, -25.00, -18.00, 'C', 'Freezer temperature', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 26 HOUR));

INSERT INTO prerequisite_routine_record (id, org_id, routine_id, performed_by, result_status, comment, last_verifier, verification_status, verified_at, created_at) VALUES
(1, 1, 1, 3, 'COMPLETED', 'Prep bench and slicer sanitized before lunch rush.', 2, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 90 MINUTE), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 2 HOUR)),
(2, 1, 1, 4, 'COMPLETED', 'Morning sanitation completed before opening.', 2, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 25 HOUR), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 26 HOUR)),
(3, 1, 2, 3, 'FAILED', 'One chicken tray arrived at 6.1C and was isolated on receipt.', 5, 'REJECTED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 HOUR)),
(4, 1, 3, 4, 'COMPLETED', 'Degreased hood filters and deep cleaned fry station.', 5, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 71 HOUR), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 72 HOUR)),
(5, 2, 4, 7, 'COMPLETED', 'Opening age-check brief completed before doors opened.', 8, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 45 MINUTE), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 HOUR)),
(6, 2, 5, 8, 'COMPLETED', 'Previous incidents reviewed and handoff notes confirmed.', 6, 'VERIFIED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 47 HOUR), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 48 HOUR)),
(7, 3, 6, 11, 'COMPLETED', 'Mixer station sanitized and ready for pastry prep.', NULL, 'WAITING', NULL, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 HOUR)),
(8, 3, 7, 15, 'FAILED', 'Two cooling labels were missing production time.', 10, 'REJECTED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 28 HOUR), DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 HOUR));

INSERT INTO deviation (id, ccp_record_id, routine_record_id, org_id, reported_by, category, reviewed_by, review_status, reviewed_at, what_went_wrong, immediate_action_taken, potential_cause, potential_preventative_measure, preventative_measure_actually_taken, created_at) VALUES
(1, 2, NULL, 1, 4, 'IK_MAT', NULL, 'OPEN', NULL, 'Seafood on the top shelf in the walk-in was above the cold-hold limit.', 'Moved the tray to backup cooling and flagged it for manager review.', 'Repeated door opening during prep and overfilled shelf layout.', 'Reduce shelf load and enforce a one-door-open rule during prep.', '', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 HOUR)),
(2, NULL, 3, 1, 3, 'IK_MAT', 5, 'CLOSED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 8 HOUR), 'A chilled chicken delivery arrived above the accepted temperature.', 'Quarantined the tray and refused it from service.', 'Supplier transport temperature drift.', 'Escalate to supplier and verify delivery temperature at the vehicle door.', 'Supplier contacted, tray rejected, and receiving checklist updated for the next shift.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 10 HOUR)),
(3, NULL, NULL, 2, 7, 'IK_ALKOHOL', NULL, 'OPEN', NULL, 'A guest became confrontational after refusal and the incident was not logged immediately.', 'Shift lead took over and service was stopped to the group.', 'Refusal wording was inconsistent and the team was split across stations.', 'Review refusal script in opening brief and require written incident handoff.', '', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 6 HOUR)),
(4, 6, NULL, 3, 15, 'IK_MAT', 10, 'CLOSED', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 22 HOUR), 'The freezer temperature rose above the safe limit during a delivery unload.', 'Moved cream pastries to the backup freezer and isolated affected stock.', 'Door held open too long while stock was reorganized.', 'Stage deliveries before opening the freezer and assign one person to restocking.', 'Restock flow changed and backup freezer now takes overflow during deliveries.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 26 HOUR)),
(5, NULL, NULL, 1, 13, 'OTHER', NULL, 'OPEN', NULL, 'Back door was left unlatched during close-down.', 'Door was secured once discovered and closing team was reminded.', 'Closing checklist was rushed during staff handover.', 'Add a final lock confirmation to the closing refresher course.', '', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 HOUR)),
(6, NULL, 8, 3, 15, 'IK_MAT', NULL, 'OPEN', NULL, 'Cooling labels were missing on two filling tubs in the cold room.', 'Items were isolated until labels could be confirmed.', 'Team skipped the final label check during a busy handover.', 'Add a second sign-off point for labeling before chilled storage.', '', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 HOUR));

INSERT INTO internal_control_review (id, org_id, reviewed_by, summary, created_at) VALUES
(1, 1, 1, 'Monthly internal review noted strong course completion, but cooling deviations still need tighter follow-up.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 7 DAY)),
(2, 1, 5, 'Spot check found good sanitation logging coverage across the prep team.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 3 DAY)),
(3, 2, 6, 'Bar controls are mostly stable, with follow-up needed on incident handoff discipline.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 5 DAY)),
(4, 3, 9, 'Cooling and labeling controls improved after the blast chill workflow was simplified.', DATE_SUB(UTC_TIMESTAMP(), INTERVAL 4 DAY));
