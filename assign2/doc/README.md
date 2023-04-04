# CPD Project 2: Distributed and Partitioned Key-Value Store

To compile the project run javac *.java through your terminal

## Server

Through your Terminal, you can run a store node at your port of choice.

> **Usage**: java Store \<IP_mcast_addr> \<IP_mcast_port> \<node_id>  \<Store_port>
>
> **Example**: `java Store 224.0.0.3 8000 1 8001

## TestClient

Through your Terminal, you can send requests to the Store nodes, and receive the response.

> **Usage**: java TestClient \<node_ap> \<operation> [\<opnd>]

**\<operation>** is the string specifying the operation the node must execute. It can be either a key-value operation, i.e. "put", "get" or "delete", or a membership operation, i.e. "join" or "leave

**\<opnd>** is the argument of the operation. It is used only for key-value operations. In the case of:

* put: file pathname of the file with the value to add
* get or delete: string of hexadecimal symbols encoding the sha-256 key returned by put.

> **Join Example**: `java TestClient 8001 join
>
> **Leave Example**:`java TestClient 8001 leave
>
> **Put Example**: `java TestClient 8001 put file.txt
>
> **Get Example**:`java TestClient 8001 get 30c127724947154e5ceea71c372a45ab255cb78b263e94a01197009e0e61de50
>
> **Delete Example**:`java TestClient 8001 delete 30c127724947154e5ceea71c372a45ab255cb78b263e94a01197009e0e61de50
>
