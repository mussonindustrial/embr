import { CssModuleCompiler, TypeScriptCompiler } from './compiler'
import { languageService } from './language'

export * from './compiler'

const ServiceRegistry: Record<string, object> = {
  typescript: TypeScriptCompiler,
  css: CssModuleCompiler,
  language: languageService,
}

export const invoke = async function (
  service: string,
  method: string,
  payloadJson: string
) {
  const payload = JSON.parse(payloadJson)

  const target = ServiceRegistry[service]
  if (!target) {
    throw new Error(`Unknown service: ${service}`)
  }

  const fn = (target as Record<string, unknown>)[method]
  if (typeof fn !== 'function') {
    throw new Error(`Unknown service method: ${service}.${method}`)
  }

  const result = await fn(payload)
  return JSON.stringify(result)
}
