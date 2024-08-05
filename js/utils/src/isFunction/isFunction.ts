const expression = /^[\s\S]*?\(([^)]*?)\)[\s\S]*?=>[\s\S]?([\s\S]*)$/
export default function isFunction(string: string): boolean {
    return expression.test(string)
}
