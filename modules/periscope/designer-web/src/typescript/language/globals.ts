import perspectiveGlobals from '@embr-modules/periscope-web/perspective-globals.json'

export const GLOBAL_MODULES = perspectiveGlobals.globalModules

export const GLOBALS: Record<string, string> = Object.fromEntries(
  GLOBAL_MODULES.map((module) => [module.specifier, module.global])
)

export function isGlobalModule(specifier: string) {
  return GLOBAL_MODULES.some((module) => module.specifier === specifier)
}
