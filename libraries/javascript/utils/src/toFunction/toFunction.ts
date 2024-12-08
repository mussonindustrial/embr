import { isAsyncFunction } from '../index'

export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

const isFunctionRegex = /^\s*(?:async\s*)?\(([^)]*?)\)\s*=>\s*([\s\S]*)$/
export default function toFunction(string: string, globals = {}) {
  const match = string.match(isFunctionRegex)
  if (!match) {
    throw new Error('invalid function definition')
  }

  const FunctionConstructor = isAsyncFunction(string)
    ? // eslint-disable-next-line @typescript-eslint/no-empty-function
      async function () {}.constructor
    : Function

  let signature: string[] = []
  if (match[1].length > 0) {
    signature = match[1].split(',').map((param) => param.trim())
  }
  const body = match[2].trim()

  if (signature.length == 0) {
    const f = FunctionConstructor(...Object.keys(globals), body)
    return () => f(...Object.values(globals))
  } else {
    const f = FunctionConstructor(...Object.keys(globals), ...signature, body)
    return (params: UserScriptParams = {}) => {
      return f(...Object.values(globals), ...Object.values(params))
    }
  }
}
