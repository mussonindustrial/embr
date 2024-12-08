const expression = /^\s*(?:async\s*)?\(([^)]*?)\)\s*=>\s*([\s\S]*)$/
export default function isFunction(string: string): boolean {
  return expression.test(string)
}
