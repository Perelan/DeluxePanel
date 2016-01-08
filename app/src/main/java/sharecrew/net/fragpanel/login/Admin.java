package sharecrew.net.fragpanel.login;

import java.util.ArrayList;

public class Admin {

    private String name, steamid, username, password;

    public Admin(String name, String steamid, String username, String password) {
        this.name     = name;
        this.steamid  = steamid;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getSteamid() {
        return steamid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "name='" + name + '\'' +
                ", steamid='" + steamid + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
