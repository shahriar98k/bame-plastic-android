if('serviceWorker' in navigator)addEventListener('load',()=>navigator.serviceWorker.register('sw.js').catch(()=>{}));
/* update notice: checks every 5 minutes whether a newer version of the game was published */
(function(){let first=null;async function sig(){try{const r=await fetch('index.html',{method:'HEAD',cache:'no-store'});return (r.headers.get('etag')||'')+'|'+(r.headers.get('last-modified')||'')}catch(e){return null}}
function show(){if(document.getElementById('upd'))return;const d=document.createElement('div');d.id='upd';d.style.display='flex';d.append('🔄 গেমের নতুন আপডেট এসেছে ');const b=document.createElement('button');b.textContent='আপডেট করুন';b.onclick=()=>location.reload();const x=document.createElement('button');x.textContent='পরে';x.onclick=()=>d.remove();d.append(b,x);document.body.appendChild(d)}
async function chk(){const s=await sig();if(!s||s==='|')return;if(first===null){first=s;return}if(s!==first)show()}
if(location.protocol.startsWith('http')){setTimeout(chk,4000);setInterval(chk,300000)}})();
