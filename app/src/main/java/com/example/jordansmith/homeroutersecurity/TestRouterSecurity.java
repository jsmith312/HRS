package com.example.jordansmith.homeroutersecurity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class TestRouterSecurity extends Activity {
    protected static final String DEBUG = "RESPONSE CODE";
    private String SSID;
    private String MAC_ADDRESS;
    private DhcpInfo d;
    private WifiManager wifi;
    private String gatewayIP;
    private NetworkInfo networkInfo;
    private ConnectivityManager connMgr;
    private ArrayList<String> stringList = new ArrayList<String>();
    private ArrayList<Integer> imgList = new ArrayList<Integer>();
    private String Log_Path;
    private Reporter report;

    private String LOG_INFO;
    private HTTPHelper httpHelper;
    private CustomListAdapter adapter;
    private EditText user;
    private EditText pass;
    private TextView logInfoText;
    private Button TestDefaultPass;
    private Button setPassUser;
    private Button logInfo;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_router_security);
        // Text Views
        adapter = new CustomListAdapter(this, stringList, imgList);
        // EditText
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        logInfoText = (TextView) findViewById(R.id.log);
        // Buttons
        logInfo = (Button)findViewById(R.id.log_info);
        TestDefaultPass = (Button) findViewById(R.id.test_pw);
        //logInfo = (Button)findViewById(R.id.log_info);
        //sendLogInfo = (Button)findViewById(R.id.send_log_info);
        //setPassUser = (Button) findViewById(R.id.set_pw);

        readReport(GetReporterExtra());

        ListView employeeList = (ListView) findViewById(R.id.listView);
        employeeList.setAdapter(adapter);
        employeeList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showInfo();
            }
        });

        getNetworkInfo();
    }

    private void readReport(Reporter rep) {
        report = rep;
        user.setText(report.getDefaultUserName());
        pass.setText(report.getDefaultPassword());
        if (!rep.getHasDefaultPassword()) {
            stringList.add("Default Password not set");
            imgList.add(R.drawable.safe_50);
            adapter.notifyAll();
            setLog(false);
        } else {
            stringList.add("Default Password set");
            imgList.add(R.drawable.restrict_50);
            setLog(true);
        }
    }

    public void showInfo() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Gateway IP: " + gatewayIP + "\n" +
                        "BSSID: " + MAC_ADDRESS + "\n" +
                        "SSID: " + SSID + "\n" + "username: " + report.getDefaultUserName() + "\n" +
                        "password: " + report.getDefaultPassword()
        );

        alertDialogBuilder.setPositiveButton("done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void sendLogInfo(View view) throws IOException {
        // Send to server when available
        WriteLogFile(LOG_INFO);
        //ReadLogFile();
        logInfoText.setText("");
    }

    public void showLogInfo(View view) {
        logInfoText.setText(LOG_INFO);
    }

    private void setLog(boolean response) {
        Date d = new Date();
        StringBuffer LOG = new StringBuffer();
        LOG.append("[");
        LOG.append(new Timestamp(d.getTime()));
        LOG.append("]: ");
        LOG.append(SSID + " ");
        LOG.append(MAC_ADDRESS);
        LOG.append(" default_pw:" + response + "\n");
        LOG_INFO = LOG.toString();
        Log.d(DEBUG, LOG.toString());
    }

    private void WriteLogFile(String log) throws IOException {
        File path = getApplicationContext().getFilesDir();
        File file = new File(path, "WifiSecurityAppLog.txt");
        Log_Path = file.getPath();
        Log.d(DEBUG, Log_Path);
        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(log.getBytes());
        stream.close();
    }

    private void ReadLogFile() throws IOException {
        File file = new File(Log_Path);
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } finally {
            in.close();
        }

        String contents = new String(bytes);
        Log.d(DEBUG, contents);
    }

    private String FormatIP(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

    private void getNetworkInfo() {
        // Network
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        // ensure network connection
        if (networkInfo != null && networkInfo.isConnected()) {
            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            d = wifi.getDhcpInfo();
            gatewayIP = FormatIP(d.gateway);
            WifiInfo info = wifi.getConnectionInfo();
            SSID = info.getSSID();
            MAC_ADDRESS = info.getBSSID();
        } else {
            //tv1.setText("No network connection available.");
        }
    }

    private Reporter GetReporterExtra() {
        String jsonMyObject = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("Reporter");
        }
        Reporter report = new Gson().fromJson(jsonMyObject, Reporter.class);
        return report;
    }

    public void setUserPass(View view) {
        InputMethodManager inputManager =
                (InputMethodManager) getApplicationContext().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void testUserPass(View view) {
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                httpHelper = new HTTPHelper("", "", "http://" + gatewayIP + "/*",
                        MAC_ADDRESS, getApplicationContext());
                int response = httpHelper.getResponse();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!user.getText().toString().matches("") &&
                    !user.getText().toString().matches("")) {
                try {
                    httpHelper.setUsername(user.getText().toString());
                    httpHelper.setPassword(pass.getText().toString());
                    int response = httpHelper.getResponse();
                    if (response == 401) {
                        Toast.makeText(getApplicationContext(), "Incorrect password: "+response,
                                Toast.LENGTH_LONG).show();
                        setLog(false);
                    } else {

                        Toast.makeText(getApplicationContext(), "Correct password: "+response,
                                Toast.LENGTH_LONG).show();
                        setLog(true);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                user.setText("");
                pass.setText("");
            }
        } else {
            //tv1.setText("No network connection available.");
        }
    }
}
