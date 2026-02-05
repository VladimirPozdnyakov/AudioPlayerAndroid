# AudioPlayer for Android

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="120" alt="AudioPlayer Logo"/>
</p>

<p align="center">
  <b>A modern, feature-rich audio player for Android</b><br>
  Built with Kotlin, Jetpack Compose & Media3
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#installation">Installation</a> •
  <a href="#building">Building</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#license">License</a>
</p>

<p align="center">
  <a href="#русский">🇷🇺 Русская версия</a>
</p>

---

## Features

- **Modern UI** — Clean, card-based design with smooth animations and gestures
- **Background Playback** — Continue listening while using other apps
- **Audio Quality Display** — Shows format, bitrate, sample rate, and quality badges (Hi-Res, Lossless)
- **Smart Organization** — Browse by All Tracks, Favorites, Artists, Albums, or Playlists
- **Custom Playlists** — Create and manage your own playlists
- **Metadata Editing** — Edit track title, artist, and album information
- **Search with History** — Quick search with recent queries saved
- **Themes** — Light, Dark, and System themes with customizable accent colors
- **Fonts** — Choose between System font or JetBrains Mono
- **Localization** — English and Russian languages
- **Folder Selection** — Choose specific folders to scan for music
- **Expandable Player** — Swipe-up mini-player that expands to full-screen
- **Multi-Artist Support** — Tracks with multiple artists are properly parsed and navigable
- **Update Checker** — Automatic check for new versions on GitHub

## Requirements

- Android 7.0 (API 24) or higher
- Storage permission for accessing audio files

## Installation

### From GitHub Releases

1. Go to [Releases](https://github.com/vladimirpozdnyakov/AudioPlayerAndroid/releases)
2. Download the latest APK
3. Install on your device (enable "Install from unknown sources" if needed)

### From Source

See [Building](#building) section below.

## Building

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build Commands

```bash
# Clone the repository
git clone https://github.com/vladimirpozdnyakov/AudioPlayerAndroid.git
cd AudioPlayerAndroid

# Build debug APK
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

The debug APK will be located at `app/build/outputs/apk/debug/app-debug.apk`

## Architecture

### Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.24 |
| UI Framework | Jetpack Compose + Material 3 |
| Media Playback | Media3 (ExoPlayer) |
| Dependency Injection | Hilt |
| Database | Room |
| Preferences | DataStore |
| Image Loading | Coil |
| Async | Kotlin Coroutines + Flow |

### Project Structure

```
app/src/main/java/com/foxelectronic/audioplayer/
├── data/
│   ├── db/              # Room database (favorites, playlists)
│   ├── model/           # Data models (Track, Playlist, AudioFormat)
│   └── repository/      # Data repositories
├── di/                  # Hilt dependency injection modules
├── network/             # GitHub API for update checking
├── service/             # Media playback service
├── ui/
│   ├── components/      # Reusable UI components
│   ├── playlist/        # Playlist-related dialogs
│   ├── settings/        # Settings screen
│   ├── theme/           # Theme configuration
│   └── update/          # Update dialog
├── update/              # Update checker logic
├── MainActivity.kt      # Main activity & screens
├── PlayerViewModel.kt   # Central state management
└── SettingsViewModel.kt # Settings state management
```

### Key Design Patterns

- **Global Player Pattern** — Singleton ExoPlayer managed at Application level for seamless playback across activities
- **MVVM** — ViewModels expose UI state via StateFlow
- **Repository Pattern** — Data access abstracted through repositories
- **Composition over Inheritance** — Composable functions for UI building blocks

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Modern Android UI toolkit
- [Media3](https://developer.android.com/guide/topics/media/media3) — Media playback library
- [Material Design 3](https://m3.material.io/) — Design system
- [Coil](https://coil-kt.github.io/coil/) — Image loading library
- [Hilt](https://dagger.dev/hilt/) — Dependency injection

---

<a name="русский"></a>
# AudioPlayer для Android

<p align="center">
  <b>Современный, функциональный аудиоплеер для Android</b><br>
  Создан на Kotlin, Jetpack Compose и Media3
</p>

---

## Возможности

- **Современный интерфейс** — Чистый дизайн в стиле карточек с плавными анимациями и жестами
- **Фоновое воспроизведение** — Продолжайте слушать музыку, используя другие приложения
- **Отображение качества аудио** — Показывает формат, битрейт, частоту дискретизации и бейджи качества (Hi-Res, Lossless)
- **Умная организация** — Просмотр по всем трекам, избранному, исполнителям, альбомам или плейлистам
- **Пользовательские плейлисты** — Создавайте и управляйте своими плейлистами
- **Редактирование метаданных** — Изменяйте название трека, исполнителя и альбом
- **Поиск с историей** — Быстрый поиск с сохранением недавних запросов
- **Темы** — Светлая, тёмная и системная темы с настраиваемым акцентным цветом
- **Шрифты** — Выбор между системным шрифтом или JetBrains Mono
- **Локализация** — Английский и русский языки
- **Выбор папок** — Выбирайте конкретные папки для сканирования музыки
- **Раскрывающийся плеер** — Мини-плеер с возможностью раскрытия на весь экран свайпом вверх
- **Поддержка нескольких исполнителей** — Треки с несколькими исполнителями правильно разбираются и доступны для навигации
- **Проверка обновлений** — Автоматическая проверка новых версий на GitHub

## Требования

- Android 7.0 (API 24) или выше
- Разрешение на доступ к хранилищу для чтения аудиофайлов

## Установка

### Из GitHub Releases

1. Перейдите в [Releases](https://github.com/vladimirpozdnyakov/AudioPlayerAndroid/releases)
2. Скачайте последний APK
3. Установите на устройство (при необходимости включите «Установка из неизвестных источников»)

### Из исходного кода

См. раздел [Сборка](#сборка) ниже.

<a name="сборка"></a>
## Сборка

### Необходимые компоненты

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34

### Команды сборки

```bash
# Клонирование репозитория
git clone https://github.com/vladimirpozdnyakov/AudioPlayerAndroid.git
cd AudioPlayerAndroid

# Сборка debug APK
./gradlew assembleDebug

# Сборка и установка на подключённое устройство
./gradlew installDebug

# Сборка release APK
./gradlew assembleRelease

# Запуск unit-тестов
./gradlew test

# Запуск инструментальных тестов
./gradlew connectedAndroidTest
```

Debug APK будет находиться в `app/build/outputs/apk/debug/app-debug.apk`

## Архитектура

### Технологический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 1.9.24 |
| UI-фреймворк | Jetpack Compose + Material 3 |
| Воспроизведение медиа | Media3 (ExoPlayer) |
| Внедрение зависимостей | Hilt |
| База данных | Room |
| Настройки | DataStore |
| Загрузка изображений | Coil |
| Асинхронность | Kotlin Coroutines + Flow |

### Структура проекта

```
app/src/main/java/com/foxelectronic/audioplayer/
├── data/
│   ├── db/              # База данных Room (избранное, плейлисты)
│   ├── model/           # Модели данных (Track, Playlist, AudioFormat)
│   └── repository/      # Репозитории данных
├── di/                  # Модули внедрения зависимостей Hilt
├── network/             # GitHub API для проверки обновлений
├── service/             # Сервис воспроизведения медиа
├── ui/
│   ├── components/      # Переиспользуемые UI-компоненты
│   ├── playlist/        # Диалоги для плейлистов
│   ├── settings/        # Экран настроек
│   ├── theme/           # Конфигурация темы
│   └── update/          # Диалог обновления
├── update/              # Логика проверки обновлений
├── MainActivity.kt      # Главная активность и экраны
├── PlayerViewModel.kt   # Центральное управление состоянием
└── SettingsViewModel.kt # Управление состоянием настроек
```

### Ключевые паттерны проектирования

- **Global Player Pattern** — Синглтон ExoPlayer на уровне Application для бесшовного воспроизведения между активностями
- **MVVM** — ViewModel'ы предоставляют UI-состояние через StateFlow
- **Repository Pattern** — Доступ к данным абстрагирован через репозитории
- **Композиция вместо наследования** — Composable-функции как строительные блоки UI

## Участие в разработке

Вклад в проект приветствуется! Не стесняйтесь отправлять Pull Request.

1. Сделайте форк репозитория
2. Создайте ветку для функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## Лицензия

Этот проект лицензирован под MIT License — см. файл [LICENSE](LICENSE) для подробностей.

## Благодарности

- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Современный UI-инструментарий Android
- [Media3](https://developer.android.com/guide/topics/media/media3) — Библиотека воспроизведения медиа
- [Material Design 3](https://m3.material.io/) — Система дизайна
- [Coil](https://coil-kt.github.io/coil/) — Библиотека загрузки изображений
- [Hilt](https://dagger.dev/hilt/) — Внедрение зависимостей
