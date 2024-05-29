import { isDate } from 'lodash'

export type PropTransform<T, U> = (prop: T) => U

function applyTransforms(
    prop: unknown,
    transforms: PropTransform<unknown, unknown>[]
): unknown {
    let value = prop
    transforms.forEach((transform) => (value = transform(value)))
    return value
}

export default function transformProps(
    props: unknown,
    transforms: PropTransform<unknown, unknown>[]
): unknown {
    if (isDate(props)) return applyTransforms(props, transforms)
    if (Array.isArray(props))
        return props.map((p) => transformProps(p, transforms))
    if (props && typeof props === 'object')
        return Object.fromEntries(
            Object.entries(props).map(([k, v]) => [
                k,
                transformProps(v, transforms),
            ])
        )
    return applyTransforms(props, transforms)
}
