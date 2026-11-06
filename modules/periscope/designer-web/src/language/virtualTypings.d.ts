/** The typings the build collects, for tests: the Designer loads them from the typings asset. */
declare module 'virtual:periscope-typings' {
  const typings: {
    libs: string[]
    roots: string[]
    files: Record<string, string>
  }
  export default typings
}
