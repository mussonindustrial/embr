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
- `systen.snmp.agent.getAgent(agent)`
   - This returns an agent proxy that supports `read(Async/Blocking)`, `write(Async/Blocking)`, `walk(Async/Blocking)`, and `readTable(Async/Blocking)`.

Add expression functions:
- `snmpRead(agent, oid1, [oid2, ...])`
- `snmpReadTable(agent, column1, [column2, ...])`
- `snmpWalk(agent, oid1, [oid2, ...])`