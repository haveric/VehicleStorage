package haveric.vehicleStorage;

import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.command.CommandSender;

import java.io.*;

public class Files {
    public static final String FILE_USED_VERSION = "used.version";
    public static final String FILE_CHANGELOG = "changelog.txt";

    protected static void init() {
    }

    protected static void reload(CommandSender sender) {
        new Files(sender);
    }

    private Files(CommandSender newSender) {
        boolean overwrite = isNewVersion();
        createFile(FILE_CHANGELOG, overwrite);

        if (overwrite) {
            MessageSender.getInstance().sendAndLog(newSender, "<gray>New version installed. Changelog have been updated.");
        }
    }

    private boolean isNewVersion() {
        boolean newVersion = true;

        try {
            File file = new File(VehicleStorage.getPlugin().getDataFolder() + File.separator + FILE_USED_VERSION);
            String currentVersion = VehicleStorage.getPlugin().getDescription().getVersion();

            if (file.exists()) {
                BufferedReader b = new BufferedReader(new FileReader(file));
                String version = b.readLine();
                b.close();
                newVersion = (version == null || !version.equals(currentVersion));
            }

            if (newVersion || file.exists()) {
                BufferedWriter b = new BufferedWriter(new FileWriter(file, false));
                b.write(currentVersion);
                b.close();
            }
        } catch (Throwable e) {
            MessageSender.getInstance().error(null, e, null);
        }

        return newVersion;
    }

    private void createFile(String file, boolean overwrite) {
        if (fileExists(file, overwrite)) {
            return;
        }

        VehicleStorage.getPlugin().saveResource(file, true);
    }

    private boolean fileExists(String file, boolean overwrite) {
        if (overwrite) {
            return false;
        }

        return new File(VehicleStorage.getPlugin().getDataFolder() + File.separator + file).exists();
    }
}
