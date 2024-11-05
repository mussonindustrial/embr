# Embr Event Stream Module [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

An Ignition module that provides an API for high-speed streaming of tag changes and alarm events via SSE (server-sent events).

Server-Sent Events (SSE) is a server push technology enabling a client to receive automatic updates from a server via an HTTP connection.
All modern browsers support server-sent events: Firefox 6+, Google Chrome 6+, Opera 11.5+, Safari 5+, Microsoft Edge 79+.

## Getting Started
1. Download the [latest version] from [releases].
2. Install the module through the Ignition Gateway web interface.

## Module Documentation
- `#TODO` [Module documentation][documentation]

## Gateway API

* [Create a EventStream Session](#Create-a-EventStream) : `POST /embr/tag/stream/session`
* [Subscribe to a EventStream Session](#Subscribe-to-a-EventStream) : `GET /embr/tag/stream/session/{session_id}`

---
### Create a EventStream Session

Creates a EventStream session containing the specified tags.
The returned `session_id` is used to access the stream.

As a security measure, the `session_id` is time-limited and single use.
If the session is not accessed within 30 seconds after creation, a time-out occurs and the session is discarded.
Only a single client may access a given `session_id`.


| Item   | Value                      |
|--------|----------------------------|
| URL    | `/embr/tag/stream/session` |
| Method | `POST`                     |
| Body   | `{ tagPaths: [] }`         |
| Auth   | `body`                     |

#### Body Example
```json
{
  "username": "admin",
  "password": "password",
  "tag_paths": [
    "[default]Tag1",
    "[default]Path/Tag2"
  ]
}
```

#### Success Response

**Code** : `200 OK`

The response is a `JSON` object containing the `session_id` of the stream and details of the tags included in the session.

Each tag is given a numeric `tag_id` in order to compress the event source message size.
The [EventSource] messages will use this `tag_id` when sending tag change information.

```json
{
  "status": "success",
  "data": {
    "session_id": "8c1a5b02-df33-4a2e-ad6f-101f76ab3d06",
    "tags": [
      {
        "tag_path": "[default]Tag1",
        "alarm_path": "prov:default:/tag:Tag1",
        "tag_id": 0
      },
      {
        "tag_path": "[default]Path/Tag2",
        "alarm_path": "prov:default:/tag:Path/Tag2",
        "tag_id": 1
      }
    ]
  }
}
```

---
### Subscribe to a EventStream Session

Subscribe to a EventStream by its `session_id`.
This endpoint is meant to be accessed by an [EventSource].
For more details, see [Mozilla's MDN WebDocs](https://developer.mozilla.org/en-US/docs/Web/API/EventSource).

| Item   | Value                                   |
|--------|-----------------------------------------|
| URL    | `/embr/tag/stream/session/{session_id}` |
| Method | `GET`                                   |
| Params | `session_id`: EventStream session id      |
| Auth   | None                                    |

#### Success Response

**Code** : `200 OK`

Messages events have the following format:

```
event: tag_change
data: {"tag_id":0,"v":"Tag Value!","q": 192,"t":1717624491517}

event: alarm_event
data: {"tag_id":0,"count":1,"displayPath":"","eventData":{"eventValue":"false","name":"AlarmName","eventTime":"Thu Jun 20 13:17:37 EDT 2024","priority":"Critical","displayPath":""},"extension":{"isShelved":"false"},"id":"454d77c5-44f8-4e52-ab3c-e1ed5c950583","isAcked":false,"isCleared":false,"isShelved":false,"label":"AlarmName","name":"AlarmName","notes":"","priority":"Critical","source":"prov:default:/tag:Tag1:/alm:AlarmName","state":"Active, Unacknowledged","values":[{"isShelved":"false"}]}
```


---
## Changelog

The [changelog](./CHANGELOG.md)) is regularly updated to reflect what's changed in each new release.

## Copyright and Licensing

Copyright (C) 2023 Musson Industrial

Free use of this software is granted under the terms of the MIT License.

[embr]: https://github.com/mussonindustrial/embr
[releases]: https://github.com/mussonindustrial/embr/releases
[documentation]: https://docs.mussonindustrial.com/
[latest version]: https://github.com/mussonindustrial/embr/releases/download/embr-chart-js-0.1.3-SNAPSHOT/Embr-Chartjs-module.modl
[EventSource]: https://developer.mozilla.org/en-US/docs/Web/API/EventSource
