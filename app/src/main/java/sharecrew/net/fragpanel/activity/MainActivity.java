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
import android.widget.TextView;
import android.widget.Toast;

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
    private Admin admin;
    private TextView admin_name;
    private TextView admin_steamid;
    private ArrayList<Report> list;
    private TextView status;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recView;
    private ListAdapter la;
    private final String TAG = "*** MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        status = (TextView) findViewById(R.id.status_message);

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

        as = new AdminSession(this);
        admin = as.getAdminSession();

        admin_name = (TextView) findViewById(R.id.admin_name);
        admin_steamid = (TextView) findViewById(R.id.admin_steamid);

        admin_name.setText(as.getAdminSession().getName());
        admin_steamid.setText(as.getAdminSession().getSteamid());

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                String admin_name = list.get(viewHolder.getAdapterPosition()).getAdmin_name();
                final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return !admin_name.equals(admin.getName()) ? 0 : makeMovementFlags(0, swipeFlags);
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

            String[] fetch_sep_report = s.split("</br>");
            Log.v("Fetch Sep String: ", Arrays.toString(fetch_sep_report));
            for(int i = 0; i < fetch_sep_report.length; i++){
                String[] temp = fetch_sep_report[i].split("\\|");

                String reported_avatar  = new HTTPFetchSteam(temp[5]).fetch_steam_avatar();
                String reporting_avatar = new HTTPFetchSteam(temp[10]).fetch_steam_avatar();

                list.add(new Report(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7],
                                    temp[8], temp[9], temp[10], temp[11], temp[12], temp[13], temp[14],
                                    reported_avatar, reporting_avatar));
            }
            System.out.println(list.toString());

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Report> result) {
            super.onPostExecute(result);

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
