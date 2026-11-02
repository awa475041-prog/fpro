package com.example

/**
 * Highly optimized JavaScript and CSS payloads injected into the loaded webpage
 * to provide features like Ad & Sponsored blocking, In-Page Video Download button,
 * Distraction-Free UI, AMOLED Dark Mode, and stripping of "Get Facebook for Android" banners.
 */
object InjectedScripts {

    /**
     * Mandatory CSS rule injected into EVERY page load to permanently kill all
     * "Get Facebook for Android" (احصل على فيسبوك لهاتف أندرويد) banners,
     * "Open in App" buttons, and video app-switch overlays.
     */
    val PURGE_APP_UPSELL_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-purge-upsell-style');
            if (existing) return;
            var style = document.createElement('style');
            style.id = 'webturbo-purge-upsell-style';
            style.innerHTML = `
                /* Facebook Mobile App install & upsell banners */
                #m-top-of-feed-upsell-banner,
                [data-sigil*="m-upsell-banner"],
                [data-sigil*="app-banner"],
                [data-sigil*="upsell"],
                [data-sigil*="install-app"],
                [data-sigil*="mobile_app_banner"],
                [data-sigil*="watch_and_scroll_upsell"],
                div[id*="upsell"],
                div[class*="upsell"],
                div[class*="app_banner"],
                div[class*="native_banner"],
                .m-app-banner,
                .fb-app-banner,
                .app-download-banner,
                .mobile-app-banner,
                .fixed-app-banner,
                .video-app-upsell,
                div[class*="video_upsell"],
                div[class*="watch_upsell"],
                /* Play Store and App Links */
                a[href*="play.google.com/store/apps/details?id=com.facebook"],
                a[href*="market://details?id=com.facebook"],
                a[href*="/mobile/download/"],
                a[href*="/get_app/"],
                a[href*="intent://"],
                div:has(a[href*="com.facebook.katana"]),
                div:has(a[href*="play.google.com/store/apps/details?id=com.facebook"]),
                div:has(a[href*="market://"]) {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0px !important;
                    max-height: 0px !important;
                    overflow: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                }
                body {
                    padding-top: 0 !important;
                    margin-top: 0 !important;
                }
            `;
            (document.head || document.documentElement).appendChild(style);
        })();
    """.trimIndent()

    /**
     * JavaScript observer that aggressively scans for and purges any
     * "احصل على فيسبوك لهاتف أندرويد" / "فتح التطبيق" / "Get Facebook" banner
     * across the entire DOM and inside videos.
     */
    val PURGE_APP_UPSELL_JS = """
        (function() {
            if (window._webturboUpsellPurged) return;
            window._webturboUpsellPurged = true;

            var bannedPhrases = [
                'احصل على فيسبوك لهاتف',
                'احصل على فيسبوك',
                'فتح التطبيق',
                'استخدام التطبيق',
                'تنزيل التطبيق',
                'شاهد في التطبيق',
                'فتح في التطبيق',
                'Get Facebook for Android',
                'Get Facebook',
                'Open in App',
                'Open the App',
                'Use App',
                'Watch in App',
                'Continue in App',
                'Install App'
            ];

            function scanAndDestroyUpsells() {
                // Remove elements containing banned phrases
                var candidates = document.querySelectorAll('div, a, span, p, header, section');
                for (var i = 0; i < candidates.length; i++) {
                    var el = candidates[i];
                    var text = (el.innerText || '').trim();
                    if (!text || text.length > 120) continue;

                    for (var j = 0; j < bannedPhrases.length; j++) {
                        if (text.indexOf(bannedPhrases[j]) !== -1) {
                            var target = el.closest('[data-sigil*="upsell"]') || 
                                         el.closest('[data-sigil*="banner"]') || 
                                         el.closest('#m-top-of-feed-upsell-banner') || 
                                         el.closest('.m-app-banner') || 
                                         el.closest('div[class*="upsell"]') || 
                                         el;
                            if (target && target.style.display !== 'none') {
                                target.style.setProperty('display', 'none', 'important');
                                target.style.setProperty('visibility', 'hidden', 'important');
                                target.style.setProperty('height', '0px', 'important');
                            }
                            break;
                        }
                    }
                }
            }

            scanAndDestroyUpsells();

            var upsellObserver = new MutationObserver(function() {
                scanAndDestroyUpsells();
            });

            if (document.body) {
                upsellObserver.observe(document.body, { childList: true, subtree: true });
            } else {
                document.addEventListener('DOMContentLoaded', function() {
                    upsellObserver.observe(document.body, { childList: true, subtree: true });
                });
            }
        })();
    """.trimIndent()

    /**
     * Pure black AMOLED CSS injection for maximum contrast, eye comfort, and battery savings.
     */
    val AMOLED_DARK_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-amoled-style');
            if (existing) { existing.remove(); return; }
            var style = document.createElement('style');
            style.id = 'webturbo-amoled-style';
            style.innerHTML = `
                html, body {
                    background-color: #000000 !important;
                    color: #E2E8F0 !important;
                }
                div, article, section, header, nav, main, aside, footer {
                    background-color: #000000 !important;
                    color: #E2E8F0 !important;
                    border-color: #1E293B !important;
                }
                a { color: #38BDF8 !important; }
                input, textarea, select {
                    background-color: #0F172A !important;
                    color: #F8FAFC !important;
                    border: 1px solid #334155 !important;
                }
                img, video { opacity: 0.95; }
            `;
            (document.head || document.documentElement).appendChild(style);
        })();
    """.trimIndent()

    val REMOVE_AMOLED_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-amoled-style');
            if (existing) existing.remove();
        })();
    """.trimIndent()

    /**
     * Distraction-free CSS: Hides persistent top navigation headers, bottom app install prompts,
     * and promotional floating bars for full immersive viewing.
     */
    val DISTRACTION_FREE_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-distraction-style');
            if (existing) { existing.remove(); return; }
            var style = document.createElement('style');
            style.id = 'webturbo-distraction-style';
            style.innerHTML = `
                header, [role="banner"], .m-header, #header,
                [data-sigil="m-header"], [data-sigil="m-bottom-nav"],
                .fixed-top, .app-download-banner, .mobile-app-banner {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0 !important;
                    opacity: 0 !important;
                }
                body { padding-top: 0 !important; margin-top: 0 !important; }
            `;
            (document.head || document.documentElement).appendChild(style);
        })();
    """.trimIndent()

    val REMOVE_DISTRACTION_FREE_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-distraction-style');
            if (existing) existing.remove();
        })();
    """.trimIndent()

    /**
     * Smart Ad & Sponsored Content Blocker:
     * Removes ads and sponsored posts on Facebook mobile and general websites
     * using real-time DOM selectors and MutationObserver.
     */
    val AD_BLOCKER_JS = """
        (function() {
            if (window._webturboAdBlockerActive) return;
            window._webturboAdBlockerActive = true;

            var adSelectors = [
                '[data-ad-preview]',
                '[aria-label*="Sponsored"]',
                '[aria-label*="ممول"]',
                '[aria-label*="إعلان"]',
                'div[data-sigil*="feed_story"]:has(a[href*="/ads/"])',
                'div[data-sigil*="feed_story"]:has([aria-label*="Sponsored"])',
                'div[data-sigil*="feed_story"]:has([aria-label*="ممول"])',
                'div[data-sigil*="feed_story"]:has([aria-label*="إعلان"])',
                '.sponsored-post',
                '.advertisement',
                '.ad-banner',
                '.adsbygoogle',
                'iframe[src*="doubleclick"]',
                'iframe[src*="adservice"]'
            ];

            var blockedCount = 0;

            function scanAndPurgeAds() {
                var found = 0;
                adSelectors.forEach(function(sel) {
                    try {
                        var elements = document.querySelectorAll(sel);
                        elements.forEach(function(el) {
                            if (el.style.display !== 'none') {
                                el.style.display = 'none';
                                el.style.visibility = 'hidden';
                                el.style.height = '0px';
                                el.classList.add('webturbo-purged-ad');
                                found++;
                            }
                        });
                    } catch(e) {}
                });

                // Deep check for Facebook Arabic & English "Sponsored" / "ممول" text spans
                var allSpans = document.querySelectorAll('span, a');
                allSpans.forEach(function(s) {
                    var txt = (s.innerText || '').trim();
                    if (txt === 'Sponsored' || txt === 'ممول' || txt === 'إعلان ممول' || txt === 'Ad') {
                        var card = s.closest('article') || s.closest('[data-sigil*="feed_story"]') || s.closest('[role="article"]') || s.closest('.storyStream');
                        if (card && card.style.display !== 'none') {
                            card.style.display = 'none';
                            card.classList.add('webturbo-purged-ad');
                            found++;
                        }
                    }
                });

                if (found > 0) {
                    blockedCount += found;
                    if (window.AndroidBridge && typeof window.AndroidBridge.onAdBlocked === 'function') {
                        window.AndroidBridge.onAdBlocked(blockedCount);
                    }
                }
            }

            scanAndPurgeAds();

            // Observe dynamic infinite scroll items
            var observer = new MutationObserver(function() {
                scanAndPurgeAds();
            });

            if (document.body) {
                observer.observe(document.body, { childList: true, subtree: true });
            } else {
                document.addEventListener('DOMContentLoaded', function() {
                    observer.observe(document.body, { childList: true, subtree: true });
                });
            }
        })();
    """.trimIndent()

    /**
     * In-Page Floating Video Downloader:
     * Detects <video> tags, extracts stream sources, and injects a stylish,
     * native-like download button directly over the video / action bar.
     */
    val VIDEO_DOWNLOADER_JS = """
        (function() {
            if (window._webturboDownloaderActive) return;
            window._webturboDownloaderActive = true;

            function injectDownloaderButtons() {
                var videos = document.querySelectorAll('video');
                videos.forEach(function(video, index) {
                    if (video.dataset.webturboAttached) return;
                    video.dataset.webturboAttached = "true";

                    var parent = video.parentElement;
                    if (!parent) return;

                    var computed = window.getComputedStyle(parent);
                    if (computed.position === 'static') {
                        parent.style.position = 'relative';
                    }

                    // Create Download Pill Button
                    var btn = document.createElement('div');
                    btn.className = 'webturbo-dl-btn';
                    btn.innerHTML = `
                        <div style="
                            position: absolute;
                            top: 12px;
                            right: 12px;
                            z-index: 999999;
                            background: rgba(10, 15, 20, 0.88);
                            backdrop-filter: blur(8px);
                            border: 1px solid #00E5FF;
                            box-shadow: 0 4px 14px rgba(0, 229, 255, 0.35);
                            color: #FFFFFF;
                            padding: 6px 14px;
                            border-radius: 20px;
                            font-size: 13px;
                            font-weight: 700;
                            display: flex;
                            align-items: center;
                            gap: 6px;
                            cursor: pointer;
                            user-select: none;
                            transition: transform 0.15s ease, background 0.15s ease;
                        ">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#00E5FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                <polyline points="7 10 12 15 17 10"></polyline>
                                <line x1="12" y1="15" x2="12" y2="3"></line>
                            </svg>
                            <span style="letter-spacing: 0.3px;">تنزيل HD</span>
                        </div>
                    `;

                    btn.addEventListener('click', function(e) {
                        e.stopPropagation();
                        e.preventDefault();
                        
                        var videoSrc = video.currentSrc || video.src;
                        if (!videoSrc) {
                            var srcNode = video.querySelector('source');
                            if (srcNode) videoSrc = srcNode.src;
                        }

                        var poster = video.poster || '';
                        var title = document.title || 'Web Video';

                        // Notify native Android bridge
                        if (window.AndroidBridge && typeof window.AndroidBridge.onVideoDetected === 'function') {
                            window.AndroidBridge.onVideoDetected(videoSrc || '', poster, title);
                        }
                    });

                    parent.appendChild(btn);
                });
            }

            injectDownloaderButtons();

            var dlObserver = new MutationObserver(function() {
                injectDownloaderButtons();
            });

            if (document.body) {
                dlObserver.observe(document.body, { childList: true, subtree: true });
            }
        })();
    """.trimIndent()

    /**
     * DOM Acceleration & Smooth Scrolling Payload:
     * Disables heavy CSS transitions during rapid scroll, optimizes touch gestures,
     * and measures page performance timing.
     */
    val ACCELERATION_JS = """
        (function() {
            // Report performance timing back to native app
            window.addEventListener('load', function() {
                setTimeout(function() {
                    try {
                        var perf = window.performance.timing;
                        var loadTime = perf.loadEventEnd - perf.navigationStart;
                        if (loadTime > 0 && window.AndroidBridge && typeof window.AndroidBridge.onPageSpeed === 'function') {
                            window.AndroidBridge.onPageSpeed(loadTime);
                        }
                    } catch(e) {}
                }, 300);
            });

            // Smooth hardware scrolling hint
            if (document.documentElement) {
                document.documentElement.style.scrollBehavior = 'smooth';
                document.documentElement.style.webkitOverflowScrolling = 'touch';
            }
        })();
    """.trimIndent()
}
