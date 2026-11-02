/* Private position checkpoint; no post text, cookies, form values or media URLs. */
(function(){
 'use strict';
 if(window.__fproPosition)return;
 
 if ('scrollRestoration' in history) {
   history.scrollRestoration = 'manual';
 }

 const token=Date.now().toString(36)+Math.random().toString(36).slice(2),limit=2000000;
 let cancelRestore=null;
 const clamp=n=>Math.max(0,Math.min(limit,Number(n)||0));

 function path(el){
   const parts=[];
   for(let i=0;el&&el!==document.body&&i<8;i++,el=el.parentElement){
     if(!el.parentElement)return null;
     parts.unshift(el.tagName.toLowerCase()+':nth-child('+(Array.prototype.indexOf.call(el.parentElement.children,el)+1)+')');
   }
   return el===document.body?'body > '+parts.join(' > '):null;
 }

 function scrollers(){
   return Array.from(document.querySelectorAll('main,section,div,[role="feed"]'))
     .slice(0,800)
     .filter(el=>el.clientHeight>100&&el.scrollHeight>el.clientHeight+100&&/auto|scroll/.test(getComputedStyle(el).overflowY));
 }

 function permalink(article){
   for(const a of Array.from(article.querySelectorAll('a[href]')).slice(0,60)){
     try{
       const u=new URL(a.href,location.href);
       if(!/(^|\.)facebook\.com$/.test(u.hostname))continue;
       if(/\/(?:posts|permalink|reel|videos)\/[^/]+/.test(u.pathname))return u.pathname.replace(/\/$/,'');
       if(/\/(?:story|permalink)\.php$/.test(u.pathname)){
         const q=new URLSearchParams();
         ['id','story_fbid','fbid','v'].forEach(k=>{if(u.searchParams.has(k))q.set(k,u.searchParams.get(k));});
         if(q.toString())return u.pathname+'?'+q.toString();
       }
     }catch(_){}
   }
   return null;
 }

 function anchor(){
   for(const el of Array.from(document.querySelectorAll('article,[role="article"]')).slice(0,100)){
     const r=el.getBoundingClientRect();
     if(r.bottom>60&&r.top<innerHeight*.7){
       const key=permalink(el);
       if(key)return {key,top:Math.round(r.top)};
     }
   }
   return null;
 }

 // Continuously track feed scroll coordinates
 let lastFeedScrollY = 0;
 function isFeedPath(){
   const p = location.pathname;
   return p === '/' || p === '' || p.indexOf('/home') !== -1;
 }

 function updateLastFeedScroll(){
   if(isFeedPath()){
     const y = window.scrollY || document.documentElement.scrollTop || 0;
     if(y > 0) {
       lastFeedScrollY = y;
       try { sessionStorage.setItem('fpro_exact_y', y.toString()); } catch(_){}
     }
   }
 }

 window.addEventListener('scroll', updateLastFeedScroll, {passive: true});
 document.addEventListener('click', function(e){
   if(isFeedPath()) updateLastFeedScroll();
 }, {capture: true, passive: true});

 function capture(){
   if(document.querySelector('input[type="password"]'))return JSON.stringify({v:1,login:true});
   const curY = isFeedPath() && lastFeedScrollY > 0 ? lastFeedScrollY : (window.scrollY || 0);
   return JSON.stringify({
     v:1,
     url:location.href,
     token,
     x:clamp(scrollX),
     y:clamp(curY),
     containers:scrollers().filter(el=>el.scrollTop>0||el.scrollLeft>0).slice(0,4).map(el=>({path:path(el),x:clamp(el.scrollLeft),y:clamp(el.scrollTop)})).filter(c=>c.path),
     anchor:anchor()
   });
 }

 function restore(raw){
  if(cancelRestore)cancelRestore();
  let s;try{s=typeof raw==='string'?JSON.parse(raw):raw;}catch(_){return false;}
  if(!s||s.v!==1||s.url!==location.href||document.querySelector('input[type="password"]'))return false;

  const targetY = clamp(s.y);
  const targetX = clamp(s.x);

  // A warm return must not disturb a live document that kept its exact position.
  if(s.token===token&&Math.abs(scrollX-targetX)<5&&Math.abs(scrollY-targetY)<5)return true;

  let stopped=false,timer=null,observer=null,settled=0;
  function stop(){
    if(stopped)return;
    stopped=true;
    clearTimeout(timer);
    if(observer)observer.disconnect();
    ['touchstart','pointerdown','wheel','keydown'].forEach(e=>window.removeEventListener(e,stop,true));
    if(cancelRestore===stop)cancelRestore=null;
  }

  ['touchstart','pointerdown','wheel','keydown'].forEach(e=>window.addEventListener(e,stop,{capture:true,passive:true}));
  cancelRestore=stop;

  const start=Date.now();
  let queued=false;

  function tick(){
   queued=false;
   if(stopped)return;
   if(location.href!==s.url||Date.now()-start>15000){stop();return;}

   // Restore inner containers
   const targets=(s.containers||[]).slice(0,4);
   let ok=true;
   for(const c of targets){
     try{
       const el=document.querySelector(c.path);
       if(el){
         el.scrollLeft=clamp(c.x);
         el.scrollTop=clamp(c.y);
         if(Math.abs(el.scrollTop-clamp(c.y))>5)ok=false;
       }else ok=false;
     }catch(_){ok=false;}
   }

   // Restore window position directly to exact coordinate
   window.scrollTo(targetX, targetY);
   if(Math.abs(scrollY-targetY)>5)ok=false;

   if(ok){
     settled++;
     if(settled>=3){stop();return;}
   } else {
     settled=0;
   }
   timer=setTimeout(tick, 150);
  }

  observer=new MutationObserver(()=>{
    if(!queued&&!stopped){
      queued=true;
      clearTimeout(timer);
      timer=setTimeout(tick, 50);
    }
  });

  observer.observe(document.documentElement,{childList:true,subtree:true});
  tick();
  return true;
 }

 window.addEventListener('popstate', function(){
   setTimeout(function(){
     if(isFeedPath()){
       let saved = lastFeedScrollY;
       if(!saved){
         try{ saved = parseInt(sessionStorage.getItem('fpro_exact_y')||'0', 10); }catch(_){}
       }
       if(saved > 0){
         window.scrollTo(0, saved);
         setTimeout(function(){ window.scrollTo(0, saved); }, 100);
       }
     }
   }, 20);
 });

 window.__fproPosition={capture,restore,cancel:()=>{if(cancelRestore)cancelRestore();}};
})();
