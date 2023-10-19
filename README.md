# ClientCommands

Adds a couple client utilities in the form of commands.

## Curent tools

All commands are subcommands of `/client`
|Command|Description|Example|
|---|---|---|
|`give <item> [amount]`|**Creative only** (alias: `/cive`)<br>Gives the player any items, with any NBT. Uses the vanilla item and nbt syntax.|`give apple{display:{Name:'{"text":"abc"}'}} 1`|
|`rename <name> [italic]`|**Creative only**<br>Renames the item the player is currently holding. Simple syntax for quick renaming of items. Can set `italic` to false.|`rename "Cool Sword" false`|
|`rename json <name>`|Same as above, but accepts any valid text component(s).|`rename json {"text":"Cool Sword","color":"gold"}`|
|`enchant add <enchantment> <level>`|**Creative only**<br>Puts the desired enchantment on the player's currently held item. Does not have the limitations the vanilla command has.|`enchant add minecraft:depth_strider 10`|
|`enchant clear`|Clears all enchantments on held item.|`enchant clear`|
|`player-head <name>`|**Creative Only**<br>Get the head of a player.|`player-head jeb_`|
