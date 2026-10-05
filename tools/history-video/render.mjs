import { createRequire } from 'module';
import { spawn } from 'child_process';
import http from 'http'; import fs from 'fs'; import path from 'path';
const require = createRequire(import.meta.url);
let pw; try { pw = require('playwright'); } catch { pw = require(require('child_process').execSync('npm root -g').toString().trim() + '/playwright'); }
const dir = path.dirname(new URL(import.meta.url).pathname);
const srv = http.createServer((q, r) => { const f = path.join(dir, decodeURIComponent(q.url.split('?')[0])); fs.readFile(f, (e, d) => { if (e) { r.writeHead(404); r.end(); return; } r.writeHead(200, { 'Content-Type': f.endsWith('.html') ? 'text/html' : f.endsWith('.js') ? 'text/javascript' : f.endsWith('.ttf') ? 'font/ttf' : 'application/octet-stream' }); r.end(d); }); }).listen(0);
const port = srv.address().port;
const browser = await pw.chromium.launch({ args: ['--disable-gpu-vsync'] });
const page = await browser.newPage({ viewport: { width: 1920, height: 1080 } });
page.on('console', m => console.log('[page]', m.text())); page.on('pageerror', e => console.log('[err]', e.message));
await page.goto(`http://127.0.0.1:${port}/index.html`);
await page.evaluate(() => window.READY);
const N = await page.evaluate(() => window.NFRAMES);
const mode = process.argv[2];
if (mode === 'snap') {
  for (const s of process.argv.slice(3)) {
    const f = Math.round(parseFloat(s) * 30);
    const t = Date.now();
    const b64 = await page.evaluate(f => { renderFrame(f); return document.getElementById('c').toDataURL('image/png').split(',')[1]; }, f);
    fs.writeFileSync(path.join(dir, `snap_${s}.png`), Buffer.from(b64, 'base64')); console.log('frame', f, Date.now() - t, 'ms');
  }
} else {
  const out = process.argv[3] || path.join(dir, 'out.mp4');
  const ff = spawn('ffmpeg', ['-y', '-loglevel', 'error', '-f', 'image2pipe', '-framerate', '30', '-c:v', 'mjpeg', '-i', '-', '-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-movflags', '+faststart', out], { stdio: ['pipe', 'inherit', 'inherit'] });
  const t0 = Date.now();
  for (let f = 0; f < N; f++) {
    const b64 = await page.evaluate(f => { renderFrame(f); return document.getElementById('c').toDataURL('image/jpeg', 0.95).split(',')[1]; }, f);
    if (!ff.stdin.write(Buffer.from(b64, 'base64'))) await new Promise(r => ff.stdin.once('drain', r));
    if (f % 150 === 0) console.log(`frame ${f}/${N}  ${((Date.now() - t0) / 1000).toFixed(0)}s`);
  }
  ff.stdin.end(); await new Promise(r => ff.on('close', r));
}
await browser.close(); srv.close();
