package haveric.vehicleStorage.commands;

import haveric.vehicleStorage.VehicleStorage;
import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class DefaultCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginDescriptionFile desc = VehicleStorage.getPlugin().getDescription();

        MessageSender.getInstance().send(sender, ChatColor.YELLOW + "---- " + ChatColor.WHITE + desc.getFullName() + ChatColor.GRAY + " by haveric " + ChatColor.YELLOW + "----");
        return true;
    }
}
