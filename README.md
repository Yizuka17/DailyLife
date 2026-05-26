# DailyLife

<p align="center">
    <a href="https://developer.android.com">
        <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform Android"/>
    </a>
    <a href="https://kotlinlang.org">
        <img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.1"/>
    </a>
    <a href="https://developer.android.com/jetpack/compose">
        <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
    </a>
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/License-CNC--1.0-6B7280" alt="License CNC-1.0"/>
    </a>
</p>

> Languages: English | [简体中文](README.zh-CN.md)

DailyLife is a Jetpack Compose personal finance app for recording daily income and expenses, viewing statistics, managing accounts, and backing up local data.

This repository is maintained at: https://github.com/Yizuka17/DailyLife

Original upstream repository: https://github.com/Evening-01/DailyLife

## Features

- Transaction add/edit flow with categories, notes, mood, account binding, and soft delete.
- Monthly overview, transaction details, charts, category rankings, and mood trend analysis.
- Asset account management with balances, account types, default account, and sorting.
- Custom categories for expense/income records.
- Personalization: theme mode, dynamic color, font scale, custom font, language, profile name/signature/avatar.
- Data management: backup and restore transactions, categories, accounts, preferences, reminders, and avatar image data.
- Extra tools: mortgage calculator and currency converter.

## Tech Stack

- Kotlin 2.1
- Jetpack Compose + Material 3
- Hilt
- Room
- Coroutines + Flow
- FastKV
- AndroidX Biometric
- Min SDK 26 / Target SDK 35

## Project Structure

```text
app/src/main/java/com/yizuka17/dailylife/
├── app/        # Application entry, main activity, navigation
├── core/       # Database, repositories, preferences, DI, design system, utilities
└── feature/    # Feature modules: home, transaction, chart, assets, me, etc.
```

## Build

1. Clone the repository:

```bash
 git clone https://github.com/Yizuka17/DailyLife.git
 cd DailyLife
```

2. Open with Android Studio, or build from terminal:

```bash
 ./gradlew assembleDebug
```

3. Optional release signing:
   - Copy `keystore.properties.example` to `keystore.properties`.
   - Fill in your signing configuration.

## Useful Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
./gradlew clean
```

## Screenshots

Screenshots can be placed in the [`images`](images/) directory.

## License

This project is licensed under the [Cooperative Non-Commercial License v1.0](LICENSE).
