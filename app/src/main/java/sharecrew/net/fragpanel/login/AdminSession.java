package sharecrew.net.fragpanel.login;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class AdminSession {

    public static final String STORE = "deluxepanel_storeage";
    SharedPreferences sp;

    public AdminSession(Context context){
        sp = context.getSharedPreferences(STORE, 0);
    }

    public void setAdminSession(Admin a){
        SharedPreferences.Editor spEdit = sp.edit();

        spEdit.putString("id",          a.getId());
        spEdit.putString("name",        a.getName());
        spEdit.putString("steamid",     a.getSteamid());
        spEdit.putString("steamid64",   a.getSteamid64());
        spEdit.putString("username",    a.getUsername());
        spEdit.putString("password",    a.getPassword());
        spEdit.putString("superadmin",  a.getSuperadmin());
        spEdit.putString("avatar",      a.getAvatar());

        spEdit.apply();
    }

    public Admin getAdminSession(){
        String id           = sp.getString("id", "DEFAULT");
        String name         = sp.getString("name", "DEFAULT");
        String steamid      = sp.getString("steamid", "DEFAULT");
        String steamid64    = sp.getString("steamid64", "DEFAULT");
        String username     = sp.getString("username", "DEFAULT");
        String password     = sp.getString("password", "DEFAULT");
        String superadmin   = sp.getString("superadmin", "DEFAULT");
        String avatar       = sp.getString("avatar", "DEFAULT");

        return new Admin(id, name, steamid, steamid64, username, password, superadmin, avatar);
    }

    public boolean isLoggedIn() { return sp.getBoolean("logged", false); }

    public void setAdminLoggedIn(boolean value){
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putBoolean("logged", value);
        spEdit.apply();
    }

    public void clear(){
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.clear();
        spEdit.apply();
    }
}
