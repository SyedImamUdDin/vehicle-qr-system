# 🚗 Vehicle QR Emergency System

A full-stack web application that uses QR codes to provide vehicle information and help people contact the vehicle owner's family during emergencies or wrong-parking situations.

## 🌐 Live Demo

**Production Website:**

https://vehicle-qr-system-production-a2ef.up.railway.app

**GitHub Repository:**

https://github.com/SyedImamUdDin/vehicle-qr-system

---

## 📌 Overview

The Vehicle QR Emergency System is designed to make it easier to contact a vehicle owner or their emergency contacts when a vehicle needs attention.

Each registered vehicle receives a unique QR code.

When someone scans the QR code, they are taken to a public vehicle information page where they can access the available emergency and contact options.

The system also allows vehicle owners to generate a printable QR poster that can be placed on their vehicle.

### Example Use Cases

- 🚗 Wrongly parked vehicle
- 🚨 Vehicle-related emergency
- 📞 Contact the vehicle owner's family
- 🅿️ Parking-related communication
- 📱 Quick access to vehicle information

---

# ✨ Features

## 👤 User Management

- User registration
- Email verification using OTP
- Secure user login
- Strong password requirements
- Password confirmation
- Show/hide password
- Forgot password functionality
- Password reset using email verification code
- BCrypt password hashing

## 🚗 Vehicle Management

- Add vehicles
- View registered vehicles
- Edit vehicle information
- Delete vehicles
- Vehicle registration number
- Vehicle model
- Vehicle color
- Vehicle ownership association

## 📱 QR Code System

- Unique QR code for each vehicle
- QR code opens the public vehicle information page
- Dynamic QR image generation
- Vehicle-specific QR URLs
- QR display inside the dashboard
- Printable QR emergency poster
- Production-ready QR links

## 🚨 Emergency System

- Emergency contact management
- Public vehicle information page
- Emergency contact functionality
- Wrong-parking communication
- Vehicle owner/family contact information

## 📧 Email System

- Email verification
- OTP generation
- OTP expiration
- Password reset codes
- Password reset email delivery
- Brevo Email API integration

## ☁️ Deployment

- Production deployment using Railway
- Cloud MySQL database
- Environment-based configuration
- Public production URL

---

# 🛠️ Technologies Used

## Backend

- Java 21
- Spring Boot 4.1
- Spring Data JPA
- Hibernate
- REST APIs
- BCrypt

## Database

- MySQL
- MySQL Connector/J

## Frontend

- HTML5
- CSS3
- JavaScript

## QR Technology

- ZXing

## Email

- Brevo Email API

## Deployment

- Railway

## Development Tools

- Maven
- Git
- GitHub
- MySQL Workbench
- VS Code / IntelliJ IDEA

---

# 🏗️ System Architecture

```text
                    ┌─────────────────────┐
                    │       User /              │
                    │      Visitor              │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Web Frontend           │
                    │ HTML / CSS / JS           │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Spring Boot API         │
                    │      REST API             │
                    └──────────┬──────────┘
                                    │
              ┌────────────────┼─────────────────┐
              │                     │                      │
              ▼                    ▼                      ▼
       ┌────────────┐   ┌─────────────┐   ┌──────────────┐
       │    MySQL       │   │ QR / ZXing      │   │ Brevo Email      │
       │   Database     │   │   Service       │   │     API          │
       └────────────┘   └─────────────┘   └──────────────┘
                                    │
                                    ▼
                    ┌─────────────────────┐
                    │       Railway             │
                    │     Deployment            │
                    └─────────────────────┘


🔄 Application Workflow
Registration
User
 ↓
Registration Form
 ↓
Strong Password Validation
 ↓
Password Hashed with BCrypt
 ↓
OTP Generated
 ↓
OTP Sent Through Brevo
 ↓
Email Verification
 ↓
Account Activated

Login
Email + Password
 ↓
Backend Validation
 ↓
BCrypt Password Verification
 ↓
Dashboard
Forgot Password
Login
 ↓
Forgot Password
 ↓
Enter Email
 ↓
Reset Code Generated
 ↓
Brevo Sends Reset Code
 ↓
Enter Reset Code
 ↓
Create Strong New Password
 ↓
Password Stored Using BCrypt
 ↓
Login
Vehicle and QR Workflow
Vehicle Owner
 ↓
Add Vehicle
 ↓
Vehicle ID Created
 ↓
Unique QR Generated
 ↓
QR Displayed in Dashboard
 ↓
Print QR Poster
 ↓
Place QR on Vehicle
 ↓
Visitor Scans QR
 ↓
Public Vehicle Information Page
 ↓
Emergency / Owner Contact
🖨️ Printable QR Poster

The system includes a printable QR poster designed for placement on a vehicle.

The poster contains:

SCAN ME heading
Wrong-parking / emergency message
Vehicle-specific QR code
Vehicle QR System branding
Production website URL
Wrong-parking information
Emergency family contact information
Safety information

Example message:

In case of wrong parking or emergency, please scan the QR code to contact the vehicle owner.

The printed QR uses the same vehicle-specific QR code displayed in the dashboard.

📂 Project Structure
vehicle-qr-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── vehicle_qr_system/
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── model/
│   │   │           ├── repository/
│   │   │           └── service/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── login.html
│   │       │   ├── register.html
│   │       │   ├── verify-email.html
│   │       │   ├── forgot-password.html
│   │       │   ├── reset-password.html
│   │       │   ├── dashboard.html
│   │       │   ├── add-vehicle.html
│   │       │   ├── edit-vehicle.html
│   │       │   ├── vehicle.html
│   │       │   └── emergency-contacts.html
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│
├── screenshots/
│   ├── login.png
│   ├── registration.png
│   ├── dashboard.png
│   ├── add-vehicle.png
│   ├── qr-code.png
│   ├── public-vehicle.png
│   └── emergency-contacts.png
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md

📸 Screenshots
Login

Registration

Dashboard

Add Vehicle

QR Code

Public Vehicle Information

Emergency Contacts

🔐 Security

The application includes several security-related features:

BCrypt password hashing
Strong password requirements
Password confirmation
Email verification
Expiring verification codes
Expiring password reset codes
Password reset through verified email
Environment variables for sensitive deployment configuration
Sensitive Information

API keys, database passwords, email credentials, and other secrets should never be committed to GitHub.

Use environment variables for production configuration.

🗄️ Database

The application uses MySQL to store:

Users
Vehicles
Email verification codes
Password reset codes
Emergency contacts

Hibernate/JPA is used to map Java entities to database tables.

☁️ Production Deployment

The application is deployed using Railway.

Production URL
https://vehicle-qr-system-production-a2ef.up.railway.app
Production Components
Spring Boot application
Railway hosting
Railway MySQL
Brevo email service
Environment-based configuration


🎯 Project Goals

The main goals of this project are:

Provide a simple way to identify a vehicle.
Allow people to contact the vehicle owner or family when necessary.
Provide a digital alternative to traditional vehicle contact stickers.
Make emergency contact information accessible through QR technology.
Provide vehicle owners with an easy management dashboard.
Provide a practical cloud-deployed full-stack application.
📈 Future Improvements

Possible future improvements include:

SMS emergency notifications
WhatsApp contact integration
Push notifications
Location-aware emergency alerts
Administrative dashboard
User profile management
Vehicle ownership transfer
QR scan analytics
Mobile application
Advanced emergency alert workflows
AI-powered emergency classification
👨‍💻 Author
Syed Imam Ud Din

Software Engineering Graduate

GitHub

https://github.com/SyedImamUdDin

📄 License

This project is currently maintained as a portfolio and learning project.


### Now replace the file

Run:

```powershell
notepad README.md