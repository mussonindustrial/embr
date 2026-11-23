import { messageOf } from '@embr-js/utils'
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

type Reply = (id: string, success: boolean, value: string) => void

let sendReply: Reply | undefined

export const connect = function (reply: Reply) {
  sendReply = reply
}

export const dispatch = function (
  id: string,
  service: string,
  method: string,
  payloadJson: string
) {
  const onSuccess = (result: string) => {
    sendReply?.(id, true, result)
  }
  const onFailure = (error: Error) => {
    sendReply?.(id, false, messageOf(error))
  }

  invoke(service, method, payloadJson).then(onSuccess, onFailure)
}
