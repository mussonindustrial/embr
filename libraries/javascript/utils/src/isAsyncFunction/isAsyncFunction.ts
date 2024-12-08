const expression = /^\s*async\s*\(([^)]*?)\)\s*=>/
export default function isAsyncFunction(string: string): boolean {
  return expression.test(string)
}
