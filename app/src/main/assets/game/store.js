/* Saves small values (name, device id) so they survive restarts: Android app storage -> localStorage -> cookie */
(function(){const mem={};
function ck(k){try{const m=document.cookie.match(new RegExp('(?:^|; )'+k+'=([^;]*)'));return m?decodeURIComponent(m[1]):null}catch(e){return null}}
function setck(k,v){try{document.cookie=k+'='+encodeURIComponent(v)+';max-age=315360000;path=/;SameSite=Lax'}catch(e){}}
const BPS={
get(k){let v=null;
try{if(window.AndroidStore){v=window.AndroidStore.get(k);if(v===''||v==='null'||v===undefined)v=null}}catch(e){v=null}
if(v==null)try{v=localStorage.getItem(k)}catch(e){v=null}
if(v==null)v=ck(k);
if(v==null&&Object.prototype.hasOwnProperty.call(mem,k))v=mem[k];
return v},
set(k,v){v=String(v);mem[k]=v;try{window.AndroidStore&&window.AndroidStore.set(k,v)}catch(e){}try{localStorage.setItem(k,v)}catch(e){}setck(k,v)},
devId(){let d=this.get('bp_dev');if(!d){d=(window.crypto&&crypto.randomUUID)?crypto.randomUUID():'d'+Date.now().toString(36)+Math.random().toString(36).slice(2,14);this.set('bp_dev',d)}return d}};
window.BPS=BPS;if(typeof module!=='undefined')module.exports=BPS})();
