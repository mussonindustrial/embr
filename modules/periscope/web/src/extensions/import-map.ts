const id = 'periscope-import-map'

export function installImportMap() {
  const existingImportMap = document.getElementById(id)
  if (!existingImportMap) {
    const importMap = {
      imports: {
        '@library/': '/data/periscope/web-library/',
      },
    }

    const element = document.createElement('script')
    element.id = id
    element.type = 'importmap'
    element.textContent = JSON.stringify(importMap, null, 2)
    document.head.append(element)
  }
}
