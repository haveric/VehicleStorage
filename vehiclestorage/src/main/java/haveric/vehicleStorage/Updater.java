package haveric.vehicleStorage;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import haveric.vehicleStorage.messages.MessageSender;
import haveric.vehicleStorage.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater {
 // The project's unique ID
    private static int projectID;

    // An optional API token to use, will be null if not submitted
    private static String apiToken;

    // Keys for extracting file information from JSON response
    private static final String API_NAME_VALUE = "name";
    private static final String API_LINK_VALUE = "downloadUrl";

    // Static information for querying the API
    private static final String API_QUERY = "/servermods/files?projectIds=";
    private static final String API_HOST = "https://api.curseforge.com";

    // Only used to link the user to manually download files
    private static String urlFiles;
    private static VehicleStorage plugin;
    private static String pluginName;

    private static FileDetails latestFile = null;

    private static BukkitTask task = null;

    private Updater() { } // Private constructor for utility class

    /**
     * Check for updates using your Curse account (with key)
     *
     * @param newProjectID The BukkitDev Project ID, found in the "About This Project" panel on the right-side of your project page.
     * @param newApiToken Your curseforge API Token, found at https://authors.curseforge.com/knowledge-base/projects/529-api
     */
    public static void init(VehicleStorage newPlugin, int newProjectID, String newApiToken) {
        plugin = newPlugin;
        urlFiles = plugin.getDescription().getWebsite() + "files";
        pluginName = plugin.getDescription().getName();
        projectID = newProjectID;
        apiToken = newApiToken;
        stop();

        updateOnce(null); // Do one initial check

        int time = Settings.getInstance().getUpdateCheckFrequency();

        if (time > 0) {
            time *= 60 * 60 * 20;
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> query(null), time, time);
        }
    }

    public static void updateOnce(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(VehicleStorage.getPlugin(), () -> query(sender));
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public static String getCurrentVersion() {
        String currentVersion = plugin.getDescription().getVersion();

        Matcher matcher = getMatcher(currentVersion);
        if (matcher.find()) {
            currentVersion = getVersion(matcher);
        }

        return currentVersion;
    }

    public static String getLatestVersion() {
        String latest = latestFile.name;
        if (latest != null) {
            Matcher matcher = getMatcher(latest);

            if (matcher.find()) {
                latest = getVersion(matcher);
            }
        }

        return latest;
    }

    protected static Matcher getMatcher(String original) {
        String versionRegex = "v?([0-9.]+)[ -]?((dev|alpha|beta)?[0-9 -]*)?";
        Pattern pattern = Pattern.compile(versionRegex);

        return pattern.matcher(original);
    }

    protected static String getVersion(Matcher matcher) {
        return matcher.group(1).replaceAll(" v", "");
    }

    /**
     *
     * @return compare<br>
     *  1: Current version is newer than the BukkitDev<br>
     *  0: Same version as BukkitDev<br>
     * -1: BukkitDev is newer than current version
     * -2: Error occurred
     */
    public static int compareVersions() {
        int compare = -2;

        String current = getCurrentVersion();
        String latest = getLatestVersion();

        if (latest != null) {
            if (current.equals(latest)) {
                compare = 0;
            } else {
                String[] currentArray = current.split("\\.");
                String[] latestArray = latest.split("\\.");

                int shortest = currentArray.length;
                int latestLength = latestArray.length;
                if (latestLength < shortest) {
                    shortest = latestLength;
                }

                for (int i = 0; i < shortest; i++) {
                    int c = Integer.parseInt(currentArray[i]);
                    int l = Integer.parseInt(latestArray[i]);

                    if (c > l) {
                        compare = 1;
                        break;
                    } else if (l > c) {
                        compare = -1;
                        break;
                    }
                }

                // Same up to the shortest version
                if (compare == -2) {
                    if (currentArray.length > latestLength) {
                        compare = 1;
                    } else {
                        compare = -1;
                    }
                }
            }
        }

        return compare;
    }

    public static String getLatestLink() {
        return latestFile.downloadUrl;
    }

    /**
     * Query the API to find the latest approved file's details.
     */
    public static void query(CommandSender sender) {
        if (Settings.getInstance().getUpdateCheckEnabled()) {

            try {
                // Create the URL to query using the project's ID
                URL url = new URL(API_HOST + API_QUERY + projectID);

                // Open a connection and query the project
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);

                if (apiToken != null && !apiToken.equals("")) {
                    // Add the API key to the request if present
                    conn.addRequestProperty("X-API-Token", apiToken);
                }

                // Add the user-agent to identify the program
                conn.addRequestProperty("User-Agent", pluginName + "/v" + getCurrentVersion() + " (by haveric)");

                // Read the response of the query and parse the array of files from the query's response
                final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                FileDetails[] fileDetails = new Gson().fromJson(reader, FileDetails[].class);

                if (fileDetails.length > 0) {
                    // Get the newest file's details
                    latestFile = fileDetails[0];
                }

                if (latestFile.name == null) {
                    if (sender != null) { // send this message only if it's a requested update check
                        MessageSender.getInstance().sendAndLog(sender, "<red>Unable to check for updates, please check manually by visiting:<yellow> " + urlFiles);
                    } else {
                        return; // block the disable message
                    }
                } else {
                    String currentVersion = getCurrentVersion();
                    String latest = getLatestVersion();

                    if (latest != null) {
                        int compare = compareVersions();

                        if (compare == 0) {
                            if (sender != null) { // send this message only if it's a requested update check
                                MessageSender.getInstance().sendAndLog(sender, "<gray>Using the latest version: " + latest);
                            } else {
                                return; // block the disable message
                            }
                        } else if (compare == -1) {
                            MessageSender.getInstance().sendAndLog(sender, "New version: <green>" + latest + "<reset>! You're using <yellow>" + currentVersion);
                            MessageSender.getInstance().sendAndLog(sender, "Grab it at: <green>" + latestFile.downloadUrl);
                        } else if (compare == 1) {
                            MessageSender.getInstance().sendAndLog(sender, "<gray>You are using a newer version: <green>" + currentVersion + "<reset>. Latest on BukkitDev: <yellow>" + latest);
                        }
                    }
                }

                if (sender == null) {
                    MessageSender.getInstance().sendAndLog(null, "<gray>You can disable this check from config.yml.");
                }
            } catch (IOException e) {
                MessageSender.getInstance().info("<red>There was an error while checking for updates");
            }
        }
    }

    private class FileDetails {

        @SerializedName("downloadUrl")
        @Expose
        public String downloadUrl;
        @SerializedName("name")
        @Expose
        public String name;

    }
}