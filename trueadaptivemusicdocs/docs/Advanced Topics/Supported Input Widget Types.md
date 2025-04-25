# Supported Input Widget Types

Some predicate/event types have instance parameters declared in their constructor parameter list. In order for these to be able to be set properly via the Music Pack creation UI, they must conform to one of the supported types by the input widget factory. In the future, we plan to allow the user to create their own widget types, as well as filling out more basic types. Here is a complete list of these types and what the resulting widget will be:

| Parameter Type  | Resulting Widget |
| :-------------- | :--------------- |
| Int             | Text input widget only allowing integers |
| UInt            | Text input widget only allowing non-negative integers |
| Boolean         | Checkbox widget  |
| Enum (or subtype of) | Dropdown of all values belonging to the enum |
| List<Enum (or subtype of)> | Multi-Select dropdown of all values belonging to the enum |
| TypedIdentifier (or subtype of) | Dropdown of all identifiers within that type. See [TypedIdentifiers](./TypedIdentifiers.md) |
| List<TypedIdentifier (or subtype of)> | Multi-Select dropdown of all identifiers within that type. See [TypedIdentifiers](./TypedIdentifiers.md) |
