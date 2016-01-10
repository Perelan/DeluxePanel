package sharecrew.net.fragpanel.reports;

public class Report {
    private String report_id;
    private String server_name;
    private String admin_name;

    private String reported_name;
    private String reported_steam_id;
    private String reported_steam_id64;
    private String reported_num_reports;

    private String reporting_name;
    private String reporting_id;
    private String reporting_steam_id;
    private String reporting_steam_id64;
    private String reporting_karma;

    private String reason;
    private String complete;
    private String date;

    private String reported_avatar;
    private String reporting_avatar;


    public Report(String report_id, String server_name, String admin_name, String reported_name, String reported_steam_id, String reported_steam_id64,
                  String reported_num_reports, String reporting_name, String reporting_id, String reporting_steam_id, String reporting_steam_id64, String reporting_karma,
                  String reason, String complete, String date, String reported_avatar, String reporting_avatar) {
        this.report_id = report_id;
        this.server_name = server_name;
        this.admin_name = admin_name;

        this.reported_name = reported_name;
        this.reported_steam_id = reported_steam_id;
        this.reported_steam_id64 = reported_steam_id64;
        this.reported_num_reports = reported_num_reports;

        this.reporting_name = reporting_name;
        this.reporting_id = reporting_id;
        this.reporting_steam_id = reporting_steam_id;
        this.reporting_steam_id64 = reporting_steam_id64;
        this.reporting_karma = reporting_karma;

        this.reason = reason;
        this.complete = complete;
        this.date = date;

        this.reported_avatar = reported_avatar;
        this.reporting_avatar = reporting_avatar;
    }

    public String getReport_id() {
        return report_id;
    }

    public String getServer_name() {
        return server_name;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public String getReported_name() {
        return reported_name;
    }

    public String getReported_steam_id() {
        return reported_steam_id;
    }

    public String getReported_steam_id64() {
        return reported_steam_id64;
    }

    public String getReported_num_reports() {
        return reported_num_reports;
    }

    public String getReporting_name() {
        return reporting_name;
    }

    public String getReporting_id(){
        return reporting_id;
    }

    public String getReporting_steam_id() {
        return reporting_steam_id;
    }

    public String getReporting_steam_id64() {
        return reporting_steam_id64;
    }

    public String getReporting_karma() {
        return reporting_karma;
    }

    public String getReason() {
        return reason;
    }

    public String getComplete() {
        return complete;
    }

    public String getDate() {
        return date;
    }

    public String getReported_avatar() {
        return reported_avatar;
    }

    public String getReporting_avatar() {
        return reporting_avatar;
    }

    @Override
    public String toString() {
        return "Report{" +
                "report_id='" + report_id + '\'' +
                ", server_name='" + server_name + '\'' +
                ", admin_name='" + admin_name + '\'' +
                ", reported_name='" + reported_name + '\'' +
                ", reported_steam_id='" + reported_steam_id + '\'' +
                ", reported_steam_id64='" + reported_steam_id64 + '\'' +
                ", reported_num_reports='" + reported_num_reports + '\'' +
                ", reporting_name='" + reporting_name + '\'' +
                ", reporting_id='" + reporting_id + '\'' +
                ", reporting_steam_id='" + reporting_steam_id + '\'' +
                ", reporting_steam_id64='" + reporting_steam_id64 + '\'' +
                ", reporting_karma='" + reporting_karma + '\'' +
                ", reason='" + reason + '\'' +
                ", complete='" + complete + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}

