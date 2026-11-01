# Sprint 1 Requirements Traceability Matrix (RTM) Status Update

| Field | Value |
|---|---|
| Source | Software Test Plan (STP), Section 13 (RTM) |
| Sprint | Sprint 1 (19 Oct to 23 Oct) |
| Evidence | `docs/test-reports/sprint1-log.md`: 43 tests run, 43 passed, 0 failed (rerun on 1 Nov 2026, `BUILD SUCCESS`) |
| Prepared by | QA Lead |

**Status key:** Passed = proven by an automated test in the 1 Nov rerun. Not Started = no test evidence yet.

## Functional requirements

| Req ID | Requirement | Test case(s) | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-F-001 | Student registration | TC-Auth-01 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-F-002 | Login authentication | TC-Auth-02 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-F-003 | Role-based access control | TC-Auth-03 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-F-004 | Lockout after 5 failures | TC-Auth-04 | 1 (stretch) | Not Started | No test seen |
| OEP-F-005 | Password reset link | TC-Auth-05 | 1 (stretch) | Not Started | No test seen |
| OEP-F-006 | Create questions | TC-QB-01 | 1 | Passed | QuestionBankApiTest |
| OEP-F-007 | Create exam from bank | TC-QB-02 | 1 | Passed | ExamCreationApiTest |
| OEP-F-008 | Duration, window, negative marking | TC-QB-03 | 1 | Passed | ExamCreationApiTest (duration, negative marking); ExamDeliveryTest (window). See GAP-01 |
| OEP-F-009 | Shuffle questions/options | TC-QB-04 | 1 (stretch) | Not Started | No test seen |
| OEP-F-010 | Exam preview | TC-QB-05 | 1 (stretch) | Not Started | No test seen |
| OEP-F-011 | Timer and auto-submit | TC-Exam-01 | 1 | Not Started | No test seen |
| OEP-F-012 | Auto-save (30s) | TC-Exam-02 | 1 | Passed | ExamDeliveryTest |
| OEP-F-013 | Review/change answers | TC-Exam-03 | 1 (stretch) | Passed | ExamDeliveryTest |
| OEP-F-014 | Scheduled window enforcement | TC-Exam-04 | 1 | Passed | ExamDeliveryTest. See GAP-01 |
| OEP-F-015 | Submission timestamp/status | TC-Exam-05 | 1 | Passed | ExamDeliveryTest |
| OEP-F-016 | Auto-grade objective questions | TC-Grade-01 | 1 | Passed | ExamDeliveryTest |
| OEP-F-017 | Manual grading | TC-Grade-02 | 1 | Not Started | No test seen |
| OEP-F-018 | Release results | TC-Grade-03 | 1 | Not Started | No test seen |
| OEP-F-019 | Tab-switch logging | TC-Mon-01 | 1 (stretch) | Not Started | No test seen |
| OEP-F-020 | Full-screen enforcement | TC-Mon-02 | 1 (stretch) | Not Started | No test seen |
| OEP-F-021 | Admin account/role management | TC-Admin-01 | 1 | Not Started | No test seen |

## Non-functional requirements

| Req ID | Requirement | Test case(s) | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-NF-001 | Response time under 2s (p90) | TC-Perf-01 | 1 | Not Started | No load test run |
| OEP-NF-002 | 500 concurrent students | TC-Perf-02 | 1 (partial) | Not Started | No load test run |
| OEP-NF-003 | 99.5% availability | Uptime monitoring / Ops report | 2 | Not Started | Phase 2 |
| OEP-NF-004 | 3-year log/result retention | TC-Ops-01 | 2 | Not Started | Phase 2 |
| OEP-NF-005 | Browser support / WCAG 2.1 AA | TC-UX-01 | 1 (partial) | Not Started | No manual check done |

## Security requirements

| Req ID | Requirement | Test case(s) | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-SR-001 | TLS 1.2+ enforcement | TC-Sec-01 | 1 | Not Started | No test seen |
| OEP-SR-002 | Salted password hashing | TC-Sec-02 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-SR-003 | No question access before start | TC-Sec-03 | 1 | Passed | ExamDeliveryTest |
| OEP-SR-004 | Audit logging | TC-Sec-04 | 1 | Not Started | No test seen |
| OEP-SR-005 | Session inactivity timeout | TC-Sec-05 | 1 | Not Started | No test seen |

## Summary

- Passed: 13 of 31 requirements
- Not Started: 18 of 31 requirements

## Notes from the Sprint 1 log

- This is a post-sprint rerun on 1 Nov 2026, not a record from 19 to 23 Oct.
- No confirmed defects were found. All 43 tests passed.
- GAP-01: tests do not check the exact start/end instant of an exam window. This is a coverage gap, not a defect.
- OBS-02: the run used Java 25, but the project declares Java 17. Java 17 is not yet verified.