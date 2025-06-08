package net.SimplyCrafted.ArenaControl;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Copyright © Brian Ronald
 * 02/09/13
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class ArenaControl extends JavaPlugin implements Listener {

    WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }
        if (worldEdit != null) {
            getLogger().info("WorldEdit detected - can use WorldEdit selection");
        } else {
            getLogger().info("WorldEdit not detected - no selection tool available");
        }
    }

    @Override
    public void onDisable() {
        saveConfig();
        PlayerInteractEvent.getHandlerList().unregister((Listener) this);
    }

    private void assignTemplateToArena(String arena, String template, CommandSender sender) {
        int arenaX1, arenaY1, arenaZ1;
        int arenaX2, arenaY2, arenaZ2;
        boolean templateMode = true;
        int templateX1, templateY1, templateZ1;
        int offsetX, offsetY, offsetZ;
        World templateWorld, arenaWorld;

        // Sanity checking - make sure the arena exists by looking for its X1
        if (getConfig().getString("arenas." + arena + ".X1") == null) {
            sender.sendMessage("Arena " + arena + " is not defined");
            return;
        }
        // Sanity checking - make sure the template exists by looking for its X
        if (getConfig().getString("templates." + template + ".X") == null) {
            if (Material.getMaterial(template) == null) {
                sender.sendMessage("Template/material " + template + " is not defined");
                return;
            } else {
                templateMode = false;
            }
        }

        // Populate local variables
        arenaX1 = getConfig().getInt("arenas." + arena + ".X1");
        arenaY1 = getConfig().getInt("arenas." + arena + ".Y1");
        arenaZ1 = getConfig().getInt("arenas." + arena + ".Z1");
        arenaX2 = getConfig().getInt("arenas." + arena + ".X2");
        arenaY2 = getConfig().getInt("arenas." + arena + ".Y2");
        arenaZ2 = getConfig().getInt("arenas." + arena + ".Z2");
        arenaWorld = getServer().getWorld(getConfig().getString("arenas." + arena + ".world"));
        if (templateMode) {
            templateX1 = getConfig().getInt("templates." + template + ".X");
            templateY1 = getConfig().getInt("templates." + template + ".Y");
            templateZ1 = getConfig().getInt("templates." + template + ".Z");
            // Set the opposite corner of the template using the dimensions of the arena
            offsetX = templateX1 - arenaX1;
            offsetY = templateY1 - arenaY1;
            offsetZ = templateZ1 - arenaZ1;
            templateWorld = getServer().getWorld(getConfig().getString("templates." + template + ".world"));

            // Copy the opaque blocks from the template to the arena, block by block.
            Material BlockType;
            byte BlockData;
            for (int iZ = arenaZ1; iZ <= arenaZ2; iZ++) {
                for (int iY = arenaY1; iY <= arenaY2; iY++) {
                    for (int iX = arenaX1; iX <= arenaX2; iX++) {
                        if (!(templateWorld.getBlockAt(iX + offsetX, iY + offsetY, iZ + offsetZ).getType().isTransparent())) {
                            BlockType = templateWorld.getBlockAt(iX + offsetX, iY + offsetY, iZ + offsetZ).getType();
                            BlockData = templateWorld.getBlockAt(iX + offsetX, iY + offsetY, iZ + offsetZ).getData();
                            arenaWorld.getBlockAt(iX, iY, iZ).setType(BlockType, false);
                            arenaWorld.getBlockAt(iX, iY, iZ).setData(BlockData, false);
                        }
                    }
                }
            }
            // Re-copy the entire template to the arena, block by block.
            for (int iZ = arenaZ1; iZ <= arenaZ2; iZ++) {
                for (int iY = arenaY1; iY <= arenaY2; iY++) {
                    for (int iX = arenaX1; iX <= arenaX2; iX++) {
                        BlockType = templateWorld.getBlockAt(iX + offsetX, iY + offsetY, iZ + offsetZ).getType();
                        BlockData = templateWorld.getBlockAt(iX + offsetX, iY + offsetY, iZ + offsetZ).getData();
                        arenaWorld.getBlockAt(iX, iY, iZ).setType(BlockType, false);
                        arenaWorld.getBlockAt(iX, iY, iZ).setData(BlockData,false);
                    }
                }
            }
            // Message to confirm that
            sender.sendMessage("Template " + template + " has been copied to arena " + arena);
        } else {
            Material material = Material.getMaterial(template);
            for (int iZ = arenaZ1; iZ <= arenaZ2; iZ++) {
                for (int iY = arenaY1; iY <= arenaY2; iY++) {
                    for (int iX = arenaX1; iX <= arenaX2; iX++) {
                        arenaWorld.getBlockAt(iX, iY, iZ).setType(material);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent (PlayerInteractEvent event) {
        // Check whether the block still exists
        if (event.getClickedBlock() == null) return;
        // Check the player did something to a sign
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;
        // Check that that something was a right click
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        // Check that this is one of *our* signs, and check permissions
        if ((sign.getLine(0).equalsIgnoreCase("§a[ArenaCtrl]") && event.getPlayer().hasPermission("ArenaControl.apply")) ||
                (sign.getLine(0).equalsIgnoreCase("§a[ArenaLock]") && event.getPlayer().hasPermission("ArenaControl.modify"))) {
            // Assign the template on the third line to the
            // arena on the second line
            assignTemplateToArena(sign.getLine(1), sign.getLine(2), event.getPlayer());
            // Successful, so eat the event.
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChangeEvent (SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getLine(0).equalsIgnoreCase("[ArenaCtrl]") || event.getLine(0).equalsIgnoreCase("§a[ArenaControl]") || event.getLine(0).equalsIgnoreCase("&a[ArenaControl]")) {
            if (!event.getPlayer().hasPermission("ArenaControl.modify")) {
                event.setLine(0,"§4[ArenaCtrl]");
                event.getPlayer().sendMessage("No permission to create an ArenaControl sign");
            } else {
                if (getConfig().getString("arenas." + event.getLine(1) + ".X1") == null) {
                    // The name wasn't that of a defined arena
                    event.getPlayer().sendMessage("Arena " + event.getLine(1) + " not found");
                    event.setLine(0, "§4[ArenaCtrl]");
                } else if ((getConfig().getString("templates." + event.getLine(2) + ".X") == null) && Material.getMaterial(event.getLine(2)) == null) {
                    // The name wasn't that of a defined arena
                    event.getPlayer().sendMessage("Template/material " + event.getLine(2) + " not found");
                    event.setLine(0, "§4[ArenaCtrl]");
                } else {
                    event.setLine(0, "§a[ArenaCtrl]");
                }
            }
        }
        if (event.getLine(0).equalsIgnoreCase("[ArenaLock]") || event.getLine(0).equalsIgnoreCase("§a[ArenaLocked]") || event.getLine(0).equalsIgnoreCase("&a[ArenaLocked]")) {
            if (!event.getPlayer().hasPermission("ArenaControl.modify")) {
                event.setLine(0, "§4[ArenaLock]");
                event.getPlayer().sendMessage("No permission to create a locked ArenaControl sign");
            } else {
                if (getConfig().getString("arenas." + event.getLine(1) + ".X1") == null) {
                    // The name wasn't that of a defined arena
                    event.getPlayer().sendMessage("Arena " + event.getLine(1) + " not found");
                    event.setLine(0,"§4[ArenaLock]");
                } else if (getConfig().getString("templates." + event.getLine(2) + ".X") == null) {
                    // The name wasn't that of a defined arena
                    event.getPlayer().sendMessage("Template " + event.getLine(2) + " not found");
                    event.setLine(0, "§4[ArenaLock]");
                } else {
                    event.setLine(0,"§a[ArenaLock]");
                }
            }
        }
    }

    // Define some strings. These are sub-commands.
    final String cmd_assign = "assign";      // Assign a template to an arena
    final String cmd_arena = "arena";        // Define, remove or list arenas
    final String cmd_template = "template";  // Define, remove or list templates
    final String cmd_list = "list";          // sub-sub-command
    final String cmd_define = "define";      // sub-sub-command
    final String cmd_remove = "remove";      // sub-sub-command

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("arenacontrol")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase(cmd_assign)) {

                    /* * * ARENACONTROL ASSIGN * * */

                    // Permission check
                    if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                        sender.sendMessage("You do not have permission to run this command");
                        return true;
                    }
                    // Assign a template to an arena
                    if (args.length == 3) {
                        // Actually do something
                        assignTemplateToArena(args[1], args[2], sender);
                    } else {
                        sender.sendMessage("You must specify the arena, then the template");
                    }
                } else if (args[0].equalsIgnoreCase(cmd_arena)) {

                    /* * * ARENACONTROL ARENA * * */

                    // Define, remove or list arena
                    if(args.length > 1) {
                        if (args[1].equalsIgnoreCase(cmd_list)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Listing arenas
                            if (args.length > 2) {
                                // A name was specified after the "list" command
                                if (getConfig().getString("arenas." + args[2] + ".X1") == null) {
                                    // The name wasn't that of a defined arena
                                    sender.sendMessage("Arena " + args[2] + " not found");
                                    return true;
                                } else {
                                    // The name was that of a defined arena, so show it to the sender
                                    sender.sendMessage("Arena " + args[2] +
                                            ": from (X=" + getConfig().getString("arenas." + args[2] + ".X1") +
                                            ",Y=" + getConfig().getString("arenas." + args[2] + ".Y1") +
                                            ",Z=" + getConfig().getString("arenas." + args[2] + ".Z1") +
                                            ") to (X=" + getConfig().getString("arenas." + args[2] + ".X2") +
                                            ",Y=" + getConfig().getString("arenas." + args[2] + ".Y2") +
                                            ",Z=" + getConfig().getString("arenas." + args[2] + ".Z2") + ") in world "
                                            + getConfig().getString("arenas." + args[2] + ".world"));
                                }
                            } else {
                                // List all defined arenas
                                for (String arena : getConfig().getKeys(true)) {
                                    // Magic numbers:
                                    // 6 is the position of the '.' in "arenas."
                                    // 7 is the position of the next character.
                                    if (arena.startsWith("arenas.") && arena.lastIndexOf(".") == 6) {
                                        arena = arena.substring(7);
                                        sender.sendMessage("Arena: " + arena);
                                    }
                                }
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_define)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Define a new arena
                            if (args.length >= 9) {
                                String world = null;
                                if (args.length == 9) {
                                    if (sender instanceof Player) {
                                        world = ((Player) sender).getWorld().getName();
                                    }
                                } else {
                                    getLogger().info("" + args.length);
                                    world = args[9];
                                }
                                if (defineArena(sender, args[2], args[3],args[4],args[5],args[6],args[7],args[8], world)) return true;
                            } else if (args.length == 3 && sender instanceof Player && worldEdit != null && worldEdit.getSelection((Player) sender) != null) {
                                Selection selection = worldEdit.getSelection((Player) sender);
                                Location min = selection.getMinimumPoint();
                                Location max = selection.getMaximumPoint();
                                if (defineArena(sender, args[2], min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ(), min.getWorld().getName())) return true;
                            } else {
                                // Wrong number of arguments for definition of an arena
                                sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
                                sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2 [WorldID]");
                                if (worldEdit != null) {
                                    sender.sendMessage("Additionally, since WorldEdit is loaded, you can select with the");
                                    sender.sendMessage("WorldEdit wand and use /arenacontrol " + cmd_arena + " " + cmd_define + " <name>");
                                }
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Remove an arena
                            if (args.length > 2) {
                                // A name was specified
                                getConfig().set("arenas." + args[2],null);
                                sender.sendMessage("Removed arena: " + args[2]);
                            } else {
                                // No name was specified
                                sender.sendMessage("Expecting the name of an arena to remove");
                                sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_remove + " <name>");
                            }
                        } else {
                            // Something other than list, define or remove was provided as a command
                            sender.sendMessage("Not a valid sub-command");
                            sender.sendMessage(cmd_arena + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                        }
                    } else {
                        // Not enough arguments following "arena" command
                        sender.sendMessage("No sub-command provided");
                        sender.sendMessage(cmd_arena + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                    }
                } else if (args[0].equalsIgnoreCase(cmd_template)) {

                    /* * * ARENACONTROL TEMPLATE * * */

                    // Define, remove or list template
                    if(args.length > 1) {
                        if (args[1].equalsIgnoreCase(cmd_list)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Listing templates
                            if (args.length > 2) {
                                // A name was specified after the "list" command
                                if (getConfig().getString("templates." + args[2] + ".X") == null) {
                                    // The name wasn't that of a defined template
                                    sender.sendMessage("Template " + args[2] + " not found");
                                    return true;
                                } else {
                                    // The name was that of a defined template, so show it to the sender
                                    sender.sendMessage("Template " + args[2] +
                                            ": from (" + getConfig().getString("templates." + args[2] + ".X") +
                                            "," + getConfig().getString("templates." + args[2] + ".Y") +
                                            "," + getConfig().getString("templates." + args[2] + ".Z") + ") in world " +getConfig().getString("templates." + args[2] + ".world"));
                                }
                            } else {
                                // List all defined templates
                                for (String template : getConfig().getKeys(true)) {
                                    // Magic numbers:
                                    // 9 is the position of the '.' in "templates."
                                    // 10 is the position of the next character.
                                    if (template.startsWith("templates.") && template.lastIndexOf(".") == 9) {
                                        template = template.substring(10);
                                        sender.sendMessage("Template: " + template);
                                    }
                                }
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_define)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Define a new template
                            if (args.length >= 6) {
                                String world = null;
                                if (args.length == 6) {
                                    if (sender instanceof Player) {
                                        world = ((Player) sender).getWorld().getName();
                                    }
                                } else {
                                    getLogger().info("" + args.length);
                                    world = args[6];
                                }
                                if (defineTemplate(sender, args[2], args[3], args[4], args[5], world)) return true;
                            } else if (args.length == 3 && sender instanceof Player && worldEdit != null && worldEdit.getSelection((Player) sender) != null) {
                                Selection selection = worldEdit.getSelection((Player) sender);
                                Location min = selection.getMinimumPoint();
                                if (defineTemplate(sender, args[2], min.getBlockX(), min.getBlockY(), min.getBlockZ(), min.getWorld().getName()))
                                    return true;
                            } else {
                                // Wrong number of arguments for definition of a template
                                sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
                                sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
                                sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z [WorldID]");
                                if (worldEdit != null) {
                                    sender.sendMessage("Additionally, since WorldEdit is loaded, you can select with the");
                                    sender.sendMessage("WorldEdit wand and use /arenacontrol " + cmd_template + " " + cmd_define + " <name>");
                                }
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                            if (sender instanceof Player && !(sender.hasPermission("ArenaControl.modify"))) {
                                sender.sendMessage("You do not have permission to run this command");
                                return true;
                            }
                            // Remove a template
                            if (args.length > 2) {
                                // A name was specified
                                getConfig().set("templates." + args[2],null);
                                sender.sendMessage("Removed template: " + args[2]);
                            } else {
                                // No name was specified
                                sender.sendMessage("Expecting the name of a template to remove");
                                sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_remove + " <name>");
                            }
                        } else {
                            // Something other than list, define or remove was provided as a command
                            sender.sendMessage("Not a valid sub-command");
                            sender.sendMessage(cmd_template + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                        }
                    } else {
                        // Not enough arguments following "template" command
                        sender.sendMessage("No sub-command provided");
                        sender.sendMessage(cmd_template + " sub-commands: " + cmd_list + ", " + cmd_define + ", " + cmd_remove);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean defineTemplate(CommandSender sender, String name, String X, String Y, String Z, String world) {
        Integer corner1X, corner1Y, corner1Z;
        try {
            // Convert all the numeric arguments
            corner1X = Integer.parseInt(X);
            corner1Y = Integer.parseInt(Y);
            corner1Z = Integer.parseInt(Z);
        } catch (NumberFormatException exception) {
            // One or more of the numeric arguments didn't parse
            sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
            sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
            sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z [WorldID]");
            // No useful input, so drop out of the command handler now.
            return true;
        }
        return defineTemplate(sender, name, corner1X, corner1Y, corner1Z, world);
    }
    private boolean defineTemplate(CommandSender sender, String name, Integer corner1X, Integer corner1Y, Integer corner1Z, String world) {
        // Save this template into the config
        getConfig().set("templates." + name + ".X",corner1X);
        getConfig().set("templates." + name +  ".Y",corner1Y);
        getConfig().set("templates." + name + ".Z",corner1Z);
        if (world != null) {
            getConfig().set("templates." + name  + ".world",world);
            sender.sendMessage("Template \"" + name + "\" defined from (" + corner1X.toString() + "," + corner1Y.toString() + "," + corner1Z.toString() + ")");
        } else if (sender instanceof Player) {
            getConfig().set("templates." + name + ".world",((Player) sender).getWorld().getName());
            sender.sendMessage("Template \"" + name + "\" defined from (" + corner1X.toString() + "," + corner1Y.toString() + "," + corner1Z.toString() + ")");
        } else {
            sender.sendMessage("You are not a player; you must specify a world ID.");
        }
        return false;
    }

    private boolean defineArena(CommandSender sender, String name, String minX, String minY, String minZ, String maxX, String maxY, String maxZ, String world) {
        Integer corner1X, corner1Y, corner1Z,
                corner2X, corner2Y, corner2Z;
        try {
            // Convert all the numeric arguments
            corner1X = Integer.parseInt(minX);
            corner1Y = Integer.parseInt(minY);
            corner1Z = Integer.parseInt(minZ);
            corner2X = Integer.parseInt(maxX);
            corner2Y = Integer.parseInt(maxY);
            corner2Z = Integer.parseInt(maxZ);
        } catch (NumberFormatException exception) {
            // One or more of the numeric arguments didn't parse
            sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
            sender.sendMessage("then (optionally) the world identifier (defaults to current world)");
            sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2 [WorldID]");
            // No useful input, so drop out of the command handler now.
            return true;
        }
        // Now compare and sort the coordinates, so that we're using a consistent corner.
        // The lowest coordinate will be chosen as corner1, and the highest will be corner2.
        Integer temp;
        if (corner2X < corner1X) {
            temp = corner2X;
            corner2X = corner1X;
            corner1X = temp;
        }
        if (corner2Y < corner1Y) {
            temp = corner2Y;
            corner2Y = corner1Y;
            corner1Y = temp;
        }
        if (corner2Z < corner1Z) {
            temp = corner2Z;
            corner2Z = corner1Z;
            corner1Z = temp;
        }
        return defineArena(sender, name, corner1X, corner1Y, corner1Z, corner2X, corner2Y, corner2Z, world);
    }

    private boolean defineArena(CommandSender sender, String name, Integer corner1X, Integer corner1Y, Integer corner1Z, Integer corner2X, Integer corner2Y, Integer corner2Z, String world) {
        // Save this arena into the config
        getConfig().set("arenas." + name + ".X1", corner1X);
        getConfig().set("arenas." + name + ".Y1", corner1Y);
        getConfig().set("arenas." + name + ".Z1", corner1Z);
        getConfig().set("arenas." + name + ".X2", corner2X);
        getConfig().set("arenas." + name + ".Y2", corner2Y);
        getConfig().set("arenas." + name + ".Z2", corner2Z);
        if (world != null) {
            getConfig().set("arenas." + name + ".world", world);
            sender.sendMessage("Arena \"" + name + "\" defined from (X=" + corner1X.toString() + ",Y=" + corner1Y.toString() + ",Z=" + corner1Z.toString() +
                    ") to (X=" + corner2X.toString() + ",Y=" + corner2Y.toString() + ",Z=" + corner2Z.toString() + ")");
        } else if (sender instanceof Player) {
            getConfig().set("arenas." + name + ".world", ((Player) sender).getWorld().getName());
            sender.sendMessage("Arena \"" + name + "\" defined from (X=" + corner1X.toString() + ",Y=" + corner1Y.toString() + ",Z=" + corner1Z.toString() +
                    ") to (X=" + corner2X.toString() + ",Y=" + corner2Y.toString() + ",Z=" + corner2Z.toString() + ")");
        } else {
            sender.sendMessage("You are not a player; you must specify a world ID.");
        }
        return false;
    }

}
