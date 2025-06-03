export type CompilerRequest = {
  source: string
  path: string
}

export type Position = {
  line: number
  column: number
  offset: number
}

export type Range = {
  start: Position
  end: Position
}

export type DiagnosticSeverity = 'error' | 'warning' | 'info'

export type Diagnostic = {
  message: string
  severity: DiagnosticSeverity
  range?: Range
  code?: string
  source?: string
  stack?: string
}

export type CompileResult = {
  success: boolean
  output?: string
  diagnostics: Diagnostic[]
}

export type FormatResult = {
  success: boolean
  output: string
  cursor: number
  diagnostics: Diagnostic[]
}

export interface Compiler {
  compile(request: CompilerRequest): Promise<CompileResult>

  format(
    request: CompilerRequest & {
      cursor: number
    }
  ): Promise<FormatResult>
}
