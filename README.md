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