-- Skrypt do inicjalizacji struktury bazy danych

-- ENUMS
CREATE TYPE user_role AS ENUM ('RESIDENT', 'ADMIN', 'MAINTAINER');
CREATE TYPE issue_status AS ENUM ('OPEN', 'IN_PROGRESS', 'CLOSED');
CREATE TYPE charge_status AS ENUM ('UNPAID', 'PARTIALLY_PAID', 'PAID');

-- USER
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    email VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    phone_number VARCHAR,
    role user_role NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    local_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- BUILDING
CREATE TABLE building (
    id SERIAL PRIMARY KEY,
    name VARCHAR,
    address VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- LOCAL
CREATE TABLE local (
    id SERIAL PRIMARY KEY,
    building_id INT NOT NULL,
    number VARCHAR NOT NULL,
    staircase VARCHAR,
    number_of_residents INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE (building_id, number)
);

-- ISSUE CATEGORY
CREATE TABLE issue_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

-- ISSUE
CREATE TABLE issue (
    id SERIAL PRIMARY KEY,
    title VARCHAR NOT NULL,
    description TEXT NOT NULL,
    category_id INT NOT NULL,
    local_id INT,
    created_by_user_id INT NOT NULL,
    main_assignee_id INT,
    status issue_status NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ISSUE ASSIGNMENT
CREATE TABLE issue_assignment (
    id SERIAL PRIMARY KEY,
    issue_id INT,
    user_id INT,
    assigned_by INT,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ISSUE STATUS HISTORY
CREATE TABLE issue_status_history (
    id SERIAL PRIMARY KEY,
    issue_id INT,
    status issue_status,
    changed_by INT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ISSUE COMMENT
CREATE TABLE issue_comment (
    id SERIAL PRIMARY KEY,
    issue_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ISSUE IMAGE
CREATE TABLE issue_image (
    id SERIAL PRIMARY KEY,
    issue_id INT,
    file_path VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CHARGE
CREATE TABLE charge (
    id SERIAL PRIMARY KEY,
    local_id INT NOT NULL,
    period_start DATE,
    period_end DATE,
    total_amount NUMERIC,
    status charge_status DEFAULT 'UNPAID',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- CHARGE ITEM TYPE
CREATE TABLE charge_item_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);

-- CHARGE ITEM
CREATE TABLE charge_item (
    id SERIAL PRIMARY KEY,
    charge_id INT,
    type_id INT,
    quantity NUMERIC,
    unit VARCHAR,
    unit_price NUMERIC,
    total NUMERIC
);

-- PAYMENT
CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    charge_id INT,
    amount NUMERIC,
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ANNOUNCEMENT
CREATE TABLE announcement (
    id SERIAL PRIMARY KEY,
    title VARCHAR,
    content TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DOCUMENT
CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    title VARCHAR,
    file_path VARCHAR,
    uploaded_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AUDIT LOG
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    user_id INT,
    action_text TEXT,
    entity_type VARCHAR,
    entity_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- FOREIGN KEYS
-- =========================

ALTER TABLE "user"
ADD CONSTRAINT fk_user_local FOREIGN KEY (local_id) REFERENCES local(id);

ALTER TABLE local
ADD CONSTRAINT fk_local_building FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE issue
ADD CONSTRAINT fk_issue_local FOREIGN KEY (local_id) REFERENCES local(id),
ADD CONSTRAINT fk_issue_category FOREIGN KEY (category_id) REFERENCES issue_category(id),
ADD CONSTRAINT fk_issue_created_by FOREIGN KEY (created_by_user_id) REFERENCES "user"(id),
ADD CONSTRAINT fk_issue_main_assignee FOREIGN KEY (main_assignee_id) REFERENCES "user"(id);

ALTER TABLE issue_assignment
ADD CONSTRAINT fk_issue_assignment_issue FOREIGN KEY (issue_id) REFERENCES issue(id),
ADD CONSTRAINT fk_issue_assignment_user FOREIGN KEY (user_id) REFERENCES "user"(id),
ADD CONSTRAINT fk_issue_assignment_assigned_by FOREIGN KEY (assigned_by) REFERENCES "user"(id);

ALTER TABLE issue_status_history
ADD CONSTRAINT fk_issue_status_issue FOREIGN KEY (issue_id) REFERENCES issue(id),
ADD CONSTRAINT fk_issue_status_user FOREIGN KEY (changed_by) REFERENCES "user"(id);

ALTER TABLE issue_comment
ADD CONSTRAINT fk_issue_comment_issue FOREIGN KEY (issue_id) REFERENCES issue(id),
ADD CONSTRAINT fk_issue_comment_user FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE issue_image
ADD CONSTRAINT fk_issue_image_issue FOREIGN KEY (issue_id) REFERENCES issue(id);

ALTER TABLE charge
ADD CONSTRAINT fk_charge_local FOREIGN KEY (local_id) REFERENCES local(id),
ADD CONSTRAINT fk_charge_created_by FOREIGN KEY (created_by) REFERENCES "user"(id);

ALTER TABLE charge_item
ADD CONSTRAINT fk_charge_item_charge FOREIGN KEY (charge_id) REFERENCES charge(id),
ADD CONSTRAINT fk_charge_item_type FOREIGN KEY (type_id) REFERENCES charge_item_type(id);

ALTER TABLE payment
ADD CONSTRAINT fk_payment_charge FOREIGN KEY (charge_id) REFERENCES charge(id);

ALTER TABLE announcement
ADD CONSTRAINT fk_announcement_user FOREIGN KEY (created_by) REFERENCES "user"(id);

ALTER TABLE document
ADD CONSTRAINT fk_document_user FOREIGN KEY (uploaded_by) REFERENCES "user"(id);

ALTER TABLE audit_log
ADD CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES "user"(id);