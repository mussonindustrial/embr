import React, { ComponentType, Suspense, lazy } from 'react'
import { ViewStateDisplay } from '@inductiveautomation/perspective-client'
import { waitForClientResourceStore } from '@/util'

type ClientResourceComponentProps = {
  resourcePath: string
  component?: string
  props?: Record<string, unknown>
  writeProps?: PropsWriter
  thisArg?: unknown
}

type PropsWriter = (path: string, value: unknown) => void

type ComponentKey = string

const componentCache = new Map<ComponentKey, ComponentType<unknown>>()

function getClientResourceComponent(
  resourcePath: string,
  component: string,
  thisArg?: unknown
) {
  const key = `${resourcePath}:${component}`
  let Component = componentCache.get(key)

  if (!Component) {
    Component = lazy(async () => {
      const clientResourceStore = await waitForClientResourceStore()
      const resource = await clientResourceStore.resource(resourcePath)

      const reference = resource[component] as React.FC
      if (reference == null) {
        const fallback: React.FC = () => (
          <FailedToLoadComponent message="No component reference was found." />
        )
        return {
          default: fallback,
        }
      }

      return {
        default: reference.bind(thisArg),
      }
    }) as ComponentType<unknown>

    componentCache.set(key, Component)
  }

  return Component
}

function FailedToLoadComponent({ message }: { message: string }) {
  return (
    <div>
      <ViewStateDisplay
        primaryMessage="Failed to Load Component"
        secondaryMessage={message}
        icon={
          <svg className="view-state-icon">
            <use xlinkHref="/res/perspective/icons/material-icons.svg#warning" />
          </svg>
        }
      />
    </div>
  )
}

export function ClientResourceComponent({
  resourcePath,
  component = 'default',
  props = {},
  writeProps,
  thisArg = undefined,
}: ClientResourceComponentProps) {
  const Component = getClientResourceComponent(resourcePath, component, thisArg)

  const p = {
    ...props,
    writeProps,
  } as Record<string, unknown>

  return (
    <Suspense fallback={<div />}>
      <Component {...p} />
    </Suspense>
  )
}
