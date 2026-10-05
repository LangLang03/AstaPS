# History video

Renders the repository's git history as an ink-wash (水墨) animation: directories
grow as brush-stroke branches, files bloom as blossoms coloured by type, deleted
files fall as petals, and each contributor's brush flicks strokes to the files a
commit touches. GitHub activity is layered on top: each star is brushed into a
gold constellation, each fork grows as a sapling at the tree's foot, and PRs,
issues and releases join the activity log, timeline and counters. Every frame is drawn with Canvas 2D in `index.html` and piped
through headless Chromium into ffmpeg.

Requirements: Python 3, Node with Playwright (Chromium), ffmpeg, and a full
(non-shallow) clone (`git fetch --unshallow`).

```sh
./fetch-fonts.sh                  # Ma Shan Zheng, Cormorant Garamond, JetBrains Mono (OFL)
python3 extract.py                # writes data.js from git log
python3 fetch_github.py           # optional: writes github.js (stars, forks, PRs, issues, releases)
                                  #   set GITHUB_TOKEN to avoid the 60 req/h anonymous limit
node render.mjs snap 10 40        # optional: preview frames at 10s and 40s -> snap_*.png
node render.mjs full out.mp4      # 1920x1080, 30 fps
```
