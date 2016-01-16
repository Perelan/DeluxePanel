package sharecrew.net.fragpanel.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import sharecrew.net.fragpanel.login.Admin;
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
    private TextView admin_steamid64;
    private ArrayList<Report> list;
    private TextView status;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recView;
    private ListAdapter la;
    private final String TAG = "*** MAIN";
    private ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        as = new AdminSession(this);

        status = (TextView) findViewById(R.id.status_message);
        avatar = (ImageView) findViewById(R.id.icon);

        recView = (RecyclerView) findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);

        handle_list();

        la.setRecylerview(recView); // Set recyclerview inside of adapter to manage scrolltolocation

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        handle_list();
                    }
                }
        );

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Picasso.with(this).load(as.getAdminSession().getAvatar()).into(avatar);

        admin_name      = (TextView) findViewById(R.id.admin_name);
        admin_steamid   = (TextView) findViewById(R.id.admin_steamid);
        admin_steamid64 = (TextView) findViewById(R.id.steam_id64);

        admin_name.setText(as.getAdminSession().getName());
        admin_steamid.setText(as.getAdminSession().getSteamid());
        admin_steamid64.setText(as.getAdminSession().getSteamid64());

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                String admin_name = list.get(viewHolder.getAdapterPosition()).getAdmin_name();
                final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return !admin_name.equals(as.getAdminSession().getName()) ? 0 : makeMovementFlags(0, swipeFlags);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Fade out the view as it is swiped out of the parent's bounds
                    final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                la.onItemRemove(viewHolder, recView);
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recView);
    }

    public void handle_list(){

        try{
            list = new AsyncList().execute().get();
            la = new ListAdapter(list);
            recView.setAdapter(la);

            if(list != null){
                status.setVisibility(View.GONE);
            }else{
                status.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.getStackTrace();
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

        @Override
        protected ArrayList<Report> doInBackground(Void... params) {
            ArrayList<Report> list = new ArrayList<>();

            HTTPReportRequest rr = new HTTPReportRequest();
            String s = rr.connect();

            if(s.equals("404")){
                return null;
            }

            try {
                JSONArray jsonArray   = new JSONArray(s);
                JSONObject jsonObject;

                Log.v(TAG, "Fetched JSON: " + s);

                for(int i = 0; i < jsonArray.length(); i++){
                    jsonObject = jsonArray.getJSONObject(i);

                    String report_id                = jsonObject.get("report_id").toString();
                    String server_info              = jsonObject.get("server_info").toString();
                    String admin_name               = jsonObject.get("admin_name").toString();
                    String reported_name            = jsonObject.get("reported_name").toString();
                    String reported_steam_id        = jsonObject.get("reported_steam_id").toString();
                    String reported_steam_id64      = jsonObject.get("reported_steam_id64").toString();
                    String reported_num_reports     = jsonObject.get("reported_num_reports").toString();
                    String reporting_name           = jsonObject.get("reporting_name").toString();
                    String reporting_id             = jsonObject.get("reporting_id").toString();
                    String reporting_steam_id       = jsonObject.get("reporting_steam_id").toString();
                    String reporting_steam_id64     = jsonObject.get("reporting_steam_id64").toString();
                    String reporting_karma          = jsonObject.get("reporting_karma").toString();
                    String reason                   = jsonObject.get("reason").toString();
                    String complete                 = jsonObject.get("complete").toString();
                    String register_date            = jsonObject.get("register_date").toString();
                    String reported_avatar          = new HTTPFetchSteam(reported_steam_id64).fetch_steam_avatar();
                    String reporting_avatar         = new HTTPFetchSteam(reporting_steam_id64).fetch_steam_avatar();

                    list.add(new Report(report_id, server_info, admin_name,
                            reported_name, reported_steam_id, reported_steam_id64, reported_num_reports,
                            reporting_name, reporting_id, reporting_steam_id, reporting_steam_id64, reporting_karma,
                            reason, complete, register_date, reported_avatar, reporting_avatar
                            ));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Report> result) {
            super.onPostExecute(result);

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
