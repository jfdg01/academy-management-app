# Academy Management App

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![SvelteKit](https://img.shields.io/badge/SvelteKit-2.16.0-FF3E00?style=for-the-badge&logo=svelte&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.0-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)

A comprehensive, full-stack academy management system designed for modern educational institutions. This platform streamlines administration, facilitates learning through robust class and exercise management, and handles secure payments—all through a responsive, cutting-edge interface.

---

## 📋 Overview

The **Academy Management App** bridges the gap between administrative needs and the educational experience. It provides a unified platform for:

*   **Administrators**: Complete system oversight, user management, and financial tracking.
*   **Professors**: Tools to create classes, upload materials, and grade student exercises efficiently.
*   **Students**: A seamless portal to enroll in courses, submit assignments, and track their progress.

Built with performance and scalability in mind, the application leverages the power of **Spring Boot** for a robust backend and **SvelteKit** for a dynamic, reactive frontend.

## 🚀 Key Features

### 👥 User Roles & Permissions
*   **Administrator**: Full control over users, courses, and system settings.
*   **Professor**: Manage curriculum, grade submissions, and interact with students.
*   **Student**: Dashboard for coursework, progress tracking, and secure payments.

### 🎓 Core Functionality
*   **Course & Class Management**: Organize educational content into structured courses and specific classes/workshops.
*   **Advanced Exercise System**: Create complex exercises, accept submissions (including file uploads up to 10MB), and provide graded feedback.
*   **Resource Management**: Centralized repository for course materials and downloads.

### 💳 Commerce & Integration
*   **Secure Payments**: Integrated **Stripe** processing for course enrollment.
*   **Media Handling**: Efficient file handling for attachments and educational resources.
*   **Internationalization (i18n)**: Native support for multiple languages via **Inlang/Paraglide**.

## 🛠️ Technology Stack

### Backend
*   **Framework**: Spring Boot 3.5.3
*   **Language**: Java 21
*   **Database**: PostgreSQL 16
*   **Security**: Spring Security (JWT + OAuth2)
*   **API Specs**: OpenAPI / Swagger
*   **Tools**: Lombok, MapStruct, Testcontainers

### Frontend
*   **Framework**: SvelteKit 2.16.0
*   **Language**: TypeScript
*   **Styling**: Tailwind CSS 4.0 + Flowbite Components
*   **State & Utils**: Stripe.js, JWT Decode
*   **Dev Tools**: Storybook, Vitest, Playwright

## 🏗️ Architecture

The application follows a modern **client-server architecture**:

1.  **RESTful API**: The Java/Spring Boot backend exposes a secure, documented API.
2.  **Reactive UI**: The SvelteKit frontend consumes the API to render a fast, SPA-like experience.
3.  **Data Layer**: PostgreSQL handles relational data integrity.
4.  **External Services**: Stripe handles secure payment transactions.

## 📦 Prerequisites

Ensure you have the following installed before starting:

*   **Java 21+**
*   **Node.js** (LTS recommended)
*   **PostgreSQL 16** (or Docker)
*   **Maven 3.9+**
*   **Docker & Docker Compose** (optional, recommended for local dev)

## 🔧 Installation & Setup

### 1. Environment Configuration

Create a `.env` type configuration for your backend environment variables (or setting them in your IDE/execution environment):

```properties
# Database
DB_PASSWORD=admin-jfdg-admin

# Security
JWT_SECRET=your-secure-app-secret-key-min-32-chars

# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

### 2. Backend Setup

**Using Docker Compose (Recommended for DB):**
```bash
cd backend/app
# Starts PostgreSQL container
docker-compose up -d
```

**Running the Application:**
```bash
cd backend/app
./mvnw spring-boot:run
```
*Server runs on: `http://localhost:8080`*

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Generate API Client (Backend must be running)
npm run generate-api

# Start Dev Server
npm run dev
```
*Client runs on: `http://localhost:5173`*

## 🧪 Testing

We maintain high code quality through rigorous testing strategies.

**Backend:**
```bash
cd backend/app
./mvnw test
```

**Frontend:**
```bash
cd frontend
# Unit Tests
npm run test:unit
# E2E Tests
npm run test:e2e
```

**Component Library:**
```bash
npm run storybook
```

## 📚 API Documentation

Interactive API documentation is automatically generated by Swagger UI.

*   **UI Explorer**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 🏃 Deployment

### Docker Production Build

**Backend Image**:
```bash
cd backend/app
docker build -t academy-management-backend .
```

**Frontend Build**:
```bash
cd frontend
npm run build
```

## 📄 License & Contact

Distributed under the **MIT License**.

Questions? Reach out to the **Academia Team** at `contact@academia.com`.
