# WebTurbo Pro: Accelerated Web-to-APK Engine with Feature Injections

Transform any web application into a lightning-fast, native Android app that outperforms standard mobile browsers. Features real-time script & stylesheet injections, automated sponsored ad removal, native in-page video download hooks, distraction-free immersion, and instant state management conforming to official Android documentation.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your preferences:
> - **Primary Default Website**: Facebook Mobile Web (`https://m.facebook.com`) with the ability to switch or configure custom URLs.
> - **Injected Supercharged Tools**:
>   1. **Floating In-Page Video Downloader**: Detects video tags (`<video>`) and injects an aligned, native-feeling download button directly above or beside action icons (like/share) with instant video stream extraction.
>   2. **Smart Ad & Sponsored Content Blocker**: Continuously eliminates sponsored posts, promotional banners, and tracking clutter using dynamic CSS injection and DOM `MutationObserver`.
>   3. **Distraction-Free Mode**: Automatically collapses cluttered headers, floating banners, and navigation bars for full-screen reading immersion.
>   4. **Deep AMOLED Dark Mode Injector**: Injects custom high-contrast dark styles optimized for OLED battery savings and eye comfort.
> - **Speed & Cache Architecture**: High-speed native cache with DOM acceleration, hardware layer rendering, disk caching, and official Android WebView lifecycle state preservation (`saveState` / `restoreState`).

---

### 1. Overview & Core Concept

**WebTurbo Pro** provides a native Android shell around web applications that is faster, cleaner, and more feature-rich than mobile Chrome or Safari. By embedding a tuned native `WebView` powered by Jetpack Compose, the app bypasses conventional browser overhead, pre-caches resources, enables hardware GPU acceleration, and injects custom client-side enhancements via a secure bidirectional JavaScript bridge (`@JavascriptInterface`).

- **Target Audience**: Users seeking an ad-free, data-light, ultra-responsive social media and web browsing experience with native download tools built directly into the web interface.
- **Key Value**: Instantaneous page rendering, zero intrusive ads or sponsored distractions, one-tap video extraction, and live customization accessible from a sleek Material 3 top control panel (⚙️).

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Cold Launch & Instant Restore**: App launches directly into `m.facebook.com` with hardware-accelerated rendering. If previously open, it instantly restores scroll position and browsing session via saved state.
2. **Browsing with Injected Superpowers**:
   - Sponsored posts and ad containers vanish smoothly without page jumps.
   - Videos in feeds or reels automatically display an integrated, stylish **Download** pill button right above the interaction bar.
   - Clicking the download button opens a native Android bottom sheet showing video resolution, direct URL, and an option to copy or trigger a native download via Android `DownloadManager`.
3. **Live Injections Settings (⚙️ Top Bar)**:
   - Tapping the gear icon opens a fluent Material 3 Modal Bottom Sheet containing live toggle switches:
     - ⚡ *Hardware & DOM Acceleration*
     - 🛡️ *Smart Ad & Sponsored Blocker*
     - 📥 *In-Page Video Downloader*
     - 🌙 *AMOLED Pure Dark Theme*
     - 📱 *Distraction-Free Minimalist UI*
     - 🔗 *Target Web URL (Customizable)*
     - 📊 *Performance Telemetry & Diagnostics* (Page load time in ms, blocked ads counter, cache size)
   - Toggling any feature immediately updates the web view in real time without refreshing or losing user state.
4. **Intuitive Navigation & Edge-to-Edge**:
   - Modern top control bar with SSL security badge, refresh button, download manager shortcut, and settings gear.
   - Pull-to-refresh with smooth M3 loading indicator.
   - Seamless predictive back handling: presses navigate back in web history if available, otherwise gracefully background the app.

#### Visual Identity & Theme
- **Theme Direction**: High-tech, sleek "Speed & Precision" aesthetic with deep charcoal/midnight surfaces (`#0F141C`), vibrant electric cyan accents (`#00E5FF`), and sapphire blue indicators (`#2979FF`).
- **Typography**: Clean, geometric sans-serif styling with prominent hierarchy for performance metrics and modal options.
- **Micro-Interactions**: Spring physics on modal reveal, ripple effects on all buttons (minimum 48dp touch targets), and animated badges indicating blocked ads count.

---

### 3. Key Product Decisions & Trade-Offs

1. **Native Jetpack Compose + Android WebView vs. Third-Party Flutter Wrappers**:
   - *Decision*: Native Kotlin + Android Jetpack Compose integrating `AndroidView(factory = { ... })` and `WebSettings`.
   - *Rationale*: Native Android provides direct zero-overhead access to the OS rendering pipeline, hardware acceleration (`LAYER_TYPE_HARDWARE`), native `DownloadManager`, and official memory/state lifecycle methods (`saveState`/`restoreState`) referenced in official Android developer guides, offering noticeably lower latency and memory footprint than Flutter runtimes.
2. **MutationObserver + Direct DOM CSS Injection vs. Network Request Interception**:
   - *Decision*: Hybrid approach combining declarative CSS rules with a fast JavaScript `MutationObserver`.
   - *Rationale*: Social media feeds load dynamically via infinite scroll (virtualized DOM). A `MutationObserver` catches new sponsored posts and video elements immediately as they enter the DOM without causing layout reflow delays.
3. **Bridge Communication (`@JavascriptInterface`)**:
   - *Decision*: A dedicated `WebTurboBridge` class annotated with `@JavascriptInterface` communicating on a background thread with Compose State via Kotlin Coroutines.
   - *Rationale*: Keeps the UI thread completely unblocked while passing video metadata (stream URL, poster, title) to the native Android UI layer for downloading.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├────────────────────────────────┬───────────────────────────────────────┤
│ TopAppBar:                     │ In-App Control Center (Bottom Sheet): │
│ • SSL Badge & Host URL         │ • Ad/Sponsored Blocker Toggle         │
│ • Reload / Stop Button         │ • Video Downloader Toggle             │
│ • Downloads Tray Button        │ • AMOLED Dark Theme Toggle            │
│ • Settings Gear Button (⚙️)   │ • Distraction-Free Header/Footer      │
│                                │ • URL Preset / Custom Address         │
├────────────────────────────────┴───────────────────────────────────────┤
│                Accelerated AndroidView(WebView) Container              │
│  - WebSettings (DOM Storage, CacheMode, Hardware Acceleration)         │
│  - WebChromeClient (Progress indicator, Console logging, Geolocation)  │
│  - WebViewClient (URL overrides, Resource caching, Page finish hooks)  │
│  - AndroidBridge (@JavascriptInterface)                                │
└──────────────────┬─────────────────────────────────┬───────────────────┘
                   │                                 │
                   ▼                                 ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│       JavaScript & CSS Injection     │  │   Native Android Services    │
│  - AdBlocker.js (MutationObserver)   │  │  - DownloadManager           │
│  - VideoDownloaderHook.js            │  │  - State Management          │
│  - AmoledDark.css                    │  │    (saveState/restoreState)  │
│  - DistractionFree.css               │  │  - SharedPreferences Config │
└──────────────────────────────────────┘  └──────────────────────────────┘
```

#### Core Components & Responsibilities
- **`MainActivity.kt`**: Main container with `enableEdgeToEdge()`, custom TopAppBar, pull-to-refresh swipe box, and the embedded `WebView`.
- **`WebTurboViewModel.kt`**: Holds reactive UI state:
  - Settings: `isAdBlockerEnabled`, `isVideoDownloaderEnabled`, `isDarkModeEnabled`, `isDistractionFreeEnabled`, `currentUrl`.
  - Telemetry: `blockedAdsCount`, `pageLoadTimeMs`, `isLoading`, `currentTitle`.
  - Detected media list for native download.
- **`InjectedScripts.kt`**: Modular JavaScript and CSS bundles:
  - *Ad & Sponsored Filter*: Targets selectors for sponsored stories, suggested posts, and marketing cards.
  - *Video Downloader Hook*: Detects `<video>` elements, extracts source/blob/stream URLs, and attaches a floating native-styled download icon.
  - *AMOLED Dark Theme*: High-contrast dark styling for web pages.
  - *Distraction-Free Mode*: Hides site banners, headers, and footers for maximum viewport real estate.
- **`WebTurboBridge.kt`**: JavaScript interface exposing:
  - `onVideoDetected(videoUrl, title)`: Notifies native Compose state when a video is found.
  - `onAdBlocked(count)`: Increments blocked ads telemetry.
  - `onLog(message)`: Safe web debugging.
- **`SettingsBottomSheet.kt`**: Clean, accessible Material 3 bottom sheet for controlling all injected features on the fly.
- **`VideoDownloadDialog.kt`**: Interactive preview dialog when downloading a detected video.

#### Testing & Verification Plan
- **Compilation Check**: Run `compile_applet` to ensure zero compilation or syntax errors.
- **Lifecycle & State Restoration**: Test configuration changes and back press behavior.
- **Edge-to-Edge & Accessibility**: Ensure system bar insets and 48dp touch targets are respected.
