<h2 align="center"><i>Embr</i></h2>
<h3 align="center">Modules for Ignition by Inductive Automation 🔥</h3>
<h3 align="center">
  <a href="https://mussonindustrial.com">
    <picture>
      <source height="48" media="(prefers-color-scheme: dark)" srcset="https://cdn.mussonindustrial.com/files/public/images/logoWhite.svg">
      <img height="48" alt="Musson Industrial" src="https://cdn.mussonindustrial.com/files/public/images/logo.svg">
    </picture>
  </a>
</h3>
<h1 align="center">
  <a href="https://github.com/mussonindustrial/embr/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/mussonindustrial/embr/build.yml?branch=8.1%2Fmain&logo=github&label=Build">
  </a>
  <a href="http://kotlinlang.org">
    <img src="https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin">
  </a>
  <a href="https://inductiveautomation.com/">
    <img src="https://img.shields.io/badge/Ignition-8.1.33+-rebeccapurple.svg">
  </a>
  <a href="https://github.com/mussonindustrial/embr/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg">
  </a>
  <br>
  <a href="https://docs.mussonindustrial.com/">
    <img src="https://img.shields.io/badge/Documentation-docs.mussonindustrial.com-white.svg?logo=docusaurus&style=for-the-badge">
  </a>
</h1>

## Directory

<div align="center">

| Module                                     | Description                                                                           | Release Notes                                                                                                                                                                                                |
|--------------------------------------------|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Charts](modules/charts/README.md)         | A collection of enhanced Perspective charting components.                             | [![version](https://img.shields.io/github/package-json/v/mussonindustrial/embr/8.1%2Fmain?filename=modules%2Fcharts%2Fpackage.json&style=for-the-badge&label=version)](modules/charts/CHANGELOG.md)          |
| [SSE](modules/sse/README.md)               | Provides an API for high-speed streaming of tag changes via SSE (server-sent events). | [![version](https://img.shields.io/github/package-json/v/mussonindustrial/embr/8.1%2Fmain?filename=modules%2Fsse%2Fpackage.json&style=for-the-badge&label=version)](modules/sse/CHANGELOG.md)                |
| [Periscope](modules/periscope/README.md)   | Design extensions and enhancements for Perspective.                                   | [![version](https://img.shields.io/github/package-json/v/mussonindustrial/embr/8.1%2Fmain?filename=modules%2Fperiscope%2Fpackage.json&style=for-the-badge&label=version)](modules/event-stream/CHANGELOG.md) |
| [Thermodynamics](modules/thermo/README.md) | Scripting functions for computing thermodynamic properties.                           | [![version](https://img.shields.io/github/package-json/v/mussonindustrial/embr/8.1%2Fmain?filename=modules%2Fthermo%2Fpackage.json&style=for-the-badge&label=version)](modules/thermo/CHANGELOG.md)          |

</div>

## Build

```sh
git clone https://github.com/mussonindustrial/embr
cd embr
yarn install --frozen-lockfile
./gradlew build
```

## Sponsors

Maintenance of this project is made possible by all the [contributors] and [sponsors].
If you'd like to sponsor this project and have your avatar or company logo appear below [click here](https://github.com/sponsors/mussonindustrial). 💖

<h1 align="center">
  <a href="https://mussonindustrial.com/">
    <img src="https://avatars.githubusercontent.com/u/84413538?s=100" alt="Musson Industrial" />
  </a>
  <a href="https://artekis.io/">
    <img src="https://avatars.githubusercontent.com/u/89804242?s=100" alt="Artekis" />
  </a>
  <a href="https://github.com/thewebpleb/">
    <img src="https://avatars.githubusercontent.com/u/86393727?s=100" alt="thewebpleb" />
  </a>
</h1>

## Links

- [License (MIT)](LICENSE)
- [Musson Industrial](https://mussonindustrial.com/)
- [Inductive Automation](https://inductiveautomation.com/)

[embr]: https://github.com/mussonindustrial/embr
[contributors]: https://github.com/mussonindustrial/embr/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
[chartjs]: https://www.chartjs.org/
