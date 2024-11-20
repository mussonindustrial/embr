# [Swiper] Component [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

![swiper.png](../examples/swiper.png)

This module provides a [Swiper] Perspective component.

Swiper is the most modern free and open source mobile touch slider with hardware accelerated transitions and amazing native behavior.

## Component Configuration

Details on how to configure the component can be found on the [Swiper documentation site][Swiper].
All property configurations supported by Swiper are supported on the Perspective component.

### Module Support

Swiper comprises many optional modules; all the currently supported modules are included and bundled with the module.
However, not all the modules have been tested for full functionality.

> **_NOTE:_** If you would like help contribute, testing of modules would be appreciated.

Details on configuring each module are outside the scope of this documentation. Please consult the [Swiper documentation site][Swiper] for complete details.

### Modules

| Name                                                                     | Description                                                                                                                                                                                                | Tested |
|--------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| [Navigation](https://swiperjs.com/swiper-api#navigation)                 | Arrow navigation                                                                                                                                                                                           | ✅      |
| [Pagination](https://swiperjs.com/swiper-api#pagination)                 | Pagination (bullets, progress bar, fraction, custom)                                                                                                                                                       | ✅      |
| [Scrollbar](https://swiperjs.com/swiper-api#scrollbar)                   | Scrollbar                                                                                                                                                                                                  | ✅      |
| [Autoplay](https://swiperjs.com/swiper-api#autoplay)                     | Automatically advance between slides                                                                                                                                                                       | ✅      |
| [Free Mode](https://swiperjs.com/swiper-api#free-mode)                   | Scrolling between slides without snapping                                                                                                                                                                  | ✅      |
| [Fade Effect](https://swiperjs.com/swiper-api#fade-effect)               | Fade transition effect                                                                                                                                                                                     | ✅      |
| [Coverflow Effect](https://swiperjs.com/swiper-api#coverflow-effect)     | Coverflow transition effect                                                                                                                                                                                | ✅      |
| [Flip Effect](https://swiperjs.com/swiper-api#flip-effect)               | Flip transition effect                                                                                                                                                                                     | ✅      |
| [Cube Effect](https://swiperjs.com/swiper-api#cube-effect)               | Cube transition effect                                                                                                                                                                                     | ✅      |
| [Cards Effect](https://swiperjs.com/swiper-api#cards-effect)             | Card transition effect                                                                                                                                                                                     | ✅      |
| [Keyboard Control](https://swiperjs.com/swiper-api#keyboard-control)     | Navigate through slides using a keyboard                                                                                                                                                                   | ✅      |
| [Mousewheel Control](https://swiperjs.com/swiper-api#mousewheel-control) | Navigate through slides using a mouse-wheel                                                                                                                                                                | ✅      |
| [Accessibility](https://swiperjs.com/swiper-api#accessibility-a11y)      | Accessibility Features                                                                                                                                                                                     | ✅      |
| [History Navigation](https://swiperjs.com/swiper-api#history-navigation) | Puts slide information into the browsers history, allowing for forward/back navigation                                                                                                                     | ❌      |
| [Hash Navigation](https://swiperjs.com/swiper-api#hash-navigation)       | Hash navigation is intended to have a link to specific slide that allows to load page with specific slide opened.                                                                                          | ❌      |
| [Controller](https://swiperjs.com/swiper-api#controller)                 | Allows linking Swiper instances together                                                                                                                                                                   | ❌      |
| [Thumbs](https://swiperjs.com/swiper-api#thumbs)                         | In addition to Controller component Swiper comes with Thumbs component that is designed to work with additional thumbs swiper in a more correct way than Controller which is used for syncing two swipers. | ❌      |
| [Creative Effect](https://swiperjs.com/swiper-api#creative-effect)       | Custom transformation effects                                                                                                                                                                              | ❌      |
| [Zoom](https://swiperjs.com/swiper-api#zoom)                             | Zoom in on active slides                                                                                                                                                                                   | ❌      |
| [Grid](https://swiperjs.com/swiper-api#grid)                             | Grid display of multiple slides                                                                                                                                                                            | ❌      |
| [Parallax](https://swiperjs.com/swiper-api#parallax)                     | Parallax transition effect                                                                                                                                                                                 | ❌      |

---

### Scriptable Options

Swiper scriptable options are supported.

Any component property value containing an arrow function `() =>` statement will be converted into a JavaScript function.
> **_NOTE:_** The converted functions do not support implicit return values. The `return` keyword must be used.

The function will have access to all parameters listed in the [Swiper documentation].

#### Global Parameters

In additional to the parameters provided by Chart.js, several Perspective specific global objects can be accessed in scriptable options.
This global objects are implicitly available and do not need to be specified as function arguments.

1. `self`
    - A reference to the Perspective component props.
    - Allows access to all properties on the Perspective component (i.e. `self.custom.myCustomProperty`).
2. `client`
    - A reference to the root Perspective client store.
    - Allows access to Perspective client properties (i.e. `client.projectName`).

#### Scriptable Option Example

```js
// Log a message when the slide is changed.
{
  "settings": {
   "onSlideChange": "() => console.log('slide changed')"
  }
}
```

---

## Changelog

The [changelog](https://github.com/mussonindustrial/embr/blob/main/modules/embr-chart-js/CHANGELOG.md) is regularly updated to reflect what's changed in each new release.

## Sponsors

Maintenance of this project is made possible by all the [contributors] and [sponsors].
If you'd like to sponsor this project and have your avatar or company logo appear below [click here](https://github.com/sponsors/mussonindustrial). 💖

[embr]: https://github.com/mussonindustrial/embr
[contributors]: https://github.com/JamesIves/github-pages-deploy-action/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
[Swiper]: https://swiperjs.com/
[Swiper documentation]: https://swiperjs.com/swiper-api#parameters
