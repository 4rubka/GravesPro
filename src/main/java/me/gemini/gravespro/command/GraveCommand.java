package me.gemini.gravespro.command;

import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.model.Grave;
import me.gemini.gravespro.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GraveCommand implements CommandExecutor, TabCompleter {

    private final GravesPro plugin;

    public GraveCommand(GravesPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "locate" -> handleLocate(sender);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleLocate(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("only-players")));
            return;
        }

        List<Grave> playerGraves = plugin.getGraveManager().getActiveGraves().values().stream()
                .filter(g -> g.getOwnerId().equals(player.getUniqueId()))
                .collect(Collectors.toList());

        if (playerGraves.isEmpty()) {
            player.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("locate-no-graves")));
            return;
        }

        player.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("locate-header")));
        for (Grave grave : playerGraves) {
            String msg = plugin.getConfigurationManager().getConfig("messages.yml").getString("locate-format")
                    .replace("<world>", grave.getLocation().getWorld().getName())
                    .replace("<x>", String.valueOf(grave.getLocation().getBlockX()))
                    .replace("<y>", String.valueOf(grave.getLocation().getBlockY()))
                    .replace("<z>", String.valueOf(grave.getLocation().getBlockZ()))
                    .replace("<remaining_time>", formatTime(grave.getRemainingTime()));
            player.sendMessage(TextUtil.parse(msg));
        }
        
        plugin.getParticleManager().toggleLocate(player);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("gravespro.admin")) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("no-permission")));
            return;
        }

        plugin.getConfigurationManager().reloadConfigs();
        sender.sendMessage(TextUtil.parse(plugin.getConfigurationManager().getConfig("messages.yml").getString("reload-success")));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.parse("<gold>GravesPro Help:"));
        sender.sendMessage(TextUtil.parse("<gray>/grave locate <white>- Locate your graves"));
        if (sender.hasPermission("gravespro.admin")) {
            sender.sendMessage(TextUtil.parse("<gray>/grave reload <white>- Reload configurations"));
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(List.of("locate"));
            if (sender.hasPermission("gravespro.admin")) {
                subcommands.add("reload");
            }
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
