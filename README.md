# 📦 InventRa — Inventaris Nexara

![CI](https://github.com/MNAUFALFAKMAL/InventRa/actions/workflows/ci.yml/badge.svg)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-brightgreen)
![KMP](https://img.shields.io/badge/KMP-Kotlin%20Multiplatform-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**InventRa** adalah aplikasi manajemen inventaris cerdas yang dirancang khusus untuk pengurus **HMIF ITERA Kabinet Nexara 2026**. Aplikasi ini memungkinkan peminjaman dan pengembalian aset organisasi secara terorganisir dengan bantuan asisten AI.

---

## 👥 Tim Kelompok

| Nama | NIM | GitHub | Role |
|------|-----|--------|------|
| **Muhammad Naufal Fikri Akmal** | 123140132 | [@MNAUFALFAKMAL](https://github.com/MNAUFALFAKMAL) | Data Engineer & Navigation Architect |
| **Nabila Ramadhani Mujahidin** | 123140062 | [@nblable](https://github.com/nblable) | UI/UX Engineer & ViewModel Developer |

---

## 🎓 Informasi Akademik

- **Mata Kuliah**: Pengembangan Aplikasi Mobile (IF25-22017)
- **Program Studi**: Teknik Informatika - Institut Teknologi Sumatera (ITERA)
- **Dosen Pengampu**: [M Habib Algifari, S.Kom., M.T.I. (mh4Scripts)](https://github.com/mh4Scripts)

---

---

*InventRa — Solusi Inventaris Digital untuk HMIF ITERA Kabinet Nexara*

```
┌─────────────────────────────────────────┐
│           PRESENTATION LAYER            │
│   Screen (Composable) ↔ ViewModel       │
│         StateFlow, UI Events            │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│             DOMAIN LAYER                │
│    Use Cases (Business Logic)           │
│    Repository Interfaces                │
│    Domain Models                        │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│              DATA LAYER                 │
│   Repository Implementations            │
│   SQLDelight (Local) ↔ Supabase (Remote)│
│   Gemini API (AI)                       │
└─────────────────────────────────────────┘
```

### Struktur Folder

```
composeApp/src/commonMain/kotlin/com/example/inventra/
├── core/
│   ├── di/              # Koin modules (AppModule)
│   └── network/         # HttpClientFactory, SupabaseClient, ApiConfig
├── data/
│   ├── local/
│   │   ├── entity/      # ItemMapper, BorrowRecordMapper
│   │   └── datastore/   # UserPreferences, DataStoreFactory
│   ├── remote/
│   │   ├── api/         # GeminiService
│   │   └── dto/         # GeminiDto, SupabaseDto
│   └── repository/      # ItemRepositoryImpl, BorrowRepositoryImpl, AuthRepositoryImpl, AIRepositoryImpl
├── domain/
│   ├── model/           # Item, BorrowRecord, User
│   ├── repository/      # ItemRepository, BorrowRepository, AuthRepository, AIRepository
│   └── usecase/         # GetAllItemsUseCase, SaveItemUseCase, DeleteItemUseCase, SearchItemsUseCase
└── presentation/
├── components/      # GlassCard, ItemCard, EmptyState, LoadingIndicator, StatusBadge, dll
├── navigation/      # Routes, AppNavHost (animasi transisi)
├── screens/
│   ├── splash/      # SplashScreen
│   ├── auth/        # LoginScreen, LoginViewModel
│   ├── dashboard/   # DashboardScreen, DashboardViewModel
│   ├── catalog/     # CatalogScreen, CatalogViewModel
│   ├── detail/      # ItemDetailScreen, ItemDetailViewModel
│   ├── addedit/     # AddEditItemScreen, AddEditItemViewModel
│   ├── history/     # HistoryScreen, HistoryViewModel
│   ├── ai/          # AIInventoryScreen, AIInventoryViewModel
│   └── profile/     # ProfileScreen
└── theme/           # InventRaTheme, Dark Mode, Color Scheme
```

---

## 🛠️ Tech Stack

| Layer | Technology | Keterangan |
|-------|------------|------------|
| **UI** | Compose Multiplatform | Material 3, Dark Mode |
| **State Management** | StateFlow + ViewModel | MVVM pattern |
| **Navigation** | Jetpack Navigation Compose | Type-safe + animasi slide/fade |
| **Networking** | Ktor Client | OkHttp (Android), Darwin (iOS) |
| **Local Database** | SQLDelight | Offline cache, CRUD |
| **Backend** | Supabase | Auth, PostgreSQL, Storage |
| **Preferences** | DataStore | User settings |
| **DI** | Koin | viewModelOf, singleOf, factory |
| **AI** | Google Gemini API | gemini-2.5-flash |
| **Testing** | Kotlin Test + Turbine | Flow testing |
| **CI/CD** | GitHub Actions | Auto build + test |

---

## 🚀 Cara Menjalankan

### Prerequisites

| Software | Versi Minimum |
|----------|---------------|
| Android Studio | Ladybug 2024.2.1+ |
| JDK | 17 |
| Android SDK | API 24+ |

### Setup

**1. Clone repository**
```bash
git clone https://github.com/MNAUFALFAKMAL/InventRa.git
cd InventRa
```

**2. Buat file `local.properties`**
```properties
sdk.dir=/path/to/android/sdk
GEMINI_API_KEY=your_gemini_api_key_here
SUPABASE_URL=your_supabase_url_here
SUPABASE_ANON_KEY=your_supabase_anon_key_here
```

Dapatkan Gemini API key di: https://aistudio.google.com/

**3. Build & Run**
```bash
# Build APK debug
./gradlew :composeApp:assembleDebug

# Install ke device/emulator
./gradlew :composeApp:installDebug
```

Atau buka di Android Studio → pilih `composeApp` → Run.

---

## 📅 Project Plan & Task Assignment

### Sprint 1 — Foundation (Minggu 11)

| Task | PIC | Status |
|------|-----|--------|
| Setup GitHub repository & branch | MNAUFALFAKMAL | ✅ |
| KMP project structure & Clean Architecture | MNAUFALFAKMAL | ✅ |
| GitHub Actions CI/CD setup | MNAUFALFAKMAL | ✅ |
| SQLDelight schema (Item.sq, BorrowRecord.sq) | MNAUFALFAKMAL | ✅ |
| Domain models & repository interfaces | MNAUFALFAKMAL | ✅ |
| Koin DI setup (AppModule, androidModule) | nblable | ✅ |
| Material 3 Theme & brand color HMIF | nblable | ✅ |
| README awal & dokumentasi | nblable | ✅ |
| DataStore setup (UserPreferences) | nblable | ✅ |

### Sprint 2 — Core Features (Minggu 12)

| Task | PIC | Status |
|------|-----|--------|
| ItemRepositoryImpl + BorrowRepositoryImpl | MNAUFALFAKMAL | ✅ |
| AuthRepositoryImpl (Supabase Auth) | MNAUFALFAKMAL | ✅ |
| AppNavHost + Routes + navigation arguments | MNAUFALFAKMAL | ✅ |
| AddEditItemScreen + ViewModel (CRUD) | MNAUFALFAKMAL | ✅ |
| ItemDetailScreen + ViewModel | MNAUFALFAKMAL | ✅ |
| DashboardScreen + ViewModel | nblable | ✅ |
| CatalogScreen + ViewModel | nblable | ✅ |
| HistoryScreen + ViewModel | nblable | ✅ |
| LoginScreen + ViewModel | nblable | ✅ |
| ProfileScreen (Dark Mode toggle) | nblable | ✅ |
| Reusable components (GlassCard, ItemCard, dll) | nblable | ✅ |

### Sprint 3 — Advanced Features (Minggu 13)

| Task | PIC | Status |
|------|-----|--------|
| Offline-first: ItemRepositoryImpl dengan SQLDelight cache | MNAUFALFAKMAL | ✅ |
| Offline-first: BorrowRepositoryImpl dengan SQLDelight cache | MNAUFALFAKMAL | ✅ |
| Background sync Supabase ↔ SQLDelight | MNAUFALFAKMAL | ✅ |
| AppNavHost animasi transisi (slide + fade) | MNAUFALFAKMAL | ✅ |
| SplashScreen dengan fade animation | MNAUFALFAKMAL | ✅ |
| Search debounce 300ms di CatalogViewModel | nblable | ✅ |
| AIInventoryScreen connect ke AIInventoryViewModel | nblable | ✅ |
| EmptyState dengan fade + scale animation | nblable | ✅ |
| Unit tests: ItemRepositoryTest, CatalogViewModelTest | nblable | ✅ |
| Unit tests: DashboardViewModelTest, BorrowRepositoryTest, ItemUseCaseTest | nblable | ✅ |

### Sprint 4 — Polish & Testing (Minggu 14) ✅

| Task | PIC | Status |
|------|-----|--------|
| Role-based access (Admin vs Member) | MNAUFALFAKMAL | ✅ |
| Reset data feature for Admin | MNAUFALFAKMAL | ✅ |
| Bug fixes semua screen | MNAUFALFAKMAL | ✅ |
| UI polish & consistency check | nblable | ✅ |
| Complete unit test coverage (40+ tests) | nblable | ✅ |
| UI Testing critical flows (10+ tests) | nblable | ✅ |
| README updated with test instructions | nblable | ✅ |

---

## 🧪 Testing & Quality Assurance

InventRa telah melalui proses pengujian yang ketat untuk memastikan stabilitas dan performa sesuai kriteria **Sprint 4**.

### 1. Unit Testing (ViewModel & Repository)
Fokus pada logika bisnis, state management, dan manipulasi data menggunakan **Kotlin Test** dan **Turbine**.
- **Total Unit Tests**: 40+ skenario.
- **Coverage**: Mencakup seluruh Repository dan ViewModel utama.

**Cara Menjalankan:**
```bash
# Menjalankan seluruh Unit Test
./gradlew :composeApp:testDebugUnitTest
```

### 2. UI Testing (Instrumented Tests)
Memverifikasi alur kritis aplikasi (*Critical Flows*) menggunakan **Compose Test Rule**.
- **Total UI Tests**: 10+ skenario.
- **Skenario Utama**: Login Flow, Catalog Filtering, Detail Navigation, dan Empty States.

**Cara Menjalankan:**
*(Pastikan emulator atau device Android sudah terhubung)*
```bash
# Menjalankan UI Test di device/emulator
./gradlew :composeApp:connectedDebugAndroidTest
```

### 3. Code Coverage
Kami menargetkan minimal **50% coverage** untuk seluruh kode basis. Logika kritis di Repository dan ViewModel telah diuji secara mendalam.

**Status**: ✅ **65%+ Coverage achieved** (Repository & ViewModel logic fully covered).

---

## 🔧 Bug Fixes & UI Polish (Sprint 4)

Dalam Sprint terakhir, kami melakukan perbaikan menyeluruh:
- **UI Polish**: Konsistensi warna tema (Biru InventRa) di seluruh header screen (History, AI Assistant).
- **Logo Branding**: Penempatan Logo HMIF di Login dan Dashboard untuk identitas organisasi.
- **Edge Cases**: Penanganan state saat data kosong (*Empty State*) dan error jaringan.
- **Role Fix**: Penyesuaian tampilan profil khusus untuk Admin (Bendahara Umum) dan Member (Staff).
- **Stability**: Perbaikan crash pada navigasi arguments dan sinkronisasi database SQLDelight.

---

## 📊 Progress Sprint

### Sprint 1: Foundation ✅
| Deliverable | Status |
|-------------|--------|
| GitHub repository & collaborators | ✅ |
| KMP project structure clean architecture | ✅ |
| GitHub Actions CI passing | ✅ |
| README lengkap dengan team info & project plan | ✅ |
| Koin DI setup (Bonus) | ✅ |

### Sprint 2: Core Features ✅
| Deliverable | Status |
|-------------|--------|
| 3+ working screens | ✅ 8 screens |
| Navigation dengan arguments | ✅ |
| Repository pattern + SQLDelight | ✅ |
| CRUD operations | ✅ |
| UI States Loading/Success/Error/Empty | ✅ |
| Semua fitur accessible (no dead ends) | ✅ |
| API Integration Supabase (Bonus) | ✅ |

### Sprint 3: Advanced Features ✅
| Deliverable | Status |
|-------------|--------|
| Search/filter dengan debounce | ✅ |
| API Integration Supabase + Gemini | ✅ |
| Offline support SQLDelight cache | ✅ |
| Additional screen (Profile) | ✅ |
| Bonus: Dark mode, animasi, AI Assistant, Splash | ✅ |

### Sprint 4: Polish & Testing ✅
| Deliverable | Status |
|-------------|--------|
| All known bugs fixed | ✅ |
| UI polished (Role-based UI) | ✅ |
| 10+ unit tests (35+ implemented) | ✅ |
| 3+ UI tests (15+ implemented) | ✅ |
| 50%+ code coverage achieved | ✅ |
| README updated with test instructions | ✅ |

---

## 🎥 Demo Video

| Sprint | Link |
|--------|------|
| Sprint 2 & 3 | [Google Drive](https://drive.google.com/file/d/1-H1Nh0JPQjPFbAODvzFAu8Zf7AJ7mHbc/view?usp=drive_link) |

---

## 📚 Referensi

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Koin DI](https://insert-koin.io/)
- [Supabase Kotlin](https://supabase.com/docs/reference/kotlin/introduction)
- [Google Gemini API](https://ai.google.dev/docs)

---

*InventRa — Solusi Inventaris Digital untuk HMIF ITERA*