package net.SimplyCrafted.ArenaControl;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaControl extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    private void assignTemplateToArena(String arena, String template, CommandSender sender) {
        if (getConfig().getString("arenas." + arena + ".X1") == null) {
            sender.sendMessage("cArena \"" + arena + "\" is not defined.");
            return;
        }
        if (getConfig().getString("templates." + template + ".X") == null) {
            sender.sendMessage("cTemplate \"" + template + "\" is not defined.");
            return;
        }

        int[] arenaCoords = new int[]{
            getConfig().getInt("arenas." + arena + ".X1"),
            getConfig().getInt("arenas." + arena + ".Y1"),
            getConfig().getInt("arenas." + arena + ".Z1"),
            getConfig().getInt("arenas." + arena + ".X2"),
            getConfig().getInt("arenas." + arena + ".Y2"),
            getConfig().getInt("arenas." + arena + ".Z2")
        };
        int[] templateCoords = new int[]{
            getConfig().getInt("templates." + template + ".X"),
            getConfig().getInt("templates." + template + ".Y"),
            getConfig().getInt("templates." + template + ".Z")
        };

        World arenaWorld = getServer().getWorld(getConfig().getString("arenas." + arena + ".world"));
        World templateWorld = getServer().getWorld(getConfig().getString("templates." + template + ".world"));

        if (arenaWorld == null || templateWorld == null) {
            sender.sendMessage("cWorld not found.");
            return;
        }

        int[] offset = new int[]{
            templateCoords[0] - arenaCoords[0],
            templateCoords[1] - arenaCoords[1],
            templateCoords[2] - arenaCoords[2]
        };

        for (int x = arenaCoords[0]; x <= arenaCoords[3]; x++) {
            for (int y = arenaCoords[1]; y <= arenaCoords[4]; y++) {
                for (int z = arenaCoords[2]; z <= arenaCoords[5]; z++) {
                    Block from = templateWorld.getBlockAt(x + offset[0], y + offset[1], z + offset[2]);
                    if (!from.isEmpty()) {
                        arenaWorld.getBlockAt(x, y, z).setBlockData(from.getBlockData(), false);
                    }
                }
            }
        }

        sender.sendMessage("aTemplate \"" + template + "\" applied to arena \"" + arena + "\".");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!sign.getLine(0).equalsIgnoreCase("[ArenaControl]")) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("ArenaControl.apply")) {
            player.sendMessage("cYou don't have permission to apply a template.");
            event.setCancelled(true);
            return;
        }

        assignTemplateToArena(sign.getLine(1), sign.getLine(2), player);
        event.setCancelled(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("arenacontrol") || args.length < 1) return false;

        switch (args[0].toLowerCase()) {
            case "assign":
                if (args.length != 3) {
                    sender.sendMessage("cUsage: /arenacontrol assign <arena> <template>");
                    return true;
                }
                if (sender instanceof Player && !sender.hasPermission("ArenaControl.apply")) {
                    sender.sendMessage("cYou lack permission to assign templates.");
                    return true;
                }
                assignTemplateToArena(args[1], args[2], sender);
                return true;
            case "arena":
                handleArenaSubcommand(sender, args);
                return true;
            case "template":
                handleTemplateSubcommand(sender, args);
                return true;
            default:
                sender.sendMessage("cUnknown subcommand. Available: assign, arena, template");
                return true;
        }
    }

    private void handleArenaSubcommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("cUsage: /arenacontrol arena <list|define|remove> ...");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list":
                listArena(sender, args);
                break;
            case "define":
                defineArena(sender, args);
                break;
            case "remove":
                removeArena(sender, args);
                break;
            default:
                sender.sendMessage("cUnknown arena subcommand: " + args[1]);
        }
    }

    private void listArena(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String name = args[2];
            if (getConfig().getString("arenas." + name + ".X1") == null) {
                sender.sendMessage("cArena \"" + name + "\" not found.");
            } else {
                sender.sendMessage("Arena \"" + name + "\" from (" +
                    getConfig().getString("arenas." + name + ".X1") + ", " +
                    getConfig().getString("arenas." + name + ".Y1") + ", " +
                    getConfig().getString("arenas." + name + ".Z1") + ") to (" +
                    getConfig().getString("arenas." + name + ".X2") + ", " +
                    getConfig().getString("arenas." + name + ".Y2") + ", " +
                    getConfig().getString("arenas." + name + ".Z2") + ") in world " +
                    getConfig().getString("arenas." + name + ".world")
                );
            }
        } else {
            getConfig().getConfigurationSection("arenas").getKeys(false)
                .forEach(k -> sender.sendMessage("Arena: " + k));
        }
    }

    private void defineArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("ArenaControl.modify")) {
            sender.sendMessage("cNo permission to define arenas.");
            return;
        }
        if (args.length < 9) {
            sender.sendMessage("cUsage: /arenacontrol arena define <name> X1 Y1 Z1 X2 Y2 Z2 [World]");
            return;
        }
        try {
            int[] coords = new int[6];
            for (int i = 0; i < 6; i++) {
                coords[i] = Integer.parseInt(args[i + 3]);
            }
            for (int i = 0; i < 3; i++) {
                if (coords[i + 3] < coords[i]) {
                    int temp = coords[i];
                    coords[i] = coords[i + 3];
                    coords[i + 3] = temp;
                }
            }
            String world = (args.length > 9) ? args[9] : ((Player) sender).getWorld().getName();
            for (int i = 0; i < 6; i++) {
                getConfig().set("arenas." + args[2] + "." + "XYZ".charAt(i % 3) + (i / 3 + 1), coords[i]);
            }
            getConfig().set("arenas." + args[2] + ".world", world);
            sender.sendMessage("aArena \"" + args[2] + "\" defined.");
        } catch (NumberFormatException e) {
            sender.sendMessage("cCoordinates must be numbers.");
        }
    }

    private void removeArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("ArenaControl.modify")) {
            sender.sendMessage("cNo permission to remove arenas.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("cUsage: /arenacontrol arena remove <name>");
            return;
        }
        getConfig().set("arenas." + args[2], null);
        sender.sendMessage("aArena \"" + args[2] + "\" removed.");
    }

    private void handleTemplateSubcommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("cUsage: /arenacontrol template <list|define|remove> ...");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list":
                listTemplate(sender, args);
                break;
            case "define":
                defineTemplate(sender, args);
                break;
            case "remove":
                removeTemplate(sender, args);
                break;
            default:
                sender.sendMessage("cUnknown template subcommand: " + args[1]);
        }
    }

    private void listTemplate(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String name = args[2];
            if (getConfig().getString("templates." + name + ".X") == null) {
                sender.sendMessage("cTemplate \"" + name + "\" not found.");
            } else {
                sender.sendMessage("Template \"" + name + "\" at (" +
                    getConfig().getString("templates." + name + ".X") + ", " +
                    getConfig().getString("templates." + name + ".Y") + ", " +
                    getConfig().getString("templates." + name + ".Z") + ") in world " +
                    getConfig().getString("templates." + name + ".world")
                );
            }
        } else {
            getConfig().getConfigurationSection("templates").getKeys(false)
                .forEach(k -> sender.sendMessage("Template: " + k));
        }
    }

    private void defineTemplate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("ArenaControl.modify")) {
            sender.sendMessage("cNo permission to define templates.");
            return;
        }
        if (args.length < 6) {
            sender.sendMessage("cUsage: /arenacontrol template define <name> X Y Z [World]");
            return;
        }
        try {
            for (int i = 0; i < 3; i++) {
                getConfig().set("templates." + args[2] + "." + "XYZ".charAt(i), Integer.parseInt(args[i + 3]));
            }
            String world = (args.length > 6) ? args[6] : ((Player) sender).getWorld().getName();
            getConfig().set("templates." + args[2] + ".world", world);
            sender.sendMessage("aTemplate \"" + args[2] + "\" defined.");
        } catch (NumberFormatException e) {
            sender.sendMessage("cCoordinates must be numbers.");
        }
    }

    private void removeTemplate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("ArenaControl.modify")) {
            sender.sendMessage("cNo permission to remove templates.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("cUsage: /arenacontrol template remove <name>");
            return;
        }
        getConfig().set("templates." + args[2], null);
        sender.sendMessage("aTemplate \"" + args[2] + "\" removed.");
    }
}
