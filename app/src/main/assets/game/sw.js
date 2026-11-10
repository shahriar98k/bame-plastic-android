const C='bp-v2';const F=["./", "index.html", "game1.js", "game2.js", "three.min.js", "fonts.css", "manifest.webmanifest", "pwa.js", "config.js", "store.js", "online.js", "ui.js", "fonts/hind-siliguri-bengali-500-normal.woff2", "fonts/hind-siliguri-bengali-700-normal.woff2", "fonts/hind-siliguri-latin-500-normal.woff2", "fonts/hind-siliguri-latin-700-normal.woff2"];
/* network-first: players always get the newest game when online, and the saved copy when offline */
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(F.map(u=>new Request(u,{cache:'reload'})))).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==C).map(x=>caches.delete(x)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{const r=e.request;if(r.method!=='GET')return;const u=new URL(r.url);if(u.origin!==location.origin)return;
e.respondWith(fetch(r,{cache:'no-cache'}).then(res=>{if(res&&res.ok){const cp=res.clone();caches.open(C).then(ch=>ch.put(r,cp))}return res}).catch(()=>caches.match(r,{ignoreSearch:true}).then(m=>m||caches.match('index.html'))))});
