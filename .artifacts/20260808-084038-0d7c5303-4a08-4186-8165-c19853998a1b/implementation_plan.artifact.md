# Implementation Plan - Add Anime Recommendations to Search Screen

This plan outlines the changes to add a "Recommended Anime" section to the search screen, which will be visible when the user hasn't started a search or applied any filters.

## Proposed Changes

### [Search Module]

Enhance the search experience by providing trending/recommended anime when the screen is in its initial state.

#### [SearchViewModel.kt](file:///C:/Users/Neil/AndroidStudioProjects/Anihub/app/src/main/java/com/watchlist/anihub/ui/screens/search/SearchViewModel.kt)

- Add a new `StateFlow` for recommendations: `val recommendations = _recommendations.asStateFlow()`.
- Add a method `fetchRecommendations()` that uses the `TRENDING_NOW` query from `AniListQueries`.
- Call `fetchRecommendations()` in the `init` block.

#### [SearchScreen.kt](file:///C:/Users/Neil/AndroidStudioProjects/Anihub/app/src/main/java/com/watchlist/anihub/ui/screens/search/SearchScreen.kt)

- Collect the `recommendations` state from the ViewModel.
- Modify the UI logic to show a "Recommended" section when:
    - `searchQuery` is blank.
    - `selectedGenre` is null.
    - `selectedYear` is null.
    - `selectedSeason` is null.
    - `selectedStatus` is null.
- The recommended section will consist of a title ("Recommended Anime") followed by a grid of anime cards, similar to the search results.

## Verification Plan

### Automated Tests
- No new automated tests are planned for this UI change.

### Manual Verification
1.  **Open Search Screen**: Verify that a "Recommended" section appears immediately with a list of trending anime.
2.  **Type in Search Bar**: Verify that the "Recommended" section disappears and is replaced by search results (or loading state).
3.  **Clear Search Bar**: Verify that the "Recommended" section reappears when the search bar is cleared and no filters are active.
4.  **Apply Filters**: Verify that applying a filter (e.g., genre) replaces the recommendations with filtered results.
5.  **Remove Filters**: Verify that recommendations return when all filters are cleared and search query is empty.
