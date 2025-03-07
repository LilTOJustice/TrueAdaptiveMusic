# MusicPack Structure
Below is the structure of a music pack:<br>
Pack Name<br>
├─ assets/<br>
│  └─ Audio Files<br>
├─ meta.json <b>(optional)</b><br>
└─ rules.json<br>

Packs can be directories or .zip, but must follow the exact structure above (with exception to those marked with <b>(optional)</b>)

## assets/
The assets folder should contain all of the audio files you plan to use in your modpack. If you are only using music that's loaded into minecraft (vanilla or modded), this directory should be empty.

As of right now, the only supported audio file type is .ogg, though we plan to try and add .mp3 and .wav support later on

## meta.json
This file is optional but can contain any extra data to display your pack. Right now there is only one field this stores, the description of the pack, and there is currently no way to set this within the UI, so if you really want to set it, you'll have to go into the meta.json generated with your mod and change it yourself.

<b>meta.json</b>
```json
{
    "description": "{Your mod description text here}"
}
```

### Fields
description: This text will show under your MusicPack's name in the pack selection screen.

## rules.json
This is the <b>most important file in the pack</b>, as it contains all of the logic that tells the mod when to play certain music. This json file defines the <b>PredicateTree</b> structure, which is a tree made of nodes that each represent a condition where certain music should be played. For a list of conditions (predicate types), see the [Predicate Types](Predicate%20Types.md) section.

The structure of the rules is recursive, with one object called a PredicateNode. A Predicate Node contains the Predicate Type, a list of music to play (either by sound event ID or by filename in the assets/ folder), and a list of all of its child nodes. This is visited in more depth within the [How a is a Predicate Node Chosen](../index.md#how-is-a-predicate-node-chosen) section of the Quick Start.

For example:

```json title="rules.json"
{
    "type": "root", //(1)!
    "musicPath": ["minecraft:music.overworld.meadow", "cool_song.ogg"], //(2)!
    "children": [ //(3)!
        {
            "type": "dimension",
            "id": "minecraft:overworld", //(4)!
            "musicPath": [] //(5)!
        }
    ]
}
```

1. The top-level node is always of type "root"
2. You can use audio files from assets/ and minecraft sound events that start with "music."
3. And now this node's children, which themselves have the same structure
4. Some predicates require extra parameters, such as an id field. This predicate will only be true when the player is in the overworld dimension
5. Since music path is empty, no music will play when this predicate is true