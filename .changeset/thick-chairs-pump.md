---
'@embr-modules/snmp': minor
---

Add scripting functions:
- `system.snmp.agent.readAsync(agent, oids)`
- `system.snmp.agent.readBlocking(agent, oids)`
- `system.snmp.agent.writeAsync(agent, oids, values)`
- `system.snmp.agent.writeBlocking(agent, oids, values)`
- `system.snmp.agent.walkAsync(agent, oids)`
- `system.snmp.agent.walkBlocking(agent, oids)`
- `system.snmp.agent.readTableAsync(agent, columns, lowerBoundIndex, upperBoundIndex)`
- `system.snmp.agent.readTableBlocking(agent, columns, lowerBoundIndex, upperBoundIndex)`
