import { compiler as cssCompiler } from './css/compiler'
import { compiler as typescriptCompiler } from './typescript/compiler'
import { languageService } from './typescript/language'

const services: Record<string, Record<string, object>> = {
  typescript: { compiler: typescriptCompiler, language: languageService },
  css: { compiler: cssCompiler },
}

export const invoke = async function (
  service: string,
  method: string,
  payloadJson: string
) {
  const payload = JSON.parse(payloadJson)

  const [language, capability] = service.split('.')
  const target = services[language]?.[capability]
  if (!target) {
    throw new Error(`Unknown service: ${service}`)
  }

  const fn = (target as Record<string, unknown>)[method]
  if (typeof fn !== 'function') {
    throw new Error(`Unknown service method: ${service}.${method}`)
  }

  const result = await fn(payload)
  return JSON.stringify(result ?? null)
}
