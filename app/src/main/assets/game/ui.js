/* start-screen helpers: Enter key starts the game, name box scrolls into view above the keyboard */
(function(){const u=document.getElementById('un'),go=document.getElementById('go');if(!u||!go)return;
u.addEventListener('keydown',e=>{if(e.key==='Enter'){e.preventDefault();u.blur();go.click()}});
u.addEventListener('focus',()=>{setTimeout(()=>{try{u.scrollIntoView({block:'center',behavior:'smooth'})}catch(e){}},350)});
go.addEventListener('touchend',()=>{try{u.blur()}catch(e){}},{passive:true})})();
