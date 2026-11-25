import { describe, expect, it, vi } from 'vitest'

const { perspectiveClientMock } = vi.hoisted(() => {
  const placeholder: unknown = new Proxy(class {}, {
    get: (target, key) =>
      key in target ? Reflect.get(target, key) : placeholder,
  })

  const perspectiveClientMock = () =>
    new Proxy(
      {},
      {
        has: () => true,
        get: (_target, key) => (key === 'then' ? undefined : placeholder),
      }
    )

  return { perspectiveClientMock }
})

vi.mock('@inductiveautomation/perspective-client', perspectiveClientMock)
vi.mock('@inductiveautomation/perspective-designer', () => ({}))

describe('project', () => {
  it('loads in a session, where the Designer library does not exist', async () => {
    const project = import('../../../../src/components/embedding/project')

    await expect(project).resolves.toBeDefined()
  })
})
