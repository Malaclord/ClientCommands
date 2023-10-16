# ClientCommands

Adds a couple client utilities in the form of commands.

## Curent tools

All commands are subcommands of `/client`
|Command|Description|Example|
|---|---|---|
|`give <item> [amount]`|**Creative only**<br>Gives the player any itemstack, with any NBT. Uses ItemStackArgument, so the regular syntax applies.|`give apple{display:{Name:'{"text":"abc"}'}} 1`|
|`rename <name> [italic]`|**Creative only**<br>Renames the item the player is currently holding. Simple syntax for quick renaming of items. Can set `italic` to false.|`rename "Cool Sword" false`|
|`rename json <name>`|Same as above, but accepts any valid text component(s).|`rename json {"text":"Cool Sword","color":"gold"}`|

## Planned tools / features
- [x] Give
- [x] Rename
- [ ] Enchant
- [ ] Modifiers (?)
- [ ] Playerhead
- [ ] Longer NBT length (not sure how yet)
