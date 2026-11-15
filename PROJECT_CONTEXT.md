# VNPT Inventory — project context

Read this file at the start of a future Codex session to resume the project.

## Assignment

This is a simulated Android developer exercise inspired by a VNPT Ninh Binh full-stack assignment. It is not an official Android exam. Build an Android app for staff to borrow telecom equipment, with a two-level approval flow and OTP handover confirmation.

### Main workflow

1. Staff creates a borrowing request: `PENDING_MANAGER`.
2. Manager approves it: `PENDING_STOREKEEPER`.
3. Storekeeper checks stock, prepares the equipment, and generates a six-digit OTP: `WAITING_CONFIRMATION`.
4. Staff enters the correct OTP: `COMPLETED`; inventory is deducted once.

Wrong OTP must not complete the request or change stock. Prevent duplicate deductions and allocation above available stock.

### Roles

- **Staff:** browse/search equipment, create requests, track own requests, confirm receipt with OTP.
- **Manager:** review and approve pending requests.
- **Storekeeper:** check availability, prepare stock, issue OTP.

### Seed data

Equipment:

| ID | Name | Code (app seed) | Stock | Unit |
|---|---|---|---:|---|
| 1 | Modem Quang iGate | MDM-001 | 50 | Cái |
| 2 | Switch 8-Port | SWT-008 | 20 | Cái |
| 3 | Kìm bấm mạng | KIM-001 | 10 | Bộ |

Demo login accounts (password is `123` for each):

| Role | Phone |
|---|---|
| Staff | `0912345678` |
| Manager | `0988888888` |
| Storekeeper | `0905111222` |

## Technical direction and agreed scope

- Kotlin, Jetpack Compose, MVVM, `StateFlow`, Navigation Compose, Repository pattern, Coroutines.
- Use Fake repositories first; a real backend and Room are not required for the initial demo.
- Hilt was discussed and explicitly deferred. Do not add Hilt unless requested later.
- The user wants to write the code themselves and prefers detailed, incremental guidance and reviews. Do not implement code on their behalf unless they explicitly ask. Explain the next concrete task, inspect their changes, and help debug.
- Current product goal is an end-to-end demo: Staff login → create request → Manager approval → Storekeeper issues OTP → Staff confirms → stock update.

## Current implementation status

### App shell and login

- `MainActivity` starts `AppRoot`.
- Splash navigates to Login.
- Login has a `LoginScreenViewModel`, `AuthLogin` interface, `AuthLoginImpl`, and `AuthLoginProvider` with the three demo accounts above.
- Login result is nullable `UserRole`; error message is exposed as a flow.
- `AppRoot` navigates to `home/{role}` and `HomeRoute` selects the role screen.
- Role navigation is wired in code, but the user has not reported a complete manual run-through of all three accounts yet.

### Role screens

- `StaffHomeScreen` has a `Scaffold` and two tabs: Equipment and My Requests. Both contents are still placeholders.
- `ManagerHomeScreen` and `StoreKeeperHomeScreen` have basic `Scaffold` shells and placeholder text. Their actual workflows are not implemented.

### Equipment listing foundation

- `ui.staff.Equipment` model exists with `id`, `name`, `code`, `unit`, and `stock`.
- `EquipmentRepository` exposes `suspend fun getAll(): List<Equipment>`.
- `EquipmentRepositoryImpl` returns the three seed equipment rows; it is currently the fake in-memory data source despite its generic name.
- `EquipmentViewModel` loads the list, stores the search query, and exposes a case-insensitive filtered list by equipment name.
- `EquipmentListContent.kt` has an `OutlinedTextField` and `LazyColumn`, but it is currently `private` and is not connected to `StaffHomeScreen`. Because it is in package `ui.staff` while the screen is in `ui.home.Screen`, change its visibility before using it from the screen (or move it into the screen package).

## Immediate next step

Finish the Staff Equipment tab vertical slice:

1. Make `EquipmentListContent` accessible from `StaffHomeScreen`.
2. Obtain `EquipmentViewModel` in the Staff screen and replace the Equipment tab placeholder with `EquipmentListContent`.
3. Keep the My Requests tab as a placeholder for now.
4. Confirm the screen shows all three items, searching `switch` leaves Switch 8-Port, and a no-match query shows an empty-result message.

After that, implement request creation and Staff's request list, then Manager approval, Storekeeper issue/OTP, and Staff OTP confirmation. Keep inventory deduction atomic/idempotent in the repository layer.

## Known cleanup / cautions

- `UserRole.NoN` is still present even though login uses `null` for no result; remove it when convenient and make `HomeRoute` exhaustive without `else -> null`.
- The login error text is currently hard-coded and English; localization is secondary to finishing the workflow.
- The current app uses in-memory fake authentication and data. This is for the exercise only, not production authentication.
- No build or tests have been run as part of the current coaching steps. Do not claim validation unless actually performed.

## Suggested resume message

“Read `PROJECT_CONTEXT.md`. Continue guiding me through the Staff Equipment tab. I will write the code; review my changes and help me fix issues.”
