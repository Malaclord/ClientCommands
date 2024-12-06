# ClientCommands

Adds a couple client utilities in the form of commands.

## Curent tools

All commands are subcommands of `/client`
|Command|Description|Example|
|---|---|---|
|`give <item> [amount]`|**Creative only** (alias: `/cive`)<br>Gives the player any itemstack, with any NBT. Vanilla syntax.|`give apple{display:{Name:'{"text":"abc"}'}} 1`|
|`rename <name> [customName]`|**Creative only**<br>Renames the item the player is currently holding. Simple syntax for quick renaming of items. Set `customName` to true to mimic anvil rename behavior.|`rename "Cool Sword" false`|
|`rename json <name>`|Same as above, but accepts any valid text component(s).|`rename json {"text":"Cool Sword","color":"gold"}`|
|`enchant add <enchantment> <level>`|**Creative only**<br>Puts the desired enchantment on the player's currently held item. Does not have the limitations the vanilla command has.|`enchant add minecraft:depth_strider 10`|
|`enchant clear`|**Creative only**<br>Clears all enchantments on held item.|`enchant clear`|
|`enchant remove <enchantment>`|**Creative only**<br>Removes enchantment from player's currently held item.|`enchant remove minecraft:depth_strider`|
|`modifier list`|Lists attribute modifiers on player's currently held item.|`modifier list`|
|`potion create <type> <effect> [<amplifier>] [infinite\|<duration>] [<hideParticles>]`|**Creative only**<br>Creates potion item of type (normal/splash/linger) with specified effect.|`potion create linger minecraft:blindness 0 infinite`|
|`potion modify color (<color>\|hex) [<colorHex>]`|**Creative only**<br>Modify the color of the held potion to one of the 16 Minecraft colors or to a custom hexadecimal color|`potion modify color hex ffaa00`|
|`potion modify effect set <effect> <amplifier> (infinite\|<duration>) [<hideParticles>]`|**Creative only**<br>Set the details of a potion effect in the held potion.|`potion modify effect set minecraft:glowing 0 100`|
|`potion modify effect remove <effect>`|**Creative only**<br>Completely removes potion effect from held potion.|`potion modify effect remove minecraft:haste`|
|`potion modify type <type>`|**Creative only**<br>Changes the potion's type (normal/splash/linger)|`potion modify type linger`|
|`modifier add <attribute> <operation> <value> <slot>`|**Creative only**<br>Add attribute modifier to held item.|`modifier add minecraft:generic.armor_toughness add_multiplied_base 2 armor`|
|`modifier remove <uuid>`|**Creative only**<br>Remove attribute modifier from held item.|`modifier remove bae7d9d5-6221-45e3-9d14-4956229557f2`|
|`modifier modify <uuid> <attribute\|operation\|value\|slot\|> <value>`|**Creative only**<br>Lists attribute modifiers on player's currently held item.|`modifier modify bae7d9d5-6221-45e3-9d14-4956229557f2 slot hand`|
|`components set`|**Creative Only**<br>Set item components for held item.|`components set [food={saturation:1,nutrition:10}]`|
|`components merge`|**Creative only**<br>Merge item components into held item.|`components merge [unbreakable={}]`|
|`components get`|Get item components of held item.|`components get`|

## Additional features
- Long (32K) character limit on chat messages

## Translators
- Latvian: @Foxytisy

## Planned tools / features
- [x] Give
- [x] Rename
- [x] Enchant
- [x] Modifiers
- [x] Playerhead
- [x] Potions
- [x] Longer NBT length (long chat limit)
