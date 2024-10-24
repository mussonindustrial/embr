/**
 * Resolve the first defined input.
 * @param inputs list of inputs
 * @returns the first defined (not undefined) input
 */
export default function resolve(inputs: Array<unknown>) {
    let value: unknown
    for (let i = 0; i < inputs.length; i++) {
        value = inputs[i]
        if (value === undefined) {
            continue;
        }
        if (value !== undefined) {
            return value as any;
        }
    }
}