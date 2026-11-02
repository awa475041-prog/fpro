/* FPRO — Intelligent Post-Level Scroll Restoration & Memory */
(function(){
 'use strict';
 if(window.__fproPosition) return;
 
 if ('scrollRestoration' in history) {
   history.scrollRestoration = 'manual';
 }

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

 function getPostKey(el) {
   if (!el) return null;
   // 1. Check for specific post permalink or video link
   for (const a of el.querySelectorAll('a[href]')) {
     try {
       const u = new URL(a.href, location.href);
       if (/\/(?:posts|permalink|reel|videos)\/[^/]+/.test(u.pathname)) return u.pathname;
       if (/\/(?:story|permalink)\.php$/.test(u.pathname)) {
         const id = u.searchParams.get('id') || u.searchParams.get('story_fbid') || u.searchParams.get('fbid');
         if (id) return id;
       }
     } catch (_) {}
   }
   // 2. Check data-ft or id or unique testid
   if (el.dataset && el.dataset.ft) return el.dataset.ft.slice(0, 60);
   if (el.id && el.id.length > 3) return el.id;
   // 3. Fallback to text fingerprint
   const text = (el.innerText || '').trim().replace(/\s+/g, ' ').slice(0, 50);
   if (text.length > 5) return text;
   return null;
 }

 // Exact Clicked Post Tracking
 let clickedPostElement = null;
 let clickedPostKey = null;
 let clickedScrollY = 0;

 function isFeedPath(){
   const p = location.pathname;
   return p === '/' || p === '' || p.indexOf('/home') !== -1 || p.indexOf('/watch') !== -1;
 }

 document.addEventListener('click', function(e) {
   const post = e.target.closest('[role="article"], article, [data-pagelet^="FeedUnit"], div[data-ft]');
   if (post) {
     const prev = document.querySelector('[data-fpro-clicked-post]');
     if (prev) prev.removeAttribute('data-fpro-clicked-post');

     post.setAttribute('data-fpro-clicked-post', 'true');
     clickedPostElement = post;
     clickedPostKey = getPostKey(post);
     clickedScrollY = window.scrollY || document.documentElement.scrollTop || 0;

     try {
       sessionStorage.setItem('fpro_target_key', clickedPostKey || '');
       sessionStorage.setItem('fpro_target_y', clickedScrollY.toString());
     } catch (_) {}
   }
 }, { capture: true, passive: true });

 function restoreClickedPost() {
   let target = document.querySelector('[data-fpro-clicked-post]');
   let savedKey = clickedPostKey;
   let savedY = clickedScrollY;

   if (!savedKey) {
     try { savedKey = sessionStorage.getItem('fpro_target_key'); } catch (_) {}
   }
   if (!savedY) {
     try { savedY = parseInt(sessionStorage.getItem('fpro_target_y') || '0', 10); } catch (_) {}
   }

   if (!target && savedKey) {
     for (const el of document.querySelectorAll('[role="article"], article, [data-pagelet^="FeedUnit"], div[data-ft]')) {
       if (getPostKey(el) === savedKey) {
         target = el;
         break;
       }
     }
   }

   if (target) {
     target.scrollIntoView({ behavior: 'auto', block: 'center' });
     return true;
   } else if (savedY > 0) {
     window.scrollTo(0, savedY);
     return true;
   }
   return false;
 }

 window.__fproRestoreClickedPost = restoreClickedPost;

 function capture(){
   if(document.querySelector('input[type="password"]')) return JSON.stringify({v:1, login:true});
   const curY = clickedScrollY > 0 ? clickedScrollY : (window.scrollY || 0);
   return JSON.stringify({
     v: 1,
     url: location.href,
     token,
     x: clamp(scrollX),
     y: clamp(curY),
     postKey: clickedPostKey,
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

   // First try restoring directly to the clicked post
   if (restoreClickedPost()) {
     return true;
   }

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

     // Check if the targeted post has rendered now
     if (restoreClickedPost()) {
       settled++;
       if (settled >= 2) { stop(); return; }
     } else {
       // Restore exact window scroll
       window.scrollTo(targetX, targetY);
       if(Math.abs(scrollY - targetY) < 10) settled++;
       else settled = 0;
       if(settled >= 3) { stop(); return; }
     }

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

 window.addEventListener('popstate', function(){
   setTimeout(restoreClickedPost, 20);
   setTimeout(restoreClickedPost, 120);
 });

 window.addEventListener('pageshow', function(){
   setTimeout(restoreClickedPost, 20);
   setTimeout(restoreClickedPost, 120);
 });

 window.__fproPosition = {
   capture,
   restore,
   cancel: () => { if(cancelRestore) cancelRestore(); }
 };
})();
