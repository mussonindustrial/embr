# Embr Tag Stream Module [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

An Ignition module that provides an API for high-speed streaming of tag changes via SSE (server-sent events).

Server-Sent Events (SSE) is a server push technology enabling a client to receive automatic updates from a server via an HTTP connection.
The EventSource API is standardized as part of HTML Living Standard by the WHATWG.
The media type for SSE is text/event-stream.

All modern browsers support server-sent events: Firefox 6+, Google Chrome 6+, Opera 11.5+, Safari 5+, Microsoft Edge 79+.

## Getting Started
1. Download the [latest version] from [releases].
2. Install the module through the Ignition Gateway web interface.

## Module Documentation
- `#TODO` [Module documentation][documentation]

## Gateway API

* [Create a TagStream](#Create-a-TagStream) : `POST /embr-tag-stream/subscribe`
* [Subscribe to a TagStream](#Subscribe-to-a-TagStream) : `GET /embr-tag-stream/stream/{id}`

---
### Create a TagStream

Create a TagStream containing the specified tags.

| Item   | Value                        |
|--------|------------------------------|
| URL    | `/embr-tag-stream/subscribe` |
| Method | `POST`                       |
| Body   | `{ tags: [] }`               |
| Auth   | None                         |

#### Body Example
```json
{
  "tags": [
    "[default]Tag1",
    "[default]Path/Tag1"
  ]
}
```

#### Success Response

**Code** : `200 OK`

The response is a `JSON` object containing the `id` of the stream.
This `id` is used to subscribe to the stream.

```json
{
    "id": "e61c7dd-5f4b-38a2-8067-3e77483fabce"
}
```

---
### Subscribe to a TagStream

Subscribe to a TagStream by its `id`.
This endpoint is meant to be accessed by an [EventSource].
For more details, see [Mozilla's MDN WebDocs](https://developer.mozilla.org/en-US/docs/Web/API/EventSource).

| Item   | Value                          |
|--------|--------------------------------|
| URL    | `/embr-tag-stream/stream/{id}` |
| Method | `GET`                          |
| Params | `id`: TagStream id             |
| Auth   | None                           |

#### Success Response

**Code** : `200 OK`

Messages events have the following format:

```json
event: [default]Path/Tag1
data: {"value":"Tag Value!","quality": 192,"timestamp":1717624491517}
```


---
## Changelog

The [changelog](https://github.com/mussonindustrial/embr/blob/main/modules/embr-chart-js/CHANGELOG.md) is regularly updated to reflect what's changed in each new release.

## Copyright and Licensing

Copyright (C) 2023 Musson Industrial

Free use of this software is granted under the terms of the MIT License.

[embr]: https://github.com/mussonindustrial/embr
[releases]: https://github.com/mussonindustrial/embr/releases
[documentation]: https://docs.mussonindustrial.com/
[latest version]: https://github.com/mussonindustrial/embr/releases/download/embr-chart-js-0.1.3-SNAPSHOT/Embr-Chartjs-module.modl
[EventSource]: https://developer.mozilla.org/en-US/docs/Web/API/EventSource
