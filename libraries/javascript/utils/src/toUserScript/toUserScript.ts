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
  globals = {}
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

  if (signature.length == 0) {
    const f = Function(...Object.keys(globals), body).bind(
      thisArg,
      ...Object.values(globals)
    )

    const script = f()
    script.signature = signature
    script.run = f()
    script.runNamed = f()
    return script
  } else {
    const f = Function(...Object.keys(globals), ...signature, body).bind(
      thisArg,
      ...Object.values(globals)
    )

    const script = (...args: unknown[]) => f(...args)() as UserScript
    script.signature = signature
    script.run = (...args: unknown[]) => f(...args)()
    script.runNamed = (params: UserScriptParams = {}) =>
      f.call(thisArg, ...signature.map((key) => params[key]))()

    return script
  }
}
