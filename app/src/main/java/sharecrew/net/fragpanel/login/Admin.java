package sharecrew.net.fragpanel.login;

import java.util.ArrayList;

public class Admin {

    private String id, name, steamid, steamid64, username, password, superadmin, avatar;

    public Admin(String id, String name, String steamid, String steamid64, String username, String password, String superadmin, String avatar) {
        this.id         = id;
        this.name       = name;
        this.steamid64  = steamid64;
        this.steamid    = steamid;
        this.password   = password;
        this.superadmin = superadmin;
        this.username   = username;
        this.avatar     = avatar;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteamid() {
        return steamid;
    }

    public String getSteamid64() {
        return steamid64;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSuperadmin() {
        return superadmin;
    }

    public String getAvatar(){
        return avatar;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", steamid='" + steamid + '\'' +
                ", steamid64='" + steamid64 + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", superadmin='" + superadmin + '\'' +
                '}';
    }
}
