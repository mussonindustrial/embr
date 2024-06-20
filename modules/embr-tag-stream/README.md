# Embr Tag Stream Module [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

An Ignition module that provides an API for high-speed streaming of tag changes via SSE (server-sent events).

Server-Sent Events (SSE) is a server push technology enabling a client to receive automatic updates from a server via an HTTP connection.
The EventSource API is standardized as part of HTML Living Standard by the WHATWG.
The media type for SSE is `text/event-stream`.

All modern browsers support server-sent events: Firefox 6+, Google Chrome 6+, Opera 11.5+, Safari 5+, Microsoft Edge 79+.

## Getting Started
1. Download the [latest version] from [releases].
2. Install the module through the Ignition Gateway web interface.

## Module Documentation
- `#TODO` [Module documentation][documentation]

## Gateway API

* [Create a TagStream Session](#Create-a-TagStream) : `POST /embr/tag/stream/session`
* [Subscribe to a TagStream Session](#Subscribe-to-a-TagStream) : `GET /embr/tag/stream/session/{session_id}`

---
### Create a TagStream Session

Creates a TagStream session containing the specified tags.
The returned `session_id` is used to access the stream.

As a security measure, the `session_id` is time-limited and single use.
If the session is not accessed within 30 seconds after creation, a time-out occurs and the session is discarded.
Only a single client may access a given `session_id`.


| Item   | Value                      |
|--------|----------------------------|
| URL    | `/embr/tag/stream/session` |
| Method | `POST`                     |
| Body   | `{ tagPaths: [] }`         |
| Auth   | None                       |

#### Body Example
```json
{
  "tagPaths": [
    "[default]Tag1",
    "[default]Path/Tag1"
  ]
}
```

#### Success Response

**Code** : `200 OK`

The response is a `JSON` object containing the `session_id` of the stream and details of the tags included in the session.

Each tag is given a numeric `id` in order to compress the event source message size.
The [EventSource] data messages will use this `id` when sending tag change information.

```json
{
  "status": "success",
  "data": {
    "session_id": "e61c7dd-5f4b-38a2-8067-3e77483fabce",
    "tags": {
      "[default]Tag1": {
        "id": 0,
        "path": "[default]Tag1"
      },
      "[default]Path/Tag1": {
        "id": 1,
        "path": "[default]Path/Tag1"
      }
    }
  }
}
```

---
### Subscribe to a TagStream Session

Subscribe to a TagStream by its `session_id`.
This endpoint is meant to be accessed by an [EventSource].
For more details, see [Mozilla's MDN WebDocs](https://developer.mozilla.org/en-US/docs/Web/API/EventSource).

| Item   | Value                                   |
|--------|-----------------------------------------|
| URL    | `/embr/tag/stream/session/{session_id}` |
| Method | `GET`                                   |
| Params | `session_id`: TagStream session id      |
| Auth   | None                                    |

#### Success Response

**Code** : `200 OK`

Messages events have the following format:

```
event: id=0
data: {"v":"Tag Value!","q": 192,"t":1717624491517}
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
