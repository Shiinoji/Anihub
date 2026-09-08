
<p align="center"> <h1 align="center">Anihub</h1> </p>

<p align="center">
  <strong>A modern Android anime tracking application powered by AniList.</strong>
</p>

<p align="center">
  Discover anime, manage your watchlist, track your progress, and keep your anime journey organized in one place.
</p>

<p align="center">
  <a href="https://github.com/Shiinoji/Anihub/releases">
    <img src="https://img.shields.io/github/v/release/Shiinoji/Anihub?style=flat-square" alt="Latest Release">
  </a>
  <a href="https://github.com/Shiinoji/Anihub">
    <img src="https://img.shields.io/github/stars/Shiinoji/Anihub?style=flat-square" alt="GitHub Stars">
  </a>
  <a href="https://github.com/Shiinoji/Anihub/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/Shiinoji/Anihub?style=flat-square" alt="License">
  </a>
</p>

---

## About

**Anihub** is an Android application designed to make discovering and managing anime simple and convenient.

The app uses the **AniList API** to provide anime information and allows users to organize their personal anime collection, track watching progress, and explore currently airing series.

Anihub is built with modern Android development tools and focuses on a clean, responsive, and user-friendly interface.

---

## Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/6015966c-ae22-413c-9bd6-3fe9cee3ffd7" width="30%" alt="AniHub Screenshot 1">
  <img src="https://github.com/user-attachments/assets/e98c908a-6b08-4c01-b2b9-2901f7812c32" width="30%" alt="AniHub Screenshot 2">
  <img src="https://github.com/user-attachments/assets/9f344871-40d0-45d6-a86d-18a45957f578" width="30%" alt="AniHub Screenshot 3">
</p>

---

## Features

### Anime Discovery

* Browse anime from AniList
* Search for anime
* View detailed anime information
* Explore genres, studios, characters, and other available information

### Watchlist

* Add anime to your personal watchlist
* Track your watching progress
* Organize your anime collection
* Quickly access your currently watching and completed anime

### Airing Calendar

* View currently airing anime
* Check upcoming episodes
* Keep track of your favorite ongoing series

### User Experience

* Modern Material 3 interface
* Smooth screen transitions and animations
* Responsive layouts
* Shimmer loading states
* Customizable color themes
* Dark and AMOLED-friendly themes
* Organized settings screen
* Error handling for network and API failures

---

## Tech Stack

Anihub is built using modern Android development technologies.

| Technology            | Purpose                      |
| --------------------- | ---------------------------- |
| **Kotlin**            | Primary programming language |
| **Jetpack Compose**   | UI development               |
| **Material 3**        | UI components and theming    |
| **AniList API**       | Anime data and metadata      |
| **GraphQL**           | AniList API communication    |
| **Retrofit**          | Network communication        |
| **Moshi**             | JSON serialization           |
| **Kotlin Coroutines** | Asynchronous operations      |
| **Android Jetpack**   | Modern Android development   |

---

## Architecture

Anihub follows a layered approach to keep the application organized and maintainable.

```text
┌─────────────────────────┐
│        UI Layer         │
│   Jetpack Compose       │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│      ViewModel Layer    │
│   State & UI Logic      │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│     Repository Layer    │
│   Data & API Handling   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│      AniList API        │
│        GraphQL           │
└─────────────────────────┘
```

The goal is to keep UI, application logic, and data operations separated so the project remains easier to maintain as new features are added.

---

## Requirements

* Android Studio
* Android SDK
* JDK compatible with the project's Gradle configuration
* Android device or emulator
* Internet connection

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Shiinoji/Anihub.git
```

### 2. Open the project

Open the cloned project in **Android Studio**.

### 3. Sync Gradle

Allow Android Studio to download and configure the required dependencies.

### 4. Run the application

Connect an Android device or start an emulator, then run the `app` configuration.

---

## API

Anihub uses the **AniList GraphQL API** to retrieve anime and related information.

AniList provides the data used by AniHub for features such as anime discovery, search, anime details, and airing information.

* [AniList](https://anilist.co/)
* [AniList API Documentation](https://docs.anilist.co/)

---

## Project Status

Anihub is actively being developed.

The project is currently focused on:

* Improving the user interface and experience
* Enhancing anime discovery
* Improving watchlist management
* Expanding airing information
* Improving application performance
* Improving reliability and error handling
* Overall application polish

---

## Roadmap

* [ ] Improve anime discovery and recommendations
* [ ] Expand airing and episode information
* [ ] Improve notification features
* [ ] Add more watchlist management options
* [ ] Improve offline support
* [ ] Add more personalization options
* [ ] Continue UI and performance improvements

---

## Releases

Check the **[Releases](https://github.com/Shiinoji/Anihub/releases)** section for available versions of AniHub.

Each release contains information about new features, improvements, and bug fixes.

---

## Contributing

Contributions, suggestions, and bug reports are welcome.

If you find a problem or have an idea for improving Anihub, feel free to open an issue or submit a pull request.

Before submitting a pull request:

1. Make sure the project builds successfully.
2. Test your changes on an Android device or emulator.
3. Keep changes focused on the relevant feature or bug.
4. Use clear commit messages.

---

## License

AniHub is released under the **MIT License**.

This means you are free to use, modify, distribute, and sublicense the project, subject to the conditions of the license.

See the [LICENSE](https://github.com/Shiinoji/Anihub/blob/master/LICENSE) file for the complete license text.

### MIT License

```text
MIT License

Copyright (c) 2026 Shiinoji

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

---

## Disclaimer

Anihub is an independent project and is **not affiliated with or endorsed by AniList**.

Anime information displayed in the application is provided through the AniList API.

---

<p align="center">
  Made with Kotlin and Jetpack Compose
</p>

