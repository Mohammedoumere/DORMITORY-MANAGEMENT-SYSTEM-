-- Dormitory Management System Database Schema

-- ... (Drop statements and other tables) ...

-- Student table
CREATE TABLE student (
    student_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 17 AND age <= 35),
    gender VARCHAR(10) NOT NULL,
    year_of_study INTEGER NOT NULL CHECK (year_of_study >= 1 AND year_of_study <= 7),
    college VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    nationality VARCHAR(50) NOT NULL,
    region VARCHAR(100),
    city VARCHAR(100),
    phone_number VARCHAR(15) NOT NULL,
    block_id VARCHAR(10),
    room_number VARCHAR(10),
    -- bed_number INTEGER, -- Removed
    resident_full_name VARCHAR(100),
    resident_relation VARCHAR(50),
    resident_phone VARCHAR(15),
    resident_region VARCHAR(100),
    resident_city VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    profile_picture_path VARCHAR(255),
    registration_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ... (rest of the SQL script)
