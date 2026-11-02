package com.example

/**
 * Highly optimized, safe JavaScript and CSS payloads injected into the loaded webpage
 * to provide features like Ad & Sponsored blocking, In-Page Video Download button,
 * Distraction-Free UI, AMOLED Dark Mode, and stripping of "Get Facebook for Android" banners.
 */
object InjectedScripts {

    /**
     * Safe CSS rule to hide Facebook Mobile App install & upsell banners
     * without breaking root containers or page layout.
     */
    val PURGE_APP_UPSELL_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-purge-upsell-style');
            if (existing) return;
            var style = document.createElement('style');
            style.id = 'webturbo-purge-upsell-style';
            style.innerHTML = `
                /* Specific Facebook upsell banners */
                #m-top-of-feed-upsell-banner,
                [data-sigil*="m-upsell-banner"],
                [data-sigil*="watch_and_scroll_upsell"],
                .m-app-banner,
                .fb-app-banner,
                .app-download-banner,
                .mobile-app-banner,
                .fixed-app-banner,
                .video-app-upsell {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0px !important;
                    max-height: 0px !important;
                    overflow: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                }
            `;
            if (document.head) {
                document.head.appendChild(style);
            } else if (document.documentElement) {
                document.documentElement.appendChild(style);
            }
        })();
    """.trimIndent()

    /**
     * Safe JavaScript observer that strictly targets only the upsell banners
     * without ever hiding page roots, articles, or feed containers.
     */
    val PURGE_APP_UPSELL_JS = """
        (function() {
            if (window._webturboUpsellPurged) return;
            window._webturboUpsellPurged = true;

            function scanAndDestroyUpsells() {
                // Strictly target known banner containers
                var bannerSelectors = [
                    '#m-top-of-feed-upsell-banner',
                    '[data-sigil*="m-upsell-banner"]',
                    '[data-sigil*="watch_and_scroll_upsell"]',
                    '.m-app-banner',
                    '.fb-app-banner',
                    '.app-download-banner',
                    '.mobile-app-banner'
                ];

                bannerSelectors.forEach(function(sel) {
                    try {
                        var elements = document.querySelectorAll(sel);
                        elements.forEach(function(el) {
                            if (el && el.id !== 'root' && el.id !== 'viewport' && el.tagName !== 'BODY') {
                                el.style.setProperty('display', 'none', 'important');
                            }
                        });
                    } catch(e) {}
                });
            }

            scanAndDestroyUpsells();

            if (window.MutationObserver && document.body) {
                var observer = new MutationObserver(function() {
                    scanAndDestroyUpsells();
                });
                observer.observe(document.body, { childList: true, subtree: true });
            }
        })();
    """.trimIndent()

    /**
     * Pure black AMOLED CSS injection for maximum contrast, eye comfort, and battery savings.
     */
    val AMOLED_DARK_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-amoled-style');
            if (existing) return;
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
            if (document.head) document.head.appendChild(style);
        })();
    """.trimIndent()

    val REMOVE_AMOLED_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-amoled-style');
            if (existing) existing.remove();
        })();
    """.trimIndent()

    /**
     * Distraction-free CSS: Hides persistent floating app install prompts
     * while preserving core navigation and feed.
     */
    val DISTRACTION_FREE_CSS = """
        (function() {
            var existing = document.getElementById('webturbo-distraction-style');
            if (existing) return;
            var style = document.createElement('style');
            style.id = 'webturbo-distraction-style';
            style.innerHTML = `
                .fixed-top, .app-download-banner, .mobile-app-banner,
                [data-sigil="m-bottom-nav"] {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0 !important;
                    opacity: 0 !important;
                }
            `;
            if (document.head) document.head.appendChild(style);
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
     * Removes ads and sponsored posts on Facebook mobile and general websites safely.
     */
    val AD_BLOCKER_JS = """
        (function() {
            if (window._webturboAdBlockerActive) return;
            window._webturboAdBlockerActive = true;

            var adSelectors = [
                '[data-ad-preview]',
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
                            if (el && el.id !== 'root' && el.id !== 'viewport' && el.tagName !== 'BODY') {
                                if (el.style.display !== 'none') {
                                    el.style.display = 'none';
                                    el.style.visibility = 'hidden';
                                    el.style.height = '0px';
                                    found++;
                                }
                            }
                        });
                    } catch(e) {}
                });

                // Check text spans for Sponsored / ممول only inside feed story cards
                try {
                    var spans = document.querySelectorAll('span, a');
                    spans.forEach(function(s) {
                        var txt = (s.innerText || '').trim();
                        if (txt === 'Sponsored' || txt === 'ممول' || txt === 'إعلان ممول') {
                            var card = s.closest('article') || s.closest('[data-sigil*="feed_story"]');
                            if (card && card.id !== 'root' && card.id !== 'viewport' && card.tagName !== 'BODY') {
                                if (card.style.display !== 'none') {
                                    card.style.display = 'none';
                                    found++;
                                }
                            }
                        }
                    });
                } catch(e) {}

                if (found > 0) {
                    blockedCount += found;
                    if (window.AndroidBridge && typeof window.AndroidBridge.onAdBlocked === 'function') {
                        window.AndroidBridge.onAdBlocked(blockedCount);
                    }
                }
            }

            scanAndPurgeAds();

            if (window.MutationObserver && document.body) {
                var observer = new MutationObserver(function() {
                    scanAndPurgeAds();
                });
                observer.observe(document.body, { childList: true, subtree: true });
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
                videos.forEach(function(video) {
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
                            background: rgba(10, 15, 20, 0.90);
                            backdrop-filter: blur(8px);
                            border: 1.5px solid #00E5FF;
                            box-shadow: 0 4px 14px rgba(0, 229, 255, 0.4);
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

                        if (window.AndroidBridge && typeof window.AndroidBridge.onVideoDetected === 'function') {
                            window.AndroidBridge.onVideoDetected(videoSrc || '', poster, title);
                        }
                    });

                    parent.appendChild(btn);
                });
            }

            injectDownloaderButtons();

            if (window.MutationObserver && document.body) {
                var dlObserver = new MutationObserver(function() {
                    injectDownloaderButtons();
                });
                dlObserver.observe(document.body, { childList: true, subtree: true });
            }
        })();
    """.trimIndent()

    /**
     * DOM Acceleration & Smooth Scrolling Payload
     */
    val ACCELERATION_JS = """
        (function() {
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

            if (document.documentElement) {
                document.documentElement.style.scrollBehavior = 'smooth';
            }
        })();
    """.trimIndent()
}
