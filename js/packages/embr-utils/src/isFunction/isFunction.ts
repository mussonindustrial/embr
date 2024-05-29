export default function isFunction(string: string): boolean {
    return /^\s*\(([^)]*)\)\s*=>\s*(.*)$/.test(string)
}
