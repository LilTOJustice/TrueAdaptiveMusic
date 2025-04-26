# MusicPack Structure

Below is the structure of a music pack:  
Pack Name  
├─ assets/  
│  └─ Audio Files  
├─ meta.json **(optional)**  
└─ rules.json  

Packs can be directories or .zip, but must follow the exact structure above.

## assets/

The assets folder should contain all the audio files you plan to use in your modpack. If you are only using music that's loaded into minecraft (vanilla or modded), this directory should be empty.

By default, the only supported audio file type is .ogg. In order to use packs that utilize other audio types, follow [this guide](../FFmpeg%20Support.md).

## meta.json

This file is optional but can contain any extra data to display your pack. Right now there is only one field this stores, the description of the pack, and there is currently no way to set this within the UI, so if you really want to set it, you'll have to go into the meta.json generated with your mod and change it yourself.

```json title="meta.json"
{
    "description": "{Your music pack description text here}"
}
```

## rules.json

This is the **most important file in the pack**, as it contains all of the logic that tells the mod when to play certain music. This json file defines the **PredicateTree** structure, which is a tree made of nodes that each represent a condition where certain music should be played. For a list of conditions (predicate types), see the [Predicates](Predicates.md) section. Additionally, each node in the tree contains a list of events that can trigger one-shot music playback when certain events occur. Check out [Events](Events.md) for more info.

The structure of the rules is recursive, with one object called a PredicateNode. A Predicate Node contains the Predicate Type, a list of music to play (either by sound event ID or by filename in the assets/ folder), any events associated with it, and a list of all of its child nodes. This is visited in more depth within the [How is a Predicate Node Chosen](./Predicates.md#how-is-a-predicate-node-chosen) section of the Predicates page.

For example:

```json title="rules.json"
{
    "type": "root", //(1)!
    "musicPath": [
        "minecraft:music.overworld.meadow",
        "cool_song.ogg",
        "cooler_song.mp3"
    ], //(2)!
    "children": [ //(3)!
        {
            "type": "dimension",
            "id": "minecraft:overworld", //(4)!
            "musicPath": [], //(5)!
            "events": []
        }
    ],
    "events": [ //(6)!
        {
            "type": "on_death",
            "musicPath": [
                "death_music.mp3"
            ]
        }
    ]
}
```

1. The top-level node is always of type "root"
2. You can use audio files from assets/ and minecraft sound events that start with "music.". Even non-ogg files are supported [if you have FFmpeg installed](../FFmpeg%20Support.md).
3. And now this node's children, which themselves have the same structure
4. Some predicates require extra parameters, such as the 'id' field here. This predicate will only be true when the player is in the overworld dimension
5. Since music path is empty, no music will play when this predicate is true
6. If the player dies, "death_music.mp3" will play. Events are passed down to the children of the predicate node they are defined in, so events defined under root will always when that event occurs unless overriden by a child.

Learn more about [events](Events.md) and [predicates](Predicates.md)
