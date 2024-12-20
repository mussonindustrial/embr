---
'@embr-modules/charts': major
'@embr-js/utils': minor
'@embr-modules/periscope': minor
---

(BREAKING) Major changes in `toUserScript` parsing.
1. Strict mode is now enabled.
2. Function body now follows standard arrow function syntax. If the body of the arrow function is a block (contained within brackets {...}) then the body **MUST** use the _return_ keyword. If the body of the arrow function is not a block (like `() => 1 + 2`) then the body **MUST NOT** use the return keyword.