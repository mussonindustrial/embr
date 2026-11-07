import ts from 'typescript'

const MDN_REFERENCE = /^\s*\*\s*\[MDN Reference]\(.*\)\s*$/gm

// Scan with TypeScript so strings and template literal types keep their text.
export function compactTypings(text: string, docs: boolean) {
  const scanner = ts.createScanner(
    ts.ScriptTarget.Latest,
    false,
    ts.LanguageVariant.Standard,
    text
  )
  const templateDepths: number[] = []
  let braceDepth = 0
  const lines: string[] = []
  let line = ''

  for (
    let kind = scanner.scan();
    kind !== ts.SyntaxKind.EndOfFileToken;
    kind = scanner.scan()
  ) {
    if (kind === ts.SyntaxKind.CloseBraceToken) {
      if (templateDepths.at(-1) === braceDepth) {
        kind = scanner.reScanTemplateToken(false)
        if (kind === ts.SyntaxKind.TemplateTail) templateDepths.pop()
      } else {
        braceDepth--
      }
    } else if (kind === ts.SyntaxKind.OpenBraceToken) {
      braceDepth++
    } else if (kind === ts.SyntaxKind.TemplateHead) {
      templateDepths.push(braceDepth)
    }
    line = append(line, kind, scanner.getTokenText(), docs)
    if (line.length > 1 && line.endsWith('\n')) {
      lines.push(line.slice(0, -1))
      line = '\n'
    }
  }
  return (lines.join('') + line).trim() + '\n'
}

function append(
  output: string,
  kind: ts.SyntaxKind,
  token: string,
  docs: boolean
) {
  switch (kind) {
    case ts.SyntaxKind.NewLineTrivia:
      return output.endsWith('\n') ? output : output.trimEnd() + '\n'
    case ts.SyntaxKind.WhitespaceTrivia:
      return /[\n ]$/.test(output) ? output : output + ' '
    case ts.SyntaxKind.MultiLineCommentTrivia:
      return docs ? output + compactComment(token) : output
    case ts.SyntaxKind.SingleLineCommentTrivia:
      return docs || token.startsWith('///') ? output + token : output
    default:
      return output + token
  }
}

const EMPTY_DOC_COMMENT = /^\/\*\*[\s*]*\*\/$/

function compactComment(comment: string) {
  const compact = comment
    .replace(MDN_REFERENCE, '')
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .filter((line, index, lines) => !isBlankBeforeEnd(line, lines[index + 1]))
    .map((line) => (line.startsWith('*') ? ' ' + line : line))
    .join('\n')
  return EMPTY_DOC_COMMENT.test(compact) ? '' : compact
}

function isBlankBeforeEnd(line: string, next: string | undefined) {
  return line === '*' && next === '*/'
}
