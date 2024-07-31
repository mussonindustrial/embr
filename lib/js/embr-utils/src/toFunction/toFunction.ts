export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

const expression = /^[\s\S]*?\(([^)]*?)\)[\s\S]*?=>[\s\S]?([\s\S]*)$/
export default function toFunction(string: string, globals = {}) {
    const match = string.match(expression)
    if (match) {
        let signature: string[] = []
        if (match[1].length > 0) {
            signature = match[1].split(',').map((param) => param.trim())
        }
        const body = match[2].trim()

        if (signature.length == 0) {
            const f = Function(...Object.keys(globals), body)
            return () => f(...Object.values(globals))
        } else {
            const f = Function(...signature, ...Object.keys(globals), body)
            return (params: UserScriptParams = {}) => {
                params.length === signature.length
                return f(...Object.values(params), ...Object.values(globals))
            }
        }
    }
    return () => {
        return 'invalid function definition'
    }
}
