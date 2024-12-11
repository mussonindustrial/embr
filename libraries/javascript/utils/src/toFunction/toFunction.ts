import { isAsyncFunction } from '../index'

export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

const isFunctionRegex = /^\s*(?:async\s*)?\(([^)]*?)\)\s*=>\s*([\s\S]*)$/
export default function toFunction(string: string, globals = {}) {
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
    const f = Function(...Object.keys(globals), body)
    return () => f(...Object.values(globals))()
  } else {
    const f = Function(...Object.keys(globals), ...signature, body)
    return (params: UserScriptParams = {}) => {
      return f(...Object.values(globals), ...Object.values(params))()
    }
  }
}
