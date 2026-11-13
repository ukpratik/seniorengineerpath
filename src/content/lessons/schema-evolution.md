---
topic: schema-evolution
subtitle: Change the shape of your data so that old code can read new data, and new code can read old data, while both run at once.
status: published
illustrative_story: false
sources:
  - id: cs-rca
    title: "CrowdStrike, External Technical Root Cause Analysis — Channel File 291 (6 August 2024)"
    url: https://www.crowdstrike.com/wp-content/uploads/2024/08/Channel-File-291-Incident-Root-Cause-Analysis-08.06.2024.pdf
    note: How content data that expected 21 input fields met sensor code that supplied 20, and the fixes CrowdStrike made.
  - id: k8s
    title: "Kubernetes documentation, Deployments"
    url: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
    note: How a rolling update scales up new pods while it scales down old ones.
  - id: rfc8259
    title: "T. Bray (ed.), RFC 8259: The JavaScript Object Notation (JSON) Data Interchange Format (IETF, December 2017)"
    url: https://www.rfc-editor.org/rfc/rfc8259
    note: Section 6 on number precision, and which integers are safe to exchange.
  - id: mdn-safe
    title: "MDN Web Docs, Number.MAX_SAFE_INTEGER"
    url: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
    note: The largest integer a JavaScript number can hold exactly, with an example of what goes wrong above it.
  - id: proto3
    title: "Protocol Buffers Documentation, Language Guide (proto 3)"
    url: https://protobuf.dev/programming-guides/proto3/
    note: Field numbers, defaults, the optional keyword, reserved fields, unknown fields and safe changes to a message.
  - id: proto-bp
    title: "Protocol Buffers Documentation, Proto Best Practices"
    url: https://protobuf.dev/best-practices/dos-donts/
    note: Short rules such as never reuse a field number, and never add a required field.
  - id: protojson
    title: "Protocol Buffers Documentation, ProtoJSON Format"
    url: https://protobuf.dev/programming-guides/json/
    note: How Protobuf maps to JSON, why 64-bit integers become strings, and what happens to unknown fields.
  - id: avro-spec
    title: "Apache Avro, Specification (version 1.12.0)"
    url: https://avro.apache.org/docs/1.12.0/specification/
    note: The schema resolution rules between the writer's schema and the reader's schema.
  - id: kleppmann
    title: "Martin Kleppmann, “Schema evolution in Avro, Protocol Buffers and Thrift” (5 December 2012)"
    url: https://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html
    note: A side-by-side look at how each format encodes fields, and how it handles old and new schemas.
  - id: confluent
    title: "Confluent Documentation, Schema Evolution and Compatibility for Schema Registry on Confluent Platform"
    url: https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html
    note: The BACKWARD, FORWARD and FULL compatibility types, and which side to upgrade first for each.
  - id: confluent-serdes
    title: "Confluent Documentation, Formats, Serializers, and Deserializers for Schema Registry on Confluent Platform"
    url: https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/index.html
    note: The wire format, where each message begins with a version byte and a 4-byte schema ID that the reader looks up in the registry.
claims_checked:
  - claim: "The IPC Template Type defined 21 input fields but the sensor code supplied 20; tests and early content used a wildcard for field 21, and on 19 July 2024 a non-wildcard value caused an out-of-bounds read. Fixes included a compile-time input count check and a runtime bounds check."
    source: cs-rca
    note: "RCA pages 2-5 say exactly this, including the Content Validator assuming 21 inputs."
  - claim: "Proto3 messages keep unknown fields when parsing and write them out again; converting to JSON loses them."
    source: proto3
    note: "Unknown Fields and Retaining Unknown Fields sections. Confirmed with Python protobuf: a v1 reader re-serialized a v2 message byte for byte."
  - claim: "Never re-use a field (tag) number; it messes up deserialization."
    source: proto-bp
    note: "Quote is exact. The page also says proto3 has no required fields."
  - claim: "A field only in the writer's schema is ignored; a reader field missing from the writer takes its default, or an error is signalled if it has none. In 1.12 a union default matches the first branch that fits."
    source: avro-spec
    note: "Schema Resolution section and the record field default attribute. Confirmed with fastavro 1.12.2 and avro 1.12.1."
  - claim: "BACKWARD is the default so consumers can rewind to the start of a topic; under BACKWARD, upgrade all consumers before producing new events; transitive modes check all earlier versions."
    source: confluent
    note: "Stated in the compatibility types and order-of-upgrading sections."
  - claim: "ProtoJSON writes int64 values as strings to avoid precision loss above 2^53, and parsers should reject unknown fields by default."
    source: protojson
    note: "Strings for int64 and JSON options sections."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: A rolling deploy is half done. New pods write messages with a new field. Old pods still read from the same queue. Which property must the format give you?
    options:
      - Backward compatibility, so new code can read old data.
      - Forward compatibility, so old code can read data written by new code.
      - Neither. A queue keeps the versions apart.
    answer: 1
    why: Old readers meet data from a newer writer. That is forward compatibility. The queue does not keep the versions apart.
  - q: You delete the Protobuf field `int64 amount = 2;`. Later a teammate adds `int64 discount = 2;`. What happens to services that still run the old code?
    options:
      - They fail to parse the message and log an error.
      - They read the discount value as the amount, with no error.
      - They skip field 2, because its name changed.
    answer: 1
    why: The wire format carries only the number and the value, not the name. Old code still maps number 2 to amount. Reserve deleted numbers so this cannot happen.
  - q: In Avro, a new reader's schema adds a field `currency` with no default. It reads a record written with the old schema. What happens?
    options:
      - The reader gets an error.
      - The field is set to null.
      - The field is set to an empty string.
    answer: 0
    why: The Avro spec says that if the reader's field has no default and the writer's schema lacks it, an error is signalled.
  - q: 'A JavaScript service parses the JSON `{"id": 9007199254740993}` with `JSON.parse`. What does it get?'
    options:
      - 9007199254740993, exactly.
      - An error, because the number is too big.
      - A nearby but different number, 9007199254740992.
    answer: 2
    why: JavaScript numbers are doubles. Integers above 2^53 − 1 cannot all be stored exactly, so the value is rounded with no error.
  - q: Your topic uses Confluent Schema Registry with the default BACKWARD mode. You delete a field that has no default. What should you upgrade first?
    options:
      - The consumers, then the producers.
      - The producers, then the consumers.
      - Order does not matter in BACKWARD mode.
    answer: 0
    why: Confluent's docs say that for BACKWARD, you upgrade all consumers before you start producing new events.
incidents: [crowdstrike-2024-07-channel-file-291]
glossary_optout: [rfc]
related: [rest-api, api-versioning, schema-migrations, queues-vs-logs, webhooks, grpc, contract-tests, kafka-internals]
---

## What broke

On 19 July 2024, Windows machines running CrowdStrike's Falcon sensor crashed. The cause was not new code. It was new data that the sensor code could not safely read. [@cs-rca]

In February 2024, sensor version 7.11 added a new template type for IPC (named pipes). Content files for it arrive from the cloud as Channel File 291. [@cs-rca]

Both parts were built at the same time, and they disagreed from day one. The template's definition said each check had 21 input fields. The sensor code supplied only 20. [@cs-rca]

For months, nobody noticed. The tests and the first live content used a wildcard for the 21st field, so the sensor never had to look at it. [@cs-rca]

Then two new content entries were sent. One had a real value for the 21st field. The content validator approved it, because it also assumed 21 inputs. The sensor tried to read input 21 from an array of 20. That out-of-bounds read crashed the system. [@cs-rca]

The data and the code that read it disagreed about the shape, and the code did not check the array size. Among its fixes, CrowdStrike now checks the number of fields when it builds the sensor. It also added a bounds check that runs on the machine. [@cs-rca] This lesson is about that gap between writer and reader, and how data formats help you close it.

## The idea in simple words

Every time you send or store data, you **encode** it into bytes. Later, some other code **decodes** it. That other code may be older or newer than the code that wrote it.

This happens all the time, not only in rare cases:

- **Rolling deploys.** Kubernetes scales up the new pods while it scales down the old ones. [@k8s] For a few minutes, v1 and v2 talk to each other.
- **Messages in a queue or log.** A message written yesterday by v1 is read today by v2. In a log, it may be read again in a year.
- **Stored data.** A database row or a file written three years ago is still read by today's code.

So you need two promises:

- **Backward compatibility:** new code can read data written by old code.
- **Forward compatibility:** old code can read data written by new code.

Forward is the harder one. Old code cannot know what you will add later. So it must be built to skip what it does not know.

> [!key] **The key idea.** Old and new code always overlap. Change your data so both can read it: add optional fields, never change the meaning of an existing one, and never reuse an old name or number.

## How it works

### JSON: no schema, so the rules live in your code

JSON carries field names, so a reader can find fields in any order. But nothing checks the shape. Two habits keep JSON safe:

- **Ignore unknown fields.** Then new fields do not break old readers. Some parsers reject them by default. ProtoJSON parsers do, for example. [@protojson] Check yours.
- **Treat missing fields as normal.** Then new readers can read old data.

Numbers are a trap. RFC 8259 lets parsers limit precision. Only integers between −(2^53 − 1) and 2^53 − 1 are safe to exchange. [@rfc8259] JavaScript stores every number as a double, so bigger integers get rounded. [@mdn-safe]

```js
JSON.parse('{"id": 9007199254740993}').id
// 9007199254740992 — a different id, and no error
```

Send big ids, such as 64-bit database keys, as strings. Protobuf's own JSON format does this for all 64-bit integers, for the same reason. [@protojson]

### Protocol Buffers: the field number is the contract

In Protobuf, each field has a field number. The bytes on the wire carry that number, not the field name. So you can rename a field, but you can never change its number. [@kleppmann]

```proto
syntax = "proto3";

// v2
message Order {
  int64 id = 1;
  int64 amount = 2 [deprecated = true];  // rupees; v1 still reads it
  int64 amount_paise = 3;                // new in v2
  optional string currency = 4;          // new in v2
}
```

Later, when no reader needs field 2, v3 deletes it and adds `reserved 2;` and `reserved "amount";`.

The rules that matter:

- **Adding and removing fields is safe.** Changing a field's number is not. [@proto3]
- **Never reuse a number.** Google's guide says it "messes up deserialization". When you delete a field, reserve its number. [@proto-bp] Also reserve the name, so no new field reuses it. JSON and text formats carry names, not numbers. [@proto3]
- **Unknown fields survive.** An old proto3 reader keeps fields it does not know, and writes them out again. [@proto3] They are lost if you convert the message to JSON. [@proto3]
- **Missing means default.** A missing string reads as empty, a number as zero, and a bool as false. [@proto3] So "zero" and "not sent" look the same.
- **Use `optional` when "not sent" matters.** Then you can check whether a value was set. [@proto3]
- **Do not change a field's type.** [@proto-bp] A few types are wire-compatible, such as `int32` and `int64`. But a big value read as `int32` is cut short with no error. [@proto3]
- **No required fields.** Proto3 has none. In proto2, never add one. [@proto-bp]

Step through a rolling deploy that shares one queue. Use Next and Back, or the ← and → keys.

[[diagram:protobuf-old-new-readers]]

### Avro: the reader needs the writer's schema

Avro has no field numbers. Binary Avro data has no field names or types either. So a reader needs the exact schema the data was written with: the **writer's schema**. [@avro-spec]

The reader also has its own schema, the **reader's schema**. Avro matches the two by field name. [@avro-spec]

- A field only in the writer's schema is ignored.
- A field only in the reader's schema takes its default.
- If that field has no default, the read fails. [@avro-spec]

So if both old and new readers must work, add or remove only fields that have a default. [@kleppmann]

```json
{
  "type": "record",
  "name": "Order",
  "fields": [
    {"name": "id", "type": "long"},
    {"name": "amount_paise", "type": "long"},
    {"name": "currency", "type": ["null", "string"], "default": null}
  ]
}
```

In Avro before 1.12, a union default had to match the first type in the union. Version 1.12 accepts the first type that matches. [@avro-spec] Many tools still use the old rule, so put `"null"` first when the default is null.

Where does the writer's schema come from? An Avro file stores it once in its header. [@avro-spec] For records stored one by one, such as rows in a database, each record carries a schema version or id. A **schema registry** maps that id to the full schema. [@kleppmann] With Confluent Schema Registry, each Kafka message begins with a version byte and a 4-byte schema ID. [@confluent-serdes]

### Schema registries and compatibility modes

A registry also guards changes. Confluent Schema Registry checks each new schema against old ones before it accepts it. [@confluent]

- **BACKWARD** (the default): consumers on the new schema can read data written with the last schema. Upgrade consumers first. [@confluent]
- **FORWARD**: consumers on the old schema can read data written with the new schema. Upgrade producers first. Then make sure consumers no longer read old data. [@confluent]
- **FULL**: both. You can upgrade either side in any order. [@confluent]

Each mode also has a `_TRANSITIVE` form. It checks against all older schemas, not only the last one. [@confluent] Confluent made BACKWARD the default so that consumers can rewind to the start of a topic. [@confluent] If your topic keeps data for a long time, a transitive mode is safer.

### What no format can catch

Every check above looks at names, numbers and types. None of them looks at meaning. Say you keep `int64 amount = 2`, but start to send paise instead of rupees. The schema is the same, so every check passes. Old readers now show prices 100 times too high.

So a change of unit or meaning is a new field, with a new name and a new number. That is why the example above added `amount_paise` instead of changing `amount`.

## Trade-offs

| Format | Schema needed to read? | New fields and old readers | Main risk | Use it when |
|---|---|---|---|---|
| JSON | No. Names are in the data. | Yes, if readers ignore unknown fields. | Nothing checks shape. Big numbers lose precision. [@rfc8259] | Public APIs, webhooks, browsers. |
| Protobuf | Yes, to know names. Unknown fields can still be skipped. [@kleppmann] | Yes. Old code skips them. [@proto3] | A reused field number gives wrong data, silently. [@proto-bp] | Calls between your own services, such as gRPC. |
| Avro | Yes. The exact writer's schema. [@avro-spec] | Yes. Old readers ignore it. A default lets new readers read old data. [@avro-spec] | Lost or wrong writer's schema means unreadable data. | Kafka topics and data files, with a registry. |

**Simple rule:** pick the format your readers can handle. Then put a compatibility check in CI, not in a reviewer's memory.

## At work: how a senior uses this

- For every data change, ask: **"Who reads this, and how old can their code be?"** Count queue backlog, log retention and stored rows, not only live services.
- Add new fields as optional, with a safe default. Never make a new field required.
- Never change the meaning or unit of a field. To move from rupees to paise, add `amount_paise` and keep `amount` until nobody reads it.
- Deprecate before you delete. Stop writing the field, wait until no reader needs it, then delete it and reserve its number and name.
- Run a schema check in CI. For Protobuf, a breaking-change linter. For Kafka, the registry's compatibility mode.
- Make readers fail safe on bad input. CrowdStrike's fix was a bounds check at runtime as well as a check at build time. [@cs-rca]
- For changes to public HTTP APIs, use [API versioning and lifecycle](/learn/api-versioning-and-lifecycle). For database columns, use [zero-downtime schema migrations](/learn/schema-migrations-zero-downtime).

## With AI

### What to ask the agent

> "Compare this .proto file with the one on main. List every change that breaks old readers or old writers: changed or reused field numbers, changed types, removed fields without `reserved`, and new required fields."

> "Here is our Avro schema and the new version. Would Confluent Schema Registry accept it in BACKWARD mode? In FORWARD mode? Explain each field."

> "Find every place our JavaScript code parses JSON ids that can be larger than 2^53 − 1. Change them to strings end to end."

### What to check yourself

- No field number was reused or renumbered. An agent may "tidy up" numbers so they run 1, 2, 3.
- Every deleted Protobuf field has its number and name in `reserved`.
- Every new Avro field has a default, and a union default matches the first type, for older Avro tools.
- No field kept its name but changed its unit or meaning. A linter cannot catch that.
- The plan says which side deploys first, and it matches the registry mode.

## Check yourself

## Go deeper

- [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees): why old messages stay around to be read again.
- [Webhooks and async APIs](/learn/webhooks-and-async-apis): JSON events that your customers decode with code you cannot update.
- [Safe deploys and config changes](/learn/safe-deploys-config-changes): rolling out content and config as carefully as code.
- Coming later: RPC and gRPC, contract and integration tests, and Kafka internals.
