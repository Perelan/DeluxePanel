package sharecrew.net.fragpanel.adapter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Context context;

    public ListAdapter(ArrayList<Report> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context  = context;
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
        Picasso.with(holder.suspected_pic.getContext()).load(mDataset.get(position).getReported_avatar()).into(holder.suspected_pic);
        Picasso.with(holder.reporting_pic.getContext()).load(mDataset.get(position).getReporting_avatar()).into(holder.reporting_pic);
        holder.task.setVisibility(View.GONE);

        if(!Utility.isTablet(context)){
            holder.expandUp.setVisibility(View.GONE);
            holder.expandable.setVisibility(View.GONE);
        }else{
            holder.expandable.setVisibility(View.VISIBLE);
            holder.expandDown.setVisibility(View.GONE);
            holder.expandUp.setVisibility(View.GONE);
        }

        if(!mDataset.get(position).getAdmin_name().equals("null")){
            holder.admin_name.setText(mDataset.get(position).getAdmin_name());
        }else{
            holder.admin_name.setText("empty");
            holder.admin_name.setTypeface(null, Typeface.ITALIC);
        }

        if(mDataset.get(position).getAdmin_name().equals("null")){
            holder.claim.setVisibility(View.VISIBLE);
            holder.command.setVisibility(View.GONE);
        }else if(!mDataset.get(position).getAdmin_name().equals(admin.getName())){
            holder.task.setVisibility(View.VISIBLE);
            holder.claim.setVisibility(View.GONE);
            holder.command.setVisibility(View.GONE);
        }else{
            holder.claim.setVisibility(View.GONE);
            holder.command.setVisibility(View.VISIBLE);
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
                        new UpdateDataTask("update_report_complete.php?").execute(data_to_send);
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
            new UpdateDataTask("update_report_complete.php?").execute(data_to_send);
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
        RelativeLayout expandable;
        ImageView expandDown, expandUp, uparrow, downarrow;
        boolean isExpanded, isRepPressed;
        Button command, claim;
        HashMap<String, String> data_to_send;

        public ViewHolder(View v) {
            super(v);
            cv                      = (CardView) v.findViewById(R.id.card_view);
            server_name             = (TextView) v.findViewById(R.id.server_text);
            admin_name              = (TextView) v.findViewById(R.id.admin_name);
            date                    = (TextView) v.findViewById(R.id.month_text);
            suspect_name            = (TextView) v.findViewById(R.id.suspect);
            suspect_steamid         = (TextView) v.findViewById(R.id.reported_steamid);
            reporting_name          = (TextView) v.findViewById(R.id.reporter);
            reporting_id            = (TextView) v.findViewById(R.id.reporting_id);
            reason                  = (TextView) v.findViewById(R.id.reason_text);
            karma                   = (TextView) v.findViewById(R.id.karma_id);
            task                    = (TextView) v.findViewById(R.id.taken_txt);
            suspected_pic           = (ImageView) v.findViewById(R.id.suspect_avatar);
            reporting_pic           = (ImageView) v.findViewById(R.id.reporter_avatar);
            expandable              = (RelativeLayout) v.findViewById(R.id.expandable_part_layout);
            expandDown              = (ImageView) v.findViewById(R.id.expandDown);
            expandUp                = (ImageView) v.findViewById(R.id.expandUp);
            uparrow                 = (ImageView) v.findViewById(R.id.uparrow);
            downarrow               = (ImageView) v.findViewById(R.id.downarrow);
            isExpanded              = false;
            command                 = (Button) v.findViewById(R.id.command_btn);
            claim                   = (Button) v.findViewById(R.id.claim_btn);
            isRepPressed            = false;
            data_to_send            = new HashMap<>();

            claim.setOnClickListener(this);
            command.setOnClickListener(this);

            if(!Utility.isTablet(v.getContext())){
                v.setOnClickListener(this);
            }

            uparrow.setOnClickListener(this);
            downarrow.setOnClickListener(this);
        }

        @SuppressWarnings("unchecked")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(final View v) {

            // Handle the claim button
            if(v.getId() == R.id.claim_btn) {
                admin_name.setText(admin.getName());
                admin_name.setTypeface(null, Typeface.NORMAL);

                claim.setVisibility(View.GONE);
                command.setVisibility(View.VISIBLE);

                data_to_send.put("id", mDataset.get(this.getAdapterPosition()).getReport_id());
                data_to_send.put("admin_id", admin.getId());

                new UpdateDataTask("update_report_admin.php?").execute(data_to_send);
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
            }else if(v.getId() == R.id.command_btn){
                // STEAM ID - COMMAND - SERVER

                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View promptsView = inflater.inflate(R.layout.popup_display, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                alertDialogBuilder.setView(promptsView);

                final RelativeLayout rl = (RelativeLayout) promptsView.findViewById(R.id.hidethispart);
                final Spinner mSpinner = (Spinner) promptsView.findViewById(R.id.popspinner);

                List<String> list = new ArrayList<>();
                list.add("Pick a command:");
                list.add("Mute");
                list.add("Silence");
                list.add("Gag");
                list.add("Kick");
                list.add("Ban");

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                        (v.getContext(), R.layout.spinner_item,list);
                dataAdapter.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);

                final NumberPicker np = (NumberPicker) promptsView.findViewById(R.id.numberPicker);
                np.setMaxValue(3600);
                np.setMinValue(0);
                Utility.setNumberPickerTextColor(np, Color.WHITE);

                final EditText edit = (EditText) promptsView.findViewById(R.id.editText);
                edit.setTextColor(Color.WHITE);
                edit.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);

                mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (parent.getSelectedItemPosition() > 0) {
                            rl.setVisibility(View.VISIBLE);
                            data_to_send.put("command", parent.getSelectedItem().toString());
                        } else {
                            rl.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                alertDialogBuilder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog

                                if (mSpinner.getSelectedItemPosition() != 0) {
                                    data_to_send.put("reason", edit.getText().toString());
                                    data_to_send.put("server", server_name.getText().toString().substring(1, server_name.getText().toString().length()));
                                    data_to_send.put("steamid", suspect_steamid.getText().toString());
                                    data_to_send.put("duration", Integer.toString(np.getValue()));
                                    new UpdateDataTask("handle_command.php?").execute(data_to_send);
                                    Toast.makeText(v.getContext(), "Command executed! (Hopefully)", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(v.getContext(), "Please pick a command!", Toast.LENGTH_LONG).show();
                                }

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(true);

            } else {

                if (!isExpanded) {
                    expandUp.setVisibility(View.VISIBLE);
                    expandDown.setVisibility(View.GONE);

                    Animation animFadeIn = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
                    expandable.setAnimation(animFadeIn);
                    expandable.setVisibility(View.VISIBLE);

                    isExpanded = true;
                } else {
                    expandUp.setVisibility(View.GONE);
                    expandDown.setVisibility(View.VISIBLE);

                    Animation animFadeOut = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_out);
                    expandable.setAnimation(animFadeOut);
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

