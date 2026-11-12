import type { ESModule } from '@embr-js/utils/types'
import type {
  ComponentProps,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import type { ReactNode } from 'react'
import { toast } from 'react-toastify'

/** The Perspective props of the React component that renders a client resource. */
export type ReactComponentContext<P = Record<string, unknown>> =
  ComponentProps<{
    resource: { path: string; export?: string }
    props: P
    style: StyleObject
  }>

/** The props the React component passes to the component it renders. */
export type ReactComponentProps<P = Record<string, unknown>> = P & {
  /** Writes a value to the React component's `props`, at a path inside them. */
  writeProps: (path: string, value: unknown) => void
  /** The Perspective props of the React component, also bound as `this`. */
  renderContext: ReactComponentContext<P>
}

/**
 * A component in a client resource, rendered by the React component.
 *
 * `P` is the shape of the React component's `props`. Written as a `function`,
 * the component has the React component's Perspective props as `this`.
 */
export type ReactComponent<P = Record<string, unknown>> = (
  this: ReactComponentContext<P>,
  props: ReactComponentProps<P>
) => ReactNode

type Resources = {
  resource(path: string): Promise<ESModule>
}

export function createPeriscopeApi(resources: Resources) {
  return {
    /** Shows a toast notification in the Perspective session. */
    toast,
    /** Loads another client resource by path, resolving to its module exports. */
    resource: (path: string) => resources.resource(path),
  }
}
