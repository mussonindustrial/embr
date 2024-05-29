export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

export default function toFunction(string: string, globals = {}) {
    const regex = /^\s*\(([^)]*)\)\s*=>\s*(.*)$/
    const match = string.match(regex)
    if (match) {
        let signature: string[] = []
        if (match[1].length > 0) {
            signature = match[1].split(',').map((param) => param.trim())
        }
        const body = match[2].trim()

        return (params: UserScriptParams = {}) => {
            if (signature.length == 0) {
                return Function(
                    ...Object.keys(globals),
                    body
                )(...Object.values(globals))
            } else {
                params.length === signature.length
                return Function(
                    ...signature,
                    ...Object.keys(globals),
                    body
                )(...Object.values(params), ...Object.values(globals))
            }
        }
    }
    return () => {
        return 'invalid function definition'
    }
}
