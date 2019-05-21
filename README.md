# Distributed_System

**peer4**
Conducts server without bitbox skeleton


**peer5**
Conducts client and server without bitbox skeleton


**Error Assumption:**

May produce error when parent dir does not exist (used "count" may fail)

if (command.get("command")...): if JSON does not contain "command", there will be an error

Peer2 wont be able to detect if a file/dir is deleted in peer1 when offline.

What if connection is lost when transfering file, the file loader will exist in the peer for ever. (it will get synchronized at next connection)

Do we need to "checkShortCut" after creating modifyFileLoader?

Do we need to kill ServerMain and Timer in Client side?

**Unfinished work**

Write peer into configration

Ensure only 10 can be connected

Copy client to server, server to client

File division transfer

File delete

File modify

How to use checkShortcut and where it is used

"checkShortCut is deleted for now"

put cancel file loader before creating file loader

**Part 2 improvements**

Change Peer_serverForClient to Object instead of Thread.