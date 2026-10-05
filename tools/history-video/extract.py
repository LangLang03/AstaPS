import subprocess, json, re
import os
HERE=os.path.dirname(os.path.abspath(__file__))
R=subprocess.run(['git','-C',HERE,'rev-parse','--show-toplevel'],capture_output=True,text=True).stdout.strip()
def g(*a): return subprocess.run(['git','-c','core.quotePath=false','-C',R,*a],capture_output=True,text=True,errors='replace').stdout
hs=g('log','--reverse','--topo-order','--format=%H%x1f%ct%x1f%an%x1f%s%x1f%P').strip().split('\n')
out=[];t_prev=0
for line in hs:
    h,ct,an,s,p=line.split('\x1f')
    ct=int(ct); t_prev=max(t_prev,ct)
    parents=p.split()
    files=[]
    if len(parents)<=1:
        ns=g('show','--no-renames','--format=','--name-status',h).strip().split('\n')
        st={}
        for l in ns:
            if not l: continue
            k,path=l.split('\t',1); st[path]=k[0]
        num=g('show','--no-renames','--format=','--numstat',h).strip().split('\n')
        for l in num:
            if not l: continue
            a,d,path=l.split('\t',2)
            a=0 if a=='-' else int(a); d=0 if d=='-' else int(d)
            files.append([path,st.get(path,'M'),a,d])
    m=re.search(r'Merge pull request #(\d+)',s)
    out.append(dict(h=h[:7],t=t_prev,a=an,s=s,merge=len(parents)>1,f=files))
import collections
alive=set()
for c in out:
    for p,k,a,d in c['f']:
        if k=='D': alive.discard(p)
        else: alive.add(p)
allp=set(p for c in out for p,*_ in c['f'])
print(len(out),'commits; ever paths',len(allp),'alive',len(alive))
for c in out:
    if len(c['f'])>500: print(c['h'],c['a'],c['s'],len(c['f']),collections.Counter(k for _,k,_,_ in c['f']))
# compact form
paths=[];pid={}
C=[]
for c in out:
    fl=[]
    for p,k,a,d in sorted(c['f']):
        if p not in pid: pid[p]=len(paths); paths.append(p)
        fl.append([pid[p],'AMD'.index(k) if k in 'AMD' else 1,a,d])
    C.append([c['t'],c['a'],c['s'],c['h'],1 if c['merge'] else 0,fl])
open(os.path.join(HERE,'data.js'),'w').write('window.DATA='+json.dumps(dict(paths=paths,commits=C),ensure_ascii=False,separators=(',',':'))+';')
