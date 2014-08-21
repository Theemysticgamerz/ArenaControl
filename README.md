# ArenaControl

A Bukkit plugin designed to allow non-administrative staff to replace the floor
of an arena based on a template, in order to enable staff without access to
WorldEdit to host parkour or spleef events.

The plugin works by copying the type and data of blocks from one pre-defined
cuboid volume (a template) into another (an arena). Although the intention was
for it to be used for parkour courses or spleef floors, it can be used to
switch the contents of entire rooms, or switch entire buildings.

## Installation

Simply place the .jar file into your Bukkit server's plugin directory. There's
no initial configuration needed.

### Permissions

The plugin uses two permissions: **ArenaControl.modify** and **ArenaControl.apply**.
The former allows a player to specify source and target volumes, while the
latter allows a player to ask the plugin to perform the copy.

# Usage

The plugin provides a single command, **/arenacontrol**. The plugin is
controlled by means of a hierarchy of sub-commands. There are two aliases
configured; **/arenactl** and **/actl**. Additionally, there is a sign interface
to allow easy switching of arenas.

## Configuring the arenas

An arena is a target volume. The **arena** sub-command has several sub-commands
of its own. To make an arena, use the **define** command:

    /arenacontrol arena define <name> <x1> <y1> <z1> <x2> <y2> <z2> [world]

- *name* - A single word defining the arena. Case sensitive.
- *x1* - X coordinate of one of the corners of the arena volume
- *y1* - Y coordinate of one of the corners of the arena volume
- *z1* - Z coordinate of one of the corners of the arena volume
- *x2* - X coordinate of the opposite corner of the arena volume
- *y2* - Y coordinate of the opposite corner of the arena volume
- *z2* - Z coordinate of the opposite corner of the arena volume
- *world* - (optional except from the console) the world in which the arena will be. Defaults to the player's current world.

Arenas can be listed with the **list** command:

    /arenacontrol arena list

This will list all of the defined arenas, one line at a time. To see the details of a specific arena, specify its name:

    /arenacontrol arena list <name>

Again, *name* is case sensitive.

Finally, an arena can be forgotten with the **remove** command:

    /arenacontrol arena remove <name>

Arenas are stored in the plugin's config.yml file.

## Configuring the templates

A template is a source volume. The **arena** sub-command has several sub-commands
of its own. To make a template, use the **define** command:

    /arenacontrol arena define <name> <x> <y> <z> [world]

- *name* - A single word defining the template. Case sensitive.
- *x* - Least X corner coordinate of the template volume
- *y* - Least Y corner coordinate of the template volume
- *z* - Least Z corner coordinate of the template volume
- *world* - (optional except from the console) the world in which the template will be. Defaults to the player's current world.

Templates can be listed with the **list** command:

    /arenacontrol arena list

This will list all of the defined templates, one line at a time. To see the details of a specific template, specify its name:

    /arenacontrol arena list <name>

Again, *name* is case sensitive.

Finally, a template can be forgotten with the **remove** command:

    /arenacontrol arena remove <name>

Templates are stored in the plugin's config.yml file.

## Applying the templates to the arenas

Once at least one template and at least one arena have been defined, a player can
use the **assign** command to fill the arena with blocks from the template. Blocks
are copied from the template's defined corner, in increasing X, Y and Z, to the
extent required to fill the arena. So, if an arena measures 10x20x2, a 10x20x2
volume will be copied from the template's coordinates into the arena. The template
will technically always be big enough to fill an arena, but it's the player's
responsibility to ensure that enough blocks have been placed in the template area
to fill the arena.

The copy process uses two passes. First, all non-transparent blocks are copied,
then all blocks are copied. This allows the template to contain torches, trapdoors,
redstone and other objects that depend on an adjacent opaque block.

Not all informatioon is copied, for performance reasons. The intention is to provide
varied parkour courses, or to rebuild a spleef arena floor, so this plugin will
not attempt to preserve the content of a sign, or the inventory of chests, dispensers,
droppers, hoppers and so forth.

### Using the command

The command to copy a template to an arena is very simple:

    /arenacontrol assign <arena> <template>

- *arena* - A single word defining the arena. Case sensitive.
- *template* - A single word defining the template. Case sensitive.

### Using a sign

A sign can be used instead of typing in the command. Once the sign has been placed,
a right-click by a player with the **ArenaControl.apply** permission will cause
the arena specified on the second line of the sign to be populated with blocks
copied form the template specified on the third line of the sign. The sign must
have three lines. Only players with the ability to assign an arena can build such
a sign.

The first line must be the literal string, **[ArenaCtrl]**. This is not case
sensitive, but must include the square brackets. *Note that this has changed
from previous versions!*

The second line must contain only the name of an arena. This is case sensitive.

The third line must contain only the name of a template. This is case sensitive.

The fourth line is ignored, and may contain any arbitrary text.

An example, for a spleef arena called *spleef1* using a template of snow blocks
named *flatsnow*:

    [ArenaCtrl]
       spleef1
      flatsnow
    Reset spleef!

The first line of the sign will be coloured green if valid, red if invalid.

In addition to **[ArenaCtrl]** is **[ArenaLock]**, which acts the same but can
only be placed or operated by somebody with **ArenaControl.modify** permissions.  