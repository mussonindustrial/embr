export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

export default function toFunction(string: string, globals = {}) {
    const regex = /^\s*\(([^)]*)\)\s*=>\s*(.*)$/
    const match = string.match(regex)
    if (match) {
        const signature = match[1].split(',').map((param) => param.trim())
        const body = match[2].trim()

        return (params: UserScriptParams = {}) => {
            params.length === signature.length
            return Function(
                ...signature,
                ...Object.keys(globals),
                body
            )(...Object.values(params), ...Object.values(globals))
        }
    }
    return () => {
        return 'invalid function definition'
    }
}
