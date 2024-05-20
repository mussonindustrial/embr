export default function readCSSVar(element: Element, property: string) {
    const propertyName = property.match(/(?<=var\()[\w-]+/)
    if (propertyName == undefined) {
        return new Error(`\`${property}\` is not a valid css property.`)
    }
    return window.getComputedStyle(element).getPropertyValue(propertyName[0])
}
