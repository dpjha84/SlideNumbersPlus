import os
s=open('index.html').read(); assert s.count('/*SAMPLES*/{}')==1
os.makedirs('dist',exist_ok=True); open('dist/index.html','w').write(s.replace('/*SAMPLES*/{}',open('samples.json').read()))
