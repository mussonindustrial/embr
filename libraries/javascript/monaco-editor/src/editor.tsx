import React, { useCallback } from 'react'
import { createRoot } from 'react-dom/client'

import 'normalize.css'
import { shikiToMonaco } from '@shikijs/monaco'
import { createHighlighter } from 'shiki/bundle/web'
import { debounce } from 'lodash'

import Editor, { EditorProps, Monaco, loader } from '@monaco-editor/react'
import * as monaco from 'monaco-editor'
loader.config({ monaco })

declare global {
  interface Window {
    editor: {
      ref?: monaco.editor.IStandaloneCodeEditor
      onMount?: EditorProps['onMount']
      onChange?: EditorProps['onChange']
      defaultValue: string
    }
  }
}

window.editor = {
  defaultValue: '',
}

function setEditor(editor: monaco.editor.IStandaloneCodeEditor) {
  window.editor.ref = editor
}

function App() {
  const onMount = useCallback(
    (editor: monaco.editor.IStandaloneCodeEditor, monaco: Monaco) => {
      setEditor(editor)
      window.editor.onMount?.call(window, editor, monaco)

      void (async () => {
        const ADDITIONAL_LANGUAGES = [
          'jsx',
          'tsx',
        ] as const satisfies Parameters<typeof createHighlighter>[0]['langs']

        for (const lang of ADDITIONAL_LANGUAGES) {
          monaco.languages.register({ id: lang })
        }

        const highlighter = await createHighlighter({
          themes: ['github-dark'],
          langs: ADDITIONAL_LANGUAGES,
        })

        // https://shiki.matsu.io/packages/monaco
        shikiToMonaco(highlighter, monaco)
      })()
    },
    []
  )

  const notifyDesigner = useCallback(
    debounce(
      (
        value: string | undefined,
        event: monaco.editor.IModelContentChangedEvent
      ) => {
        window.editor.onChange?.call(window, value, event)
      },
      1000,
      { leading: false, trailing: true }
    ),
    []
  )

  const onChange = useCallback(
    (
      value: string | undefined,
      event: monaco.editor.IModelContentChangedEvent
    ) => {
      notifyDesigner(value, event)
    },
    []
  )

  const options = {
    selectOnLineNumbers: true,
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          flexBasis: 'auto',
          color: 'var(--vscode-menu-foreground)',
          background: 'var(--vscode-menu-background)',
          fontFamily: 'Consolas, "Courier New", monospace',
          padding: '0.5rem',
          marginBottom: '0.5rem',
          borderBottom: 'var(--vscode-menu-separatorBackground) 1px solid',
        }}
      >
        <div>Component Definition</div>
        <div
          style={{
            color: 'var(--vscode-textSeparator-foreground)',
            lineHeight: '1.15rem',
            fontSize: '0.80rem',
            marginLeft: 'auto',
          }}
        >
          (jsx)
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
        <Editor
          defaultLanguage="jsx"
          defaultValue={window.editor.defaultValue}
          defaultPath={'file:///main.jsx'}
          onMount={onMount}
          onChange={onChange}
          theme="github-dark"
          options={options}
        />
      </div>
    </div>
  )
}

const domNode = document.getElementById('root')
if (domNode) {
  const root = createRoot(domNode)
  root.render(<App />)
}
