package sharecrew.net.fragpanel.adapter;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import sharecrew.net.fragpanel.R;
import sharecrew.net.fragpanel.extra.Utility;
import sharecrew.net.fragpanel.login.Admin;
import sharecrew.net.fragpanel.login.AdminSession;
import sharecrew.net.fragpanel.reports.HTTPUpdateData;
import sharecrew.net.fragpanel.reports.Report;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private ArrayList<Report> mDataset = null;
    private Admin admin;
    private final String TAG = "******* List Adapter";
    private ArrayList<Report> reportToDelete;

    public ListAdapter(ArrayList<Report> mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list, parent, false);
        admin = new AdminSession(v.getContext()).getAdminSession();
        reportToDelete = new ArrayList<>();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.suspect_name.setText(mDataset.get(position).getReported_name());
        holder.suspect_steamid.setText(mDataset.get(position).getReported_steam_id());
        holder.reporting_name.setText(mDataset.get(position).getReporting_name());
        holder.reporting_id.setText(mDataset.get(position).getReporting_id());
        holder.date.setText(Utility.convert_time(mDataset.get(position).getDate()));
        holder.server_name.setText(mDataset.get(position).getServer_name());
        holder.karma.setText(mDataset.get(position).getReporting_karma());
        holder.reason.setText(mDataset.get(position).getReason());
        System.out.println(mDataset.get(position).getReported_avatar());
        Picasso.with(holder.suspected_pic.getContext()).load(mDataset.get(position).getReported_avatar()).into(holder.suspected_pic);
        Picasso.with(holder.reporting_pic.getContext()).load(mDataset.get(position).getReporting_avatar()).into(holder.reporting_pic);
        holder.expandUp.setVisibility(View.GONE);
        holder.expandable.setVisibility(View.GONE);
        holder.task.setVisibility(View.GONE);

        if(!mDataset.get(position).getAdmin_name().equals("null")){
            holder.admin_name.setText(mDataset.get(position).getAdmin_name());
        }else{
            holder.admin_name.setText("empty");
            holder.admin_name.setTypeface(null, Typeface.ITALIC);
        }

        if(mDataset.get(position).getAdmin_name().equals("null")){
            holder.claim.setVisibility(View.VISIBLE);
            holder.kick.setVisibility(View.GONE);
            holder.ban.setVisibility(View.GONE);
            holder.mute.setVisibility(View.GONE);
        }else if(!mDataset.get(position).getAdmin_name().equals(admin.getName())){
            holder.task.setVisibility(View.VISIBLE);
            holder.claim.setVisibility(View.GONE);
            holder.kick.setVisibility(View.GONE);
            holder.ban.setVisibility(View.GONE);
            holder.mute.setVisibility(View.GONE);
        }else{
            holder.claim.setVisibility(View.GONE);
            holder.kick.setVisibility(View.VISIBLE);
            holder.ban.setVisibility(View.VISIBLE);
            holder.mute.setVisibility(View.VISIBLE);
        }
    }

    public ArrayList<Report> getReportToDelete() {
        return reportToDelete;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView){
        int adapterPosition = viewHolder.getAdapterPosition();
        final Report mReport = mDataset.get(adapterPosition);

        Snackbar snackbak = Snackbar
                .make(viewHolder.itemView, "Given report has been removed. Restore it while you can. :)", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        System.out.println(viewHolder.getAdapterPosition());
                        int mAdapterPosition = viewHolder.getAdapterPosition() + 1;
                        mDataset.add(mAdapterPosition , mReport);
                        notifyItemInserted(mAdapterPosition);
                        recyclerView.scrollToPosition(mAdapterPosition);
                        reportToDelete.remove(mReport);

                        HashMap<String, String> data_to_send = new HashMap<>();
                        data_to_send.put("id", mReport.getReport_id());
                        data_to_send.put("complete", "0");
                        new UpdateDataTask("update_report.php?").execute(data_to_send);

                    }
                }).setActionTextColor(Color.RED);

        snackbak.show();
        mDataset.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        reportToDelete.add(mReport);

        for(int i = 0; i < reportToDelete.size(); i++){
            HashMap<String, String> data_to_send = new HashMap<>();
            data_to_send.put("id", reportToDelete.get(i).getReport_id());
            data_to_send.put("complete", "1");
            new UpdateDataTask("update_report.php?").execute(data_to_send);
        }
    }

    /**
     * This class fetches placement of the containers and assign the rightful view to each one of them.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView server_name, admin_name, date, suspect_name, suspect_steamid,
                reporting_id, reporting_name, reason, karma, task;
        ImageView suspected_pic, reporting_pic;
        ViewGroup expandable;
        ImageView expandDown, expandUp, uparrow, downarrow;
        boolean isExpanded, isRepPressed;
        Button mute, kick, ban, claim;
        HashMap<String, String> data_to_send;

        public ViewHolder(View v) {
            super(v);
            cv              = (CardView) v.findViewById(R.id.card_view);
            server_name     = (TextView) v.findViewById(R.id.server_text);
            admin_name      = (TextView) v.findViewById(R.id.admin_name);
            date            = (TextView) v.findViewById(R.id.month_text);
            suspect_name    = (TextView) v.findViewById(R.id.suspect);
            suspect_steamid = (TextView) v.findViewById(R.id.reported_steamid);
            reporting_name  = (TextView) v.findViewById(R.id.reporter);
            reporting_id    = (TextView) v.findViewById(R.id.reporting_id);
            reason          = (TextView) v.findViewById(R.id.reason_text);
            karma           = (TextView) v.findViewById(R.id.karma_id);
            task            = (TextView) v.findViewById(R.id.taken_txt);
            suspected_pic   = (ImageView) v.findViewById(R.id.suspect_avatar);
            reporting_pic   = (ImageView) v.findViewById(R.id.reporter_avatar);
            expandable      = (ViewGroup) v.findViewById(R.id.expandable_part_layout);
            expandDown      = (ImageView) v.findViewById(R.id.expandDown);
            expandUp        = (ImageView) v.findViewById(R.id.expandUp);
            uparrow         = (ImageView) v.findViewById(R.id.uparrow);
            downarrow       = (ImageView) v.findViewById(R.id.downarrow);
            isExpanded      = false;
            mute            = (Button) v.findViewById(R.id.mute_btn);
            kick            = (Button) v.findViewById(R.id.kick_btn);
            ban             = (Button) v.findViewById(R.id.ban_btn);
            claim           = (Button) v.findViewById(R.id.claim_btn);
            isRepPressed    = false;
            data_to_send    = new HashMap<>();

            uparrow.setOnClickListener(this);
            downarrow.setOnClickListener(this);
            claim.setOnClickListener(this);

            v.setOnClickListener(this);
        }

        @SuppressWarnings("unchecked")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {

            // Handle the claim button
            if(v.getId() == R.id.claim_btn) {
                admin_name.setText(admin.getName());
                admin_name.setTypeface(null, Typeface.NORMAL);

                claim.setVisibility(View.GONE);
                mute.setVisibility(View.VISIBLE);
                kick.setVisibility(View.VISIBLE);
                ban.setVisibility(View.VISIBLE);
            // Handle karma upvote and downvote
            }else if (v.getId() == R.id.uparrow || v.getId() == R.id.downarrow) {
                if (!admin_name.getText().equals(admin.getName())) {
                    Toast.makeText(v.getContext(), "You've to be the claiming admin to do that!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    int karma_count = Integer.parseInt(karma.getText().toString());

                    if (v.getId() == R.id.uparrow) {
                        if (isRepPressed) {
                            karma_count += 2;
                        } else {
                            karma_count++;
                        }
                        uparrow.setEnabled(false);
                        downarrow.setEnabled(true);
                    } else {
                        if (isRepPressed) {
                            karma_count -= 2;
                        } else {
                            karma_count--;
                        }
                        uparrow.setEnabled(true);
                        downarrow.setEnabled(false);
                    }

                    karma.setText(String.format("%s", karma_count));
                    isRepPressed = true;

                    data_to_send.put("user", reporting_id.getText().toString());
                    data_to_send.put("karma", karma.getText().toString());
                    new UpdateDataTask("update_karma.php?").execute(data_to_send);
                }
            // Handle the expand view (able to press the whole card to expand).
            }else if(v.getId() == R.id.ban_btn || v.getId() == R.id.mute_btn || v.getId() == R.id.kick_btn){
                // STEAM ID - COMMAND - SERVER

                data_to_send.put("server", server_name.getText().toString().substring(1, server_name.getText().toString().length()));
                data_to_send.put("steamid", suspect_steamid.getText().toString());

                if(v.getId() == R.id.ban_btn)
                    data_to_send.put("command", "ban");
                else if(v.getId() == R.id.kick_btn)
                    data_to_send.put("command", "kick");
                else
                    data_to_send.put("command", "mute");


                new UpdateDataTask("handle_command.php?").execute(data_to_send);
            } else {
                if (!isExpanded) {
                    expandUp.setVisibility(View.VISIBLE);
                    expandDown.setVisibility(View.GONE);
                    expandable.setVisibility(View.VISIBLE);
                    isExpanded = true;
                } else {
                    expandUp.setVisibility(View.GONE);
                    expandDown.setVisibility(View.VISIBLE);
                    expandable.setVisibility(View.GONE);
                    isExpanded = false;
                }
            }
        }
    }

    /**
     * This part is probably a no-no, as I've read async task isn't good to have inside of
     * an recycler adapter. But I guess I'll change it in the future.
     */
    public class UpdateDataTask extends AsyncTask<HashMap<String, String>, Void, Void> {

        private String link;

        UpdateDataTask(String link){
            this.link = link;
        }

        @SafeVarargs
        @Nullable
        @Override
        protected final Void doInBackground(HashMap<String, String>... params) {

            HashMap<String, String> list = params[0];
            list.put("key", Utility.KEY);

            new HTTPUpdateData(link).update_data(list);
            return null;
        }
    }
}

