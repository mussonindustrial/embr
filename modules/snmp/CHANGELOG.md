# @embr-modules/snmp

## 0.4.0

### Minor Changes

- 95196b4: Add scripting functions:
  - `system.snmp.agent.readAsync(agent, oids)`
  - `system.snmp.agent.readBlocking(agent, oids)`
  - `system.snmp.agent.writeAsync(agent, oids, values)`
  - `system.snmp.agent.writeBlocking(agent, oids, values)`
  - `system.snmp.agent.walkAsync(agent, oids)`
  - `system.snmp.agent.walkBlocking(agent, oids)`
  - `system.snmp.agent.readTableAsync(agent, columns, lowerBoundIndex, upperBoundIndex)`
  - `system.snmp.agent.readTableBlocking(agent, columns, lowerBoundIndex, upperBoundIndex)`
  - `systen.snmp.agent.getAgent(agent)`
    - This returns an agent proxy that supports `read(Async/Blocking)`, `write(Async/Blocking)`, `walk(Async/Blocking)`, and `readTable(Async/Blocking)`.

  Add expression functions:
  - `snmpRead(agent, oid1, [oid2, ...])`
  - `snmpReadTable(agent, column1, [column2, ...])`
  - `snmpWalk(agent, oid1, [oid2, ...])`

### Patch Changes

- Updated dependencies [424d5ce]
- Updated dependencies [6dd3999]
- Updated dependencies [0a98ae5]
  - @embr-jvm/core-common@0.8.0
  - @embr-jvm/core-gateway@0.8.0

## 0.3.0

### Minor Changes

- 3a45993: Modules no longer include `kotlin-stdlib`.
  - All modules now rely on the Kotlin standard library provided with Ignition.
- 59bafb8: Initial published release.

### Patch Changes

- 3d505ee: Bump `org.snmp4j:snmp4j` from `3.9.3` to `3.9.6`
- Updated dependencies [59bafb8]
  - @embr-jvm/core-common@0.7.1
  - @embr-jvm/core-gateway@0.7.1
