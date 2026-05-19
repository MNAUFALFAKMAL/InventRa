# 📦 InventRa — Inventaris Nexara App

![CI](https://github.com/MNAUFALFAKMAL/InventRa/actions/workflows/ci.yml/badge.svg)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-brightgreen)
![KMP](https://img.shields.io/badge/KMP-Kotlin%20Multiplatform-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Aplikasi manajemen inventaris berbasis AI yang dibangun dengan Kotlin Multiplatform (KMP) dan Compose Multiplatform. InventRa membantu pengguna mencatat, mengelola, dan menganalisis inventaris barang secara efisien dengan bantuan AI.

---

## 👥 Tim

| Nama | NIM | GitHub | Role            |
|------|-----|--------|-----------------|
| Muhammad Naufal Fikri Akmal | 123140132 | [@MNAUFALFAKMAL](https://github.com/MNAUFALFAKMAL) | Backend Developer |
| Nabila Ramadhani Mujahidin | 123140062 | [@nblable](https://github.com/nblable) | UI/UX Developer |

> **Mata Kuliah:** IF25-22017 Pengembangan Aplikasi Mobile  
> **Program Studi:** Teknik Informatika — Institut Teknologi Sumatera (ITERA)  
> **Dosen:** Pak Habib ([mh4Scripts](https://github.com/mh4Scripts))

---

## 📱 Deskripsi Aplikasi

**InventRa** adalah aplikasi manajemen inventaris modern yang memungkinkan pengguna untuk:
- Mencatat dan melacak barang inventaris
- Mengkategorikan item berdasarkan jenis
- Mendapatkan insight dari AI tentang inventaris
- Mengakses data secara offline (offline-first)

**Target Pengguna:** Individu, UMKM, atau tim kecil yang membutuhkan solusi inventaris sederhana namun powerful.

---

## ✨ Fitur

### Minimum (Wajib)
- [ ] **Item List Screen** — Tampilkan semua item inventaris
- [ ] **Item Detail Screen** — Detail lengkap per item
- [ ] **Add/Edit Item Screen** — Form tambah dan edit item
- [ ] **Category Screen** — Filter berdasarkan kategori
- [ ] **Search Screen** — Cari item berdasarkan nama/kode
- [ ] **CRUD Operations** — Create, Read, Update, Delete item
- [ ] **Local Storage** — SQLDelight untuk data persisten
- [ ] **State Management** — StateFlow + ViewModel (MVVM)
- [ ] **Navigation** — Multi-screen dengan argument passing
- [ ] **Dependency Injection** — Koin DI setup
- [ ] **Unit Tests** — Minimal 10 unit tests, coverage > 50%
- [ ] **UI Tests** — Minimal 3 UI tests

### Bonus
- [ ] **AI Integration** (+10%) — Gemini API untuk analisis inventaris
- [ ] **Dark Mode** (+5%) — Support tema gelap/terang
- [ ] **Offline First** (+5%) — App berfungsi tanpa internet
- [ ] **CI/CD** (+5%) — GitHub Actions automated build & test
- [ ] **iOS Support** (+10%) — Build untuk iOS simulator

---

## 🏗️ Arsitektur

Menggunakan **Clean Architecture + MVVM**:

```
┌─────────────────────────────────────────┐
│           PRESENTATION LAYER            │
│   Screen (Composable) ◄──► ViewModel   │
│          StateFlow / UI State           │
└─────────────────────┬───────────────────┘
                      │ calls
┌─────────────────────▼───────────────────┐
│             DOMAIN LAYER                │
│   Use Cases ──► Repository Interface   │
│           Domain Models                 │
└─────────────────────┬───────────────────┘
                      │ implements
┌─────────────────────▼───────────────────┐
│              DATA LAYER                 │
│  SQLDelight (Local) │ Ktor (Remote AI)  │
│       DataStore (Preferences)           │
└─────────────────────────────────────────┘
```

### Dependency Rule
Domain tidak tahu tentang Data atau Presentation. Dependencies hanya mengarah ke dalam (inward).

---

## 🛠️ Tech Stack

| Layer | Teknologi |
|-------|-----------|
| **Framework** | Kotlin Multiplatform, Compose Multiplatform |
| **UI** | Material Design 3, Compose |
| **State** | StateFlow, ViewModel |
| **Navigation** | Compose Navigation (Type-safe Routes) |
| **Networking** | Ktor Client |
| **Local DB** | SQLDelight |
| **Preferences** | DataStore |
| **DI** | Koin |
| **AI** | Google Gemini API |
| **Testing** | kotlin.test, Turbine, MockK |

---

## 📁 Struktur Project

```
composeApp/src/
├── commonMain/kotlin/com/example/inventra/
│   ├── core/
│   │   ├── di/          # Koin modules
│   │   ├── network/     # HTTP client, API config
│   │   └── util/        # Extensions, helpers
│   ├── data/
│   │   ├── local/       # SQLDelight, DataStore
│   │   ├── remote/      # Ktor, DTOs
│   │   └── repository/  # Repository implementations
│   ├── domain/
│   │   ├── model/       # Domain models
│   │   ├── repository/  # Repository interfaces
│   │   └── usecase/     # Business logic
│   └── presentation/
│       ├── navigation/  # Routes, NavHost
│       ├── screens/     # home, addnote, detail, ai
│       ├── components/  # Reusable composables
│       └── theme/       # Material theme
├── commonMain/sqldelight/ # Database schema
├── androidMain/           # Android-specific code
└── iosMain/               # iOS-specific code
```

---

## 🚀 Setup & Menjalankan

### Prasyarat
- Android Studio Ladybug (2024.2.1+)
- JDK 17+
- Android SDK API 34/35
- Git

### Langkah

```bash
# 1. Clone repository
git clone https://github.com/YOUR_USERNAME/InventRa.git
cd InventRa

# 2. Buat local.properties
cp local.properties.example local.properties
# Edit: isi sdk.dir dan GEMINI_API_KEY
```

Dapatkan Gemini API key gratis di [Google AI Studio](https://aistudio.google.com/)

```bash
# 3. Build project
./gradlew :composeApp:assembleDebug

# 4. Install ke device/emulator
./gradlew :composeApp:installDebug
```

Atau buka di Android Studio → Sync → Run `composeApp`.

---

## 🧪 Testing

```bash
# Semua unit tests
./gradlew :composeApp:testDebugUnitTest

# Atau semua target
./gradlew allTests
```

---

## 📅 Project Plan

### Sprint 1 — Planning & Setup (Minggu 11)
| Task | PIC | Status |
|------|-----|--------|
| Setup GitHub repository & CI/CD | Semua | ✅ Done |
| KMP project structure (Clean Architecture) | Lead | ✅ Done |
| Domain models & interfaces | Lead | ✅ Done |
| Koin DI setup | Lead | ✅ Done |
| README & dokumentasi | Semua | ✅ Done |
| Tema & branding InventRa | UI/UX | ✅ Done |

### Sprint 2 — Core Features (Minggu 12)
| Task | PIC | Status |
|------|-----|--------|
| Item List Screen + ViewModel | UI/UX | 🔲 Todo |
| Add/Edit Item Screen | UI/UX | 🔲 Todo |
| Item Detail Screen | UI/UX | 🔲 Todo |
| SQLDelight schema & repository impl | Lead | 🔲 Todo |
| Navigation setup (5 screens) | Lead | 🔲 Todo |
| Unit tests untuk Repository | QA | 🔲 Todo |

### Sprint 3 — Advanced Features (Minggu 13)
| Task | PIC | Status |
|------|-----|--------|
| Search dengan debounce | Lead | 🔲 Todo |
| Filter & sort by category/date | UI/UX | 🔲 Todo |
| Gemini AI integration | Lead | 🔲 Todo |
| Offline-first implementation | Lead | 🔲 Todo |
| Unit tests lanjutan | QA | 🔲 Todo |

### Sprint 4 — Polish & Testing (Minggu 14)
| Task | PIC | Status |
|------|-----|--------|
| Bug fixes | Semua | 🔲 Todo |
| UI polish & animasi | UI/UX | 🔲 Todo |
| Dark mode | UI/UX | 🔲 Todo |
| 10+ unit tests, 3+ UI tests | QA | 🔲 Todo |
| Code coverage > 50% | QA | 🔲 Todo |

### Sprint 5 — Final Preparation (Minggu 15)
| Task | PIC | Status |
|------|-----|--------|
| Final bug fixes | Semua | 🔲 Todo |
| Release APK | Lead | 🔲 Todo |
| Slide presentasi | Semua | 🔲 Todo |
| Demo script | Semua | 🔲 Todo |
| README final | Semua | 🔲 Todo |

---

## 📄 Dokumentasi Tambahan

| Dokumen | Deskripsi |
|---------|-----------|
| [Cara Menjalankan](./docs/CARA_MENJALANKAN.md) | Setup lengkap |
| [Struktur Kode](./docs/STRUKTUR_KODE.md) | Arsitektur detail |
| [Git Workflow](./docs/GIT_WORKFLOW.md) | Branching strategy |
| [Troubleshooting](./docs/TROUBLESHOOTING.md) | Solusi masalah umum |

---

## 🤝 Contributing

1. Buat branch dari `develop`: `git checkout -b feature/nama-fitur`
2. Commit dengan konvensi: `feat: deskripsi singkat`
3. Push dan buat Pull Request ke `develop`
4. Request review dari teammate
5. Merge setelah approved

---

*InventRa — Dikembangkan untuk mata kuliah Pengembangan Aplikasi Mobile, ITERA*
