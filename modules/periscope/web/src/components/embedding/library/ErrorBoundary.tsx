import React, { ReactNode } from 'react'

type ErrorBoundaryProps = {
  resetKey: unknown
  fallback: (message: string) => ReactNode
  children: ReactNode
}

type ErrorBoundaryState = { error?: Error }

export class ErrorBoundary extends React.Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  state: ErrorBoundaryState = {}

  static getDerivedStateFromError(error: Error) {
    return { error }
  }

  componentDidUpdate(previous: ErrorBoundaryProps) {
    if (this.state.error && previous.resetKey !== this.props.resetKey) {
      this.setState({ error: undefined })
    }
  }

  render() {
    const { error } = this.state

    if (error == null) {
      return this.props.children
    }

    return this.props.fallback(`Error rendering component: ${error.message}`)
  }
}
