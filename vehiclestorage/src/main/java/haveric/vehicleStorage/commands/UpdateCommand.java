package haveric.vehicleStorage.commands;

import haveric.vehicleStorage.Updater;
import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        MessageSender.getInstance().sendAndLog(sender, ChatColor.GRAY + "Checking for updates...");

        Updater.updateOnce(sender);

        return true;
    }
}
