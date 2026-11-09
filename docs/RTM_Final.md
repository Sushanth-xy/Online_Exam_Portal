# Final Requirements Traceability Matrix (RTM): Phase 1 Quality Audit

| Field | Value |
|---|---|
| Source | Software Test Plan (STP), Section 13 (RTM) |
| Sprint | Sprint 2 (26 Oct to 30 Oct 2026), Step 2.11 |
| Evidence date | 7 Nov 2026 |
| Regression evidence | Full backend suite: 82 tests, 81 passed, 0 failed, 1 skipped (DEF-01 probe), BUILD SUCCESS |
| Other evidence | `docs/test-reports/nfr-audit.md` (Step 2.10), `docs/test-reports/sprint1-log.md` |
| Freeze status | **Conditional.** See open items at the bottom. |

**Status key:**
- **Passed** = proven by an automated test or a recorded audit result.
- **Not Accepted** = a gap, or the evidence is not enough.
- **Deferred** = planned for Phase 2.

## Functional requirements

| Req ID | Requirement | Test case | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-F-001 | Student registration | TC-Auth-01 | 1 | Passed | AuthControllerTest, AuthRbacIntegrationTest |
| OEP-F-002 | Login authentication | TC-Auth-02 | 1 | Passed | AuthControllerTest, AuthRbacIntegrationTest |
| OEP-F-003 | Role-based access control | TC-Auth-03 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-F-004 | Lockout after 5 failures | TC-Auth-04 | 1 (stretch) | Passed | SecurityLockoutTest |
| OEP-F-005 | Password reset link | TC-Auth-05 | 1 (stretch) | Passed | SecurityLockoutTest |
| OEP-F-006 | Create questions | TC-QB-01 | 1 | Passed | QuestionBankApiTest |
| OEP-F-007 | Create exam from bank | TC-QB-02 | 1 | Passed | ExamCreationApiTest |
| OEP-F-008 | Duration, window, negative marking | TC-QB-03 | 1 | Passed | ExamCreationApiTest, ExamDeliveryTest. See GAP-01 |
| OEP-F-009 | Shuffle questions/options | TC-QB-04 | 1 (stretch) | Not Accepted | No test found |
| OEP-F-010 | Exam preview | TC-QB-05 | 1 (stretch) | Not Accepted | No test found |
| OEP-F-011 | Timer and auto-submit | TC-Exam-01 | 1 | Passed (DEF-01 open) | SubmissionAutoGradeTest: the attempt becomes TIMED_OUT. DEF-01: saved answers are not graded on timeout. |
| OEP-F-012 | Auto-save (30s) | TC-Exam-02 | 1 | Passed | ExamDeliveryTest |
| OEP-F-013 | Review/change answers | TC-Exam-03 | 1 (stretch) | Passed | ExamDeliveryTest |
| OEP-F-014 | Scheduled window enforcement | TC-Exam-04 | 1 | Passed | ExamDeliveryTest. See GAP-01 |
| OEP-F-015 | Submission timestamp/status | TC-Exam-05 | 1 | Passed | ExamDeliveryTest |
| OEP-F-016 | Auto-grade objective questions | TC-Grade-01 | 1 | Passed (DEF-01 open) | ExamDeliveryTest, SubmissionAutoGradeTest: grading works on manual submit. DEF-01: no grading on timeout. |
| OEP-F-017 | Manual grading | TC-Grade-02 | 1 | Passed | ManualGradingApiTest, GradingApiTest |
| OEP-F-018 | Release results | TC-Grade-03 | 1 | Not Accepted | Feature not built (GAP-02) |
| OEP-F-019 | Tab-switch logging | TC-Mon-01 | 1 (stretch) | Passed | ProctoringApiTest |
| OEP-F-020 | Full-screen enforcement | TC-Mon-02 | 1 (stretch) | Passed | ProctoringApiTest (backend incident recording only; browser behavior not tested) |
| OEP-F-021 | Admin account/role management | TC-Admin-01 | 1 | Not Accepted | The only TC-Admin-01 test queries audit logs, not accounts or roles (GAP-03) |

## Non-functional requirements

| Req ID | Requirement | Test case | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-NF-001 | Response time under 2s (p90) | TC-Perf-01 | 1 | Passed (local baseline) | nfr-audit.md: p90 178 ms (page load), 47 ms (save and submit) |
| OEP-NF-002 | 500 concurrent students | TC-Perf-02 | 1 (partial) | Not Accepted | No load test run |
| OEP-NF-003 | 99.5% availability | Uptime monitoring / Ops report | 2 | Deferred | Phase 2 |
| OEP-NF-004 | 3-year log/result retention | TC-Ops-01 | 2 | Passed | AuditTrailAndRetentionTest (ahead of plan) |
| OEP-NF-005 | Browser support / WCAG 2.1 AA | TC-UX-01 | 1 (partial) | Not Accepted | nfr-audit.md: A11Y-01 (page title) not met |

## Security requirements

| Req ID | Requirement | Test case | Phase | Status | Evidence |
|---|---|---|---|---|---|
| OEP-SR-001 | TLS 1.2+ enforcement | TC-Sec-01 | 1 | Not Accepted | No test found |
| OEP-SR-002 | Salted password hashing | TC-Sec-02 | 1 | Passed | AuthRbacIntegrationTest |
| OEP-SR-003 | No question access before start | TC-Sec-03 | 1 | Passed | ExamDeliveryTest |
| OEP-SR-004 | Audit logging | TC-Sec-04 | 1 | Passed | AuditTrailAndRetentionTest |
| OEP-SR-005 | Session inactivity timeout | TC-Sec-05 | 1 | Passed | SecurityLockoutTest (expired token rejected) |

## Summary

- Passed: 23 of 31 (2 of them, OEP-F-011 and OEP-F-016, carry the open defect DEF-01)
- Not Accepted: 7 (OEP-F-009, F-010, F-018, F-021, NF-002, NF-005, SR-001)
- Deferred to Phase 2: 1 (OEP-NF-003)

## Open items before the freeze

| ID | Item | Needed |
|---|---|---|
| DEF-01 | Confirmed defect: timed-out attempts are not auto-graded. Proven by `TimeoutGradingProbeTest` (expected 4.0, was 0.0). Proposed severity: High. | A fix by the developer, a severity decision, then re-enable the probe test and rerun the regression |
| GAP-02 | Results release is not built (`resultsPublished` is only a field in `Exam.java`). | Build the feature, or the reviewer accepts it as a known gap |
| GAP-03 | No test matches admin account/role management (OEP-F-021). | Add a test, or confirm the requirement is out of scope |
| A11Y-01 | Page title is "frontend" (WCAG 2.1 SC 2.4.2). | Fix the title and retest |
| GAP-01 | The exact start/end instant of the exam window is not tested. | Optional boundary test |

## Freeze decision

The RTM is **not fully frozen**. It is frozen conditionally until the reviewer decides on DEF-01, GAP-02 and GAP-03.