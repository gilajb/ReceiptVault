# Receipt Vault — Android App

A smart receipt scanner and expense tracker built with Kotlin, CameraX, ML Kit OCR, Room, Firebase, and MPAndroidChart.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Camera | CameraX |
| OCR | Google ML Kit Text Recognition v2 (on-device) |
| Local DB | Room (SQLite) |
| Auth | Firebase Authentication (Email + Google Sign-In) |
| Cloud DB | Firestore (with offline persistence) |
| Image Storage | Firebase Storage |
| Architecture | MVVM + Repository |
| Async | Kotlin Coroutines + LiveData |
| Charts | MPAndroidChart |
| Navigation | Jetpack Navigation Component |
| DI | Hilt |

---

## Project Structure

```
app/src/main/java/com/receiptvault/
├── ReceiptVaultApp.kt             # @HiltAndroidApp Application class
├── data/
│   ├── local/
│   │   ├── ReceiptDatabase.kt     # Room database
│   │   ├── dao/ReceiptDao.kt      # DAO with parameterized queries
│   │   └── entities/Receipt.kt   # Room entity
│   └── remote/
│       └── FirestoreDataSource.kt # Firestore + Storage operations
├── di/
│   ├── DatabaseModule.kt          # Hilt: Room DB providers
│   └── FirebaseModule.kt          # Hilt: Firebase providers
├── repository/
│   └── ReceiptRepository.kt       # Single source of truth
├── ui/
│   ├── MainActivity.kt            # Single Activity host
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginFragment.kt
│   │   └── SignupFragment.kt
│   ├── home/
│   │   ├── HomeViewModel.kt
│   │   └── HomeFragment.kt
│   ├── scanner/
│   │   ├── ScannerViewModel.kt
│   │   ├── ScannerFragment.kt     # CameraX viewfinder
│   │   └── ConfirmationFragment.kt # OCR result + edit form
│   ├── history/
│   │   ├── HistoryViewModel.kt
│   │   ├── HistoryFragment.kt     # Search + category filter
│   │   └── ReceiptAdapter.kt
│   ├── detail/
│   │   ├── DetailViewModel.kt
│   │   └── DetailFragment.kt
│   └── analytics/
│       ├── AnalyticsViewModel.kt
│       └── AnalyticsFragment.kt   # Bar chart, pie chart, budget bars
└── utils/
    ├── OcrParser.kt               # ML Kit text → structured data
    ├── CsvExporter.kt             # MediaStore CSV export
    └── Extensions.kt             # Kotlin extension functions
```

---

## Setup Instructions

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com) and create a new project.
2. Add an Android app with package name: `com.receiptvault`
3. Download `google-services.json` and place it at: `app/google-services.json`
4. Enable **Authentication** → Sign-in methods → **Email/Password** and **Google**
5. Enable **Cloud Firestore** in test or production mode
6. Enable **Firebase Storage**
7. Deploy the security rules from `firestore.rules` and `storage.rules`

### 2. Google Sign-In Web Client ID

1. In the Firebase Console → Project Settings → General → Your apps → Web client ID
2. Copy the Web Client ID
3. Add to `local.properties`:
   ```
   GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
   ```
   This file is git-ignored and never committed.

### 3. Build & Run

```bash
# Open in Android Studio Hedgehog or newer
# Sync Gradle → Run on device or emulator (API 26+)
```

---

## Modules Overview

### Module 1 — Camera & OCR Scanner
- `ScannerFragment`: CameraX live preview + capture
- `ConfirmationFragment`: ML Kit processes image, extracts merchant/date/amount/category
- User can review and edit all extracted fields before saving

### Module 2 — Receipt History
- `HistoryFragment`: RecyclerView with SearchView + Spinner category filter
- Tap any receipt → `DetailFragment` with edit/delete/share

### Module 3 — Analytics Dashboard
- `AnalyticsFragment`: Monthly bar chart + category pie chart (MPAndroidChart)
- Budget ProgressBars per category with color-coded thresholds (green/orange/red)
- Month navigation with Previous/Next buttons

### Module 4 — Firebase Auth & Cloud Sync
- Email/password + Google Sign-In via Firebase Auth
- All local receipts sync to Firestore on login
- Firestore offline persistence enabled — syncs when connection restored
- Security rules ensure users access only their own data

### Module 5 — Export
- Exports receipts as CSV using MediaStore API (compatible API 26+)
- Saved to device Downloads folder
- Android share sheet integration via `Intent.ACTION_SEND`

---

## Security Notes

- No API keys or secrets in source code — only in `local.properties` (git-ignored)
- All Room queries use parameterized inputs (no raw SQL concatenation)
- All user text inputs are sanitized via `sanitizeInput()` before saving
- Firestore & Storage rules enforce per-UID access control

---

## Architecture

```
Fragment/Activity (observe LiveData)
       │
  ViewModel (business logic, coroutines)
       │
  Repository (single source of truth)
    /     \
Room DAO   FirestoreDataSource
(local)    (remote)
```

MVVM strictly maintained — no business logic in UI layer. Hilt provides all dependencies.
