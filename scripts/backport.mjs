#!/usr/bin/env node
// yarn backport [versions...] [--reset]   Backport this branch's commits to the other release lines as draft PRs.

import { spawnSync } from 'node:child_process'

const REMOTE = 'origin'
const LINKS_START = '<!-- backport:start -->'
const LINKS_END = '<!-- backport:end -->'

class UserError extends Error {}
class BackportBlocked extends Error {}

function backport(args) {
  const reset = args.includes('--reset')
  const requestedLines = args.filter((arg) => !arg.startsWith('--'))

  git('fetch', REMOTE)
  const lines = releaseLines()
  const source = currentChange(lines)
  const targetLines = chooseTargetLines(lines, source, requestedLines)
  const commits = commitsToBackport(source)

  requireCleanCheckout()
  pushSourceBranch(source)

  console.log(`Backporting ${commits.length} commit(s) from ${source.branch} to: ${targetLines.join(', ')}`)
  const backportedBranches = []
  const problems = []
  try {
    for (const line of targetLines) {
      const branch = `${line}/${source.name}`
      try {
        backportCommits(commits, branch, line, source, reset)
        backportedBranches.push(branch)
      } catch (error) {
        if (!(error instanceof BackportBlocked)) throw error
        problems.push(`${branch} was not backported: ${error.message}`)
        if (cherryPickInProgress()) break
      }
    }
  } finally {
    if (!cherryPickInProgress()) git('checkout', '--quiet', source.branch)
  }

  const pullRequests = openPullRequests(source, lines, [source.branch, ...backportedBranches], commits[0])
  linkPullRequests(pullRequests)

  console.log('\nPull requests:')
  for (const pr of pullRequests) {
    console.log(`  (${pr.line}) ${pr.url}${pr.isDraft ? '  [draft]' : ''}`)
  }
  if (problems.length) throw new UserError(problems.join('\n\n'))
}

function releaseLines() {
  const mainBranch = new RegExp(`^${REMOTE}/([^/]+)/main$`)
  return git('for-each-ref', '--format=%(refname:short)', `refs/remotes/${REMOTE}`)
    .split('\n')
    .map((ref) => ref.match(mainBranch)?.[1])
    .filter(Boolean)
    .sort((a, b) => Number(a) - Number(b))
}

function currentChange(lines) {
  if (cherryPickInProgress()) {
    throw new UserError(
      `A cherry-pick is in progress. Finish it with 'git cherry-pick --continue',\n` +
        `then switch back to the branch you are backporting from.`
    )
  }

  const branch = git('branch', '--show-current')
  const [, line, name] = branch.match(/^([^/]+)\/(.+)$/) ?? []
  if (!lines.includes(line) || name === 'main') {
    throw new UserError(
      `Run this from a branch named <version>/<name>, where <version> is one of: ${lines.join(', ')}.\n` +
        `The current branch is '${branch || '(detached)'}'.`
    )
  }
  if (backportedCommits(`${REMOTE}/${line}/main`).length) {
    throw new UserError(`${branch} is itself a backport. Switch back to the branch you are backporting from.`)
  }
  return { branch, line, name }
}

function chooseTargetLines(lines, source, requestedLines) {
  const otherLines = lines.filter((line) => line !== source.line)
  const unknown = requestedLines.filter((line) => !otherLines.includes(line))
  if (unknown.length) {
    throw new UserError(`Cannot backport to: ${unknown.join(', ')}. Other release lines: ${otherLines.join(', ')}.`)
  }
  return requestedLines.length ? otherLines.filter((line) => requestedLines.includes(line)) : otherLines
}

function commitsToBackport(source) {
  const sourceMain = `${REMOTE}/${source.line}/main`
  const commits = git('rev-list', '--reverse', '--no-merges', `${sourceMain}..HEAD`).split('\n').filter(Boolean)
  if (!commits.length) {
    throw new UserError(`${source.branch} has no commits that are not on ${source.line}/main.`)
  }
  return commits
}

function requireCleanCheckout() {
  if (git('status', '--porcelain', '--untracked-files=no')) {
    throw new UserError('Commit or stash your changes first: backporting switches branches.')
  }
}

function pushSourceBranch(source) {
  if (!gitSucceeds('push', '--set-upstream', REMOTE, source.branch)) {
    throw new UserError(
      `${source.branch} could not be pushed.\n` +
        `If you rewrote its history, force-push it, then run 'yarn backport --reset'.`
    )
  }
}

function backportCommits(commits, branch, line, source, reset) {
  const lineMain = `${REMOTE}/${line}/main`
  checkOut(branch, lineMain, reset)

  const alreadyBackported = backportedCommits(lineMain)
  if (alreadyBackported.some((commit) => !commits.includes(commit))) {
    throw new BackportBlocked(
      `it holds commits backported from history that is no longer on ${source.branch}.\n` +
        `    Run 'yarn backport --reset' to rebuild it (conflict resolutions made there are lost).`
    )
  }

  const pending = commits.filter((commit) => !alreadyBackported.includes(commit))
  for (const commit of pending) {
    cherryPick(commit, source)
    console.log(`  ${branch}: backported '${subjectOf(commit)}'`)
  }
  if (!pending.length) console.log(`  ${branch}: already up to date`)

  const force = reset ? ['--force-with-lease'] : []
  if (!gitSucceeds('push', ...force, '--set-upstream', REMOTE, branch)) {
    throw new BackportBlocked(`it could not be pushed. Reconcile it with ${REMOTE}/${branch}.`)
  }
}

function checkOut(branch, lineMain, reset) {
  const remoteBranch = `${REMOTE}/${branch}`

  if (reset) {
    git('checkout', '--quiet', '--no-track', '-B', branch, lineMain)
  } else if (refExists(`refs/heads/${branch}`)) {
    git('checkout', '--quiet', branch)
    gitSucceeds('merge', '--ff-only', remoteBranch)
  } else if (refExists(`refs/remotes/${remoteBranch}`)) {
    git('checkout', '--quiet', '--track', '-b', branch, remoteBranch)
  } else {
    git('checkout', '--quiet', '--no-track', '-b', branch, lineMain)
  }
}

function cherryPickInProgress() {
  return refExists('CHERRY_PICK_HEAD')
}

// `git cherry-pick -x` records each source commit in the backported commit's message.
function backportedCommits(lineMain) {
  const messages = git('log', '--format=%B', `${lineMain}..HEAD`)
  return [...messages.matchAll(/\(cherry picked from commit ([0-9a-f]{40})\)/g)].map((match) => match[1])
}

function cherryPick(commit, source) {
  if (gitSucceeds('cherry-pick', '-x', '--keep-redundant-commits', commit)) return

  const conflicts = git('diff', '--name-only', '--diff-filter=U')
  throw new BackportBlocked(
    `'${subjectOf(commit)}' did not apply cleanly.\n` +
      conflicts.split('\n').map((file) => `      ${file}\n`).join('') +
      `    You are now on this branch. Resolve the conflicts, then run:\n` +
      `      git cherry-pick --continue\n` +
      `      git checkout ${source.branch}\n` +
      `      yarn backport`
  )
}

function subjectOf(commit) {
  return git('log', '-1', '--format=%s', commit)
}

function openPullRequests(source, lines, pushedBranches, firstCommit) {
  const existing = lines.map((line) => findPullRequest(line, source.name)).filter(Boolean)
  const sourcePr = existing.find((pr) => pr.line === source.line)
  const title = withoutLinePrefix(sourcePr?.title ?? subjectOf(firstCommit))
  const body = withoutLinks(sourcePr?.body ?? '')

  return lines
    .map((line) => {
      const branch = `${line}/${source.name}`
      const alreadyOpen = existing.find((pr) => pr.line === line)
      if (alreadyOpen || !pushedBranches.includes(branch)) return alreadyOpen

      gh('pr', 'create', '--draft', '--base', `${line}/main`, '--head', branch,
        '--title', `(${line}) ${title}`, '--body', body)
      return findPullRequest(line, source.name)
    })
    .filter(Boolean)
}

function findPullRequest(line, name) {
  const branch = `${line}/${name}`
  const found = gh('pr', 'list', '--head', branch, '--base', `${line}/main`, '--state', 'open',
    '--json', 'number,url,title,body,isDraft')
  const [pr] = JSON.parse(found || '[]')
  return pr ? { ...pr, line } : null
}

function linkPullRequests(pullRequests) {
  const links = pullRequests.map((pr) => `(${pr.line}) #${pr.number}`).join(' · ')
  const linkBlock = `${LINKS_START}\nRelease lines: ${links}\n${LINKS_END}`

  for (const pr of pullRequests) {
    const currentBody = (pr.body ?? '').trim()
    const linkedBody = [withoutLinks(currentBody), linkBlock].filter(Boolean).join('\n\n')
    if (linkedBody !== currentBody) gh('pr', 'edit', pr.number, '--body', linkedBody)
  }
}

function withoutLinePrefix(title) {
  return title.replace(/^\([^)]*\)\s*/, '')
}

function withoutLinks(body) {
  const start = body.indexOf(LINKS_START)
  const end = body.indexOf(LINKS_END)
  if (start === -1 || end === -1) return body.trim()
  return (body.slice(0, start) + body.slice(end + LINKS_END.length)).trim()
}

function refExists(ref) {
  return gitSucceeds('rev-parse', '--verify', '--quiet', `${ref}^{commit}`)
}

function git(...args) {
  return runOrFail('git', args)
}

function gitSucceeds(...args) {
  return run('git', args).status === 0
}

function gh(...args) {
  return runOrFail('gh', args.map(String))
}

function runOrFail(command, args) {
  const result = run(command, args)
  if (result.status !== 0) {
    throw new UserError(`${command} ${args.join(' ')}\n${(result.stderr || result.stdout).trim()}`)
  }
  return result.stdout.trim()
}

function run(command, args) {
  const result = spawnSync(command, args, { encoding: 'utf8' })
  if (result.error) throw new UserError(`Could not run ${command}: ${result.error.message}`)
  return result
}

try {
  backport(process.argv.slice(2))
} catch (error) {
  if (!(error instanceof UserError)) throw error
  console.error(`\n${error.message}`)
  process.exitCode = 1
}
