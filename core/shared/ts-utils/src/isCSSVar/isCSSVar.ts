export default function isCSSVar(string: string): boolean {
    return /^ *var\(--/.test(string)
}
