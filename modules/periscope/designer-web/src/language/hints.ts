import ts from 'typescript'
import { classify } from './classify'
import { displayText } from './convert'
import { documentationHtml } from './documentation'
import { files, service, textOf } from './project'
import {
  ClassificationsResult,
  FileRequest,
  InlayHintsResult,
  PositionRequest,
  QuickInfoResult,
  SignatureHelpResult,
} from './protocol'
import { signatureText } from './signature'

const inlayHintPreferences: ts.UserPreferences = {
  includeInlayParameterNameHints: 'literals',
  includeInlayParameterNameHintsWhenArgumentMatchesName: false,
  includeInlayVariableTypeHints: true,
  includeInlayVariableTypeHintsWhenTypeMatchesName: false,
  includeInlayFunctionLikeReturnTypeHints: true,
  includeInlayEnumMemberValueHints: true,
}

export async function classifications({
  path,
}: FileRequest): Promise<ClassificationsResult> {
  const fileName = files.fileNameOf(path)
  return { tokens: classify(service, fileName, textOf(fileName)) }
}

export async function quickInfo({
  path,
  offset,
}: PositionRequest): Promise<QuickInfoResult | null> {
  const info = service.getQuickInfoAtPosition(files.fileNameOf(path), offset)
  if (info == null) return null

  return {
    span: info.textSpan,
    display: signatureText(info.displayParts),
    documentationHtml: documentationHtml(info.documentation, info.tags),
  }
}

export async function signatureHelp({
  path,
  offset,
}: PositionRequest): Promise<SignatureHelpResult | null> {
  const help = service.getSignatureHelpItems(files.fileNameOf(path), offset, {})
  if (help == null) return null

  return {
    items: help.items.map((item) => ({
      prefix: displayText(item.prefixDisplayParts),
      parameters: item.parameters.map((p) => displayText(p.displayParts)),
      separator: displayText(item.separatorDisplayParts),
      suffix: displayText(item.suffixDisplayParts),
      documentationHtml: documentationHtml(item.documentation, item.tags),
    })),
    selectedItem: help.selectedItemIndex,
    argumentIndex: help.argumentIndex,
  }
}

export async function inlayHints({
  path,
}: FileRequest): Promise<InlayHintsResult> {
  const fileName = files.fileNameOf(path)
  const span = { start: 0, length: textOf(fileName).length }
  const hints = service
    .provideInlayHints(fileName, span, inlayHintPreferences)
    .filter((hint) => !isArrowReturnType(fileName, hint))
  return {
    hints: hints.map((hint) => ({
      text: hintText(hint),
      position: hint.position,
      kind:
        hint.kind === ts.InlayHintKind.Parameter
          ? 'parameter'
          : hint.kind === ts.InlayHintKind.Enum
            ? 'enum'
            : 'type',
    })),
  }
}

function hintText(hint: ts.InlayHint) {
  return typeof hint.text === 'string'
    ? hint.text
    : (hint.displayParts ?? []).map((part) => part.text).join('')
}

// Arrow return types are noise on callbacks.
function isArrowReturnType(fileName: string, hint: ts.InlayHint) {
  const after = textOf(fileName).slice(hint.position).trimStart()
  return hint.kind === ts.InlayHintKind.Type && after.startsWith('=>')
}
