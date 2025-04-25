# Typed Identifiers

The True Adaptive Music API provides some convenience types for handling registry sets of identifiers. This is to help the widget factory filter down all identifiers to a specific registry. All of these inherit directly from the `TypedIdentifier` type which inherits from the `Identifier` type native to Minecraft, and contain a `getRegistryIds` helper function to get all the current registered identifiers under a give scope. Here is a list of current `TypedIdentifier` subtypes, each corresponding to the registry of the same name:

- BiomeIdentifier
- DimensionIdentifier
- EntityTypeIdentifier
- StructureIdentifier
- StructureSetIdentifier

More will be added in later versions of TrueAdaptiveMusic. Also be aware that some identifier types hail from dynamic registries, meaning they will be empty until a world is loaded.