## Format
Each entry in this page will follow this format.

### Type Format
type_name: required_parameters `array` (optional_parameters `array`) `abstract`

abstract: If the abstract tag is present, then the options for 1 or more parameters can only be set when the user is loaded into a Minecraft world due to the fact that the registries for these are only initialized when in a world.

array: If the array tag is present, then you can provide more than one option for this parameter

### Parameter Format
parameter_name: ParameterType

## Types

### biome [id: BiomeIdentifier `array`] `abstract`
True when the player is in any biome within the `id` array

### boss [id: EntityTypeIdentifier `array`]
True when there is a boss bar on the player's screen for any of the entity types within the `id` array

### combat
True when a mob is within sufficient range of the player and is attacking them

### day
True when the ticks for the day is between 0 and 12999 inclusive

### dimension [id: DimensionIdentifier `array`] `abstract`
True when the player is in any dimension within the `id` array

### first_day
True when the total world ticks is between 0 and 24000 inclusive (within one minecraft day)

### height [above: Boolean, y: Int]
True when the player's y coordinate is `above`/`below` the `y` value specified

### night
True when the ticks for the day is between 13000 and 23999 inclusive

### structure [id: StructureIdentifier `array`] `abstract`
True when the player is within the maximum bounds of all features assigned to any of the structure types within the `id` array

### structure_set [id: StructureSetIdentifier `array`] `abstract`
Same as `structure`, but for sets of structure types rather than individual structure types.

### title_screen
True when the player is not in a world

### weather [weatherType: Enum('Clear', 'Rain', 'Thunder')]
True when the weather is the type specified by `weatherType`