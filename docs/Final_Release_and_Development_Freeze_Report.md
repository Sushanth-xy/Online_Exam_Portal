# Final Release and Development Freeze Report

**Project:** Online Exam Portal  
**Issue:** #25 – Final Release & Development Freeze  
**Prepared by:** Sudarshan D – Product Owner  
**Date:** 9 November 2026  
**Branch:** `docs/25-final-release-freeze`  
**Release status:** Conditional – pending final acceptance and release activities

---

## 1. Introduction

This report documents the final project status of the Online Exam Portal. It summarizes the implemented functionality, testing results, deviations from the Software Requirements Specification (SRS) and Software Test Plan (STP), and the remaining activities required before the final release.

The objective is to establish a clear record of the delivered system and provide a basis for the Product Owner's final acceptance decision.

## 2. Testing Summary

The existing Phase 1 backend regression test suite was executed using the Maven test command.

| Metric | Result |
|---|---:|
| Total tests | 82 |
| Passed | 81 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 1 |
| Build result | BUILD SUCCESS |

The skipped test, `TimeoutGradingProbeTest`, was intentionally disabled because it reproduces the timeout-grading issue documented as DEF-01.

### 2.1 Areas covered

The automated tests cover:

- User registration and authentication
- Role-based access control
- Account lockout and password-reset functionality
- Question bank operations
- Exam creation and configuration
- Exam delivery and answer handling
- Autosave and answer updates
- Exam submission and objective grading
- Manual grading
- Proctoring incident logging
- Audit logging and retention operations
- Health endpoint functionality

### 2.2 Testing limitations

The recorded test run primarily verifies backend functionality. It does not independently establish production-scale performance, full browser compatibility, complete accessibility compliance, or all student-facing workflows.

The timeout-grading probe remains skipped and should be re-enabled after the associated issue is resolved and retested.

## 3. Deviations from the Software Requirements Specification

The following deviations and limitations were identified by comparing the SRS with the final RTM and available test evidence.

| Requirement | Expected functionality | Recorded status / deviation |
|---|---|---|
| F-009 | Shuffle questions and options | Not accepted in the final RTM due to missing verification evidence |
| F-010 | Preview an examination | Not accepted due to missing verification evidence |
| F-011 | Timer and automatic submission | Implemented, but timeout grading remains an open issue |
| F-016 | Automatic grading of objective questions | Verified for normal submission; timeout grading remains an open issue |
| F-018 | Instructor-controlled result publication | Not accepted; results-release functionality was recorded as incomplete |
| F-021 | Administrative account and role management | Not accepted due to insufficient matching test evidence |
| NF-002 | Support 500 concurrent students | Not verified through a load test |
| NF-003 | 99.5% availability during examination windows | Deferred to Phase 2 |
| NF-004 | Three-year log and result retention | Audit-log policy and archival behavior tested using simulated historical records; actual three-year production retention and result retention were not independently demonstrated |
| NF-005 | Browser compatibility and WCAG 2.1 AA | Partial checks performed; complete compliance not established |
| SR-001 | Mandatory TLS 1.2 or higher | Not accepted due to missing verification evidence |

The final RTM records 23 of 31 requirements as passed, 7 as not accepted and 1 as deferred. Two of the passed requirements carry the open timeout-grading issue.

## 4. Deviations from the Software Test Plan

The original Test Plan describes a broader verification scope than the evidence available for the final Phase 1 report.

The following limitations are recorded:

1. Phase 1 testing concentrated primarily on backend automated tests.
2. The planned 500-user concurrency target was not tested.
3. Production availability was not measured over examination windows.
4. Browser verification was limited primarily to Chrome and Firefox.
5. Full WCAG 2.1 AA verification was not completed.
6. Actual three-year retention was not observed over a continuous three-year period.
7. Frontend workflows and manual acceptance checks were not comprehensively represented in the automated test results.

These limitations should be considered when interpreting the test results. A successful backend test run does not automatically establish that every end-to-end requirement has been verified.

## 5. Architecture and Design Documentation

The repository review did not locate a submitted Software Architecture Document (SAD).

Therefore, a complete comparison against the submitted SAD cannot be certified from the available project files. The original submitted SAD should be attached or referenced before this section is treated as fully verified.

## 6. Performance and Accessibility

The recorded local performance audit reported:

- Login page p90 load time: 178 ms.
- Answer autosave and submission p90: 47 ms.

These measurements were obtained in a local testing environment and do not demonstrate performance under 500 concurrent users or production deployment conditions.

Accessibility checks included targeted keyboard navigation and sampled contrast measurements. The audit identified:

- A page-title issue (`frontend`) associated with WCAG 2.1 Success Criterion 2.4.2.
- A short-answer textarea that should have an explicit accessible label.

Full accessibility compliance remains unverified.

## 7. Known Issues and Follow-up Actions

| ID | Description | Required action |
|---|---|---|
| DEF-01 | Timed-out attempts are not automatically graded using saved answers | Fix, re-enable the probe test and rerun regression |
| GAP-02 | Results-release functionality lacks verification | Implement and test, or formally record an approved scope decision |
| GAP-03 | Administrative account and role management lacks matching test evidence | Add appropriate verification or document an approved scope decision |
| A11Y-01 | Page title is not descriptive | Correct the page title and retest |
| A11Y-02 | Short-answer field lacks an explicit label | Add a persistent accessible label |
| NF-002 | 500-user concurrency target unverified | Perform a load test before claiming compliance |
| SR-001 | TLS enforcement unverified | Verify deployment configuration |

## Instructor Module Demonstration

**Recording:** [Watch Instructor Module Demo](https://drive.google.com/file/d/1uA8at7mesi-GzgIpfQj7D-ao4OLLq3n1/view?usp=sharing)

The instructor module demonstration covers:
- Instructor dashboard and navigation.
- Question creation and management.
- Exam creation and configuration.
- Exam preview.
- Available proctoring incident interface.

**Scope:** This recording covers the instructor module only.
Student, grading, and administrative workflows require additional
demonstration evidence to satisfy the complete-product demo requirement.

## 8. Final Release Assessment

The project has a substantial implementation and automated testing baseline. The recorded backend regression run completed successfully, with one intentionally skipped test.

However, the final RTM and test summary identify unresolved verification gaps and an open timeout-grading issue. Consequently, the available evidence supports a **conditional freeze**, rather than an unconditional final acceptance.

Before declaring the final release complete, the project team should:

1. Review and disposition DEF-01, GAP-02 and GAP-03.
2. Complete the required regression and acceptance checks.
3. Attach the final two-minute demonstration covering the complete product.
4. Confirm the submitted SAD and document any architecture deviations.
5. Update the final RTM and test summary with approved dispositions.
6. Create the final GitHub release tag after approval.
7. Record the development freeze and prevent further feature changes except approved fixes.

## 9. Product Owner's Conclusion

The Online Exam Portal has reached a substantial level of implementation, and the recorded automated backend regression run provides evidence of working core functionality.

The remaining work primarily concerns resolving or formally accepting known gaps, completing the final demonstration and documentation, and performing the release and freeze activities.

As Product Owner, I recommend **conditional acceptance** until the outstanding issues and release requirements have been reviewed and documented.

---

**Prepared by:**  
Sudarshan D  
Product Owner – Online Exam Portal

**Document status:** Final draft pending team review and release approval.
