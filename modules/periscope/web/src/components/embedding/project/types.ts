import type { ComponentProps } from '@inductiveautomation/perspective-client'
import type { ComponentEvents } from '@embr-js/perspective-client'

import type { Values } from '@/api'

export type PerspectiveProps = ComponentProps<
  Values & { events?: ComponentEvents }
>
