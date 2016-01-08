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

        spEdit.putString("name", a.getName());
        spEdit.putString("steamid", a.getSteamid());
        spEdit.putString("username", a.getUsername());
        spEdit.putString("password", a.getPassword());

        spEdit.apply();
    }

    public Admin getAdminSession(){
        String name     = sp.getString("name", "DEFAULT");
        String steamid  = sp.getString("steamid", "DEFAULT");
        String username = sp.getString("username", "DEFAULT");
        String password = sp.getString("password", "DEFAULT");

        return new Admin(name, steamid, username, password);
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
