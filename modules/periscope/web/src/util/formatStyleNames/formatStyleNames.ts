/**
 * Convert class names to Perspective Style Class notation.
 * @param input
 * @returns
 */
export default function formatStyleNames(
  classNames: string | string[] | undefined
): string {
  if (classNames === undefined) {
    return ''
  }

  return (Array.isArray(classNames) ? classNames : classNames.split(' '))
    .map((c) => c.trim())
    .filter((c) => !!c)
    .map((i) => 'psc-' + i.trim())
    .join(' ')
}
