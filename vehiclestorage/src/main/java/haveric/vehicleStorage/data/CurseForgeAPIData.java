package haveric.vehicleStorage.data;

// Used for serializing / deserializing data from curseforge api

public class CurseForgeAPIData {

    String name;
    String downloadUrl;

    public String getName() {
        return this.name;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setName(final String newName) {
        this.name = newName;
    }

    public void setDownloadUrl(final String newDownloadUrl) {
        this.downloadUrl = newDownloadUrl;
    }
}