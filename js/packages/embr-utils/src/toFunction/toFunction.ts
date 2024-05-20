export type UserScriptParams = Record<string, unknown>
export type UserScript = (params?: UserScriptParams) => unknown

export default function toFunction(string: string): UserScript {
    string = string.replace(/^ *<script>/, '')
    string = string.replace(/<\/script> *$/, '')

    return (params: UserScriptParams = {}) => {
        return Function(
            ...Object.keys(params),
            string
        )(...Object.values(params))
    }
}
