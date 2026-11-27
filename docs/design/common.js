const fs=require('fs'); const V=JSON.parse(fs.readFileSync('variants.json'));
const ANIM=`
@keyframes tw{0%,100%{opacity:.25}50%{opacity:1}}.tw{animation:tw 2.4s ease-in-out infinite}
@keyframes wb{0%,100%{opacity:.9}45%{opacity:.08}}.wb{animation:wb 3s ease-in-out infinite}
@keyframes drift{from{transform:translateX(-160px)}to{transform:translateX(640px)}}.cl{animation:drift 40s linear infinite}
@keyframes bird{from{transform:translate(-40px,0)}to{transform:translate(640px,-40px)}}.bd{animation:bird 14s linear infinite}
@keyframes sweep{0%,100%{transform:rotate(-13deg)}50%{transform:rotate(13deg)}}.beam{animation:sweep 6s ease-in-out infinite;transform-origin:297px 46px}
@keyframes fall{from{transform:translateY(-60px)}to{transform:translateY(1000px)}}.rn{animation:fall .9s linear infinite}
@keyframes snow{from{transform:translate(0,-30px)}to{transform:translate(40px,1000px)}}.sn{animation:snow 8s linear infinite}
@keyframes boat{from{transform:translateX(-60px)}to{transform:translateX(600px)}}.bt{animation:boat 26s linear infinite}
@keyframes s1{from{transform:translateX(-5px)}to{transform:translateX(5px)}}@keyframes s2{from{transform:translateX(-9px)}to{transform:translateX(9px)}}@keyframes s3{from{transform:translateX(-15px)}to{transform:translateX(15px)}}
.d1{animation:s1 9s ease-in-out infinite alternate}.d2{animation:s2 9s ease-in-out infinite alternate}.d3{animation:s3 9s ease-in-out infinite alternate}
.neon .sk-g rect:not(.w):not(.bs),.neon .sk-g path,.neon .sk-g polygon{fill:#080420;stroke:var(--n);stroke-width:1.3px}
.neon .sk-g .bs{fill:#080420;stroke:none}.neon .hzr{display:none}.neon .sk-g{filter:drop-shadow(0 0 3px var(--n))}
.neon .sk-g line{stroke:var(--n)}
`;
const BASE=`*{box-sizing:border-box}body{margin:0;background:#0b0b12;font-family:Poppins,sans-serif;color:#fff}${ANIM}
.ph{position:relative;border-radius:26px;overflow:hidden;flex:none}.lb{position:absolute;left:0;right:0;bottom:0;padding:26px 12px 10px;background:linear-gradient(transparent,rgba(0,0,0,.65));font-size:13px;font-weight:600;text-align:center}
.pill{background:rgba(10,10,30,.45);backdrop-filter:blur(8px);padding:6px 12px;border-radius:16px;font-weight:700;font-size:12px}`;
const phone=(svg,label,w=270,h=480)=>`<div class=ph style="width:${w}px;height:${h}px">${svg}<div class=lb>${label}</div></div>`;
module.exports={V,BASE,phone,ANIM};
