/* FPRO — ذاكرة مواضيع بإزاحة دقيقة، لإعادة فتح التطبيق فقط.
   نقاء جولد بوك في الرجوع: لا تحييد لـ scrollRestoration، ولا مستمعات
   popstate/pageshow، ولا تمركز للتدوينة المفتوحة. استرجاع كروم الأصلي
   لكل مدخل سجل هو من يملك كل عملية رجوع — دون أي تدخل منا. */
(function(){
 'use strict';
 if(window.__fproPosition) return;

 const token = Date.now().toString(36) + Math.random().toString(36).slice(2);
 const limit = 2000000;
 let cancelRestore = null;
 const clamp = n => Math.max(0, Math.min(limit, Number(n) || 0));

 function path(el){
   const parts = [];
   for(let i = 0; el && el !== document.body && i < 8; i++, el = el.parentElement){
     if(!el.parentElement) return null;
     parts.unshift(el.tagName.toLowerCase() + ':nth-child(' + (Array.prototype.indexOf.call(el.parentElement.children, el) + 1) + ')');
   }
   return el === document.body ? 'body > ' + parts.join(' > ') : null;
 }

 function scrollers(){
   return Array.from(document.querySelectorAll('main,section,div,[role="feed"]'))
     .slice(0, 800)
     .filter(el => el.clientHeight > 100 && el.scrollHeight > el.clientHeight + 100 && /auto|scroll/.test(getComputedStyle(el).overflowY));
 }

 function capture(){
   if(document.querySelector('input[type="password"]')) return JSON.stringify({v:1, login:true});
   // الإزاحة الحية الحقيقية فقط — لا قيمة قديمة من آخر نقرة.
   return JSON.stringify({
     v: 1,
     url: location.href,
     token,
     x: clamp(scrollX),
     y: clamp(window.scrollY || document.documentElement.scrollTop || 0),
     containers: scrollers().filter(el => el.scrollTop > 0 || el.scrollLeft > 0).slice(0, 4).map(el => ({
       path: path(el),
       x: clamp(el.scrollLeft),
       y: clamp(el.scrollTop)
     })).filter(c => c.path)
   });
 }

 function restore(raw){
   if(cancelRestore) cancelRestore();
   let s;
   try { s = typeof raw === 'string' ? JSON.parse(raw) : raw; } catch(_) { return false; }
   if(!s || s.v !== 1 || s.url !== location.href || document.querySelector('input[type="password"]')) return false;

   const targetY = clamp(s.y);
   const targetX = clamp(s.x);

   let stopped = false, timer = null, observer = null, settled = 0;
   function stop(){
     if(stopped) return;
     stopped = true;
     clearTimeout(timer);
     if(observer) observer.disconnect();
     ['touchstart', 'pointerdown', 'wheel', 'keydown'].forEach(e => window.removeEventListener(e, stop, true));
     if(cancelRestore === stop) cancelRestore = null;
   }

   ['touchstart', 'pointerdown', 'wheel', 'keydown'].forEach(e => window.addEventListener(e, stop, {capture: true, passive: true}));
   cancelRestore = stop;

   const start = Date.now();
   let queued = false;

   function tick(){
     queued = false;
     if(stopped) return;
     if(location.href !== s.url || Date.now() - start > 12000){ stop(); return; }
     // إزاحة النافذة الدقيقة فقط — انتظر حتى يستقر المحتوى الكسول.
     window.scrollTo(targetX, targetY);
     if(Math.abs(scrollY - targetY) < 10) settled++;
     else settled = 0;
     if(settled >= 3) { stop(); return; }

     timer = setTimeout(tick, 100);
   }

   observer = new MutationObserver(() => {
     if(!queued && !stopped){
       queued = true;
       clearTimeout(timer);
       timer = setTimeout(tick, 40);
     }
   });

   observer.observe(document.documentElement, {childList: true, subtree: true});
   tick();
   return true;
 }

 window.__fproPosition = {
   capture,
   restore,
   cancel: () => { if(cancelRestore) cancelRestore(); }
 };
})();
