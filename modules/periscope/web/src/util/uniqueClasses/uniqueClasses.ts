/**
 * Coalesces CSS class names into a unique list.
 * @param classNames a space-separated string or list of class names
 * @returns a list of unique class names
 */
export default function uniqueClasses(
  classNames: string | string[] = ''
): string[] {
  const classes = (
    Array.isArray(classNames) ? classNames : classNames.split(' ')
  )
    .map((c) => c.trim())
    .filter((c) => !!c)

  const unique: string[] = []

  classes.forEach((c) => {
    if (unique.indexOf(c) < 0) unique.push(c)
  })

  return unique
}
