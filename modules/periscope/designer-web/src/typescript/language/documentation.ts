import { Marked } from 'marked'
import ts from 'typescript'

const markdown = new Marked({
  async: false,
  renderer: {
    html: ({ text }) => escapeHtml(text),
  },
})

export function documentationHtml(
  documentation: ts.SymbolDisplayPart[] | undefined,
  tags: ts.JSDocTagInfo[] = []
) {
  const parameters = tags.filter((tag) => tag.name === 'param')
  const returns = tags.find((tag) => RETURNS.includes(tag.name))
  const others = tags.filter(
    (tag) => !parameters.includes(tag) && tag !== returns
  )

  return [
    blockHtml(markdownOf(documentation)),
    parametersHtml(parameters),
    returns ? returnsHtml(returns) : '',
    ...others.map(tagHtml),
  ].join('')
}

const RETURNS = ['returns', 'return']

function parametersHtml(parameters: ts.JSDocTagInfo[]) {
  if (parameters.length === 0) return ''
  const items = parameters.map(({ text = [] }) => {
    const [name, ...description] = text
    return `<li><b>${escapeHtml(name?.text ?? '')}</b> ${inlineHtml(markdownOf(description))}</li>`
  })
  return `<p><b>Parameters</b></p><ul>${items.join('')}</ul>`
}

function returnsHtml(returns: ts.JSDocTagInfo) {
  return `<p><b>Returns: </b>${inlineHtml(markdownOf(returns.text))}</p>`
}

function tagHtml(tag: ts.JSDocTagInfo) {
  const text = markdownOf(tag.text)
  const name = `<i>@${escapeHtml(tag.name)}</i>`
  if (text.includes('\n')) return `<p>${name}</p>${blockHtml(text)}`
  return `<p>${name} ${inlineHtml(text)}</p>`
}

function markdownOf(parts: ts.SymbolDisplayPart[] = []) {
  return parts
    .map((part) => {
      if (part.kind === 'link') return ''
      if (part.kind === 'linkName') return `\`${part.text}\``
      return part.text
    })
    .join('')
    .trim()
}

function blockHtml(text: string) {
  return markdown.parse(text) as string
}

function inlineHtml(text: string) {
  return markdown.parseInline(text.replace(/^[\s-]+/, '')) as string
}

function escapeHtml(text: string) {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}
