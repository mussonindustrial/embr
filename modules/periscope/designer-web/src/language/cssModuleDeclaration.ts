const commentsAndStrings =
  /\/\*[\s\S]*?\*\/|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'/g
const classSelector = /\.(-?[_a-zA-Z][_a-zA-Z0-9-]*)/g
const globalBlock = /:global\s*\([^)]*\)/g
const urls = /url\([^)]*\)/g

function toCamelCase(name: string) {
  return name.replace(/-+(\w)/g, (_, letter: string) => letter.toUpperCase())
}

function classNames(source: string) {
  const css = source
    .replace(commentsAndStrings, '')
    .replace(globalBlock, '')
    .replace(urls, '')
  const names = [...css.matchAll(classSelector)].map((match) => match[1])
  return [...new Set(names)].sort()
}

// Matches the compiler's `localsConvention: 'dashes'`.
function members(className: string) {
  return [...new Set([className, toCamelCase(className)])].map(
    (name) =>
      `  /** The class name for \`.${className}\`. */\n  readonly ${JSON.stringify(name)}: string`
  )
}

export function cssModuleDeclaration(source: string) {
  return [
    '/** The class names this module defines, by their selector and in camelCase. */',
    `declare const styles: {\n${classNames(source).flatMap(members).join('\n')}\n}`,
    '/** The CSS text, as compiled. */',
    'export declare const cssText: string',
    '/** The stylesheet built from the CSS text, applied to the document when the module loads. */',
    'export declare const stylesheet: CSSStyleSheet',
    '/** Adopts the stylesheet into a document or shadow root, once. */',
    'export declare function applyStyle(target?: Document | ShadowRoot): CSSStyleSheet',
    'export { styles }',
    'export default styles',
    '',
  ].join('\n')
}
