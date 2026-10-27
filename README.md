# Online Exam Portal

A comprehensive and secure web portal for conducting, proctoring, and grading online examinations.

## Team
- Tanish Jaladanki
- Sumiran Vuppuluri
- Sushanth B
- Sudarshan D

## Tech Stack
- **Backend:** Spring Boot (Java 17, Maven)
- **Frontend:** React (Vite, TypeScript, React Router, Axios)
- **Database:** PostgreSQL 16

## Project Structure
```
/
├── backend/            # Spring Boot application
├── frontend/           # React + TypeScript frontend
├── docs/               # Project documentation and specifications
├── docker-compose.yml  # Docker Compose config for PostgreSQL
├── .gitignore          # Git ignore rules
└── README.md           # Project documentation
```

## How to Run

1. **Start Database:**
   ```bash
   docker compose up -d
   ```

2. **Start Backend:**
   ```bash
   cd backend && ./mvnw spring-boot:run
   ```
   The backend will start at `http://localhost:8080`. Health endpoint: `http://localhost:8080/api/v1/health`.

3. **Start Frontend:**
   ```bash
   cd frontend && npm install && npm run dev
   ```
   The frontend will run at `http://localhost:5173`.

## Workflow Rules
- Create a dedicated branch per feature (e.g., `feat/login`).
- Open a Pull Request (PR) targeting `main`.
- At least one teammate must review and approve the PR before merging.
