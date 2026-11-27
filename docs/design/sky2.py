import random, math
W,H=540,960
SUNSET=('#2a1a5e','#c0507a','#ffb870','#7a4684','#4f2a6b','#241240')
def rnd(seed): return random.Random(seed)

PAL={ # sky top, sky mid, sky bottom, far, mid, near, accent glow, window
 'london':{'day':('#6fa8dc','#cfe3f2','#fbe7c6','#b7c9dc','#7f93ad','#3b4a63'),'night':('#0b1030','#26275f','#8a5a8c','#2a2f5c','#1a1d42','#0b0d24')},
 'paris': {'day':('#f6a5a0','#fbd1b0','#fff0c9','#e2b7b8','#b98a9f','#6b4f72'),'night':('#150f3a','#3a2060','#c4587a','#3b2358','#251440','#0f0a24')},
 'newyork':{'day':('#4a90d9','#9cc8ee','#f3e3cf','#9db6d0','#6b86a6','#2f4561'),'night':('#050a22','#14265a','#3a5ea0','#1b2a58','#0f1a3c','#060b1f')},
 'tokyo': {'day':('#ff9a8b','#ffc9a8','#fff1d0','#e6b4b0','#b98aa0','#5b4570'),'night':('#0d0630','#35124f','#e0457b','#32154f','#1d0b38','#0a0420')}}

def windows(x,y,w,h,seed,night,density=0.55):
    r=rnd(seed); out=[]
    if not night: return ''
    cols=max(1,int(w//7)); rows=max(1,int(h//9))
    for i in range(cols):
        for j in range(rows):
            if r.random()<density:
                c=r.choice(['#ffd98a','#ffe9b0','#ffc86b','#fff3d0'])
                blink=r.random()<.12
                cls='w wb' if blink else 'w'
                st=f' style="animation-delay:-{r.uniform(0,3):.1f}s"' if blink else ''
                out.append(f'<rect class="{cls}"{st} x="{x+3+i*7:.1f}" y="{y+4+j*9:.1f}" width="3.2" height="4.2" fill="{c}" opacity="{r.uniform(.55,.95):.2f}"/>')
    return ''.join(out)

DEPTH={1:1,2:2,3:1,4:2,5:3,6:1,7:2,8:3,9:2,10:3}
HAZE=[None]
def blocks(seed,y0,hmin,hmax,col,night,wmin=16,wmax=40,win=True,x0=-40,x1=580):
    return f'<g class="d{DEPTH.get(seed,2)}">'+_blocks(seed,y0,hmin,hmax,col,night,wmin,wmax,win,x0,x1)+(f'<rect x="-40" y="{y0-hmax-10}" width="620" height="{hmax+60}" class="hzr" fill="url(#{HAZE[0]})" opacity="{.75 if DEPTH.get(seed,2)==1 else .4}"/>' if HAZE[0] and DEPTH.get(seed,2)<3 else '')+'</g>'

def _blocks(seed,y0,hmin,hmax,col,night,wmin=16,wmax=40,win=True,x0=-10,x1=550):
    r=rnd(seed); x=x0; s=f'<rect class="bs" x="-10" y="{y0-hmin*0.6:.0f}" width="560" height="{H}" fill="{col}"/>'
    while x<x1:
        w=r.uniform(wmin,wmax); h=r.uniform(hmin,hmax)
        s+=f'<rect x="{x:.1f}" y="{y0-h:.1f}" width="{w:.1f}" height="{h+200:.1f}" fill="{col}"/>'
        if r.random()<.25: s+=f'<rect x="{x+w*.3:.1f}" y="{y0-h-8:.1f}" width="{w*.4:.1f}" height="8" fill="{col}"/>'
        if win: s+=windows(x,y0-h,w,h,int(x*10)+seed,night)
        x+=w+r.uniform(0,3)
    return s

def light(hexc,night,amt=.28,warm=None):
    if not night: return hexc
    if warm: return warm
    r,g,b=[int(hexc[i:i+2],16) for i in (1,3,5)]
    f=lambda v:int(v+(255-v)*amt)
    return '#%02x%02x%02x'%(f(r),f(g),f(b))

def london(night,c):
    far,mid,near=c[3],c[4],c[5]
    lm=light(near,night,.34)
    s=blocks(1,640,40,100,far,night,win=False)+blocks(2,690,50,120,mid,night)
    # Parliament + Big Ben at x~330
    bx=330
    s+=f'<g fill="{lm}">'
    s+=f'<rect x="{bx-120}" y="640" width="120" height="30"/><rect x="{bx-112}" y="626" width="104" height="16"/>'
    for i in range(6): s+=f'<polygon points="{bx-112+i*17},626 {bx-103+i*17},600 {bx-94+i*17},626"/>'
    s+=f'<rect x="{bx-18}" y="400" width="36" height="260"/><rect x="{bx-22}" y="392" width="44" height="12"/>'
    s+=f'<rect x="{bx-20}" y="330" width="40" height="64"/><polygon points="{bx-22},330 {bx},262 {bx+22},330"/><rect x="{bx-1}" y="238" width="2" height="26"/>'
    for dx in (-18,18): s+=f'<polygon points="{bx+dx-4},392 {bx+dx},372 {bx+dx+4},392"/>'
    s+='</g>'
    cl='#fff3c7' if night else '#f5efe0'
    s+=f'<circle cx="{bx}" cy="352" r="13" fill="{cl}" opacity=".95"/><circle cx="{bx}" cy="352" r="13" fill="none" stroke="{near}" stroke-width="1.5"/><path d="M{bx} 352 V344 M{bx} 352 L{bx+5} 355" stroke="{near}" stroke-width="1.4"/>'
    # Tower Bridge at left
    tx=120
    s+=f'<g fill="{lm}"><rect x="{tx-70}" y="560" width="22" height="110"/><rect x="{tx+48}" y="560" width="22" height="110"/>'
    for ox in (-70,48):
        s+=f'<polygon points="{tx+ox-2},560 {tx+ox+11},520 {tx+ox+24},560"/>'
        s+=f'<rect x="{tx+ox+4}" y="500" width="14" height="22"/><polygon points="{tx+ox+2},500 {tx+ox+11},478 {tx+ox+20},500"/>'
    s+=f'<rect x="{tx-48}" y="590" width="96" height="8"/><rect x="{tx-48}" y="640" width="96" height="6"/></g>'
    s+=f'<path d="M{tx-170} 640 Q{tx-120} 590 {tx-59} 560 M{tx+59} 560 Q{tx+120} 590 {tx+170} 640 M{tx-59} 560 Q{tx} 650 {tx+59} 560" fill="none" stroke="{lm}" stroke-width="3"/>'
    s+=f'<rect x="-10" y="650" width="560" height="{H}" fill="{near}"/>'
    s+=f'<rect x="-10" y="672" width="560" height="{H}" fill="{c[4]}" opacity=".55"/>'
    return s

def paris(night,c):
    far,mid,near=c[3],c[4],c[5]
    s=blocks(3,640,26,60,far,night,wmin=20,wmax=34,win=False)+blocks(4,700,30,70,mid,night,wmin=22,wmax=36)
    cx=270; g=light(near,night,warm='#e0a63c')
    # Eiffel: legs w/ arch, platforms, upper tower
    s+=f'<g fill="{g}">'
    s+=f'<path d="M{cx-92} 700 L{cx-58} 560 L{cx-48} 560 L{cx-62} 700 Z M{cx+92} 700 L{cx+58} 560 L{cx+48} 560 L{cx+62} 700 Z"/>'
    s+=f'<path d="M{cx-62} 700 Q{cx} 600 {cx+62} 700 L{cx+50} 700 Q{cx} 625 {cx-50} 700 Z"/>'
    s+=f'<rect x="{cx-60}" y="552" width="120" height="9"/>'
    s+=f'<path d="M{cx-52} 552 L{cx-22} 440 L{cx-14} 440 L{cx-30} 552 Z M{cx+52} 552 L{cx+22} 440 L{cx+14} 440 L{cx+30} 552 Z"/>'
    s+=f'<path d="M{cx-36} 552 Q{cx} 500 {cx+36} 552 L{cx+28} 552 Q{cx} 516 {cx-28} 552 Z"/>'
    s+=f'<rect x="{cx-26}" y="432" width="52" height="8"/>'
    s+=f'<path d="M{cx-20} 432 L{cx-7} 330 L{cx+7} 330 L{cx+20} 432 Z"/><rect x="{cx-12}" y="352" width="24" height="5"/>'
    s+=f'<polygon points="{cx-4},330 {cx},270 {cx+4},330"/><rect x="{cx-1}" y="252" width="2" height="22"/>'
    s+='</g>'
    # lattice hint
    s+=''.join(f'<line x1="{cx-40+i*16}" y1="{556}" x2="{cx-22+i*11}" y2="{620}" stroke="{mid}" stroke-width=".8" opacity=".5"/>' for i in range(6))
    # beam
    # trees/foreground
    s+=blocks(5,760,16,34,near,night,wmin=14,wmax=26,win=False)
    return s

def newyork(night,c):
    far,mid,near=c[3],c[4],c[5]
    s=blocks(6,660,50,140,far,night,win=False)+blocks(7,700,60,150,mid,night)
    g=light(near,night,.36); s+=f'<g fill="{g}">'
    # Empire State
    ex=300
    s+=f'<rect x="{ex-28}" y="520" width="56" height="200"/><rect x="{ex-22}" y="480" width="44" height="42"/><rect x="{ex-16}" y="450" width="32" height="32"/><rect x="{ex-10}" y="426" width="20" height="26"/><rect x="{ex-5}" y="408" width="10" height="20"/><rect x="{ex-1}" y="352" width="2" height="58"/>'
    # One WTC (spire)
    wx=190
    s+=f'<polygon points="{wx-22},720 {wx-18},400 {wx},388 {wx+18},400 {wx+22},720"/><rect x="{wx-0.8}" y="300" width="1.6" height="92"/>'
    # Chrysler crown
    kx=410
    s+=f'<rect x="{kx-20}" y="500" width="40" height="220"/><path d="M{kx-20} 500 Q{kx} 440 {kx+20} 500 Z"/><path d="M{kx-14} 488 Q{kx} 452 {kx+14} 488" fill="none" stroke="{mid}" stroke-width="2"/><rect x="{kx-1}" y="420" width="2" height="40"/>'
    s+='</g>'
    s+=windows(ex-28,520,56,190,11,night,.5)+windows(wx-20,410,40,300,12,night,.5)+windows(kx-20,500,40,210,13,night,.5)
    # foreground low buildings + water
    s+=blocks(8,780,20,50,near,night,wmin=26,wmax=50,win=night)
    return s

def tokyo(night,c):
    far,mid,near=c[3],c[4],c[5]
    # Fuji
    s=f'<polygon points="-20,640 150,470 190,450 230,470 400,640" fill="{far}" opacity=".8"/><polygon points="150,470 190,450 230,470 214,482 198,474 188,486 172,476" fill="#ffffff" opacity="{.55 if not night else .25}"/>'
    s+=blocks(9,690,40,110,mid,night)
    tx=380; red='#ff5b4a' if not night else '#ff7a5c'
    s+=f'<g fill="{red}">'
    s+=f'<path d="M{tx-64} 720 L{tx-12} 470 L{tx-8} 470 L{tx-52} 720 Z M{tx+64} 720 L{tx+12} 470 L{tx+8} 470 L{tx+52} 720 Z"/>'
    s+=f'<rect x="{tx-34}" y="600" width="68" height="9"/><rect x="{tx-22}" y="520" width="44" height="8"/><rect x="{tx-14}" y="470" width="28" height="6"/>'
    s+=f'<path d="M{tx-10} 470 L{tx-3} 360 L{tx+3} 360 L{tx+10} 470 Z"/><polygon points="{tx-2},360 {tx},300 {tx+2},360"/><rect x="{tx-1}" y="280" width="2" height="22"/>'
    s+='</g>'
    s+=''.join(f'<line x1="{tx-30+i*15}" y1="610" x2="{tx-22+i*11}" y2="716" stroke="#fff" stroke-width="1.4" opacity=".7"/>' for i in range(5))
    s+=blocks(10,770,40,120,near,night,wmin=20,wmax=44)
    return s

CITIES={'london':london,'paris':paris,'newyork':newyork,'tokyo':tokyo}

def svg(city,night):
    c=PAL[city]['night' if night else 'day']
    id=f'{city}{int(night)}'
    s=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}"><defs><linearGradient id="sk{id}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[0]}"/><stop offset=".55" stop-color="{c[1]}"/><stop offset="1" stop-color="{c[2]}"/></linearGradient>'
    s+=f'<linearGradient id="gw{id}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[4]}" stop-opacity=".55"/><stop offset=".35" stop-color="{c[5]}" stop-opacity=".85"/><stop offset="1" stop-color="{c[5]}"/></linearGradient><linearGradient id="gr{id}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[5]}" stop-opacity="0"/><stop offset="1" stop-color="{c[5]}" stop-opacity=".9"/></linearGradient></defs>'
    s+=f'<rect width="{W}" height="{H}" fill="url(#sk{id})"/>'
    r=rnd(42)
    if night:
        for _ in range(70): s+=f'<circle cx="{r.uniform(0,W):.0f}" cy="{r.uniform(0,520):.0f}" r="{r.uniform(.5,1.6):.1f}" fill="#fff" opacity="{r.uniform(.3,.9):.2f}"/>'
        s+=f'<circle cx="90" cy="150" r="30" fill="#fff6d8"/><circle cx="100" cy="143" r="30" fill="{c[0]}" opacity=".0"/>'
        s+='<circle cx="90" cy="150" r="64" fill="#fff6d8" opacity=".10"/>'
    else:
        s+=f'<circle cx="430" cy="190" r="130" fill="url(#{pfx}ml)"/><circle cx="430" cy="190" r="46" fill="#fff4cf" opacity=".95"/>'
        for (x,y,sc) in ((80,140,1),(300,90,.8),(470,330,.9)): s+=f'<g opacity=".75" fill="#fff"><ellipse cx="{x}" cy="{y}" rx="{50*sc}" ry="{12*sc}"/><ellipse cx="{x+24*sc}" cy="{y-9*sc}" rx="{28*sc}" ry="{12*sc}"/></g>'
    sk=CITIES[city](night,c); gy=650*.9-190+ (10 if city=='london' else 0)
    s+=f'<g transform="translate(54,-190) scale(.9)">'+sk+'</g>'
    # ground / water below the skyline with a soft reflection
    s+=f'<rect x="0" y="{gy:.0f}" width="{W}" height="{H}" fill="{c[5]}"/>'
    s+=f'<clipPath id="rc{id}"><rect x="0" y="{gy:.0f}" width="{W}" height="300"/></clipPath><g clip-path="url(#rc{id})" opacity="{.28 if night else .18}"><g transform="translate(0,{2*gy:.0f}) scale(1,-1)"><g transform="translate(54,-190) scale(.9)">{sk}</g></g></g>'
    s+=f'<rect x="0" y="{gy:.0f}" width="{W}" height="{H}" fill="url(#gw{id})"/>'
    s+=f'<rect x="0" y="0" width="{W}" height="{H}" fill="{c[5]}" opacity="{.25 if night else .06}"/>'
    s+='</svg>'
    return s


import json
NEON={'london':'#41e0ff','paris':'#ff4fd8','newyork':'#5aa8ff','tokyo':'#ff6a4f'}
def svg2(city,mode='night',weather=None,neon=False,pfx='a'):
    night = mode!='day'
    c = SUNSET if mode=='sunset' else PAL[city]['night' if mode=='night' else 'day']
    if neon: c=('#06031a','#140a3a','#2a0f55',c[3],c[4],'#05020f')
    HAZE[0]=pfx+'hz'
    gy=650*.9-190+(10 if city=='london' else 0)
    r=rnd(42)
    s=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="100%" height="100%" preserveAspectRatio="xMidYMid slice" class="{"neon" if neon else ""}" style="--n:{NEON[city]}"><defs>'
    s+=f'<linearGradient id="{pfx}sk" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[0]}"/><stop offset=".55" stop-color="{c[1]}"/><stop offset="1" stop-color="{c[2]}"/></linearGradient>'
    s+=f'<linearGradient id="{pfx}hz" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[2]}" stop-opacity="0"/><stop offset="1" stop-color="{c[2]}" stop-opacity=".85"/></linearGradient>'
    s+=f'<linearGradient id="{pfx}gw" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c[4]}" stop-opacity=".55"/><stop offset=".35" stop-color="{c[5]}" stop-opacity=".85"/><stop offset="1" stop-color="{c[5]}"/></linearGradient>'
    s+=f'<radialGradient id="{pfx}gl"><stop offset="0" stop-color="#ffd9a0" stop-opacity=".55"/><stop offset="1" stop-color="#ffd9a0" stop-opacity="0"/></radialGradient><radialGradient id="{pfx}ml"><stop offset="0" stop-color="#fff6d8" stop-opacity=".35"/><stop offset="1" stop-color="#fff6d8" stop-opacity="0"/></radialGradient>'
    s+=f'<clipPath id="{pfx}rc"><rect x="0" y="{gy:.0f}" width="{W}" height="300"/></clipPath></defs>'
    s+=f'<rect width="{W}" height="{H}" fill="url(#{pfx}sk)"/>'
    if night:
        for _ in range(70):
            tw=' class="tw" style="animation-delay:-%.1fs"'%r.uniform(0,2.4) if r.random()<.5 else ''
            s+=f'<circle{tw} cx="{r.uniform(0,W):.0f}" cy="{r.uniform(0,420 if mode=="night" else 220):.0f}" r="{r.uniform(.5,1.6):.1f}" fill="#fff" opacity="{r.uniform(.3,.9) if mode=="night" else r.uniform(.1,.4):.2f}"/>'
    if mode=='night':
        s+=f'<circle cx="90" cy="150" r="90" fill="url(#{pfx}ml)"/><circle cx="90" cy="150" r="30" fill="#fff6d8"/>'
    elif mode=='sunset':
        s+=f'<circle cx="150" cy="330" r="200" fill="url(#{pfx}gl)"/><circle cx="150" cy="330" r="70" fill="#ffd08a"/>'
    else:
        s+=f'<circle cx="430" cy="190" r="130" fill="url(#{pfx}ml)"/><circle cx="430" cy="190" r="46" fill="#fff4cf" opacity=".95"/>'
    cc='#fff' if mode=='day' else ('#ffd0c0' if mode=='sunset' else '#6a60a8')
    if not neon:
        for i,(x,y,sc) in enumerate(((60,140,1),(300,90,.8),(430,300,.9))):
            s+=f'<g class="cl" style="animation-delay:-{i*13}s;animation-duration:{38+i*9}s"><g opacity="{.75 if mode=="day" else .35}" fill="{cc}"><ellipse cx="{x}" cy="{y}" rx="{50*sc}" ry="{12*sc}"/><ellipse cx="{x+24*sc}" cy="{y-9*sc}" rx="{28*sc}" ry="{12*sc}"/></g></g>'
        if mode!='night':
            for i in range(3): s+=f'<path class="bd" style="animation-delay:-{i*4.5}s" d="M0 {220+i*34} q7 -8 14 0 q7 -8 14 0" fill="none" stroke="{"#3a3550" if mode=="day" else "#2a1a40"}" stroke-width="1.8"/>'
    sk=CITIES[city](night,c)
    s+=f'<g class="sk-g" transform="translate(54,-190) scale(.9)">{sk}</g>'
    s+=f'<rect x="0" y="{gy:.0f}" width="{W}" height="{H}" fill="{c[5]}"/>'
    s+=f'<g clip-path="url(#{pfx}rc)" opacity="{.28 if night else .18}"><g transform="translate(0,{2*gy:.0f}) scale(1,-1)"><g class="sk-g" transform="translate(54,-190) scale(.9)">{sk}</g></g></g>'
    s+=f'<rect x="0" y="{gy:.0f}" width="{W}" height="{H}" fill="url(#{pfx}gw)"/>'
    if night:
        for i,(y,col) in enumerate(((gy+36,'#ffd98a'),(gy+70,'#9fe8ff'))):
            s+=f'<g class="bt" style="animation-duration:{26+i*10}s;animation-delay:-{i*9}s"><rect x="0" y="{y:.0f}" width="26" height="4" rx="2" fill="#0a0618" opacity=".8"/><circle cx="6" cy="{y-1:.0f}" r="2.4" fill="{col}"/><rect x="2" y="{y+5:.0f}" width="26" height="3" fill="{col}" opacity=".22"/></g>'
    if city=='paris' and mode=='night':
        s+=f'<polygon class="beam" points="297,46 238,-320 356,-320" fill="#ffe9a8" opacity=".14"/>'
    if weather=='rain':
        for i in range(70): s+=f'<line class="rn" style="animation-delay:-{r.uniform(0,.9):.2f}s;animation-duration:{.7+r.uniform(0,.4):.2f}s" x1="{r.uniform(0,W):.0f}" y1="{r.uniform(-60,H):.0f}" x2="{r.uniform(0,W)-6:.0f}" y2="0" stroke="#cfe4ff" stroke-width="1.1" opacity=".45"/>'.replace('y2="0"','y2="%d"'%0) if False else f'<line class="rn" style="animation-delay:-{r.uniform(0,.9):.2f}s;animation-duration:{.7+r.uniform(0,.4):.2f}s" x1="{(x:=r.uniform(0,W+40)):.0f}" y1="{(y:=r.uniform(-60,H)):.0f}" x2="{x-6:.0f}" y2="{y+22:.0f}" stroke="#cfe4ff" stroke-width="1.1" opacity=".5"/>'
        s+=f'<rect width="{W}" height="{H}" fill="#2a3550" opacity=".28"/>'
    if weather=='snow':
        for i in range(90): s+=f'<circle class="sn" style="animation-delay:-{r.uniform(0,8):.1f}s;animation-duration:{6+r.uniform(0,5):.1f}s" cx="{r.uniform(0,W):.0f}" cy="{r.uniform(-40,H):.0f}" r="{r.uniform(1.6,3.6):.1f}" fill="#fff" opacity="{r.uniform(.7,1):.2f}"/>'
        s+=f'<rect width="{W}" height="{H}" fill="#8fa3c4" opacity=".18"/>'
    s+=f'<rect width="{W}" height="{H}" fill="{c[5]}" opacity="{.18 if night else .05}"/></svg>'
    return s

if __name__=='__main__':
    V={}
    V['paris_sunset']=svg2('paris','sunset',pfx='p1')
    V['newyork_day']=svg2('newyork','day',pfx='n1')
    V['tokyo_night']=svg2('tokyo','night',pfx='t1')
    for m in ('day','sunset','night'): V['london_'+m]=svg2('london',m,pfx='l'+m[0])
    V['london_rain']=svg2('london','day',weather='rain',pfx='lr')
    V['paris_snow']=svg2('paris','day',weather='snow',pfx='ps')
    for c in ('paris','newyork','tokyo'): V['neon_'+c]=svg2(c,'night',neon=True,pfx='nn'+c[:2])
    V['paris_night']=svg2('paris','night',pfx='pn')
    V['newyork_sunset']=svg2('newyork','sunset',pfx='ns')
    V['london_panorama']=svg2('london','sunset',pfx='lp')
    json.dump(V,open('variants.json','w'))
    print(len(V),'variants')
