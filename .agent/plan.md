# Project Plan

AniHub: An anime tracking and discovery app.
Key Features:
- Dynamic Theming (Light, Dark, AMOLED, System) with multiple color palettes based on user provided wireframes.
- AniList GraphQL API integration using Retrofit.
- HomeScreen with 'Trending Now', 'Most Popular', and 'Seasonal Anime' rows.
- SearchScreen with grid results.
- WatchlistScreen with 'Watchlist' and 'Favorites' tabs.
- AnimeDetailScreen with banner, poster, metadata, description, characters, and recommendations.
Tech Stack: Jetpack Compose (M3), Retrofit, Hilt, Coil, Coroutines, Flow.

## Project Brief

# AniHub Project Brief

### Features
1. **Dynamic Discovery (Home)**: Browse trending, popular, and seasonal anime titles via the AniList GraphQL API, presented in high-performance adaptive carousels and grids.
2. **Unified Search**: Search the entire AniList database with a responsive grid view that maintains performance even with large result sets.
3. **Comprehensive Anime Details**: View rich metadata including synopses, character lists, and recommendations, featuring high-definition banners and posters.
4. **Adaptive Watchlist**: Manage personal "Watchlist" and "Favorites" collections with a layout that adapts seamlessly across mobile, foldable, and tablet devices.

### High-Level Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Navigation 3 (State-driven)
- **Adaptive Strategy**: Compose Material Adaptive library for multi-pane and responsive layouts.
- **Networking**: Retrofit & OkHttp for GraphQL API integration.
- **Asynchrony**: Kotlin Coroutines and Flow for reactive data handling.
- **Image Loading**: Coil for optimized image fetching and caching.

### UI Design Image
![UI Design](file:///C:/Users/Neil/AndroidStudioProjects/Anihub2/input_images/image_1.png)

## Implementation Steps

### Task_1_Foundation: Initialize the project foundation: Set up networking with Retrofit for AniList GraphQL API, persistence with Room for the watchlist, and configure the Material 3 theme with dynamic color support and Edge-to-Edge display.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - AniList API client fetches anime data
  - Room database is functional
  - M3 theme and Edge-to-Edge configured
  - Project builds successfully
- **StartTime:** 2026-06-25 23:08:57 PST

### Task_2_Navigation_Home: Implement the core navigation using Navigation 3 and build the HomeScreen featuring 'Trending Now', 'Most Popular', and 'Seasonal Anime' sections.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Navigation structure works
  - HomeScreen displays lists from API
  - UI matches design in C:/Users/Neil/AndroidStudioProjects/Anihub2/input_images/image_1.png

### Task_3_Search_Details: Develop the SearchScreen with a responsive grid and the AnimeDetailScreen displaying rich metadata, characters, and recommendations.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Search returns results from AniList
  - Detail screen displays all required metadata
  - UI matches design in C:/Users/Neil/AndroidStudioProjects/Anihub2/input_images/image_1.png

### Task_4_Watchlist_Adaptive: Implement the WatchlistScreen with 'Watchlist' and 'Favorites' tabs using Room for persistence and integrate adaptive layouts for multi-pane support.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Anime can be added to/removed from local watchlist
  - Watchlist/Favorites data persists
  - App layout adapts to tablets/foldables

### Task_5_Polish_Verify: Finalize the app with an adaptive icon, UI polish, and run a full verification to ensure stability and alignment with requirements.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Adaptive icon implemented
  - App does not crash
  - App alignment with user requirements confirmed
  - UI matches design in C:/Users/Neil/AndroidStudioProjects/Anihub2/input_images/image_1.png

