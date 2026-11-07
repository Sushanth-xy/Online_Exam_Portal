# Phase 1 Test Summary Report

| Field | Value |
|---|---|
| Project | Online Exam Portal |
| Story | Step 2.11, Final Quality Audit and RTM Freeze |
| Prepared by | QA Lead |
| Date | 7 Nov 2026 |
| Traceability | STP Section 6 (Exit Criteria), Section 9, Section 13 (RTM) |
| Final RTM | `docs/RTM_Final.md` |

## 1. Regression result

| Run | Command | Tests | Passed | Failed | Errors | Skipped | Result |
|---|---|---:|---:|---:|---:|---:|---|
| Full backend regression | `.\mvnw clean test` from `backend\` | 81 | 81 | 0 | 0 | 0 | BUILD SUCCESS |

Test classes: AuthControllerTest, AuthRbacIntegrationTest, SecurityLockoutTest, QuestionBankApiTest, ExamCreationApiTest, ExamDeliveryTest, SubmissionAutoGradeTest, ManualGradingApiTest, GradingApiTest, ProctoringApiTest, AuditTrailAndRetentionTest, HealthControllerTest, OepBackendApplicationTests.

Non-functional evidence comes from `docs/test-reports/nfr-audit.md` (6 Nov 2026).

## 2. Requirement coverage

- Passed: 23 of 31 requirements
- Not Accepted: 7 (OEP-F-009, F-010, F-018, F-021, NF-002, NF-005, SR-001)
- Deferred to Phase 2: 1 (OEP-NF-003)

Details are in `docs/RTM_Final.md`.

## 3. Defects, findings and gaps

| ID | Type | Severity | Description | Status |
|---|---|---|---|---|
| DEF-01 | Suspected defect | To be triaged | When time runs out, `ExamService.autosave` and `startExamAttempt` set the attempt to TIMED_OUT but do not grade the saved answers. Only `submitExam` grades. A timed-out student may get a total score of 0. Found by code reading. Not yet confirmed by a test. | Open |
| DEF-02 | Defect (fixed) | Medium | Status TIMED_OUT was rolled back to IN_PROGRESS because the method threw an error inside a transaction. Found by TC-Exam-01. Fixed with `@Transactional(noRollbackFor = IllegalStateException.class)` (PR #44). | Closed |
| GAP-01 | Coverage gap | Low | The exact start/end instant of the exam window is not tested. | Open |
| GAP-02 | Missing feature | To be triaged | Results release is not built. `resultsPublished` exists only as a field in `Exam.java`. TC-Grade-03 cannot run. | Open |
| GAP-03 | Evidence gap | To be triaged | No test matches admin account/role management (OEP-F-021). | Open |
| A11Y-01 | Accessibility finding | Medium | Page title is "frontend" (WCAG 2.1 SC 2.4.2). | Open |
| A11Y-02 | Accessibility follow-up | Low | Short-answer field has a placeholder but no explicit label. | Open |

## 4. Exit criteria assessment

| Criterion | Result |
|---|---|
| Full regression of all test suites executed | Met: 81 of 81 passed |
| Zero blocking or high-severity defects | **Cannot be confirmed.** DEF-01 and GAP-02 need a severity decision. |
| RTM updated for Phase 1 | Done: 23 Passed, 7 Not Accepted, 1 Deferred |
| Final Test Summary Report delivered | This document |

## 5. Recommendation

The freeze should be **conditional**. All automated tests pass, but DEF-01, GAP-02 and GAP-03 need a decision from the reviewer (accept as known gaps, or fix and retest) before the RTM is called fully accepted.

## 6. Limits of this report

- This is a backend automated run. It does not cover frontend tests, browser behavior, or manual checks.
- Performance results are local baselines, not production numbers. The 500-concurrent-student target was not tested.
- Accessibility results cover only the sampled pages. Full WCAG 2.1 AA conformance is not confirmed.
- The nfr-audit and sprint1-log results are post-sprint reruns, not records from the sprint dates.