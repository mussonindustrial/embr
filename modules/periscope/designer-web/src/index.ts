import { CssModuleCompiler, TypeScriptCompiler, Compiler } from './compiler'

export * from './compiler'

const CompilerRegistry = {
  typescript: TypeScriptCompiler,
  css: CssModuleCompiler,
}
type CompilerKey = keyof typeof CompilerRegistry
type CompilerMethod = keyof Compiler

export const invoke = async function (
  compiler: CompilerKey,
  method: CompilerMethod,
  payloadJson: string
) {
  const payload = JSON.parse(payloadJson)

  const service = CompilerRegistry[compiler]
  if (!service) {
    throw new Error(`Unknown compiler: ${compiler}`)
  }

  const fn = service[method]
  if (typeof fn !== 'function') {
    throw new Error(`Unknown compiler method: ${compiler}.${method}`)
  }

  const result = await fn(payload)
  return JSON.stringify(result)
}
