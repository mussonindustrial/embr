import ts from 'typescript'

const INDENT = '    '
const OPENING = ['(', '<', '[', '{']
const CLOSING = [')', '>', ']', '}']

export function signatureText(parts: ts.SymbolDisplayPart[] = []) {
  const start = parts.findIndex((_, index) => startsParameters(parts, index))
  if (start < 0) return ts.displayPartsToString(parts)

  let depth = 0
  let text = ts.displayPartsToString(parts.slice(0, start)) + '(\n' + INDENT
  for (let index = start + 1; index < parts.length; index++) {
    const part = parts[index]
    if (isPunctuation(part, OPENING)) depth++
    if (isPunctuation(part, CLOSING) && depth-- === 0) {
      return text + '\n' + ts.displayPartsToString(parts.slice(index))
    }
    if (depth === 0 && part.text === ',') {
      text += ',\n' + INDENT
      if (parts[index + 1]?.kind === 'space') index++
      continue
    }
    text += part.text
  }
  return text
}

function startsParameters(parts: ts.SymbolDisplayPart[], index: number) {
  const next = parts[index + 1]
  return (
    parts[index].text === '(' &&
    depthAt(parts, index) === 0 &&
    (next?.kind === 'parameterName' || next?.text === '...')
  )
}

function depthAt(parts: ts.SymbolDisplayPart[], index: number) {
  return parts
    .slice(0, index)
    .reduce(
      (depth, part) =>
        depth +
        (isPunctuation(part, OPENING) ? 1 : 0) -
        (isPunctuation(part, CLOSING) ? 1 : 0),
      0
    )
}

function isPunctuation(part: ts.SymbolDisplayPart, texts: string[]) {
  return part.kind === 'punctuation' && texts.includes(part.text)
}
