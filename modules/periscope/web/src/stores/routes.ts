const BASE = '/data/embr-periscope'

export const routes = {
  clientResourceHash: (project: string) =>
    `${BASE}/client-resource/${project}/hash.json`,
  clientBundle: (project: string, hash: string) =>
    `${BASE}/client-bundle/${project}/${encodeURIComponent(hash)}`,
  clientResource: (project: string, hash: string, path: string) =>
    `${BASE}/client-resource/${project}/${hash}/${encodeURI(path)}`,
  systemModule: (hash: string, name: string) =>
    `${BASE}/system-module/${hash}/${name}`,
  globalModule: (hash: string, name: string) =>
    `${BASE}/global-module/${hash}/${name}`,
}
