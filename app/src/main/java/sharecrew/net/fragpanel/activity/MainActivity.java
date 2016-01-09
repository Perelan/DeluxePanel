package sharecrew.net.fragpanel.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import sharecrew.net.fragpanel.reports.HTTPFetchSteam;
import sharecrew.net.fragpanel.reports.HTTPReportRequest;
import sharecrew.net.fragpanel.reports.Report;
import sharecrew.net.fragpanel.R;
import sharecrew.net.fragpanel.adapter.ListAdapter;
import sharecrew.net.fragpanel.login.AdminSession;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private AdminSession as;
    private TextView admin_name;
    private TextView admin_steamid;
    private ArrayList<Report> list;
    private TextView status;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recView;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recView = (RecyclerView) findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recView.setLayoutManager(llm);

        status = (TextView) findViewById(R.id.status_message);
        status.setVisibility(View.GONE);

        handle_list();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        as = new AdminSession(this);

        admin_name = (TextView) findViewById(R.id.admin_name);
        admin_steamid = (TextView) findViewById(R.id.admin_steamid);

        admin_name.setText(as.getAdminSession().getName());
        admin_steamid.setText(as.getAdminSession().getSteamid());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.blue, R.color.green, R.color.purple);
        mSwipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    handle_list();
                }
            }
        );
    }


    public void handle_list(){

        try{
            list = new AsyncList(this).execute().get();
        }catch (Exception e){
            e.getStackTrace();
        }

        if(list != null){
            ListAdapter la = new ListAdapter(list);
            recView.setAdapter(la);
        }else{
            status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!as.isLoggedIn()){
            startActivity(new Intent(getApplicationContext(), Splash.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action

        } else if (id == R.id.logout) {
            as.clear();
            as.setAdminLoggedIn(false);
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class AsyncList extends AsyncTask<Void, Void, ArrayList<Report>> {

        private Activity activity;
        ArrayList<Report> list;

        public AsyncList(Activity activity){
            this.activity = activity;
            list = new ArrayList<>();
        }

        @Override
        protected ArrayList<Report> doInBackground(Void... params) {

            HTTPReportRequest rr = new HTTPReportRequest();
            String s = rr.connect();

            if(s.equals("404")){
                return null;
            }
            Log.v("Connect String: ", "" + s);
            String[] fetch_sep_report = s.split("</br>");
            Log.v("Fetch Sep String: ", "" + Arrays.toString(fetch_sep_report));
            for(int i = 0; i < fetch_sep_report.length; i++){
                String[] temp = fetch_sep_report[i].split("\\|");

                String reported_avatar  = new HTTPFetchSteam(temp[5]).fetch_steam_avatar();
                String reporting_avatar = new HTTPFetchSteam(temp[9]).fetch_steam_avatar();

                list.add(new Report(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7],
                                    temp[8], temp[9], temp[10], temp[11], temp[12], temp[13], reported_avatar, reporting_avatar));
            }
            System.out.println(list.toString());

            return list;
        }




        @Override
        protected void onPostExecute(ArrayList<Report> result) {
            super.onPostExecute(result);
            //Here you can update the view

            // Notify swipeRefreshLayout that the refresh has finished
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
