-- VitalAid Database Schema
-- Drop existing database if exists
DROP DATABASE IF EXISTS vitalaid_db;
CREATE DATABASE vitalaid_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vitalaid_db;

-- Users Table (Main Authentication)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    user_type ENUM('DONOR', 'PATIENT', 'HOSPITAL', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB;

-- Donors Table
CREATE TABLE donors (
    donor_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    weight DECIMAL(5, 2),
    last_donation_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    medical_conditions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_blood_group (blood_group),
    INDEX idx_city (city),
    INDEX idx_available (is_available)
) ENGINE=InnoDB;

-- Patients Table
CREATE TABLE patients (
    patient_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    emergency_contact VARCHAR(15) NOT NULL,
    medical_history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_blood_group (blood_group)
) ENGINE=InnoDB;

-- Hospitals Table
CREATE TABLE hospitals (
    hospital_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    hospital_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    contact_person VARCHAR(100) NOT NULL,
    license_number VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    bed_capacity INT,
    has_blood_bank BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_city (city),
    INDEX idx_verified (is_verified)
) ENGINE=InnoDB;

-- Blood Stock Table
CREATE TABLE blood_stock (
    stock_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    quantity_ml INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiry_date DATE,
    min_threshold INT DEFAULT 500,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    UNIQUE KEY unique_hospital_blood (hospital_id, blood_group),
    INDEX idx_blood_group (blood_group),
    INDEX idx_quantity (quantity_ml)
) ENGINE=InnoDB;

-- Plasma Stock Table
CREATE TABLE plasma_stock (
    plasma_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    quantity_ml INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiry_date DATE,
    min_threshold INT DEFAULT 200,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    UNIQUE KEY unique_hospital_plasma (hospital_id, blood_group),
    INDEX idx_blood_group (blood_group)
) ENGINE=InnoDB;

-- Ventilators Table
CREATE TABLE ventilators (
    ventilator_id INT PRIMARY KEY AUTO_INCREMENT,
    hospital_id INT NOT NULL,
    ventilator_type ENUM('INVASIVE', 'NON_INVASIVE', 'TRANSPORT') NOT NULL,
    model_name VARCHAR(100),
    serial_number VARCHAR(50) UNIQUE NOT NULL,
    status ENUM('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'DAMAGED') DEFAULT 'AVAILABLE',
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    location_in_hospital VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    INDEX idx_hospital (hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Blood/Plasma Requests Table
CREATE TABLE requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    request_type ENUM('BLOOD', 'PLASMA', 'VENTILATOR') NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'),
    quantity_ml INT,
    urgency ENUM('CRITICAL', 'URGENT', 'NORMAL') DEFAULT 'NORMAL',
    required_by DATETIME NOT NULL,
    hospital_id INT,
    status ENUM('PENDING', 'APPROVED', 'FULFILLED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_status (status),
    INDEX idx_urgency (urgency),
    INDEX idx_type (request_type)
) ENGINE=InnoDB;

-- Donor Matching Table
CREATE TABLE donor_matches (
    match_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    donor_id INT NOT NULL,
    match_score DECIMAL(5, 2),
    distance_km DECIMAL(8, 2),
    notification_sent BOOLEAN DEFAULT FALSE,
    donor_response ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    response_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES donors(donor_id) ON DELETE CASCADE,
    INDEX idx_request (request_id),
    INDEX idx_donor (donor_id)
) ENGINE=InnoDB;

-- Notifications Table
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('REQUEST', 'MATCH', 'STOCK_ALERT', 'APPROVAL', 'GENERAL') NOT NULL,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM',
    is_read BOOLEAN DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    INDEX idx_type (notification_type)
) ENGINE=InnoDB;

-- Donation History Table
CREATE TABLE donation_history (
    donation_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    hospital_id INT NOT NULL,
    donation_type ENUM('BLOOD', 'PLASMA', 'PLATELETS') NOT NULL,
    quantity_ml INT NOT NULL,
    donation_date DATE NOT NULL,
    hemoglobin_level DECIMAL(4, 2),
    blood_pressure VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donors(donor_id) ON DELETE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    INDEX idx_donor (donor_id),
    INDEX idx_date (donation_date)
) ENGINE=InnoDB;

-- Ventilator Allocation Table
CREATE TABLE ventilator_allocations (
    allocation_id INT PRIMARY KEY AUTO_INCREMENT,
    ventilator_id INT NOT NULL,
    patient_id INT NOT NULL,
    request_id INT,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'TERMINATED') DEFAULT 'ACTIVE',
    notes TEXT,
    FOREIGN KEY (ventilator_id) REFERENCES ventilators(ventilator_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES requests(request_id) ON DELETE SET NULL,
    INDEX idx_ventilator (ventilator_id),
    INDEX idx_patient (patient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Audit Log Table
CREATE TABLE audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- Insert Default Admin User
INSERT INTO users (username, password, email, phone, user_type, status) 
VALUES ('admin', 'admin123', 'admin@vitalaid.com', '9999999999', 'ADMIN', 'ACTIVE');

-- Insert Sample Hospitals
INSERT INTO users (username, password, email, phone, user_type) VALUES
('apollo_blr', 'hospital123', 'apollo@bangalore.com', '8012345601', 'HOSPITAL'),
('fortis_blr', 'hospital123', 'fortis@bangalore.com', '8012345602', 'HOSPITAL'),
('manipal_blr', 'hospital123', 'manipal@bangalore.com', '8012345603', 'HOSPITAL');

INSERT INTO hospitals (user_id, hospital_name, registration_number, address, city, state, pincode, latitude, longitude, contact_person, is_verified, bed_capacity, has_blood_bank) VALUES
(2, 'Apollo Hospital Bangalore', 'APL-BLR-2020-001', '154/11, Bannerghatta Road', 'Bangalore', 'Karnataka', '560076', 12.9121, 77.5937, 'Dr. Ramesh Kumar', TRUE, 300, TRUE),
(3, 'Fortis Hospital Bangalore', 'FTS-BLR-2020-002', '14, Cunningham Road', 'Bangalore', 'Karnataka', '560052', 12.9991, 77.5958, 'Dr. Priya Sharma', TRUE, 250, TRUE),
(4, 'Manipal Hospital Bangalore', 'MNP-BLR-2020-003', '98, HAL Airport Road', 'Bangalore', 'Karnataka', '560017', 12.9539, 77.6617, 'Dr. Anil Verma', TRUE, 400, TRUE);

-- Initialize Blood Stock for Hospitals
INSERT INTO blood_stock (hospital_id, blood_group, quantity_ml, expiry_date, min_threshold) VALUES
(1, 'A+', 2500, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'A-', 1200, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'B+', 2000, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'B-', 800, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'AB+', 1500, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'AB-', 600, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'O+', 3000, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500),
(1, 'O-', 1000, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 500);

-- Initialize Plasma Stock
INSERT INTO plasma_stock (hospital_id, blood_group, quantity_ml, expiry_date, min_threshold) VALUES
(1, 'A+', 1000, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 200),
(1, 'B+', 800, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 200),
(1, 'O+', 1200, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 200),
(1, 'AB+', 600, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 200);

-- Initialize Ventilators
INSERT INTO ventilators (hospital_id, ventilator_type, model_name, serial_number, status, location_in_hospital) VALUES
(1, 'INVASIVE', 'Medtronic PB980', 'VNT-APL-001', 'AVAILABLE', 'ICU - Ward A'),
(1, 'INVASIVE', 'Medtronic PB980', 'VNT-APL-002', 'AVAILABLE', 'ICU - Ward A'),
(1, 'NON_INVASIVE', 'ResMed Stellar 150', 'VNT-APL-003', 'AVAILABLE', 'ICU - Ward B'),
(1, 'TRANSPORT', 'Hamilton T1', 'VNT-APL-004', 'AVAILABLE', 'Emergency'),
(2, 'INVASIVE', 'Draeger Evita V800', 'VNT-FTS-001', 'AVAILABLE', 'ICU - Floor 3'),
(2, 'INVASIVE', 'Draeger Evita V800', 'VNT-FTS-002', 'IN_USE', 'ICU - Floor 3'),
(3, 'INVASIVE', 'Philips Respironics V60', 'VNT-MNP-001', 'AVAILABLE', 'Critical Care Unit');

-- Create Views for Dashboard Statistics
CREATE VIEW donor_statistics AS
SELECT 
    blood_group,
    COUNT(*) as total_donors,
    SUM(CASE WHEN is_available = TRUE THEN 1 ELSE 0 END) as available_donors,
    city
FROM donors
GROUP BY blood_group, city;

CREATE VIEW hospital_inventory AS
SELECT 
    h.hospital_name,
    h.city,
    bs.blood_group,
    bs.quantity_ml as blood_quantity,
    ps.quantity_ml as plasma_quantity,
    COUNT(v.ventilator_id) as total_ventilators,
    SUM(CASE WHEN v.status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_ventilators
FROM hospitals h
LEFT JOIN blood_stock bs ON h.hospital_id = bs.hospital_id
LEFT JOIN plasma_stock ps ON h.hospital_id = ps.hospital_id AND bs.blood_group = ps.blood_group
LEFT JOIN ventilators v ON h.hospital_id = v.hospital_id
GROUP BY h.hospital_id, h.hospital_name, h.city, bs.blood_group;

CREATE VIEW pending_requests AS
SELECT 
    r.request_id,
    r.request_type,
    r.blood_group,
    r.quantity_ml,
    r.urgency,
    r.status,
    p.full_name as patient_name,
    p.city as patient_city,
    u.phone as patient_phone,
    r.created_at,
    r.required_by
FROM requests r
JOIN patients p ON r.patient_id = p.patient_id
JOIN users u ON p.user_id = u.user_id
WHERE r.status IN ('PENDING', 'APPROVED')
ORDER BY r.urgency DESC, r.created_at ASC;