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
|`modifier add <attribute> <operation> <value> <slot>`|**Creative only**<br>Add attribute modifier to held item.|`modifier add minecraft:generic.armor_toughness add_multiplied_base 2 armor`|
|`modifier remove <uuid>`|**Creative only**<br>Remove attribute modifier from held item.|`modifier remove bae7d9d5-6221-45e3-9d14-4956229557f2`|
|`modifier modify <uuid> <attribute\|operation\|value\|slot\|> <value>`|**Creative only**<br>Lists attribute modifiers on player's currently held item.|`modifier modify bae7d9d5-6221-45e3-9d14-4956229557f2 slot hand`|

## Additional features
- Long (32K) character limit on chat messages

## Planned tools / features
- [x] Give
- [x] Rename
- [x] Enchant
- [x] Modifiers
- [x] Playerhead
- [x] Potions
- [x] Longer NBT length (long chat limit)
