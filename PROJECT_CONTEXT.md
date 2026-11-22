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

- Kotlin, Jetpack Compose, MVVM, StateFlow, Navigation Compose, Repository pattern, Coroutines, and Room.
- A loopback mock REST API is the workflow authority in debug builds; Room is its persistent local cache for the UI.
- Debug builds use OkHttp MockWebServer at `http://127.0.0.1:<random-port>/api/`; this is a client mock, not a backend reachable by other devices.
- Hilt was explicitly deferred. A manual `AppContainer` provides shared dependencies.
- Fake credentials, `X-Actor-Role` headers, and the returned demo token are not production authentication.

## Current implementation

- `MainActivity` launches `AppRoot`; Splash navigates to Login.
- Login uses `LoginScreenViewModel`, `AuthLogin`, and `AuthLoginImpl`; login now calls `POST /api/auth/login` against the local mock API.
- Navigation routes to `home/{role}/{phone}`; `HomeRoute` shows Staff, Manager, or Storekeeper UI and supports logout.
- `InventoryDatabase` is a versioned Room database with exported schema. Equipment and request tables use separate entities. Requests have a foreign key to equipment and indexes for common lookups.
- `RoomEquipmentRepository` calls Retrofit endpoints and mirrors successful responses into Room. DAO Flows feed the UI.
- The debug-only MockWebServer dispatcher handles login, equipment, request listing/creation, approval, issue, and OTP confirmation. It persists mock backend state in SharedPreferences across process restarts.
- On application startup, equipment and requests are fetched from the fake API and synchronized into Room. The database does not use destructive migration fallback.
- Staff can search equipment, create requests, track statuses, and confirm receipt with OTP.
- Manager can approve `PENDING_MANAGER` requests.
- Storekeeper can issue `PENDING_STOREKEEPER` requests, generate six-digit OTPs, and view codes awaiting handover.
- The fake API validates roles, request state, stock availability, and OTP. Correct OTP completes the request and deducts stock in the server-side operation; wrong OTP leaves backend state unchanged. Successful API responses are then cached in Room transactions.

## Validation and remaining work

- `:app:assembleDebug` completed successfully after the full mock API integration.
- No automated tests or emulator walkthrough have been run. Manually demonstrate the full workflow by logging in as each role, logging out between roles, and checking that stock persists after OTP confirmation and app restart.
- Database schema is version 1. If entities change, increment the version, add a Room migration, and commit the exported schema under `app/schemas`.
- The API contract is invented for learning because the official endpoint specification was not provided. Replace the loopback API and role header approach if a real backend contract becomes available.
- After the walkthrough, improve any loading/error states or layout issues found.

## Collaboration note

The user prefers detailed, incremental guidance but has also explicitly asked Codex to implement requested project work directly and commit/push it. Follow the latest instruction for each task.
