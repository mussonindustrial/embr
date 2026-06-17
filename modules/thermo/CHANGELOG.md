# @embr-modules/thermo

## 3.2.2

### Patch Changes

- Updated dependencies [588ffb7]
  - @embr-jvm/core-common@0.9.1
  - @embr-jvm/core-gateway@0.9.1

## 3.2.1

### Patch Changes

- 05f93b2: Assign correct `groupId`.

## 3.2.0

### Minor Changes

- f64c92d: Bump minimum compatible Ignition version from `8.1.33` to `8.1.49`.

## 3.1.0

### Minor Changes

- 8c76341: Bump minimum compatible Ignition version from `8.1.33` to `8.1.49`.

### Patch Changes

- @embr-jvm/core-common@0.9.0
- @embr-jvm/core-gateway@0.9.0

## 3.0.1

### Patch Changes

- Updated dependencies [760c0eb]
  - @embr-jvm/core-common@0.8.2
  - @embr-jvm/core-gateway@0.8.2

## 3.0.0

### Major Changes

- 9fae5d4: Introduce an optional licensing model.

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

## 2.0.1

### Patch Changes

- 35bf7c2: `PyArgOverload`: Enforce strong return typing.
- Updated dependencies [2d6a1ad]
- Updated dependencies [35bf7c2]
- Updated dependencies [e619625]
  - @embr-jvm/core-common@0.8.0
  - @embr-jvm/core-gateway@0.8.0

## 2.0.0

### Major Changes

- fc47d7e: Modules no longer include `kotlin-stdlib`.
  - All modules now rely on the Kotlin standard library provided with Ignition.

### Patch Changes

- Updated dependencies [f36eaee]
  - @embr-jvm/core-common@0.7.1
  - @embr-jvm/core-gateway@0.7.1

## 1.0.5

### Patch Changes

- 92c744f: Fake changeset.

## 1.0.4

### Patch Changes

- c661a93: Reduce startup/shutdown logging levels. Ignition's default logging already logs these events, so we are effectively double logging for no reason.
- Updated dependencies [a8b2973]
  - @embr-jvm/core-common@0.7.0
  - @embr-jvm/core-gateway@0.7.0

## 1.0.3

### Patch Changes

- Updated dependencies [40ad4a7]
  - @embr-jvm/core-common@0.6.1
  - @embr-jvm/core-gateway@0.6.1

## 1.0.2

### Patch Changes

- Updated dependencies [7f90e53]
- Updated dependencies [7f90e53]
  - @embr-jvm/core-common@0.6.0
  - @embr-jvm/core-gateway@0.6.0

## 1.0.1

### Patch Changes

- Updated dependencies [4d7f150]
  - @embr-jvm/core-common@0.5.0
  - @embr-jvm/core-gateway@0.5.0

## 1.0.0

### Major Changes

- 6f46916: First major release to coincide with release of public documentation. No changes from 0.1.6.

### Patch Changes

- 6f46916: (chore) Correct package.json dependencies to included `perspective-common`, `perspective-designer`, and `perspective-gateway` references.

## 0.1.6

### Patch Changes

- 8f93be4: (docs) Fix scripting hint signature for `specificGibbsFreeEnergy`.

## 0.1.5

### Patch Changes

- Updated dependencies [0a9d03e]
  - @embr-jvm/core-common@0.4.1

## 0.1.4

### Patch Changes

- 02a59ba: republish previously pulled patch release

## 0.1.3

### Patch Changes

- bd7e4b6: chore: update ignition module plugin

## 0.1.2

### Patch Changes

- 91f567d: Fix issue with duplicate identical overloads for `refractiveIndex`

## 0.1.1

### Patch Changes

- 9182d09: Initial release!
- Updated dependencies [9182d09]
- Updated dependencies [9182d09]
  - @embr-jvm/core-common@0.4.0
