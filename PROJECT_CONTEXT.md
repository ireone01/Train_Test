# VNPT Inventory — project context

Read this file at the start of a future Codex session to resume the project.

## Assignment

This is a simulated Android developer exercise inspired by a VNPT Ninh Binh full-stack assignment. It is not an official Android exam. Build an Android app for borrowing and issuing telecom equipment with a two-level approval workflow and OTP handover confirmation.

Workflow:

1. Staff creates a request: `PENDING_MANAGER`.
2. Manager approves: `PENDING_STOREKEEPER`.
3. Storekeeper checks availability, issues equipment, and generates a six-digit OTP: `WAITING_CONFIRMATION`.
4. Staff enters the OTP: `COMPLETED`; stock is deducted once.

Wrong OTP must not complete a request or change stock. Prevent duplicate deductions and issuing above available stock.

### Roles and demo data

| Role | Phone | Password |
|---|---|---|
| Staff | `0912345678` | `123` |
| Manager | `0988888888` | `123` |
| Storekeeper | `0905111222` | `123` |

| ID | Equipment | Code | Initial stock | Unit |
|---:|---|---|---:|---|
| 1 | Modem Quang iGate | MDM-001 | 50 | Cái |
| 2 | Switch 8-Port | SWT-008 | 20 | Cái |
| 3 | Kìm bấm mạng | KIM-001 | 10 | Bộ |

## Technical choices

- Kotlin, Jetpack Compose, MVVM, StateFlow, Navigation Compose, Repository pattern, Coroutines.
- In-memory Fake Repository is the current data layer. No real backend or Room is required for the demo.
- Hilt was explicitly deferred. Dependencies are provided through a small manual `AppContainer` so workflow state is shared by all role destinations.
- Fake credentials and role routing are for the exercise only, not production authentication.

## Current implementation

- `MainActivity` launches `AppRoot`; Splash navigates to Login.
- Login uses `LoginScreenViewModel`, `AuthLogin`, and `AuthLoginImpl` with the three demo accounts. A successful login returns a `UserSession` containing phone and role.
- Navigation routes to `home/{role}/{phone}`; `HomeRoute` shows Staff, Manager, or Storekeeper UI and supports logout.
- `EquipmentRepositoryImpl` is a shared in-memory repository provided by `AppContainer`. It owns equipment and request StateFlows and serializes workflow mutations with a `Mutex`.
- `EquipmentViewModel` exposes equipment search by name. Staff sees an equipment `LazyColumn`, creates requests with quantity validation, and sees personal request statuses.
- Manager sees `PENDING_MANAGER` requests and can approve them.
- Storekeeper sees `PENDING_STOREKEEPER` requests, can issue equipment and generate a six-digit OTP, and sees OTPs awaiting handover.
- Staff can enter the OTP for their own `WAITING_CONFIRMATION` request. Correct OTP completes the request and deducts stock once. Wrong OTP leaves request and stock unchanged.
- `BorrowRequestViewModel` connects role screens to the shared repository; repository methods also check the actor role.

## Validation and remaining work

- `:app:assembleDebug` completed successfully after the end-to-end implementation.
- No automated tests or emulator walkthrough have been run. Manually demonstrate the full workflow by logging in as each of the three roles, logging out between roles, and checking stock after OTP confirmation.
- Potential polish after manual walkthrough: improve loading/error presentation, localize remaining strings, and review layout/keyboard behavior. Prioritize any issues found in the demo flow.

## Collaboration note

The user initially preferred detailed coaching and writing code themselves. In the latest request they explicitly asked Codex to complete the whole application, then commit and push it. For future changes, follow the user's latest instruction; ask whether they want coaching or direct implementation only if the desired mode is unclear.
