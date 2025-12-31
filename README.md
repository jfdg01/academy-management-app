# Academy Management App

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![SvelteKit](https://img.shields.io/badge/SvelteKit-2.16-FF3E00?style=for-the-badge&logo=svelte&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-Payments-008CDD?style=for-the-badge&logo=stripe&logoColor=white)

Running an efficient educational academy means managing a lot of moving parts—from student enrollment and payments to exercise grading and material distribution.

I built this platform to handle all of that complexity without the fragility of patched-together spreadsheets. It combines the rock-solid stability of a **Spring Boot** backend with the modern, reactive experience of **SvelteKit**.

---

## 🛠 Under the Hood

The goal was simple: Enterprise-grade architecture that doesn't feel like legacy software.

*   **Spring Boot 3.5 & Java 21**: The backend is strict. It handles the heavy lifting, security (OAuth2 + JWT), and data integrity. It’s built to scale, not just to demo.
*   **SvelteKit (Frontend)**: I didn't want a clunky admin panel. The frontend is fast, responsive, and uses Tailwind so it looks good on any device.
*   **PostgreSQL**: Relational data requires a relational DB. No shortcuts here.
*   **Docker Compose**: The entire stack (DB, API, Frontend) spins up with a single command.

## ✨ Why It Works

### Role-Based Access that actually works
*   **Admins**: Have god-mode. They manage users, finances, and global settings.
*   **Professors**: Focus on teaching. They create courses, upload materials, and grade submissions.
*   **Students**: A clean dashboard to track progress, submit work, and pay fees.

### Money handled right
Integration with **Stripe** means payments are secure and automated. Webhooks handle the status updates so you don't have to manually check who paid what.

### The Feedback Loop
The core of learning is feedback. The exercise system allows students to upload work (up to 10MB) and professors to grade it directly within the platform.

## 🚀 Running Locally

You can run the whole thing in Docker, or peel it apart for development.

### The Easy Way (Docker)
```bash
cd backend/app
docker-compose up -d
```
This spins up the Database and the Backend.

### The Dev Way

1.  **Backend**:
    ```bash
    cd backend/app
    ./mvnw spring-boot:run
    ```

2.  **Frontend**:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

## 📄 License

Code is for private reference.
