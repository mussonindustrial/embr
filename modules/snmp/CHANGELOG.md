# @embr-modules/snmp

## 0.5.3

### Patch Changes

- Updated dependencies [7f5ebc0]
  - @embr-jvm/core-common@0.8.3
  - @embr-jvm/core-gateway@0.8.3

## 0.5.2

### Patch Changes

- d11ca3b: Assign correct `groupId`.

## 0.5.1

### Patch Changes

- Updated dependencies [4675d8f]
  - @embr-jvm/core-common@0.8.2
  - @embr-jvm/core-gateway@0.8.2

## 0.5.0

### Minor Changes

- dd650fb: Introduce an optional licensing model.

  ## Embr Module Licensing
  - To offer a more complete support model, Musson Industrial is introducing optional licensing for the Embr suite.
  - Don't worry, these modules will always remain free to use and open source.

  Here's what's changing:

  **Standard Edition**
  - Embr modules are free to use but will run in trial mode when unlicensed.
  - **No functionality will be impacted by an expired trial.**
  - You can remove the trial status by purchasing a low-cost license from our web store.

  **Maker Edition**
  - Embr modules are free to use with no trial mode.
    Licenses can be purchased at the [Musson Industrial Web Store](https://mussonindustrial.com/store).

  ## Expected Questions
  - **Q:** Is Embr still open source?
  - **A:** Yes! The entire Embr suite is still MIT-licensed, and this will never change.
  - **Q:** Can I continue to use Embr Charts/Periscope/Thermo/SNMP for free?
  - **A:** Yes! The modules will report as being in trial mode, but no functionality will be impacted. You will still continue to receive updates.
  - **Q:** Why should I purchase a license?
  - **A:** A license removes the trial banner and supports continued development of the Embr suite, helping ensure these modules remain useful, reliable, and actively maintained.
  - **Q:** What if I upgrade from 8.1 to 8.3?
  - **A:** License keys are not tied to a specific Ignition version and will never expire.
  - **Q:** Will you offer redundant gateway, integrator, or bulk purchase discounts?
  - **A:** We aim to keep licensing simple, affordable, and automated, so there are currently no additional discounts.
  - **Q:** I have questions regarding licenses, special setups, or long-term support guarantees.
  - **A:** We're here for you! For setups that fall outside the standard automated licensing process, please reach out so we can provide guidance.

### Patch Changes

- @embr-jvm/core-common@0.8.1
- @embr-jvm/core-gateway@0.8.1

## 0.4.1

### Patch Changes

- f268040: Update to Snmp4J `3.9.7`

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
