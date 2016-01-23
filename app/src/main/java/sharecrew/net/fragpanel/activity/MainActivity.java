package sharecrew.net.fragpanel.activity;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import sharecrew.net.fragpanel.extra.Utility;
import sharecrew.net.fragpanel.reports.HTTPFetchSteam;
import sharecrew.net.fragpanel.reports.HTTPReportRequest;
import sharecrew.net.fragpanel.reports.Report;
import sharecrew.net.fragpanel.R;
import sharecrew.net.fragpanel.adapter.ListAdapter;
import sharecrew.net.fragpanel.login.AdminSession;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, getListListener{

    private AdminSession as;
    private TextView admin_name;
    private TextView admin_steamid;
    private TextView admin_steamid64;
    private ArrayList<Report> mList;
    private TextView status;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recView;
    private ListAdapter la;
    private final String TAG = "*** MAIN";
    private ImageView avatar;
    private getListListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        as = new AdminSession(this);
        listener = this;

        if(!as.isLoggedIn()){
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            finish();
        }

        admin_name      = (TextView) findViewById(R.id.admin_name);
        admin_steamid   = (TextView) findViewById(R.id.admin_steamid);
        admin_steamid64 = (TextView) findViewById(R.id.steam_id64);

        admin_name.setText(as.getAdminSession().getName());
        admin_steamid.setText(as.getAdminSession().getSteamid());
        admin_steamid64.setText(as.getAdminSession().getSteamid64());

        status = (TextView) findViewById(R.id.status_message);
        avatar = (ImageView) findViewById(R.id.icon);

        Picasso.with(this).load(as.getAdminSession().getAvatar()).into(avatar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        recView.setVisibility(View.GONE);
                        new AsyncList(listener).execute();
                    }
                }
        );
        mSwipeRefreshLayout.setRefreshing(true);

        recView = (RecyclerView) findViewById(R.id.recycler_view);
        recView.setHasFixedSize(true);
        GridLayoutManager glm;

        if(Utility.isTablet(this)){
            glm = new GridLayoutManager(this, 2);
        }else{
            glm = new GridLayoutManager(this, 1);
        }
        glm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(glm);

        new AsyncList(this).execute();
    }

    @Override
    protected void onStart() {

        super.onStart();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                String admin_name = mList.get(viewHolder.getAdapterPosition()).getAdmin_name();
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

    @Override
    public void onDataFetch(ArrayList<Report> list) {

        if(!Utility.isNetworkAvailable(this)){
            status.setVisibility(View.VISIBLE);
            status.setText("Please connect to a network!");
        }else{
            try{
                mList = list;
                la = new ListAdapter(mList, this);
                recView.setAdapter(la);

                if(list != null){
                    status.setVisibility(View.GONE);
                }else{
                    status.setText("No Entries!");
                    status.setVisibility(View.VISIBLE);
                }

                recView.setVisibility(View.VISIBLE);

            }catch (Exception e){
                e.getStackTrace();
            }
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

        private getListListener listener;
        private ProgressDialog dialog;

        public AsyncList(getListListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            this.dialog.setTitle("Please wait");
            this.dialog.setMessage("Loading all reports. This might take some time.");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

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
            listener.onDataFetch(result);
            mSwipeRefreshLayout.setRefreshing(false);
            dialog.dismiss();
        }
    }
}
