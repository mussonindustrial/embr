import postcss, { CssSyntaxError } from 'postcss'
import postcssModules from 'postcss-modules'

import {
  Compiler,
  CompileResult,
  CompilerRequest,
  Diagnostic,
} from '../compiler/protocol'
import * as parserPostCss from 'prettier/parser-postcss'
import { errorDiagnostic, formatWith } from '../compiler/utils'

class InMemoryLoader {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  fetch(_file: string, _relativeTo: string) {
    return Promise.reject(new Error('FS disabled'))
  }
}

function mapError(error: unknown): Diagnostic {
  if (error instanceof CssSyntaxError) {
    return {
      severity: 'error',
      source: 'css-modules',
      message: error.message,
      stack: error.stack,
      range: {
        start: {
          line: error.input?.line ?? -1,
          column: error.input?.column ?? -1,
          offset: error.input?.offset ?? -1,
        },
        end: {
          line: error.input?.endLine ?? -1,
          column: error.input?.endColumn ?? -1,
          offset: error.input?.endOffset ?? -1,
        },
      },
    }
  }

  return errorDiagnostic('css-modules', error)
}

function mapDiagnostic(warning: postcss.Warning): Diagnostic {
  const start = {
    line: warning.line,
    column: warning.column,
    offset: warning.node?.source?.start?.offset ?? -1,
  }

  const end = {
    line: warning.endLine ?? warning.line,
    column: warning.endColumn ?? warning.column,
    offset: warning.node?.source?.end?.offset ?? -1,
  }

  return {
    severity: 'warning',
    source: 'postcss',
    message: warning.text,
    range: {
      start,
      end,
    },
  }
}

export async function compile({
  source,
  path,
}: CompilerRequest): Promise<CompileResult> {
  let classes: Record<string, string> = {}

  try {
    const result = await postcss([
      postcssModules({
        exportGlobals: true,
        localsConvention: 'dashes',
        Loader: InMemoryLoader,
        resolve: () => null,
        getJSON(_cssFilename: string, json: Record<string, string>) {
          classes = json
        },
      }),
    ]).process(source, {
      from: path,
    })

    const css = JSON.stringify(result.css)
    const styles = JSON.stringify(classes)

    const output = `
export const cssText = ${css}

export const stylesheet = new CSSStyleSheet()
stylesheet.replaceSync(cssText)

export function applyStyle(target = document) {
  const sheets = target.adoptedStyleSheets

  if (!sheets.includes(stylesheet)) {
    target.adoptedStyleSheets = [...sheets, stylesheet]
  }

  return stylesheet
}

applyStyle()

export const styles = ${styles}

export default styles
    `.trim()

    return {
      success: true,
      output,
      diagnostics: result.warnings().map(mapDiagnostic),
    }
  } catch (error) {
    return {
      success: false,
      diagnostics: [mapError(error)],
    }
  }
}

export const format = formatWith('css', [parserPostCss])

export const compiler: Compiler = { compile, format }
