# Sprint 1 Demo and Acceptance Sign-off

**Project:** Online Exam Portal  
**Sprint:** Sprint 1  
**Prepared by:** Sudarshan D - Product Owner  
**Date:** 1 November 2026  
**Status:** Conditional acceptance - outstanding functionality documented below

## 1. Sprint 1 Demo

**Demo video:** [Watch the Sprint 1 Demo](https://drive.google.com/file/d/10fLlK1oBcX9qwDf4dgQjlwDlsbKu2RGM/view?usp=sharing)

The intended one-minute demonstration covers:

1. User registration and login.
2. Role-based navigation to the appropriate dashboard.
3. Instructor question creation.
4. Exam configuration, question selection, and student-view preview.
5. Backend automated-test evidence for answer handling, submission, and objective-question grading.

The demonstration must distinguish the instructor's preview from an actual student exam attempt. Backend test evidence must not be presented as proof of a completed student-facing workflow.

## 2. Test Evidence

The Sprint 1 test log records a post-sprint backend test rerun on 1 November 2026:

- **43 tests run**
- **43 passed**
- **0 failures**
- **0 errors**
- **0 skipped**

The rerun used Java 25, although the project declares Java 17. Verification under the declared Java version remains outstanding.

The tests provide evidence for backend authentication, role-based access, question and exam creation, answer handling, submission, and objective grading. They do not establish completion of all frontend workflows, nonfunctional requirements, or manual test-plan checks.

## 3. Deviations and Outstanding Work

| Area | Observed status | Rationale / follow-up |
|---|---|---|
| Student exam-taking UI | Not demonstrated in the inspected frontend | The student dashboard currently shows a welcome message and logout action. Implement and verify the student attempt workflow. |
| Timer and automatic submission UI | Not demonstrated | Backend duration checks exist, but a connected student timer and timeout submission workflow were not found in the inspected frontend. |
| Autosave integration | Backend endpoint exists; frontend integration not found | Connect the student interface to autosave and verify persistence and restoration. |
| Objective auto-grading | Backend implementation and automated-test evidence exist | Demonstrate grading through a complete student attempt once the UI workflow is connected. |
| Password lockout and reset | Not started in the Sprint 1 RTM | Carry forward to a subsequent sprint. |
| Question shuffling, manual grading, results release, and proctoring controls | Not started in the Sprint 1 RTM | Carry forward and test before claiming full SRS coverage. |
| Performance, availability, and long-term retention | Not verified | Require dedicated performance, operational, and retention testing. |
| Java version | Post-sprint tests used Java 25 rather than declared Java 17 | Repeat the test suite using Java 17. |
| Exam scheduling boundary | Exact start/end boundary remains unverified | Add tests for attempts immediately before, at, and after the permitted window. |

## 4. Acceptance Assessment

The available evidence demonstrates meaningful Sprint 1 progress in authentication, role-based access, instructor authoring, exam configuration, and backend exam-processing logic.

However, the complete student-facing exam-taking workflow has not been demonstrated, several requirements remain unverified, and the full SRS acceptance criteria have not been established.

**Decision:** Conditional acceptance for the demonstrated functionality only. Full Sprint 1 acceptance remains pending verification of the outstanding requirements and review of the recorded demo.

## 5. Sign-off

**Product Owner:** Sudarshan D  
**Decision:** Conditional acceptance; full acceptance pending outstanding verification.  
**Demo recording and repository link:** [Watch the Sprint 1 Demo](https://drive.google.com/file/d/10fLlK1oBcX9qwDf4dgQjlwDlsbKu2RGM/view?usp=sharing)