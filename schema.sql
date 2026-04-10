DROP TABLE IF EXISTS internal_control_review;
DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS internal_control_review;
DROP TABLE IF EXISTS deviation;
DROP TABLE IF EXISTS prerequisite_routine_record;
DROP TABLE IF EXISTS ccp_record;
DROP TABLE IF EXISTS ccp_corrective_measure;
DROP TABLE IF EXISTS ccp_user_bridge;
DROP TABLE IF EXISTS routine_user_bridge;
DROP TABLE IF EXISTS file_course_bridge;
DROP TABLE IF EXISTS course_link;
DROP TABLE IF EXISTS user_course_bridge_responsible;
DROP TABLE IF EXISTS course_user_bridge_progress;
DROP TABLE IF EXISTS danger_risk_combo;
DROP TABLE IF EXISTS org_user_bridge_danger_analysis_collaborator;
DROP TABLE IF EXISTS org_user_bridge;
DROP TABLE IF EXISTS file_object;
DROP TABLE IF EXISTS prerequisite_standard;
DROP TABLE IF EXISTS prerequisite_routine;
DROP TABLE IF EXISTS prerequisite_category;
DROP TABLE IF EXISTS ccp;
DROP TABLE IF EXISTS mapping_point;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS todo;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS interval_rule;
DROP TABLE IF EXISTS organization;
DROP TABLE IF EXISTS users;

CREATE TABLE users ( -- The users of the system
    id INT AUTO_INCREMENT PRIMARY KEY,
    password_data TEXT NOT NULL,
    legal_name TEXT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    refresh_token TEXT,
    user_id INT,
    org_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE organization ( -- A organization is typically a resturaunt
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_address TEXT,
    org_name TEXT NOT NULL,
    alcohol_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    food_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    org_number INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE SET NULL;

CREATE TABLE todo ( -- Basic todo for any task which doesn't neatly fit our schema
    id INT AUTO_INCREMENT PRIMARY KEY,
    task TEXT NOT NULL,
    assigned_to INT,
    org_id INT,
    CONSTRAINT fk_todo_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT fk_todo_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION
);

CREATE TABLE org_user_bridge ( -- showing who is a part of what orgs
    org_id INT,
    user_id INT,
    user_role ENUM('OWNER', 'MANAGER', 'WORKER') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (org_id, user_id), -- A user should only be able to have one role, not more
    CONSTRAINT fk_org_user_bridge_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT fk_org_user_bridge_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE org_user_bridge_danger_analysis_collaborator ( -- Showing who is a part of the HACCP group doing the danger analysis
    org_id INT,
    user_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (org_id, user_id),
    CONSTRAINT fk_danger_analysis_collaborator_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT fk_danger_analysis_collaborator_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE product_category ( -- For the danger analysis
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name TEXT NOT NULL,
    product_description TEXT NOT NULL,
    org_id INT,
    flowchart JSON NOT NULL, -- showing the process the product goes through
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE
);

CREATE TABLE danger_risk_combo ( -- A point under the product category showing a risk in the process the product goes through
    id INT AUTO_INCREMENT PRIMARY KEY,
    danger TEXT NOT NULL,
    danger_corrective_measure TEXT NOT NULL,
    severity_score INT NOT NULL,
    likelihood_score INT NOT NULL,
    product_category_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_danger_risk_combo_product_category
        FOREIGN KEY (product_category_id) REFERENCES product_category(id) ON DELETE CASCADE
);

CREATE TABLE file_object ( -- A file in seaweedfs
    id INT AUTO_INCREMENT PRIMARY KEY,
    uploaded_by INT NOT NULL,
    file_name TEXT NOT NULL,
    org_id INT NOT NULL,
    object_key TEXT NOT NULL,
    read_access ENUM('OWNER', 'MANAGER', 'WORKER', 'ANYONE_IN_ORG', 'PUBLIC') NOT NULL,
    delete_access ENUM('OWNER', 'MANAGER', 'WORKER', 'ANYONE_IN_ORG', 'PUBLIC') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_object_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT fk_file_object_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION
);

CREATE TABLE course (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title TEXT NOT NULL,
    course_description TEXT NOT NULL,
    org_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION
);

CREATE TABLE course_user_bridge_progress ( -- showing who has completed what courses
    course_id INT,
    user_id INT,
    is_completed BOOLEAN NOT NULL,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- here having last_updated is especially important
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id, user_id),
    CONSTRAINT fk_course_user_bridge_progress_course
        FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_user_bridge_progress_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_course_bridge_responsible ( -- Who is responsible for verifying that the people taking a course actually has completed it?
    course_id INT,
    user_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id, user_id),
    CONSTRAINT fk_user_course_bridge_responsible_course
        FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_course_bridge_responsible_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE course_link ( -- Some courses have links attached to stuff like learning resources
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT,
    link TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_link_course
        FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE file_course_bridge (
    course_id INT,
    file_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id, file_id),
    CONSTRAINT fk_file_course_bridge_course
        FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_course_bridge_file
        FOREIGN KEY (file_id) REFERENCES file_object(id) ON DELETE CASCADE
);

CREATE TABLE mapping_point ( -- For "IK alkohol" its a specific law, and how to handle it in the organization
    id INT AUTO_INCREMENT PRIMARY KEY,
    title TEXT,
    challenges TEXT,
    measures TEXT,
    responsible_for_point TEXT NOT NULL,
    law VARCHAR(25),
    severity_dots TINYINT UNSIGNED,
    org_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mapping_point_org
        FOREIGN KEY (org_id) REFERENCES organization(id)
);

CREATE TABLE interval_rule ( -- Info about something which has to repeat with some interval
    id INT AUTO_INCREMENT PRIMARY KEY,
    interval_start BIGINT UNSIGNED NOT NULL, -- seconds since 1970
    interval_repeat_time BIGINT UNSIGNED NOT NULL, -- seconds since interval_start
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ccp ( -- Critical Control Point
    id INT AUTO_INCREMENT PRIMARY KEY,
    how TEXT NOT NULL,
    equipment TEXT NOT NULL,
    instructions_and_calibration TEXT NOT NULL,
    immediate_corrective_action TEXT NOT NULL,
    critical_min DECIMAL(10,2) NOT NULL,
    critical_max DECIMAL(10,2) NOT NULL,
    unit TEXT,
    ccp_name TEXT NOT NULL,
    monitored_description TEXT,
    org_id INT,
    interval_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ccp_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT fk_ccp_interval
        FOREIGN KEY (interval_id) REFERENCES interval_rule(id) ON DELETE SET NULL
);

CREATE TABLE prerequisite_category ( -- "Grunnforutsetning kategori"
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name TEXT NOT NULL,
    org_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prerequisite_category_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE
);

CREATE TABLE prerequisite_standard ( -- A standard is any standard which should be upheld and does't fit in our datamodel
    id INT AUTO_INCREMENT PRIMARY KEY,
    standard_name TEXT NOT NULL,
    standard_description TEXT NOT NULL,
    prerequisite_category_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prerequisite_standard_category
        FOREIGN KEY (prerequisite_category_id) REFERENCES prerequisite_category(id) ON DELETE CASCADE
);

CREATE TABLE prerequisite_routine ( -- A routine to meet "grunnforutsetninger"
    id INT AUTO_INCREMENT PRIMARY KEY,
    immediate_corrective_action TEXT NOT NULL,
    title TEXT NOT NULL,
    prerequisite_category_id INT,
    prerequisite_description TEXT NOT NULL,
    org_id INT,
    interval_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prerequisite_routine_category
        FOREIGN KEY (prerequisite_category_id) REFERENCES prerequisite_category(id) ON DELETE CASCADE,
    CONSTRAINT fk_prerequisite_routine_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT fk_prerequisite_routine_interval
        FOREIGN KEY (interval_id) REFERENCES interval_rule(id) ON DELETE SET NULL
);

CREATE TABLE routine_user_bridge ( -- Who has what roles for a routine?
    user_id INT,
    routine_id INT,
    user_role ENUM('VERIFIER', 'DEVIATION_RECEIVER', 'PERFORMER', 'DEPUTY') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, routine_id, user_role),
    CONSTRAINT fk_routine_user_bridge_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_routine_user_bridge_routine
        FOREIGN KEY (routine_id) REFERENCES prerequisite_routine(id) ON DELETE CASCADE
);

CREATE TABLE ccp_user_bridge ( -- Who has what roles for a CCP?
    user_id INT,
    ccp_id INT,
    user_role ENUM('VERIFIER', 'DEVIATION_RECEIVER', 'PERFORMER', 'DEPUTY') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, ccp_id, user_role),
    CONSTRAINT fk_ccp_user_bridge_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ccp_user_bridge_ccp
        FOREIGN KEY (ccp_id) REFERENCES ccp(id) ON DELETE CASCADE
);

CREATE TABLE ccp_corrective_measure ( -- A corrective measure for when something goes wrong with a CCP for each product category. Is needed on a per product basis
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_category_id INT,
    ccp_id INT,
    measure_description TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ccp_corrective_measure_product_category
        FOREIGN KEY (product_category_id) REFERENCES product_category(id) ON DELETE CASCADE,
    CONSTRAINT fk_ccp_corrective_measure_ccp
        FOREIGN KEY (ccp_id) REFERENCES ccp(id) ON DELETE CASCADE
);

CREATE TABLE ccp_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    ccp_id INT,

    last_verifier INT,
    verification_status ENUM('SKIPPED', 'VERIFIED', 'REJECTED', 'WAITING') NOT NULL DEFAULT 'WAITING',
    verified_at DATETIME,

    performed_by INT,
    comment TEXT, -- by performer
    measured_value DECIMAL(10,2) NOT NULL, -- reasonable default precision for measurements
    critical_min DECIMAL(10,2) NOT NULL,
    critical_max DECIMAL(10,2) NOT NULL,
    unit TEXT,
    ccp_name TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ccp_record_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION,
    CONSTRAINT fk_ccp_record_ccp
        FOREIGN KEY (ccp_id) REFERENCES ccp(id) ON DELETE SET NULL,
    CONSTRAINT fk_ccp_record_last_verifier
        FOREIGN KEY (last_verifier) REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT fk_ccp_record_performed_by
        FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE prerequisite_routine_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    routine_id INT,
    performed_by INT,
    result_status ENUM('COMPLETED', 'FAILED') NOT NULL,
    comment TEXT, -- by performer

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prerequisite_routine_record_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION,
    CONSTRAINT fk_prerequisite_routine_record_routine
        FOREIGN KEY (routine_id) REFERENCES prerequisite_routine(id) ON DELETE SET NULL,
    CONSTRAINT fk_prerequisite_routine_record_performed_by
        FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT fk_prerequisite_routine_record_last_verifier
        FOREIGN KEY (last_verifier) REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE deviation ( -- A "avvik" / deviation for any mistake anywhere, not just ccp or a prerequisite routine
    id INT AUTO_INCREMENT PRIMARY KEY,
    ccp_record_id INT,
    routine_record_id INT,
    org_id INT NOT NULL,
    reported_by INT,
    category ENUM('IK_MAT', 'IK_ALKOHOL', 'OTHER') NOT NULL DEFAULT 'OTHER',

    reviewed_by INT, -- a DEVIATION_RECEIVER who reviews the deviation
    review_status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    reviewed_at DATETIME,

    what_went_wrong TEXT NOT NULL,
    immediate_action_taken TEXT NOT NULL,
    potential_cause TEXT NOT NULL,
    potential_preventative_measure TEXT NOT NULL,
    preventative_measure_actually_taken TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_deviation_ccp_record
        FOREIGN KEY (ccp_record_id) REFERENCES ccp_record(id) ON DELETE SET NULL,
    CONSTRAINT fk_deviation_routine_record
        FOREIGN KEY (routine_record_id) REFERENCES prerequisite_routine_record(id) ON DELETE SET NULL,
    CONSTRAINT fk_deviation_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE NO ACTION,
    CONSTRAINT fk_deviation_reported_by
        FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT fk_deviation_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE certificates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    certificate_name TEXT NOT NULL,
    user_id INT NOT NULL,
    file_id INT NOT NULL,
    org_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_certificates_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_file
        FOREIGN KEY (file_id) REFERENCES file_object(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE SET NULL
);

CREATE TABLE internal_control_review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    reviewed_by INT NOT NULL,
    summary TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_internal_control_review_org
        FOREIGN KEY (org_id) REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT fk_internal_control_review_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE NO ACTION
);
