package sharecrew.net.fragpanel.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import sharecrew.net.fragpanel.extra.Utility;
import sharecrew.net.fragpanel.reports.HTTPFetchSteam;
import sharecrew.net.fragpanel.reports.HTTPReportRequest;
import sharecrew.net.fragpanel.reports.HTTPUpdateData;
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
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recView.setLayoutManager(llm);

        handle_list();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        handle_list();
                    }
                }
        );
        /*
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                handle_action(recyclerView, reverseSortedPositions);
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                handle_action(recyclerView, reverseSortedPositions);
                                la.onItemRemove(viewHolder, recView);

                            }

                            public void handle_action(RecyclerView recyclerView, int[] reverseSortedPositions){
                                for(final int position : reverseSortedPositions){
                                    final ArrayList<Report> temp = new ArrayList<>(list.size());
                                    temp.add(list.get(position));

                                    new UpdateDataTask("update_report.php?", "1").execute(list.get(position).getReport_id());
                                    final int store_position = position;
                                    list.remove(position);
                                    la.notifyItemRemoved(position);

                                    Snackbar snackbak = Snackbar.make(recyclerView, "The report were removed! Restore it while you can.", Snackbar.LENGTH_LONG);
                                    snackbak.setAction("UNDO", new View.OnClickListener() {

                                                @Override
                                                public void onClick(View view) {
                                                    for (int i = 0; i < temp.size(); i++) {
                                                        list.add(temp.get(i));
                                                    }

                                                    System.out.println(list);

                                                    new UpdateDataTask("update_report.php?", "0").execute(list.get(store_position).getReport_id());
                                                    la.notifyItemInserted(position);
                                                }
                                            }).setActionTextColor(Color.RED);

                                    snackbak.show();
                                }
                                la.notifyDataSetChanged();
                            }
                        });
        recView.addOnItemTouchListener(swipeTouchListener);*/


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
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
    }

    public void handle_list(){

        try{
            list = new AsyncList().execute().get();
        }catch (Exception e){
            e.getStackTrace();
        }

        la = new ListAdapter(list);
        recView.setAdapter(la);

        if(list != null){
            status.setVisibility(View.GONE);
        }else{
            status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    public class UpdateDataTask extends AsyncTask<String, Void, Void> {

        private String link;
        private String value;
        UpdateDataTask(String link, String value){
            this.link = link;
            this.value = value;
        }

        protected final Void doInBackground(String... params) {
            HashMap<String, String> list = new HashMap<>();
            list.put("key", Utility.KEY);
            list.put("id", params[0]);
            list.put("complete", value);
            new HTTPUpdateData(link).update_data(list);
            return null;
        }
    }
}
