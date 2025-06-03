import { isAsyncFunction } from '../index'

export type UserScriptParams = Record<string, unknown>
export type NormalFunction = (...args: unknown[]) => unknown
export type NamedParameterFunction = (params?: UserScriptParams) => unknown
export type UserScript = NormalFunction & {
  signature: string[]
  run: NormalFunction
  runNamed: NamedParameterFunction
}

const isFunctionRegex = /^\s*(?:async\s*)?\(([^)]*?)\)\s*=>\s*([\s\S]*)$/
export default function toUserScript(
  string: string,
  thisArg = {},
  globals: Record<string, unknown> | (() => Record<string, unknown>) = {}
): UserScript {
  const match = string.match(isFunctionRegex)
  if (!match) {
    throw new Error('invalid function definition')
  }

  const async = isAsyncFunction(string) ? 'async' : ''

  let signature: string[] = []
  if (match[1].length > 0) {
    signature = match[1].split(',').map((param) => param.trim())
  }
  const body = `"use strict"; return ${async} () => ${match[2].trim()}`

  const compiled = Function(
    '__globals',
    ...signature,
    `
      with (__globals) {
        ${body}
      }
    `
  )

  function resolveGlobals(): Record<string, unknown> {
    return typeof globals === 'function' ? globals() : globals
  }

  const run = (...args: unknown[]) => {
    const resolvedGlobals = resolveGlobals()
    return compiled.call(thisArg, resolvedGlobals, ...args)()
  }

  const runNamed = (params: UserScriptParams = {}) => {
    const args = signature.map((key) => params[key])
    return run(...args)
  }

  const script = ((...args: unknown[]) => run(...args)) as UserScript
  script.signature = signature
  script.run = run
  script.runNamed = runNamed

  return script
}
