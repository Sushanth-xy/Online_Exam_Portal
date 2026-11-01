# Sprint 1 Test Execution Log and Defect Report

| Field | Value |
|---|---|
| Sprint | Sprint 1 — 19 Oct–23 Oct |
| Test engineer | Sumiran Vuppuluri (`@sumirxn`) |
| Report prepared | 1 Nov 2026 |
| Traceability | Software Test Plan (STP), Sections 8 and 9; `docs/SPRINT_PLAN.md`, Sprint 1 stories 1.8–1.10 |
| Status | Post-sprint rerun; this is not a contemporaneous execution record from 19–23 Oct |

## Execution summary

No Sprint 1 execution log was present in the repository before this report was prepared. The checked-in backend suite was run on 1 Nov 2026 to provide verifiable results against the current repository version.

| Run | Command and environment | Tests | Passed | Failed | Errors | Skipped | Result |
|---|---|---:|---:|---:|---:|---:|---|
| 1 | `.\mvnw.cmd test` from `backend\`; initial `JAVA_HOME` pointed to the JDK `bin` directory | 0 | 0 | 0 | 0 | 0 | Did not start; Maven reported that `JAVA_HOME` was not configured correctly |
| 2 | `.\mvnw.cmd test` from `backend\`; `JAVA_HOME=C:\Program Files\Java\jdk-25` | 43 | 43 | 0 | 0 | 0 | **Passed** (`BUILD SUCCESS`) |

### Results by test class

| Test class | Tests | Passed | Failed | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| `AuthControllerTest` | 8 | 8 | 0 | 0 | 0 |
| `AuthRbacIntegrationTest` | 22 | 22 | 0 | 0 | 0 |
| `ExamCreationApiTest` | 3 | 3 | 0 | 0 | 0 |
| `ExamDeliveryTest` | 5 | 5 | 0 | 0 | 0 |
| `HealthControllerTest` | 1 | 1 | 0 | 0 | 0 |
| `OepBackendApplicationTests` | 1 | 1 | 0 | 0 | 0 |
| `QuestionBankApiTest` | 3 | 3 | 0 | 0 | 0 |
| **Total** | **43** | **43** | **0** | **0** | **0** |

The run covers registration, login, role-based access, credential and token cases, question and exam creation, exam-window gating, candidate answer confidentiality, autosave and restoration, answer updates, submission and objective grading, plus health and application-context checks. The test case names and results are in Maven Surefire output under `backend/target/surefire-reports/`.

## Test data and configuration

- The STP's planned test data includes student, instructor, and admin accounts; MCQ, true/false, and short-answer questions tagged by topic and difficulty; exams with varied durations and negative-marking rules; and scripted bulk accounts for load testing.
- Tests use the test profile's in-memory H2 database in PostgreSQL compatibility mode (`jdbc:h2:mem:oep`); Hibernate creates and drops the schema. No production or external PostgreSQL data was used.
- Authentication and API tests create their own users, credentials, JWTs, questions, exams, and attempts. Fixtures include student, instructor, and admin roles; MCQ, true/false, and short-answer questions; and exams with varied schedules, durations, marks, and negative-marking factors. Data is reset between tests where configured.
- The automated run did not use scripted bulk accounts or execute load tests; the STP's load-test data configuration was not exercised.
- The test datasource uses the H2 driver, `sa` with a blank password, and `open-in-view: false`. JWT settings come from `backend/src/test/resources/application.yml`.
- Execution environment: Windows 11, Oracle Java 25.0.1, Maven Wrapper Maven 3.10.0. The project POM declares Java 17; this run therefore does not establish execution on Java 17.

## Defect and anomaly report

| ID | Type | Observation | Impact / disposition |
|---|---|---|---|
| — | Confirmed defects | None observed in this run. | All 43 executed tests passed. This does not establish that untested behaviors are defect-free. |
| OBS-01 | Environment/setup | The first attempt stopped before tests ran because `JAVA_HOME` included `\bin`. | Corrected for the successful rerun by setting `JAVA_HOME` to the JDK root. No test result was counted for the blocked attempt. |
| OBS-02 | Environment deviation | The successful run used Java 25.0.1 while the project declares Java 17. | The run passed, but Java 17 compatibility remains unverified by this execution. |
| OBS-03 | Runtime warning | Spring Security logged that an `AuthenticationProvider` bean is configured and `UserDetailsService` beans will not be used for automatic username/password configuration. | Non-fatal; tests passed. Review whether this configuration is intentional. |
| OBS-04 | Runtime warnings | Java 25 emitted dynamic-agent and restricted native-access warnings from test/runtime dependencies. | Non-fatal in this run; tests passed. |
| GAP-01 | Coverage gap | Tests cover exams scheduled in the future and already expired, but do not assert behavior at the exact start/end instant. | Boundary equality remains unverified; this is a coverage gap, not a confirmed defect. |

## Evidence and scope notes

- The successful run was executed using the Maven wrapper from `backend\` on 1 Nov 2026.
- The report reflects the checked-in suite at that time, not evidence that these exact tests ran during the Sprint 1 dates.
- This is a backend automated-test run. Manual STP checks, frontend tests, performance measurements, and accessibility checks are not represented in these counts.
- Related references: [Software Test Plan](../Software_Test_Plan_Online_Exam_Portal.pdf) and [Sprint Plan](../SPRINT_PLAN.md).
