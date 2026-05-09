The True Adaptive Music API provides some convenience types for handling registry sets of identifiers. This is to help show only the relevant identifiers from the proper registry when displaying options in the UI. All of these inherit directly from the `TypedIdentifier` class which inherits from the `Identifier` class native to Minecraft.

If your modded predicate/event type needs to take an `Identifier` as an argument, you should use a `TypedIdentifier` subtype instead.

Below is a list of current `TypedIdentifier` subtypes, each corresponding to the registry of the same name:

- BiomeIdentifier
- BlockIdentifier
- DimensionIdentifier
- EntityAttributeIdentifier
- EntityIdentifier
- FluidIdentifier
- StatusEffectIdentifier
- StructureIdentifier
- StructurePieceIdentifier
- StructureSetIdentifier

More will be added in later versions of True Adaptive Music, and feel free to suggest new ones. Also be aware that some identifier types hail from dynamic registries, meaning they will be empty until a world is loaded as host.