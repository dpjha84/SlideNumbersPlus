import json,re
V=json.load(open('variants.json'))
svg=V['paris_night'].replace('preserveAspectRatio="xMidYMid slice"','preserveAspectRatio="xMidYMin slice"')
css=open('common.js').read()
anim=re.search(r"const ANIM=`(.*?)`;",css,re.S).group(1)
html='''<title>Tile Motion Lab</title>
<meta name="claude:viewport-fit-sides" content="cover">
<style>
/* Layout: one phone-width stage. Paris skyline fills the top, the board floats over the water, tuning sliders sit below. */
:root{--bg:#0d0a24;--ink:#f5f0ff;--mute:#a79fc9;--gold:#ffc766;--gold2:#e9902f;--panel:rgba(18,12,48,.55);--line:rgba(255,255,255,.14);--ok:#7be0a4;
--font:'Poppins',system-ui,sans-serif}
*{box-sizing:border-box}
html,body{margin:0}
body{background:var(--bg);color:var(--ink);font-family:var(--font);padding-left:max(16px,env(safe-area-inset-left,0px));padding-right:max(16px,env(safe-area-inset-right,0px));padding-block:0}
'''+anim+'''
.stage{position:relative;max-width:440px;margin:0 auto;padding-top:24px;padding-bottom:28px;isolation:isolate}
.sky{position:absolute;left:-16px;right:-16px;top:-60px;height:560px;z-index:-1;-webkit-mask-image:linear-gradient(#000 70%,transparent);mask-image:linear-gradient(#000 70%,transparent);transition:filter .6s}
.sky svg{width:100%;height:100%;display:block}
.sky.win{filter:brightness(1.35) saturate(1.2)}
.hud{display:flex;justify-content:space-between;align-items:center;gap:8px}
.pill{background:var(--panel);backdrop-filter:blur(8px);border:1px solid var(--line);padding:7px 13px;border-radius:999px;font-weight:600;font-size:13px;font-variant-numeric:tabular-nums}
.pill b{color:var(--gold)}
.spacer{height:190px}
#board{position:relative;width:100%;aspect-ratio:1;background:rgba(14,10,40,.32);backdrop-filter:blur(8px);border:1px solid var(--line);border-radius:22px;touch-action:none;user-select:none}
.tile{position:absolute;left:0;top:0;border-radius:16px;display:grid;place-items:center;font-weight:700;color:#fff;text-shadow:0 1px 6px rgba(0,0,0,.45);cursor:pointer;will-change:transform;
background:linear-gradient(155deg,hsla(var(--h),100%,68%,.82),hsla(calc(var(--h) + 30),100%,58%,.66));border:1px solid hsla(var(--h),100%,88%,.55);
backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);box-shadow:inset 0 1px 0 rgba(255,255,255,.55),inset 0 -8px 16px hsla(calc(var(--h) + 30),100%,75%,.22),0 4px 12px rgba(0,0,0,.28);transition:box-shadow .12s,background .12s,border-color .12s}
.tile.up{box-shadow:inset 0 1px 0 rgba(255,255,255,.55),0 14px 24px rgba(0,0,0,.45)}
.tile.ok{background:linear-gradient(155deg,hsla(var(--h),100%,78%,.9),hsla(calc(var(--h) + 28),95%,58%,.8));border-color:#fff;box-shadow:inset 0 1px 0 rgba(255,255,255,.8),0 0 20px hsla(var(--h),100%,65%,.85),0 0 4px #fff}
.tile.ok::after{content:"";position:absolute;right:7px;top:7px;width:7px;height:7px;border-radius:50%;background:#fff;box-shadow:0 0 8px #fff}
.row{display:flex;gap:10px;margin-top:16px}
button{font:inherit;font-weight:600;font-size:14px;color:var(--ink);background:var(--panel);border:1px solid var(--line);border-radius:14px;padding:11px 16px;cursor:pointer;flex:1;min-height:44px}
button.main{background:linear-gradient(160deg,var(--gold),var(--gold2));color:#3a2200;border:0}
button:focus-visible,.tile:focus-visible{outline:3px solid #fff;outline-offset:2px}
.tune{margin-top:20px;background:var(--panel);border:1px solid var(--line);border-radius:18px;padding:14px 16px;display:grid;gap:12px}
.tune h2{margin:0;font-size:12px;letter-spacing:.12em;text-transform:uppercase;color:var(--mute)}
.tune label{display:grid;grid-template-columns:1fr auto;gap:2px 8px;font-size:13px;color:var(--ink)}
.tune input[type=range]{grid-column:1/-1;width:100%;accent-color:var(--gold)}
.tune output{color:var(--gold);font-variant-numeric:tabular-nums}
.tune .chk{display:flex;gap:10px;align-items:center;grid-template-columns:none}
.hint{margin:14px 2px 0;font-size:12.5px;line-height:1.5;color:var(--mute)}
#banner{position:absolute;left:0;right:0;top:50%;margin:-60px auto 0;width:min(86%,320px);text-align:center;padding:18px;border-radius:22px;background:rgba(13,10,36,.88);border:1px solid var(--gold);transform:scale(0);opacity:0;pointer-events:none;transition:transform .55s cubic-bezier(.34,1.8,.5,1),opacity .2s;z-index:5}
#banner.show{transform:scale(1);opacity:1;pointer-events:auto}
#banner h3{margin:0 0 4px;font-size:22px}#banner p{margin:0 0 12px;color:var(--mute);font-size:13px}
@media (prefers-reduced-motion:reduce){.tw,.wb,.cl,.bd,.beam,.bt,.d1,.d2,.d3{animation:none}}
</style>
<div class="stage">
 <div class="sky" id="sky">'''+svg+'''</div>
 <div class="hud"><span class="pill">Level <b>7</b> · Paris</span><span class="pill">Moves <b id="mv">0</b> / par <b id="par">0</b></span></div>
 <div class="spacer"></div>
 <div id="board" role="application" aria-label="Sliding puzzle board. Tap or swipe tiles, or use arrow keys."></div>
 <div id="banner" role="status"><h3>Paris cleared ★★★</h3><p id="bmsg"></p><button class="main" id="again">Next shuffle</button></div>
 <div class="row"><button id="shuf">Shuffle</button><button id="size">Board: 4×4</button><button class="main" id="nudge">Test bump</button></div>
 <div class="tune">
  <h2>Tune the feel</h2>
  <label>Spring stiffness <output id="oK">10000</output><input id="K" type="range" min="600" max="16000" value="10000"></label>
  <label>Settle (lower = a little more rebound) <output id="oC">200</output><input id="C" type="range" min="30" max="320" value="200"></label>
  <label>Chain ripple delay <output id="oD">0 ms</output><input id="D" type="range" min="0" max="80" value="0"></label>
  <label class="chk"><input id="sq" type="checkbox"> Jelly squash (not recommended)</label>
  <label class="chk"><input id="hp" type="checkbox" checked> Haptic tick (phones that allow it)</label>
  <label class="chk"><input id="rm" type="checkbox"> Reduce motion (plain short slides)</label>
 </div>
 <p class="hint">Tap a tile in line with the gap, or swipe it. Several tiles in a row slide together with a tiny ripple. A tile that cannot move bumps against the stop. Solve it to see the city light up. Same spring values will go into the Compose build.</p>
</div>
<script>
(function(){
var N=4,grid=[],tiles={},moves=0,par=0,solved=false,cell=80,gap=7;
var board=document.getElementById('board'),mvEl=document.getElementById('mv'),parEl=document.getElementById('par');
var P={K:10000,C:200,D:0,sq:false,hp:true,rm:window.matchMedia&&matchMedia('(prefers-reduced-motion:reduce)').matches};
document.getElementById('rm').checked=P.rm;
function bind(id,key,fmt){var el=document.getElementById(id),o=document.getElementById('o'+id);el.oninput=function(){P[key]=+el.value;if(o)o.textContent=fmt?fmt(el.value):el.value}}
bind('K','K');bind('C','C');bind('D','D',function(v){return v+' ms'});
document.getElementById('sq').onchange=function(){P.sq=this.checked};
document.getElementById('hp').onchange=function(){P.hp=this.checked};
document.getElementById('rm').onchange=function(){P.rm=this.checked};
function tick(ms){if(P.hp)try{navigator.vibrate&&navigator.vibrate(ms)}catch(e){}}
function pos(r,c){return{x:gap+c*(cell+gap),y:gap+r*(cell+gap)}}
function goalOf(r,c){return r*N+c+1===N*N?0:r*N+c+1}
function build(){
 board.innerHTML='';tiles={};cell=(board.clientWidth-gap*(N+1))/N;
 for(var v=1;v<N*N;v++){var el=document.createElement('div');el.className='tile';el.textContent=v;el.tabIndex=-1;
  el.style.width=el.style.height=cell+'px';el.style.fontSize=(cell*.42)+'px';var gr=Math.floor((v-1)/N),gc=(v-1)%N;el.style.setProperty('--h',Math.round(175+185*((gr+gc)/(2*N-2))));board.appendChild(el);
  var t={v:v,el:el,x:0,y:0,vx:0,vy:0,tx:0,ty:0,s:1,vs:0,ts:1};tiles[v]=t;attach(t)}
 reset()}
function reset(){grid=[];for(var r=0;r<N;r++){grid[r]=[];for(var c=0;c<N;c++)grid[r][c]=goalOf(r,c)}syncAll(true);mark()}
function find(v){for(var r=0;r<N;r++)for(var c=0;c<N;c++)if(grid[r][c]===v)return[r,c]}
function syncAll(snap){for(var v in tiles){var rc=find(+v),p=pos(rc[0],rc[1]),t=tiles[v];t.tx=p.x;t.ty=p.y;if(snap){t.x=p.x;t.y=p.y;t.vx=t.vy=0}}}
function empty(){return find(0)}
function shuffle(){
 solved=false;moves=0;mvEl.textContent=0;hideBanner();
 reset();var last=null,steps=N*N*6;
 for(var i=0;i<steps;i++){var e=empty(),opts=[[1,0],[-1,0],[0,1],[0,-1]].filter(function(d){var r=e[0]+d[0],c=e[1]+d[1];return r>=0&&c>=0&&r<N&&c<N&&!(last&&d[0]===-last[0]&&d[1]===-last[1])});
  var d=opts[Math.floor(Math.random()*opts.length)];last=d;var r=e[0]+d[0],c=e[1]+d[1];grid[e[0]][e[1]]=grid[r][c];grid[r][c]=0}
 par=Math.max(steps*.55|0,N*N);parEl.textContent=par;
 var k=0;for(var v in tiles){(function(t){var rc=find(t.v),p=pos(rc[0],rc[1]);setTimeout(function(){t.tx=p.x;t.ty=p.y;t.vs=0},k++*16)})(tiles[v])}
 mark()}
function isSolved(){for(var r=0;r<N;r++)for(var c=0;c<N;c++)if(grid[r][c]!==goalOf(r,c))return false;return true}
/* move a line of tiles from (r,c) one step toward the gap in direction dr,dc; returns false when blocked */
function slide(r,c,dr,dc,fast){
 var line=[],rr=r,cc=c;
 while(rr>=0&&cc>=0&&rr<N&&cc<N){line.push([rr,cc]);if(grid[rr][cc]===0)break;rr+=dr;cc+=dc}
 var last=line[line.length-1];if(!last||grid[last[0]][last[1]]!==0||line.length<2)return false;
 for(var i=line.length-1;i>0;i--)grid[line[i][0]][line[i][1]]=grid[line[i-1][0]][line[i-1][1]];
 grid[r][c]=0;
 var lead=line.length-1;
 for(var j=line.length-2;j>=0;j--){(function(v,order){var t=tiles[v],rc=find(v),p=pos(rc[0],rc[1]);
   var go=function(){t.tx=p.x;t.ty=p.y;t.vs=fast?0:-.5;if(!P.rm&&!fast){}};
   var d=(P.rm||fast)?0:order*P.D;if(d)setTimeout(go,d);else go()})(grid[line[j+1][0]][line[j+1][1]],lead-1-j)}
 moves++;mvEl.textContent=moves;tick(8);setTimeout(mark,fast?120:lead*P.D+160);
 if(isSolved())setTimeout(win,260+(P.rm?0:line.length*P.D));
 return true}
function shake(t){if(P.rm){t.vs=-4;return}t.vx+=(t.v%2?1:-1)*150;tick(14)}
function dirMove(t,dr,dc){var rc=find(t.v);if(!slide(rc[0],rc[1],dr,dc))shake(t)}
function tapMove(t){var rc=find(t.v),e=empty(),dr=0,dc=0;
 if(rc[0]===e[0]&&rc[1]!==e[1])dc=e[1]>rc[1]?1:-1;else if(rc[1]===e[1]&&rc[0]!==e[0])dr=e[0]>rc[0]?1:-1;else return shake(t);
 slide(rc[0],rc[1],dr,dc)}
function mark(){for(var v in tiles){var rc=find(+v);tiles[v].el.classList.toggle('ok',goalOf(rc[0],rc[1])===+v)}}
function dragInfo(t){var rc=find(t.v),e=empty(),dr=0,dc=0;
 if(rc[0]===e[0]&&rc[1]!==e[1])dc=e[1]>rc[1]?1:-1;else if(rc[1]===e[1]&&rc[0]!==e[0])dr=e[0]>rc[0]?1:-1;else return null;
 var chain=[],r=rc[0],c=rc[1];while(grid[r][c]!==0){var tt=tiles[grid[r][c]],p=pos(r,c);chain.push({t:tt,bx:p.x,by:p.y});r+=dr;c+=dc}
 return{dr:dr,dc:dc,chain:chain,r:rc[0],c:rc[1],off:0}}
function attach(t){var sx,sy,id=null,info=null,moved=false,done=false,b0,lt=0,lo=0,vel=0;
 t.el.addEventListener('pointerdown',function(ev){if(solved)return;id=ev.pointerId;sx=ev.clientX;sy=ev.clientY;moved=false;done=false;vel=0;lo=0;lt=performance.now();
  t.ts=1.03;t.el.classList.add('up');try{t.el.setPointerCapture(id)}catch(e){}
  info=dragInfo(t);var rc=find(t.v);b0=pos(rc[0],rc[1])});
 t.el.addEventListener('pointermove',function(ev){if(id!==ev.pointerId||done)return;var dx=ev.clientX-sx,dy=ev.clientY-sy;
  if(!moved&&Math.hypot(dx,dy)<6)return;moved=true;var step=cell+gap;
  if(info){var off=Math.max(0,Math.min(step,dx*info.dc+dy*info.dr)),now=performance.now();
   if(now>lt){vel=(off-lo)/(now-lt)*1000;lt=now;lo=off}
   info.off=off;info.chain.forEach(function(c){c.t.drag=true;c.t.tx=c.bx+info.dc*off;c.t.ty=c.by+info.dr*off});
   if(off>(cell+gap)*.2){info.chain.forEach(function(c){c.t.drag=false});var i2=info;info=null;done=true;t.ts=1;t.el.classList.remove('up');slide(i2.r,i2.c,i2.dr,i2.dc,true);return}}
  else{var h=Math.abs(dx)>Math.abs(dy),k=Math.min(12,Math.max(Math.abs(dx),Math.abs(dy))*.2);
   t.drag=true;t.tx=b0.x+(h?Math.sign(dx)*k:0);t.ty=b0.y+(h?0:Math.sign(dy)*k)}});
 var end=function(ev){if(id!==ev.pointerId)return;id=null;if(done){done=false;return}t.ts=1;t.el.classList.remove('up');t.drag=false;
  if(info)info.chain.forEach(function(c){c.t.drag=false});
  if(!moved){if(!solved)tapMove(t);return}
  if(info&&(info.off>(cell+gap)*.2||vel>300)){slide(info.r,info.c,info.dr,info.dc,true)}
  else{if(info)info.chain.forEach(function(c){c.t.tx=c.bx;c.t.ty=c.by});else{t.tx=b0.x;t.ty=b0.y}tick(6)}
  info=null};
 t.el.addEventListener('pointerup',end);
 t.el.addEventListener('pointercancel',function(ev){end(ev)})}
document.addEventListener('keydown',function(ev){var m={ArrowLeft:[0,1],ArrowRight:[0,-1],ArrowUp:[1,0],ArrowDown:[-1,0]}[ev.key];if(!m||solved)return;
 var e=empty(),r=e[0]+m[0],c=e[1]+m[1];if(r<0||c<0||r>=N||c>=N)return;ev.preventDefault();slide(r,c,-m[0],-m[1])});
function win(){solved=true;tick(30);var k=0,order=[];for(var r=0;r<N;r++)for(var c=0;c<N;c++)if(grid[r][c])order.push(grid[r][c]);
 order.forEach(function(v,i){setTimeout(function(){var t=tiles[v];t.vs=P.rm?0:1.6;t.el.classList.add('ok')},P.rm?0:i*45)});
 document.getElementById('sky').classList.add('win');
 setTimeout(function(){document.getElementById('bmsg').textContent=moves+' moves · par '+par+(moves<=par?' · 3 stars':' · 2 stars');document.getElementById('banner').classList.add('show')},P.rm?0:order.length*45+300)}
function hideBanner(){document.getElementById('banner').classList.remove('show');document.getElementById('sky').classList.remove('win')}
document.getElementById('again').onclick=shuffle;document.getElementById('shuf').onclick=shuffle;
document.getElementById('nudge').onclick=function(){var e=empty(),ids=[];for(var r=0;r<N;r++)for(var c=0;c<N;c++){var v=grid[r][c];if(v&&r!==e[0]&&c!==e[1])ids.push(v)}shake(tiles[ids[Math.floor(Math.random()*ids.length)]])};
document.getElementById('size').onclick=function(){N=N===4?3:N===3?5:4;this.textContent='Board: '+N+'×'+N;gap=N===5?6:7;build();shuffle()};
var last=performance.now();
function frame(now){var dt=Math.min((now-last)/1000,1/30);last=now;
 var sub=4,h=dt/sub;
 for(var v in tiles){var t=tiles[v];var K=P.rm?10000:P.K,C=P.rm?200:P.C;if(t.drag){K=12000;C=220}
  for(var i=0;i<sub;i++){t.vx+=(-K*(t.x-t.tx)-C*t.vx)*h;t.x+=t.vx*h;t.vy+=(-K*(t.y-t.ty)-C*t.vy)*h;t.y+=t.vy*h;
   t.vs+=(-K*(t.s-t.ts)-C*.7*t.vs)*h;t.s+=t.vs*h}
  var ax=Math.min(Math.abs(t.vx)/2400,.13),ay=Math.min(Math.abs(t.vy)/2400,.13),sx=1,sy=1;
  if(P.sq&&!P.rm){sx=1+ax-ay*.7;sy=1+ay-ax*.7}
  t.el.style.transform='translate3d('+t.x.toFixed(2)+'px,'+t.y.toFixed(2)+'px,0) scale('+(t.s*sx).toFixed(3)+','+(t.s*sy).toFixed(3)+')'}
 requestAnimationFrame(frame)}
window.addEventListener('resize',function(){var nc=(board.clientWidth-gap*(N+1))/N;if(Math.abs(nc-cell)>1){cell=nc;for(var v in tiles){tiles[v].el.style.width=tiles[v].el.style.height=cell+'px';tiles[v].el.style.fontSize=cell*.42+'px'}syncAll(true)}});
build();shuffle();requestAnimationFrame(frame);
window.__t={slide:slide,find:find,empty:empty,grid:function(){return grid},tiles:tiles};
})();
</script>
'''
open('motion.html','w').write(html)
