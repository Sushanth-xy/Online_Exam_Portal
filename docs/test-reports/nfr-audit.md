# Sprint 2 Non-Functional Verification Report

| Field | Value |
|---|---|
| Story | #23 — Sub-2s Latency Benchmark and WCAG 2.1 AA Audit |
| Sprint | Sprint 2 — 26 Oct–30 Oct 2026 |
| Test engineer | Sumiran Vuppuluri (`@sumirxn`) |
| Execution date | 6 Nov 2026 |
| Traceability | STP Section 9; TC-Perf-01; TC-UX-01; OEP-NF-001; OEP-NF-005 |
| Status | Performance threshold met in local samples; accessibility review found an issue, so WCAG 2.1 AA conformance is **not confirmed** |

This is a post-sprint rerun, not a contemporaneous execution record from 26–30 Oct.

## Summary

| Case | Verification | Result |
|---|---|---|
| TC-Perf-01 / OEP-NF-001 | Login page load event, 20 browser reload samples | **Pass:** p90 178 ms (limit 2,000 ms) |
| TC-Perf-01 / OEP-NF-001 | Answer autosave followed by exam submission, 30 samples | **Pass (local baseline):** p90 47 ms (limit 2,000 ms) |
| TC-UX-01 / OEP-NF-005 | Keyboard navigation, representative text contrast, page title | **Partial:** tested controls navigated by keyboard and sampled contrasts passed; generic page title is an accessibility finding |

## Performance benchmark

### Page load

- **Page:** `/login` on the local Vite development server (`http://127.0.0.1:5173`).
- **Method:** 20 browser reloads; measured `PerformanceNavigationTiming.loadEventEnd - startTime` for each navigation. The p90 uses the nearest-rank method.
- **Results:** n=20; p50 **157 ms**; p90 **178 ms**; min **142 ms**; max **183 ms**.

### Answer save and submission

- **Method:** 30 fresh student attempts against the exam autosave and submit API operations, using Spring `MockMvc` and the test H2 in-memory database. Each sample measures the combined answer autosave plus submission request time. The p90 uses the nearest-rank method.
- **Results:** n=30; p50 **33 ms**; p90 **47 ms**; min **22 ms**; max **171 ms**.
- **Execution:** the backend test application context was warmed before the samples. A temporary benchmark test was used for this run and removed afterward; the fixture and results are synthetic and local.

Both measured p90 values are below 2.0 seconds. These are local, single-user baselines: the browser run used the Vite development server, while the API run used in-process `MockMvc` and H2 rather than a real network connection to PostgreSQL. They do not establish production latency, production database behavior, or performance under concurrent load. OEP-NF-002's 500-concurrent-student target was not tested.

## Accessibility review

### Keyboard navigation

- **Login:** Tab order was brand link → email → password → Sign in → Create one. Email and password have explicit labels.
- **Student exam:** a mocked exam fixture was used to inspect the rendered UI without creating or changing real exam data. Tab order reached Enter fullscreen → short-answer field → MCQ option → Save answers → Submit exam.
- This was a targeted keyboard check, not a full keyboard-only execution of every route, modal, or state. Screen-reader and assistive-technology testing was not performed.

### Contrast checks

Ratios were computed from rendered foreground/background colors. WCAG AA requires 4.5:1 for normal text and 3:1 for large text.

| Screen / element | Foreground / background | Ratio | Result |
|---|---|---:|---|
| Login brand, link, and button label | `#4f46e5` / `#ffffff` | 6.29:1 | Pass |
| Login heading and field labels | `#172033` / `#ffffff` | 16.27:1 | Pass |
| Login subtitle and footer text | `#697386` / `#ffffff` | 4.78:1 | Pass |
| Home backend-up status | `#008000` / `#f3f6fc` | 4.75:1 | Pass |

Only the listed rendered text states were measured. Contrast in every component, disabled state, and color theme was not exhaustively audited.

### Finding

| ID | Severity | Observation | WCAG reference / disposition |
|---|---|---|---|
| A11Y-01 | Medium | The browser reports the document title as **“frontend”** rather than a meaningful product/page title. This title is supplied by the static HTML entry point and is shared by routed screens. | WCAG 2.1 SC 2.4.2, Page Titled. **Not met** for the tested page; update and retest the title before claiming AA conformance. |
| A11Y-02 | Low / follow-up | The short-answer textarea in the student exam has a placeholder but no explicit associated `<label>` or `aria-label`. The browser exposed the placeholder as its textbox name, but a persistent visible label and assistive-technology check are still recommended. | Review form-label usability and test with a screen reader; not recorded as a confirmed WCAG failure in this limited run. |

## Scope and evidence notes

- No production database, production-like deployment, load generator, concurrency test, automated axe scan, or cross-browser matrix was used.
- The student exam UI was rendered with a browser-mocked response for keyboard inspection; its API behavior was not included in the browser accessibility run.
- The results verify only the sampled routes and states. They do not establish full WCAG 2.1 AA conformance.
- Related traceability: [RTM, non-functional requirements](../RTM_Sprint1.md) and [Software Test Plan](../Software_Test_Plan_Online_Exam_Portal.pdf).
