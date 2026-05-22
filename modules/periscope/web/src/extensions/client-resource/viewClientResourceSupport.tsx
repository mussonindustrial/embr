import { View } from '@inductiveautomation/perspective-client'
import React, { ReactElement } from 'react'
import { ClientResourceComponent } from '@/extensions'
import { unDollarQualify } from '@/util'

const SUPPORT_FLAG = Symbol.for('embr.client-resource-support')

type ViewLifecycle = (...args: unknown[]) => unknown

type PatchedView = View & {
  [SUPPORT_FLAG]?: boolean
  render: (...args: unknown[]) => ReactElement | null
  componentDidMount?: ViewLifecycle
  componentDidUpdate?: ViewLifecycle
  componentWillUnmount?: ViewLifecycle
}

const clientResourcePrefix = '@/'

function isClientResourcePath(path: unknown): path is string {
  return typeof path === 'string' && path.startsWith(clientResourcePrefix)
}

export function installViewClientResourceSupport(): void {
  const proto = View.prototype as PatchedView

  if (proto[SUPPORT_FLAG]) return
  proto[SUPPORT_FLAG] = true

  const {
    render,
    componentDidMount,
    componentDidUpdate,
    componentWillUnmount,
  } = proto

  proto.render = function patchedRender(...args): ReactElement | null {
    const parent = this.props.parent

    const writeProps = (path: string, value: unknown) => {
      const tree = parent?.props
      if (tree == null) {
        console.warn('No parent props found')
        return
      }

      tree.write(`params.${path}`, value)
    }

    if (!isClientResourcePath(this.props?.resourcePath)) {
      return render.apply(this, args)
    }

    const resourcePath = this.props?.resourcePath.slice(
      clientResourcePrefix.length
    )

    return (
      <ClientResourceComponent
        resourcePath={resourcePath}
        props={unDollarQualify(this.props?.params) as Record<string, never>}
        writeProps={writeProps}
        thisArg={parent}
      />
    )
  }

  function maybeSkip(original?: ViewLifecycle): ViewLifecycle {
    return function (this: View, ...args: unknown[]) {
      if (isClientResourcePath(this.props?.resourcePath)) {
        return
      }

      return original?.apply(this, args)
    }
  }

  proto.componentDidMount = maybeSkip(componentDidMount)
  proto.componentDidUpdate = maybeSkip(componentDidUpdate)
  proto.componentWillUnmount = maybeSkip(componentWillUnmount)
}
