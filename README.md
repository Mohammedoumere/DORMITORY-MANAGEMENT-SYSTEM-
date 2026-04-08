# Dormitory Management System

A comprehensive JavaFX application for managing university dormitory operations with PostgreSQL database.

## Features

### User Roles
1. **Student Service Admin**
   - Manages student and proctor registrations
   - Assigns blocks to students
   - Views all records
   - Manages system settings
2. **Proctor**
   - Views assigned block details
   - Assigns rooms to students
   - Manages student information in their block
   - Updates room occupancy

3. **Student**
   - Registers with ID from Student Service
   - Views assigned block and room
   - Updates personal information
   - Adds resident information

### Core Features
- User authentication and authorization
- Student registration and management
- Proctor registration and management
- Block and room assignment
- Resident information management
- Ethiopian phone number validation
- Email validation (@gmail.com only)
- Password management
- Profile settings

## System Requirements

### Software Requirements
- Java 11 or higher
- JavaFX 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- NetBeans IDE (recommended)

### Hardware Requirements
- 4GB RAM minimum
- 2GB free disk space
- 1.5GHz processor or higher

## Installation Guide

### Step 1: Database Setup
1. Install PostgreSQL
2. Open pgAdmin or psql
3. Run the SQL script: `sql/dormitory_db.sql`
4. Update database credentials in `DatabaseConnection.java`

### Step 2: Project Setup
1. Clone or download the project
2. Open in NetBeans as Maven project
3. Wait for Maven dependencies to download
4. Update database credentials in `DatabaseConnection.java`

### Step 3: Configuration
1. Update PostgreSQL connection details:
   - Open `DatabaseConnection.java`
   - Modify `username` and `password`
   - Update `jdbcUrl` if needed

2. Configure application properties:
   - Default port: 5432
   - Database name: dormitory_db

### Step 4: Build and Run
1. Clean and build project:
   ```bash
   mvn clean compile
### OUTPUT
1.   **Student Service Admin**
   <img width="1920" height="1080" alt="Screenshot (10)" src="https://github.com/user-attachments/assets/49129cf1-50a2-4fb8-9bf4-8618245edeed" />
   <img width="1920" height="1080" alt="Screenshot (11)" src="https://github.com/user-attachments/assets/9e969d01-4b5e-42c8-90ae-cca4c9aa237d" />
  <img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e2fdb2e9-3a6e-4649-bc1b-f52aac61a973" />
  <img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e4394be9-7be7-40ac-ac56-3f65a8c7d672" />
    <img width="1920" height="1080" alt="Screenshot (18)" src="https://github.com/user-attachments/assets/ecd22802-16ab-4a16-b320-ea78061f2cdd" />
    
2. **Student**
   <img width="1920" height="1080" alt="Screenshot (15)" src="https://github.com/user-attachments/assets/9c0b5a47-5453-4126-a956-9dfddbd4cc2b" />
   <img width="1920" height="1080" alt="Screenshot (16)" src="https://github.com/user-attachments/assets/006308fe-85f1-4c92-bcfa-3ec0b084a331" />
   <img width="1920" height="1080" alt="Screenshot (20)" src="https://github.com/user-attachments/assets/4742b5d4-6750-4475-99eb-c7fc704861e0" />
   
3. **Proctor**
   <img width="1920" height="1080" alt="Screenshot (21)" src="https://github.com/user-attachments/assets/2f227829-ceb7-41ce-9e5d-a77d7c45c0b7" />
   <img width="1920" height="1080" alt="Screenshot (22)" src="https://github.com/user-attachments/assets/bea796d4-fe53-435b-8030-36a5ebefd0b0" />
   <img width="1920" height="1080" alt="Screenshot (23)" src="https://github.com/user-attachments/assets/fa77f4aa-e67a-41e9-8d77-c4936937d201" />








