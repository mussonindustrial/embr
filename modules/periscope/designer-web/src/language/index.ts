import { completionDetails, completions } from './completions'
import { codeFixes, diagnostics } from './diagnostics'
import {
  formatOnType,
  jsxClosingTag,
  linkedEditing,
  organizeImports,
} from './editing'
import { classifications, inlayHints, quickInfo, signatureHelp } from './hints'
import {
  definition,
  highlights,
  outline,
  references,
  symbols,
} from './navigation'
import { setTypings, removeFile, setFiles, updateFile, warmUp } from './project'

export * from './protocol'

export const languageService = {
  setTypings,
  formatOnType,
  linkedEditing,
  setFiles,
  updateFile,
  removeFile,
  warmUp,
  jsxClosingTag,
  outline,
  symbols,
  inlayHints,
  classifications,
  diagnostics,
  completions,
  completionDetails,
  quickInfo,
  definition,
  references,
  highlights,
  signatureHelp,
  codeFixes,
  organizeImports,
}
