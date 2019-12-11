package haveric.vehicleStorage.commands;

import haveric.vehicleStorage.VehicleStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        VehicleStorage.getPlugin().reload(sender);

        return true;
    }
}
