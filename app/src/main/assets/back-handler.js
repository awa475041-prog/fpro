// ===== معالج ضغطة الرجوع — منقول من جولد بوك (GoldBook) بتصريح المالك =====
// يعرّف window.backHandlerNB التي يستدعيها BackHandler في MainActivity.
// القيم المعادة:
//   "exit"      → نحن في الخلاصة وهي في الأعلى تماماً: خروج دافئ
//   "scrolling" → الخلاصة كانت منزلقة: تمرير سلس للأعلى، والضغطة الثانية تخرج
//   "true"      → الصفحة تعاملت مع الضغط بنفسها (قائمة/حوارية عبر Escape)
//   "false"     → لا رأي للصفحة: القرار يرجع لسجل المحرك (nativeBack)
(() => {
    'use strict';
    // نفس تعريفات site-engine.js حتى يبقى مفهوم «الخلاصة» متطابقاً في التطبيق كله
    const isDesktopMode = () => document.querySelector('html[id="facebook"]') !== null;
    const isFeed = () => location.pathname === '/' &&
        ['m.facebook.com', 'www.facebook.com'].includes(location.hostname) &&
        (isDesktopMode() || Array.from(document.querySelectorAll('[role="button"] span')).some(s => s.textContent === '󱥆'));

    window.backHandlerNB = () => {
        const dialogs = document.querySelectorAll('div[role="dialog"]');
        const isMenu = document.querySelector('div[role="menu"]');

        function scrollToTop() {
            if (window.scrollY !== 0) {
                // لقطع أي حدث تمرير جارٍ قبل التمرير السلس للأعلى
                document.body.style.overflow = 'hidden';
                setTimeout(() => {
                    document.body.style.overflow = '';
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                }, 30);
                return "scrolling";
            } else return "exit";
        }

        if (isDesktopMode()) {
            if (isFeed() && !isMenu && dialogs.length === 1)
                return scrollToTop();
            else if (isMenu || dialogs.length > 1) {
                const escapeEvent = new KeyboardEvent('keydown', {
                    key: 'Escape',
                    code: 'Escape',
                    keyCode: 27,
                    which: 27,
                    bubbles: true,
                    cancelable: true
                });
                window.dispatchEvent(escapeEvent);
                return "true";
            } else return "false";
        } else if (isFeed() && !isMenu && !dialogs.length) {
            return scrollToTop();
        } else return "false";
    };
})();
