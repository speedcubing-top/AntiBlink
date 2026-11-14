package top.speedcubing.antiblink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlinkCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;

    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "set", "status");
    private static final List<String> KEYS = Arrays.asList("enabled", "burst-gap-ms", "burst-count", "block-action-ms", "op-bypass", "staff-alert", "debug");

    public BlinkCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("blinkguard.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /acblink <reload|set|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                configManager.load();
                sender.sendMessage("§a[BlinkGuard] Config reloaded.");
                return true;

            case "set":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /acblink set <key> <value>");
                    return true;
                }
                if (configManager.set(args[1], args[2])) {
                    sender.sendMessage("§a[BlinkGuard] Set §e" + args[1] + " §ato §e" + args[2]);
                } else {
                    sender.sendMessage("§c[BlinkGuard] Unknown key or invalid value: " + args[1]);
                }
                return true;

            case "status":
                sender.sendMessage("§8§m-----------------------");
                sender.sendMessage("§6BlinkGuard Status");
                sender.sendMessage("§7Enabled: §f" + configManager.enabled);
                sender.sendMessage("§7Burst Gap: §f" + configManager.burstGapMs + "ms");
                sender.sendMessage("§7Burst Count: §f" + configManager.burstCount);
                sender.sendMessage("§7Block Action: §f" + configManager.blockActionMs + "ms");
                sender.sendMessage("§7OP Bypass: §f" + configManager.opBypass);
                sender.sendMessage("§7Staff Alert: §f" + configManager.staffAlert);
                sender.sendMessage("§7Debug: §f" + configManager.debug);
                sender.sendMessage("§8§m-----------------------");
                return true;

            default:
                sender.sendMessage("§cUnknown subcommand.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUB_COMMANDS) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            for (String key : KEYS) {
                if (key.startsWith(args[1].toLowerCase())) completions.add(key);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String key = args[1].toLowerCase();
            if (key.equals("enabled") || key.equals("op-bypass") || key.equals("debug") || key.equals("staff-alert")) {
                if ("true".startsWith(args[2].toLowerCase())) completions.add("true");
                if ("false".startsWith(args[2].toLowerCase())) completions.add("false");
            }
        }

        return completions;
    }
}