# Delivery Priorities and Implemented Functionality

## Project Focus

This project was developed as a multi-tenant internal control platform for restaurants and serving establishments, with separate but related support for IK-Mat and IK-Alkohol workflows. Our main focus was to implement the functionality that supports daily compliance work in a realistic operational setting, rather than attempting to cover every possible feature at a shallow level.

We therefore prioritized:

- authentication and role-based access control
- organization-based tenant separation
- mobile workflows for daily task execution
- desktop workflows for setup, administration, and follow-up
- database-backed storage of routines, logs, deviations, training, and compliance configuration

This prioritization follows the project description directly: we chose to complete the most important functionality with real backend and database integration, and we intentionally avoided presenting unfinished functionality as part of the final delivery.

## Prioritized Functionality Implemented

### Priority 1: Core Compliance Workflows

The highest priority was to deliver the workflows that are needed in day-to-day operations.

- Shared authentication flow for user registration and login
- JWT-based authentication with authorization checks on backend endpoints
- Session-based frontend login handling for short-lived sessions
- Role-based flows for owner, manager, and worker users
- Organization-aware sessions, including safe organization switching for users with access to multiple organizations
- Mobile routines view for workers
- Mobile CCP logging
- Mobile deviation registration
- Mobile mapping and action overview

These features were prioritized first because they represent the core operational value of the system: employees must be able to log in, see the correct organizational context, complete required tasks, and register deviations in real time.

### Priority 2: Administrative and Compliance Management

After the core worker workflows, we prioritized the desktop functionality needed by managers and administrators.

- Team composition overview
- Add, remove, and change roles for users in an organization
- Course and training administration
- Prerequisite categories, standards, and routines
- Critical Control Point (CCP) setup and editing
- Organization creation and membership handling

These desktop modules support the management side of internal control work. They allow an organization to configure routines, assign responsibilities, maintain training records, and manage the structure needed for ongoing compliance.

### Priority 3: Real Backend and Database Integration

An important priority for this project was to move central modules away from mock data and connect them to the actual backend and database.

- Real MySQL-backed persistence for central modules
- Spring Boot REST backend connected to frontend views
- Seed and schema scripts for test and demonstration data
- Multi-tenant organization separation backed by backend validation
- Real data loading and storage for key desktop and mobile workflows

We considered this essential because the project is intended to be a full-stack application, not just a frontend prototype. A major part of our work was therefore making sure the main views store and retrieve real data from the database.

## Lower-Priority Functionality Not Fully Included

The following functionality was considered valuable, but was intentionally treated as lower priority and is not presented as fully implemented delivery scope:

- advanced inspection and reporting views
- export to PDF or JSON
- notification and reminder systems
- richer analytics and dashboard functionality
- more advanced document and certificate workflows
- broader automation for follow-up and compliance reminders

These areas are natural extensions of the platform, but we chose not to frame them as completed functionality because the assignment explicitly states that unfinished functionality should not be part of the delivery.

## Why We Chose These Priorities

We chose these priorities for four main reasons.

First, the system must solve the most important practical problem before anything else: daily compliance work. Logging routines, registering deviations, and maintaining CCP-related data are more important than secondary features such as advanced reporting.

Second, a usable role and organization model was necessary before the rest of the platform could function correctly. Since the project is multi-tenant and role-sensitive, authentication, authorization, and organization separation had to be treated as foundation work.

Third, we prioritized complete, database-backed functionality over a larger amount of partially finished functionality. This gave us a more robust and testable solution and aligned better with the assignment requirements.

Fourth, we interpreted the assignment requirement on prioritization strictly: it is better to deliver fewer finished modules with good quality than to claim broad feature coverage without sufficient completion.

## Technical Quality and Delivery Readiness

The project was developed in line with the technical direction described in the assignment.

- Frontend implemented in Vue 3
- Backend implemented in Java and Spring Boot
- MySQL-based persistence
- Swagger/OpenAPI documentation on backend endpoints
- Test support and CI/CD configuration included in the project
- Docker-based startup for easier execution and testing
- Session storage used for short-lived frontend authentication sessions
- Seed data and schema scripts included for testing and demonstration

In addition, we focused on making the implemented functionality realistic to run and evaluate. This includes real authentication flows, real organization-aware data access, and real storage of the most important compliance-related entities.

## Priority Overview

For the final delivery, our priorities can be summarized as follows:

1. Core compliance workflows
2. Administrative control and configuration
3. Full-stack integration and delivery readiness
4. Future extensions beyond the current delivery scope

This reflects our main development strategy: complete the workflows that matter most, ensure they are backed by the database and backend, and only then consider broader extensions.
