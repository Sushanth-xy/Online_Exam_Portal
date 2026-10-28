# Online Exam Portal (OEP) — Sprint Planning & Backlog Specification

**Document Version:** 1.0  
**Date:** October 2026  
**Project Group:** Group 6 — Problem Statement No. 6  
**Course:** Software Engineering (Mini-Project Deliverables Part-2)  

---

## 1. Project Overview & Deliverables Part-2 Alignment

The **Online Exam Portal (OEP)** is a secure, responsive web-based platform allowing academic institutions to create, schedule, deliver, proctor, and evaluate examinations. 

In accordance with the **SE Mini-Project Deliverables Part-2** guidelines:
- **Agile Framework:** 2 distinct Sprints replacing external Jira boards with GitHub native Issues, Milestones, and Labels.
- **Story Points:** Fibonacci sequence (`1, 2, 3, 5, 8`) assigned to each item based on complexity and risk.
- **Equal Work Distribution:** Work is divided equally across all 4 team members across Sprint 1 (13 points each) and Sprint 2 (13 points each), totaling **26 story points per member** (104 story points total).
- **Automated CI/CD:** GitHub Actions workflow enabled on all pull requests and branch commits (`.github/workflows/ci.yml`).
- **Git Flow:** Feature branches raised by assigned owners, reviewed and approved by the repo owner (`@Sushanth-xy`) prior to merge.
- **Sprint Outcomes:**
  - **Sprint 1 (19th Oct – 23rd Oct):** Core working MVP (Auth, Question Bank, Timed Exam Delivery, Auto-Submit, Instant Objective Auto-Grading) + 1-minute demo video.
  - **Sprint 2 (26th Oct – 30th Oct):** Subjective Manual Grading, Result Publication, Proctoring Violation Logs, Admin Management, Security Hardening + 2-minute demo video + documentation freeze.

---

## 2. Team Roster & Workload Balancing

| Registration No. | Member Name | GitHub Username | Role in Project | Sprint 1 Pts | Sprint 2 Pts | Total Pts |
| :--- | :--- | :--- | :--- | :---: | :---: | :---: |
| **PES2UG24CS543** | **Sushanth B** | `@Sushanth-xy` | Developer / System Architect / Repo Owner | 13 | 13 | **26** |
| **PES2UG24CS549** | **Tanish Jaladanki** | `@tan-git-06` | QA Lead / Authoring & Grading Modules | 13 | 13 | **26** |
| **PES2UG24CS536** | **Sumiran Vuppuluri** | `@sumirxn` | Test Engineer / Auth & Delivery Frontend | 13 | 13 | **26** |
| **PES2UG24CS528** | **Sudarshan D** | `@SudarshanD231` | Product Owner / Proctoring & Admin UI | 13 | 13 | **26** |
| **Total** | | | | **52** | **52** | **104** |

---

## 3. Sprint 1: Core Portal & Exam Engine MVP (Oct 19 – Oct 23)

### Goal:
Deliver a working end-to-end exam delivery flow where students register/login, instructors author questions and schedule exams, students take exams with live countdown timer and 30s auto-save, exams auto-submit on timeout, and objective questions are immediately auto-graded.

| Issue | Title | Assignee | Story Points | Module | Traceability (SRS / STP) | Status |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| [#2](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/2) | CI/CD Automated Build & Test Pipeline | `@Sushanth-xy` | 3 | DevOps | `OEP-NF-003`, Deliverables 2b | **DONE** |
| [#3](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/3) | User Registration, Authentication & JWT Security Backend | `@Sushanth-xy` | 5 | Auth | `OEP-F-001`, `OEP-F-002`, `OEP-SR-002` (`TC-Auth-01`, `TC-Auth-02`) | **DONE** |
| [#4](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/4) | Exam Delivery Engine & Auto-Save Session Management Backend | `@Sushanth-xy` | 5 | Exam | `OEP-F-012`, `OEP-F-015` (`TC-Exam-02`, `TC-Exam-05`) | To Do |
| [#5](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/5) | Question Bank Management API (MCQ, True/False, Short Answer) | `@tan-git-06` | 5 | Question Bank | `OEP-F-006` (`TC-QB-01`) | To Do |
| [#6](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/6) | Exam Creation, Scheduling & Window Protection API | `@tan-git-06` | 5 | Exam / Security | `OEP-F-007`, `OEP-F-008`, `OEP-SR-003` (`TC-QB-02`, `TC-QB-03`, `TC-Sec-03`) | To Do |
| [#7](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/7) | Instant Auto-Grading Engine for Objective Questions Backend | `@tan-git-06` | 3 | Grading | `OEP-F-016` (`TC-Grade-01`) | To Do |
| [#8](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/8) | Auth Views: Registration, Login & Role-Based Route Guards Frontend | `@sumirxn` | 5 | Auth / Frontend | `OEP-F-001`, `OEP-F-002`, `OEP-F-003` (`TC-Auth-03`) | To Do |
| [#9](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/9) | Timed Exam UI with Live Countdown Timer & Auto-Submit Frontend | `@sumirxn` | 5 | Exam / Frontend | `OEP-F-011`, `OEP-F-012`, `OEP-F-013` (`TC-Exam-01`, `TC-Exam-03`) | To Do |
| [#10](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/10) | Sprint 1 Automated Test Suite & STP Phase 1 Verification | `@sumirxn` | 3 | QA / Testing | STP Phase 1 (`TC-Auth-01` to `TC-Grade-01`) | To Do |
| [#11](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/11) | Instructor Dashboard: Question Bank Authoring & Exam Creator UI | `@SudarshanD231` | 5 | Instructor UI | `OEP-F-006`, `OEP-F-007`, `OEP-F-008`, `OEP-F-010` (`TC-QB-05`) | To Do |
| [#12](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/12) | Student Dashboard: Available Exams & Scheduled Window Gate UI | `@SudarshanD231` | 5 | Student UI | `OEP-F-014` (`TC-Exam-04`) | To Do |
| [#13](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/13) | Client-Side Proctoring: Fullscreen Enforcement & Tab-Switch Triggers | `@SudarshanD231` | 3 | Proctoring | `OEP-F-019`, `OEP-F-020` (`TC-Mon-01`, `TC-Mon-02`) | To Do |

---

## 4. Sprint 2: Grading, Proctoring, Admin & System Hardening (Oct 26 – Oct 30)

### Goal:
Deliver instructor subjective grading workflows, result publication controls, proctoring violation dashboards, admin account/role management, audit trail logging, security hardening, complete test plan execution, and final documentation freeze.

| Issue | Title | Assignee | Story Points | Module | Traceability (SRS / STP) |
| :---: | :--- | :--- | :---: | :--- | :--- |
| [#14](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/14) | Subjective Question Manual Grading & Feedback API Backend | `@Sushanth-xy` | 5 | Grading | `OEP-F-017` (`TC-Grade-02`) |
| [#15](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/15) | Security Hardening: Account Lockout & Password Reset Backend | `@Sushanth-xy` | 5 | Auth / Security | `OEP-F-004`, `OEP-F-005`, `OEP-SR-005` (`TC-Auth-04`, `TC-Auth-05`, `TC-Sec-05`) |
| [#16](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/16) | System Audit Trail Logging & 3-Year Retention Compliance | `@Sushanth-xy` | 3 | Admin / Ops | `OEP-SR-004`, `OEP-NF-004` (`TC-Sec-04`, `TC-Ops-01`) |
| [#17](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/17) | Dynamic Question/Option Shuffling & Random Exam Subset Generation | `@tan-git-06` | 5 | Question Bank | `OEP-F-007`, `OEP-F-009` (`TC-QB-04`) |
| [#18](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/18) | Result Publication Controls & Release Notification Workflow | `@tan-git-06` | 5 | Grading / Notifications | `OEP-F-018` (`TC-Grade-03`) |
| [#19](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/19) | End-to-End Test Plan Execution, Defect Triage & RTM Sign-off | `@tan-git-06` | 3 | QA / Testing | STP Verification & RTM |
| [#20](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/20) | Instructor Manual Grading & Subjective Review Interface Frontend | `@sumirxn` | 5 | Grading UI | `OEP-F-017` (`TC-Grade-02`) |
| [#21](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/21) | Student Exam Results, Sectional Breakdown & Scorecard UI | `@sumirxn` | 5 | Results UI | `OEP-F-018` (`TC-Grade-03`) |
| [#22](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/22) | Performance & Accessibility Verification (Sub-2s & WCAG 2.1 AA) | `@sumirxn` | 3 | NFR / UX | `OEP-NF-001`, `OEP-NF-005` (`TC-Perf-01`, `TC-UX-01`) |
| [#23](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/23) | Proctoring Incident Logs & Instructor Live Monitoring Dashboard | `@SudarshanD231` | 5 | Proctoring | `OEP-F-019`, `OEP-F-020` (`TC-Mon-01`, `TC-Mon-02`) |
| [#24](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/24) | Admin Dashboard: User Lifecycle, Role Reassignment & Audit Logs UI | `@SudarshanD231` | 5 | Admin UI | `OEP-F-021` (`TC-Admin-01`) |
| [#25](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/25) | Sprint 2 Video Demonstrations, Deviations Report & Release Freeze | `@SudarshanD231` | 3 | Docs / Delivery | Deliverables 2g, 2i, 3b, 3c, 3d |

---

## 5. Git & PR Workflow Guidelines for Team Members

1. **Pulling & Branching:**
   - Always ensure your local `main` branch is up-to-date: `git pull origin main`.
   - Branch naming format:
     - `feat/<issue-number>-<short-description>` (e.g., `feat/2-user-auth-jwt`)
     - `test/<issue-number>-<short-description>` (e.g., `test/9-sprint1-tests`)
     - `fix/<issue-number>-<short-description>`

2. **Commits:**
   - Use conventional commit messages:
     - `feat: implement user registration and bcrypt hashing (resolves #2)`
     - `test: add junit tests for countdown timer and autosave (resolves #9)`

3. **Pull Request (PR) Lifecycle:**
   - Push your branch to GitHub: `git push -u origin feat/<branch-name>`.
   - Open a PR targeting `main`.
   - Fill out the PR template (`.github/PULL_REQUEST_TEMPLATE.md`), linking the issue (e.g., `Fixes #2`).
   - The GitHub Actions CI pipeline will trigger automatically to build both Maven (Spring Boot) and npm (Vite React).
   - Tag `@Sushanth-xy` for code review.
   - Once approved and CI checks pass, merge into `main`.
