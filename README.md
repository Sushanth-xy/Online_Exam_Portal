# Online Exam Portal

A comprehensive and secure web portal for conducting, proctoring, and grading online examinations.

## Team
- **Sushanth B** (`@Sushanth-xy`) — Developer / System Architect / Repo Owner (26 Story Points)
- **Tanish Jaladanki** (`@tan-git-06`) — QA Lead / Question Bank & Grading Engine (26 Story Points)
- **Sumiran Vuppuluri** (`@sumirxn`) — Test Engineer / Auth, Security & Delivery (26 Story Points)
- **Sudarshan D** (`@SudarshanD231`) — Product Owner / Frontend UI, Proctoring & Admin (26 Story Points)

## Agile Sprint Management (No Jira — GitHub Native)
All work is tracked via **GitHub Issues**, **Milestones**, and **Labels** with balanced Story Point allocation:
- **[Sprint 1 Milestone](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/milestone/1)** (19th Oct – 23rd Oct): Core Working MVP (52 Story Points — 13 per member)
- **[Sprint 2 Milestone](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/milestone/2)** (26th Oct – 30th Oct): Subjective Grading, Proctoring, Admin, Hardening (52 Story Points — 13 per member)
- **Full Sprint Plan & Traceability Matrix:** See [SPRINT_PLAN.md](file:///Users/sushanth/Code/Online_Exam_Portal_SE/docs/SPRINT_PLAN.md)
- **SRS & Test Plan:** Located in [docs/](file:///Users/sushanth/Code/Online_Exam_Portal_SE/docs)

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
