package com.github.Brianetta.ArenaControl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
public class ArenaControl extends JavaPlugin{

    @Override
    public void onEnable ()
    {
        saveDefaultConfig();
    }

    @Override
    public void onDisable ()
    {
        saveConfig();
    }

    // Define some strings. These are sub-commands.
    final String cmd_assign = "assign";      // Assign a template to an arena
    final String cmd_arena = "arena";        // Define, remove or list arenas
    final String cmd_template = "template";  // Define, remove or list templates
    final String cmd_list = "list";          // sub-sub-command
    final String cmd_define = "define";      // sub-sub-command
    final String cmd_remove = "remove";      // sub-sub-command

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (cmd.getName().equalsIgnoreCase("arenacontrol")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase(cmd_assign)) {
                    // Assign a template to an arena
                    if (args.length == 3) {
                        // Copy template arg[2] into arena arg[1]
                    } else {
                        sender.sendMessage("You must specify the arena, then the template");
                    }
                } else if (args[0].equalsIgnoreCase(cmd_arena)) {
                    // Define, remove or list arena
                    if(args.length > 1) {
                        if (args[1].equalsIgnoreCase(cmd_list)) {
                            // List all defined arenas
                            for (String arena : getConfig().getKeys(true)) {
                                // Magic numbers:
                                // 6 is the position of the '.' in "arenas."
                                // 7 is the position of the next character.
                                if (arena.startsWith("arenas.") && arena.lastIndexOf(".") == 6) {
                                    arena = arena.substring(7);
                                    sender.sendMessage("Arena: " + arena);
                                } else continue;
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_define)) {
                            // Define a new arena
                            if (args.length == 9) {
                                Integer corner1X, corner1Y, corner1Z,
                                        corner2X, corner2Y, corner2Z;
                                try {
                                    // Convert all the numeric arguments
                                    corner1X = Integer.parseInt(args[3]);
                                    corner1Y = Integer.parseInt(args[4]);
                                    corner1Z = Integer.parseInt(args[5]);
                                    corner2X = Integer.parseInt(args[6]);
                                    corner2Y = Integer.parseInt(args[7]);
                                    corner2Z = Integer.parseInt(args[8]);
                                } catch (NumberFormatException exception) {
                                    // One or more of the numeric arguments didn't parse
                                    sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
                                    sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2");
                                    // No useful input, so drop out of the command handler now.
                                    return true;
                                }
                                // Now compare and sort the coordinates, so that we're using a consistent corner.
                                // The lowest coordinate will be chosen as corner1, and the highest will be corner2.
                                Integer temp;
                                if (corner2X < corner1X) {temp = corner2X; corner2X = corner1X; corner1X = temp;}
                                if (corner2Y < corner1Y) {temp = corner2Y; corner2Y = corner1Y; corner1Y = temp;}
                                if (corner2Z < corner1Z) {temp = corner2Z; corner2Z = corner1Z; corner1Z = temp;}
                                sender.sendMessage("Arena \"" + args[2] + "\" defined from ("+corner1X.toString()+","+corner1Y.toString()+","+corner1Z.toString()+
                                        ") to ("+corner2X.toString()+","+corner2Y.toString()+","+corner2Z.toString()+")");
                                // Save this arena into the config
                                getConfig().set("arenas." + args[2] + ".X1",corner1X);
                                getConfig().set("arenas." + args[2] + ".Y1",corner1Y);
                                getConfig().set("arenas." + args[2] + ".Z1",corner1Z);
                                getConfig().set("arenas." + args[2] + ".X2",corner2X);
                                getConfig().set("arenas." + args[2] + ".Y2",corner2Y);
                                getConfig().set("arenas." + args[2] + ".Z2",corner2Z);
                            } else {
                                // Wrong number of arguments for definition of an arena
                                sender.sendMessage("Expecting the name, then six numbers: X Y Z for corner 1, X Y Z for corner 2");
                                sender.sendMessage("/arenacontrol " + cmd_arena + " " + cmd_define + " <name> X1 Y1 Z1 X2 Y2 Z2");
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                            // Remove an arena
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
                    // Define, remove or list template
                    if(args.length > 1) {
                        if (args[1].equalsIgnoreCase(cmd_list)) {
                            // List all defined templates
                        } else if (args[1].equalsIgnoreCase(cmd_define)) {
                            // Define a new template
                            if (args.length == 6) {
                                Integer corner1X, corner1Y, corner1Z;
                                try {
                                    // Convert all the numeric arguments
                                    corner1X = Integer.parseInt(args[3]);
                                    corner1Y = Integer.parseInt(args[4]);
                                    corner1Z = Integer.parseInt(args[5]);
                                } catch (NumberFormatException exception) {
                                    // One or more of the numeric arguments didn't parse
                                    sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
                                    sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z");
                                    // No useful input, so drop out of the command handler now.
                                    return true;
                                }
                                sender.sendMessage("Arena \"" + args[2] + "\" defined from ("+corner1X.toString()+","+corner1Y.toString()+","+corner1Z.toString()+")");
                            } else {
                                // Wrong number of arguments for definition of a template
                                sender.sendMessage("Expecting the name, then three numbers: X Y Z for the bottom corner");
                                sender.sendMessage("/arenacontrol " + cmd_template + " " + cmd_define + " <name> X Y Z");
                            }
                        } else if (args[1].equalsIgnoreCase(cmd_remove)) {
                            // Remove an template
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

}
