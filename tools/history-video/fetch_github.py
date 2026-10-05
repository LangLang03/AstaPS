"""Fetch star/fork/PR/issue/release timelines from the GitHub REST API into github.js.

Unauthenticated requests are limited to 60/hour; set GITHUB_TOKEN to raise that.
"""
import json, os, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = sys.argv[1] if len(sys.argv) > 1 else 'MeChen618/AstaPS'


def get(path, accept='application/vnd.github+json'):
    cmd = ['curl', '-sSf', '-H', f'Accept: {accept}', '-H', 'X-GitHub-Api-Version: 2022-11-28']
    if os.environ.get('GITHUB_TOKEN'):
        cmd += ['-H', f"Authorization: Bearer {os.environ['GITHUB_TOKEN']}"]
    return json.loads(subprocess.run(cmd + [f'https://api.github.com/{path}'], capture_output=True, text=True, check=True).stdout)


def pages(path, accept='application/vnd.github+json'):
    out, page = [], 1
    sep = '&' if '?' in path else '?'
    while True:
        batch = get(f'{path}{sep}per_page=100&page={page}', accept)
        out += batch
        if len(batch) < 100:
            return out
        page += 1


repo = get(f'repos/{REPO}')
stars = pages(f'repos/{REPO}/stargazers', 'application/vnd.github.star+json')
forks = pages(f'repos/{REPO}/forks?sort=oldest')
issues = pages(f'repos/{REPO}/issues?state=all')  # includes PRs
pulls = pages(f'repos/{REPO}/pulls?state=all')
releases = pages(f'repos/{REPO}/releases')

data = dict(
    repo=REPO,
    created=repo['created_at'],
    totals=dict(stars=repo['stargazers_count'], forks=repo['forks_count'], watchers=repo.get('subscribers_count', 0),
                openIssues=repo['open_issues_count'], language=repo.get('language'), license=(repo.get('license') or {}).get('spdx_id'),
                description=repo.get('description') or '', topics=repo.get('topics', [])),
    stars=[[s['starred_at'], s['user']['login']] for s in stars],
    forks=[[f['created_at'], f['owner']['login']] for f in forks],
    pulls=[[p['number'], p['created_at'], p['merged_at'], p['closed_at'], p['user']['login'], p['title']] for p in pulls],
    issues=[[i['number'], i['created_at'], i['closed_at'], i['user']['login'], i['title']] for i in issues if 'pull_request' not in i],
    releases=[[r['published_at'] or r['created_at'], r['tag_name'], r['name'] or ''] for r in releases if not r.get('draft')],
)
open(os.path.join(HERE, 'github.js'), 'w').write('window.GH=' + json.dumps(data, ensure_ascii=False, separators=(',', ':')) + ';')
print(f"{REPO}: {len(data['stars'])} stars, {len(data['forks'])} forks, {len(data['pulls'])} PRs, "
      f"{len(data['issues'])} issues, {len(data['releases'])} releases, watchers {data['totals']['watchers']}")
