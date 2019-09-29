package haveric.vehicleStorage.messages;

import org.bukkit.ChatColor;

public class MessageUtil {
    public static String parseColors(String message, boolean removeColors) {
        String parsedColors = null;

        if (message != null) {
            for (ChatColor color : ChatColor.values()) {
                String colorString = "";

                if (!removeColors) {
                    colorString = color.toString();
                }

                message = message.replaceAll("(?i)<" + color.name() + ">", colorString);
            }

            if (removeColors) {
                parsedColors = ChatColor.stripColor(message);
            } else {
                parsedColors = ChatColor.translateAlternateColorCodes('&', message);
            }
        }

        return parsedColors;
    }
}
