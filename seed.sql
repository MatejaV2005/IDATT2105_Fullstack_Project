-- Seed data for local development
-- All passwords are: password123
-- BCrypt hash generated with cost factor 10

INSERT INTO users (id, password_data, legal_name, email) VALUES
(1, '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm1z98LYGqcEEG5.BITK', 'Ola Nordmann', 'ola@test.com'),
(2, '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm1z98LYGqcEEG5.BITK', 'Kari Hansen', 'kari@test.com'),
(3, '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm1z98LYGqcEEG5.BITK', 'Per Olsen', 'per@test.com');

INSERT INTO organization (id, org_name, org_address, org_number, alcohol_enabled, food_enabled) VALUES
(1, 'Testrestauranten', 'Storgata 1, 0182 Oslo', 100001, true, true);

INSERT INTO org_user_bridge (org_id, user_id, user_role) VALUES
(1, 1, 'OWNER'),
(1, 2, 'MANAGER'),
(1, 3, 'WORKER');

INSERT INTO product_category (id, product_name, product_description, org_id, flowchart) VALUES
(1, 'Fersk fisk', 'Fersk fisk og skalldyr', 1, '[]'),
(2, 'Kjoett', 'Rodt kjoett og fjorfe', 1, '[]');
