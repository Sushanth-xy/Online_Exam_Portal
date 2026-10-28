# Online Exam Portal (OEP) — Sprint Planning & Role-Based Backlog

**Document Version:** 2.0  
**Project Group:** Group 6 — Problem Statement No. 6  
**Course:** Software Engineering (Mini-Project Deliverables Part-2)  

---

## 1. Project Overview & Role-Based Specialization

In strict accordance with **Section 9 of the Software Test Plan (STP)** and the **SE Mini-Project Deliverables Part-2** guidelines:
- **Developer (`@Sushanth-xy`):** Backend application architecture, data persistence, and REST APIs.
- **Product Owner (`@SudarshanD231`):** Frontend React UI, student/instructor dashboards, sprint demo videos (1-min & 2-min), and deviations documentation.
- **QA Lead (`@tan-git-06`):** Automated QA framework, API integration test suites, Requirements Traceability Matrix (RTM) maintenance, and quality gates.
- **Test Engineer (`@sumirxn`):** Automated STP test case implementations (`TC-Auth-*`, `TC-Exam-*`, `TC-Grade-*`, `TC-Mon-*`), test execution logs, performance benchmarking, and accessibility audits.
- **Story Points:** Fibonacci scale (`1, 2, 3, 5, 8`). Balanced at **13 Story Points per member in Sprint 1** and **13 Story Points per member in Sprint 2** (**26 Story Points total each**).

---

## 2. Team Roster & Workload Balancing

| Registration No. | Member Name | GitHub Username | Official Role (STP Sec. 9) | Sprint 1 Pts | Sprint 2 Pts | Total Pts |
| :--- | :--- | :--- | :--- | :---: | :---: | :---: |
| **PES2UG24CS543** | **Sushanth B** | `@Sushanth-xy` | Developer / Repo Owner | 13 | 13 | **26** |
| **PES2UG24CS528** | **Sudarshan D** | `@SudarshanD231` | Product Owner | 13 | 13 | **26** |
| **PES2UG24CS549** | **Tanish Jaladanki** | `@tan-git-06` | QA Lead | 13 | 13 | **26** |
| **PES2UG24CS536** | **Sumiran Vuppuluri** | `@sumirxn` | Test Engineer | 13 | 13 | **26** |
| **Total** | | | | **52** | **52** | **104** |

---

## 3. Sprint 1: Core Portal & Exam Engine MVP (Oct 19 – Oct 23)

### Goal:
Deliver a working end-to-end exam delivery flow where students register/login, instructors author questions and schedule exams, students take exams with live countdown timer and 30s auto-save, exams auto-submit on timeout, and objective questions are immediately auto-graded.

| Issue | Title | Assignee | Pts | Role | Module | Status |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| [#2](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/2) | CI/CD Automated Build & Test Pipeline Setup | `@Sushanth-xy` | 3 | Developer | DevOps | **DONE** |
| [#3](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/3) | [Backend] User Registration, Authentication & JWT Security | `@Sushanth-xy` | 5 | Developer | Auth | **DONE** |
| [#4](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/4) | [Backend] Exam Engine, Question Bank & Timed Delivery APIs with 30s Autosave | `@Sushanth-xy` | 5 | Developer | Exam Delivery | To Do |
| [#8](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/8) | [Frontend] Auth Views: Interactive Login, Registration & Role-Based Route Guards | `@SudarshanD231` | 5 | Product Owner | Auth UI | To Do |
| [#11](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/11) | [Frontend] Instructor Dashboard: Question Bank Authoring & Exam Assembly UI | `@SudarshanD231` | 5 | Product Owner | Instructor UI | To Do |
| [#13](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/13) | [Product & Delivery] Sprint 1 Demo Video (1 min), Acceptance Sign-off & README Updates | `@SudarshanD231` | 3 | Product Owner | Delivery / PO | To Do |
| [#5](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/5) | [QA / Test] Automated Integration Test Suite for Auth & RBAC Security (TC-Auth-01 to TC-Auth-03) | `@tan-git-06` | 5 | QA Lead | QA / Security | To Do |
| [#6](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/6) | [QA / Test] Automated API Test Suite for Question Bank & Exam Creation (TC-QB-01 to TC-QB-03) | `@tan-git-06` | 5 | QA Lead | QA / Exam | To Do |
| [#10](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/10) | [QA / Test] Sprint 1 Quality Gate, Defect Triage & RTM Status Matrix (Phase 1 Baseline) | `@tan-git-06` | 3 | QA Lead | QA / RTM | To Do |
| [#9](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/9) | [QA / Test] Automated Test Cases for 30s Autosave & State Recovery (TC-Exam-02, TC-Exam-03) | `@sumirxn` | 5 | Test Engineer | Testing / Exam | To Do |
| [#12](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/12) | [QA / Test] Automated Test Cases for Exam Window Gating & Boundary Edge Conditions (TC-Exam-04, TC-Exam-05) | `@sumirxn` | 5 | Test Engineer | Testing / Exam | To Do |
| [#7](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/7) | [QA / Test] Sprint 1 Test Execution Logs & Defect Reports Generation | `@sumirxn` | 3 | Test Engineer | Testing / Logs | To Do |

---

## 4. Sprint 2: Grading, Proctoring, Admin & Hardening (Oct 26 – Oct 30)

### Goal:
Deliver instructor subjective grading workflows, result publication controls, proctoring violation dashboards, admin account/role management, audit trail logging, security hardening, complete test plan execution, and final documentation freeze.

| Issue | Title | Assignee | Pts | Role | Module | Status |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| [#14](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/14) | [Backend] Subjective Question Manual Grading & Feedback API | `@Sushanth-xy` | 5 | Developer | Grading | To Do |
| [#15](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/15) | [Backend] Security Hardening: Account Lockout (5 attempts) & Password Reset | `@Sushanth-xy` | 5 | Developer | Security | To Do |
| [#16](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/16) | [Backend] System Audit Trail Logging Interceptor & 3-Year Retention Compliance | `@Sushanth-xy` | 3 | Developer | Admin / Ops | To Do |
| [#20](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/20) | [Frontend] Student Exam Portal: Live Countdown Timer, Question Palette & Autosave Sync | `@SudarshanD231` | 5 | Product Owner | Student UI | To Do |
| [#24](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/24) | [Frontend] Proctoring Violation Alerts & Admin User Lifecycle Management Dashboard | `@SudarshanD231` | 5 | Product Owner | Admin UI | To Do |
| [#25](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/25) | [Product & Delivery] Sprint 2 Demo Video (2 min), SRS Deviations Report & Final Release Freeze | `@SudarshanD231` | 3 | Product Owner | Delivery / PO | To Do |
| [#17](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/17) | [QA / Test] Automated End-to-End Test Suite for Exam Submission & Auto-Grading (TC-Exam-01, TC-Grade-01) | `@tan-git-06` | 5 | QA Lead | QA / Grading | To Do |
| [#18](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/18) | [QA / Test] Automated Verification of Subjective Grading & Results Release (TC-Grade-02, TC-Grade-03) | `@tan-git-06` | 5 | QA Lead | QA / Results | To Do |
| [#19](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/19) | [QA / Test] Final Quality Audit, Defect Sign-off & RTM Acceptance Freeze (Status: Accepted) | `@tan-git-06` | 3 | QA Lead | QA / RTM | To Do |
| [#21](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/21) | [QA / Test] Automated Proctoring Verification: Tab-Switch & Fullscreen Exit Logging (TC-Mon-01, TC-Mon-02) | `@sumirxn` | 5 | Test Engineer | Testing / Mon | To Do |
| [#22](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/22) | [QA / Test] Security Test Verification: Account Lockout (TC-Auth-04) & Token Expiry (TC-Sec-05) | `@sumirxn` | 5 | Test Engineer | Testing / Sec | To Do |
| [#23](https://github.com/Sushanth-xy/Online_Exam_Portal_SE/issues/23) | [QA / Test] Non-Functional Verification: Sub-2s Latency Benchmark (TC-Perf-01) & WCAG 2.1 AA Audit (TC-UX-01) | `@sumirxn` | 3 | Test Engineer | Testing / NFR | To Do |

---

## 5. Git & PR Workflow Guidelines for Team Members

1. **Pulling & Branching:**
   - Always ensure your local `main` branch is up-to-date: `git pull origin main`.
   - Branch naming format:
     - Developer: `feat/<issue-num>-<short-description>` (e.g., `feat/4-exam-delivery-engine`)
     - Product Owner: `feat/<issue-num>-<short-description>` or `docs/<issue-num>-<description>` (e.g., `feat/8-frontend-auth-views`)
     - QA Lead / Test Engineer: `test/<issue-num>-<short-description>` (e.g., `test/5-auth-rbac-test-suite`)

2. **Commits:**
   - Conventional commit messages linking the issue:
     - `feat: implement student exam taking screen (closes #20)`
     - `test: automated test suite for question bank api (closes #6)`

3. **Pull Request (PR) Lifecycle:**
   - Push branch to GitHub: `git push -u origin <branch-name>`.
   - Open a PR targeting `main`.
   - Fill out `.github/PULL_REQUEST_TEMPLATE.md` with `Closes #<issue-id>`.
   - GitHub Actions CI pipeline triggers automatically to test & build backend and frontend.
   - Tag `@Sushanth-xy` for code review approval before merging to `main`.
