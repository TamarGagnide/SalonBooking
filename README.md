# SalonBooking
Salon appointment reservation system

## Project Description

SalonBooking is a web application designed to simplify appointment booking at a beauty salon. Customers can browse salon services, choose a stylist, view available time slots, and book or cancel appointments. Stylists can view their schedules, while the salon administrator manages services, stylists, and working hours. The system aims to reduce manual scheduling and prevent appointment conflicts.

## Stakeholders

| Role | Responsibilities and interests |
|---|---|
| Customer | Wants to find suitable services and book appointments quickly. |
| Stylist | Wants an accurate schedule and clear information about upcoming appointments. |
| Salon Administrator / Owner | Wants to manage services, stylists, schedules, and bookings efficiently. |

**Who pays:** The salon owner funds the system.

**Veto authority:** The salon owner has the final say on business requirements and salon policies. The developer determines the technical implementation within the agreed scope.

## Project Scope

The initial version supports one salon with multiple stylists and services.

The system will support customer accounts, service browsing, appointment availability, booking and cancellation, stylist schedules, and administrative management of services and working hours.

Online payments, customer reviews, loyalty programs, email reminders, and multiple salon branches are outside the initial MVP scope.

## Non-Functional Requirements

**NFR-01 — Performance:** The system must return service lists and appointment availability results within 2 seconds for at least 95% of requests under a test load of 50 concurrent users.

**NFR-02 — Concurrent Usage:** The system must support at least 50 simultaneous users without crashing or losing booking data.

**NFR-03 — Booking Integrity:** The system must prevent overlapping active appointments for the same stylist, including when multiple customers attempt to book the same time slot simultaneously.

**NFR-04 — Access Control:** The system must prevent unauthorized access to customer appointments and administrative functions. Automated tests must verify access permissions for each user role.

## Definition of Done

A user story is considered complete when:

- The functionality meets all defined acceptance criteria.
- Input validation and error handling are implemented where required.
- Access permissions are enforced for protected functionality.
- Relevant automated tests pass.
- The code is reviewed and merged into the main branch.
- The application builds and runs successfully.
- Relevant documentation is updated.

## MVP Scope

The Minimum Viable Product (MVP) supports one salon with multiple stylists and services. It includes customer registration and login, browsing services and stylists, checking availability, booking and cancelling appointments, viewing personal appointments, viewing stylist schedules, managing salon services, and configuring stylist working hours.

### MVP User Stories

| Issue | User Story | Points |
|---|---|---:|
| SALON-01 | Customer Registration | 3 |
| SALON-02 | Customer Login | 3 |
| SALON-03 | Browse Salon Services | 2 |
| SALON-04 | View Salon Stylists | 2 |
| SALON-05 | Check Appointment Availability | 5 |
| SALON-06 | Book an Appointment | 5 |
| SALON-07 | View My Appointments | 3 |
| SALON-08 | Cancel an Appointment | 3 |
| SALON-09 | View Stylist Schedule | 3 |
| SALON-10 | Manage Salon Services | 5 |
| SALON-11 | Manage Stylist Working Hours | 5 |
| **Total** | **11 user stories** | **39** |

All listed stories are classified as **Must have** and form the MVP.

### Out of Scope for the MVP

- Online payments
- Customer reviews and ratings
- Loyalty and rewards programs
- Email reminders
- Multiple salon branches

