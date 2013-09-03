package com.github.Brianetta.ArenaControl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        getLogger().info(getConfig().getString("testString"));
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
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//            } else {
//            }
            if(args.length > 0) {
                if(args[0].equalsIgnoreCase(cmd_assign)){
                    // Assign a template to an arena
                } else if (args[0].equalsIgnoreCase(cmd_arena)) {
                    // Define, remove or list arena
                } else if (args[0].equalsIgnoreCase(cmd_template)) {
                    // Define, remove or list template
                }
            }
            return true;
        }
        return false;
    }

}
