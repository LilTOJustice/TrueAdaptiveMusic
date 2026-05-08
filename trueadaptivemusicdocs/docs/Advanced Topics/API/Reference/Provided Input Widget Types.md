# Supported Input Widget Types

The following parameter types are provided widget registrations by the TAMClient at runtime and thus can be used as arguments for any trigger type (predicate or event).

| Parameter Type                          | Resulting Widget                                                                                       |
|:----------------------------------------|:-------------------------------------------------------------------------------------------------------|
| `String`                                | Text input allowing any printable character                                                            |
| `Int`                                   | Text input widget only allowing integers                                                               |
| `UInt`                                  | Text input widget only allowing non-negative integers                                                  |
| `Double`                                | Text input widget only allowing any real number                                                        |
| `Boolean`                               | Checkbox                                                                                               |
| `Enum (or subtype of)`                  | Dropdown of all values belonging to the enum                                                           |
| `List<Enum (or subtype of)>`            | Multi-Select dropdown of all values belonging to the enum                                              |
| `TypedIdentifier (or subtype of)`       | Dropdown of all identifiers within that type. See [TypedIdentifiers](TypedIdentifier.md).              |
| `List<TypedIdentifier (or subtype of)>` | Multi-Select dropdown of all identifiers within that type. See [TypedIdentifiers](TypedIdentifier.md). |
