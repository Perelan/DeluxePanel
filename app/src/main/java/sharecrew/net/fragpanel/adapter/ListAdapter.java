package sharecrew.net.fragpanel.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import sharecrew.net.fragpanel.R;
import sharecrew.net.fragpanel.login.Admin;
import sharecrew.net.fragpanel.login.AdminSession;
import sharecrew.net.fragpanel.reports.HTTPFetchSteam;
import sharecrew.net.fragpanel.reports.HTTPReportRequest;
import sharecrew.net.fragpanel.reports.HTTPUpdateKarma;
import sharecrew.net.fragpanel.reports.Report;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private ArrayList<Report> mDataset = null;
    private AdminSession as;
    private Admin a;
    private final String TAG = "******* List Adapter";
    private HashMap<String, String> karma_list;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView server_name, admin_name, date, suspect_name, reporting_name,reason, karma, task;
        ImageView suspected_pic, reporting_pic;
        ViewGroup expandable;
        ImageView expandDown, expandUp, uparrow, downarrow;
        boolean isClicked, isPressed;
        Button mute, kick, ban, complete, claim;

        public ViewHolder(View v) {
            super(v);
            cv              = (CardView) v.findViewById(R.id.card_view);
            server_name     = (TextView) v.findViewById(R.id.server_text);
            admin_name      = (TextView) v.findViewById(R.id.admin_name);
            date            = (TextView) v.findViewById(R.id.month_text);
            suspect_name    = (TextView) v.findViewById(R.id.suspect);
            reporting_name  = (TextView) v.findViewById(R.id.reporter);
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
            isClicked       = false;
            mute            = (Button) v.findViewById(R.id.mute_btn);
            kick            = (Button) v.findViewById(R.id.kick_btn);
            ban             = (Button) v.findViewById(R.id.ban_btn);
            claim           = (Button) v.findViewById(R.id.claim_btn);
            isPressed       = false;

            uparrow.setOnClickListener(this);
            downarrow.setOnClickListener(this);
            claim.setOnClickListener(this);
            v.setOnClickListener(this);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            // Handle the claim button
            if(v.getId() == R.id.claim_btn) {
                admin_name.setText(a.getName());
                admin_name.setTypeface(null, Typeface.NORMAL);

                claim.setVisibility(View.GONE);
                mute.setVisibility(View.VISIBLE);
                kick.setVisibility(View.VISIBLE);
                ban.setVisibility(View.VISIBLE);
            // Handle karma upvote and downvote
            }else if(v.getId() == R.id.uparrow || v.getId() == R.id.downarrow){
                if(!admin_name.getText().equals(a.getName())){
                    Toast.makeText(v.getContext(), "You've to be the claiming admin to do that!",
                            Toast.LENGTH_SHORT).show();
                }else{
                    if(v.getId() == R.id.uparrow){
                        int karma_count = Integer.parseInt((String) karma.getText());
                        if(isPressed){
                            karma_count += 2;
                        }else{
                            karma_count++;
                        }
                        karma.setText(String.format("%s", karma_count));
                        uparrow.setEnabled(false);
                        downarrow.setEnabled(true);
                        karma_list.put("user", (String) reporting_name.getText());
                        karma_list.put("karma", String.format("%s", karma_count));
                        new UpdateKarma(karma_list).execute();
                        //uparrow.setBackgroundResource(R.drawable.downarrow);
                        isPressed = true;
                    }else{
                        int karma_count = Integer.parseInt((String) karma.getText());
                        if(isPressed){
                            karma_count -= 2;
                        }else{
                            karma_count--;
                        }
                        karma.setText(String.format("%s", karma_count));
                        uparrow.setEnabled(true);
                        downarrow.setEnabled(false);
                        karma_list.put("user", (String) reporting_name.getText());
                        karma_list.put("karma", String.format("%s", karma_count));
                        new UpdateKarma(karma_list).execute();
                        isPressed = true;
                    }
                }
            // Handle the expand view (able to press the whole card to expand).
            }else{
                if (!isClicked) {
                    expandUp.setVisibility(View.VISIBLE);
                    expandDown.setVisibility(View.GONE);
                    expandable.setVisibility(View.VISIBLE);
                    isClicked = true;
                } else {
                    expandUp.setVisibility(View.GONE);
                    expandDown.setVisibility(View.VISIBLE);
                    expandable.setVisibility(View.GONE);
                    isClicked = false;
                }
            }
        }
    }
    public ListAdapter(ArrayList<Report> mDataset) {
        this.mDataset = mDataset;

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list, parent, false);
        as = new AdminSession(v.getContext());
        a = as.getAdminSession();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.suspect_name.setText(mDataset.get(position).getReported_name());
        holder.reporting_name.setText(mDataset.get(position).getReporting_name());
        holder.date.setText(convert(mDataset.get(position).getDate()));
        holder.server_name.setText(mDataset.get(position).getServer_name());
        holder.karma.setText(mDataset.get(position).getReporting_karma());
        holder.reason.setText(mDataset.get(position).getReason());
        System.out.println(mDataset.get(position).getReported_avatar());
        Picasso.with(holder.suspected_pic.getContext()).load(mDataset.get(position).getReported_avatar()).into(holder.suspected_pic);
        Picasso.with(holder.reporting_pic.getContext()).load(mDataset.get(position).getReporting_avatar()).into(holder.reporting_pic);
        holder.expandUp.setVisibility(View.GONE);
        holder.expandable.setVisibility(View.GONE);
        holder.isClicked = false;
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
        }else if(!mDataset.get(position).getAdmin_name().equals(a.getName())){
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public String convert(String date){
        // 2015-12-27 21:00:16
        try{
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Calendar c = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            Date d = df.parse(date);
            c.setTime(d);

            long diff        = (start.getTime().getTime() - c.getTime().getTime());
            long diffDays    = diff / (24 * 60 * 60 * 1000);
            long diffHours   = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffSeconds = diff / 1000 % 60;

            if(diffDays == 0 && diffHours == 0) {
                return String.format("%s mins, %s secs ago", diffMinutes, diffSeconds);
            }else if(diffDays == 0){
                return String.format("%s hrs, %s mins ago", diffHours, diffMinutes);
            }else{
                return String.format("%s days, %s hrs ago", diffDays, diffHours);
            }

        }catch(ParseException e){
            e.getStackTrace();
        }

        return "ERROR";
    }

    // The part which is a nono
    public class UpdateKarma extends AsyncTask<String, Void, Void> {

        private HashMap<String, String> list;

        public UpdateKarma(HashMap<String, String> list){
            this.list = list;
            list.put("key", "fragpanel123");
        }

        @Override
        protected Void doInBackground(String... params) {
            list.put("user", params[0]);
            list.put("karma", params[1]);

            HTTPUpdateKarma uk = new HTTPUpdateKarma();
            uk.update_data("http://www.sharecrew.net/deluxepanel/update_karma.php", list);

            return null;
        }
    }
}

