package com.example.jordansmith.homeroutersecurity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity {
    protected static final String DEBUG = "RESPONSE CODE";
    private String SSID;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView response;
    private Button TestDefaultPass;
    private Button logInfo;
    private Button sendLogInfo;
    private Button setPassUser;
    private String MAC_ADDRESS;
    private DhcpInfo d;
    private WifiManager wifi;
    ProgressBar pbM;
    private static HTTPHelper httpHelper;
    private String gatewayIP;
    private static NetworkInfo networkInfo;
    private ConnectivityManager connMgr;
    private String LOG_INFO;
    private String Company;
    private static Reporter rep;
    private ScrollView sv;
    private ListView listview;
    private EditText user;
    private ArrayList<String> stringList = new ArrayList<String>();
    private ArrayList<Integer> imgList = new ArrayList<>();
    private String Log_Path;
    private EditText pass;
    CustomListAdapter adapter;
    private ProgressBar pbDefaultM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_test);
        pbDefaultM = (ProgressBar) findViewById(R.id.pbDefault);
        // ensure network connection

        // Network
        connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
            d=wifi.getDhcpInfo();
            gatewayIP = FormatIP(d.gateway);
            //tv3.setText("Default Gateway: " + gatewayIP);
            WifiInfo info = wifi.getConnectionInfo();
            SSID = info.getSSID().toString();
            //tv1.setText("SSID: " + SSID);
            //Log.d(DEBUG, info.getMacAddress());
            MAC_ADDRESS = info.getBSSID();
            httpHelper = new HTTPHelper("", "", "http://"+gatewayIP+"/*",
                    MAC_ADDRESS, getApplicationContext());
            HTTPThread thread = new HTTPThread(pbDefaultM);
            thread.execute(httpHelper);
        } else {
            tv1.setText("No network connection available.");
        }
    }
    private String FormatIP(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

    @SuppressWarnings("deprecation")
    public class HTTPThread extends AsyncTask<HTTPHelper, Integer, Reporter> {
        private HTTPHelper httpHelper;
        private String DEBUG = "DEBUG";
        private boolean hasDefaultPW = false;
        private String Company;
        private ProgressBar pbM;

        public HTTPThread(ProgressBar pb) {
            pbM = pb;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbM.setVisibility(View.VISIBLE);
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress[0]);
            pbM.setProgress(progress[0]);
        }

        protected void onPostExecute(Reporter result) {
            super.onPostExecute(result);
            pbM.setVisibility(View.INVISIBLE);
            pbM.setProgress(0);
            Intent activity = new Intent(TestActivity.this, TestRouterSecurity.class);
            activity.putExtra("Reporter", new Gson().toJson(result));
            startActivity(activity);
            finish();
        }

        @SuppressLint("NewApi")
        protected Reporter doInBackground(HTTPHelper... params) {
            Reporter rep = new Reporter(false, "", "", "");
            // TODO Auto-generated method stub
            httpHelper = params[0];
            int code = -1;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://www.macvendorlookup.com/api/v2/"+httpHelper.getMac());
            try {
                HttpResponse httpResponse = httpClient.execute(post);
                code = httpResponse.getStatusLine().getStatusCode();
                if (code >= 200 && code <= 399) {
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null) {
                        String responseString = EntityUtils.toString(entity, "UTF-8");
                        String jsonObj = responseString.substring(1, responseString.length() - 1);
                        JSONObject result = new JSONObject(jsonObj);
                        Company = result.getString("company");
                        rep.setCompany(Company);
                    }
                }
            } catch (ClientProtocolException e) {
                // Log exception
                e.printStackTrace();
            } catch (IOException e) {
                // Log exception
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // replace the first 2 parameters with the API call that will
            // send back the username and password.
            try {
                HttpClient httpClient2 = new DefaultHttpClient();
                HttpPost post2 = new HttpPost("http://52.89.45.40/routepass.py");
                List<NameValuePair> arguments = new ArrayList<NameValuePair>();
                arguments.add(new BasicNameValuePair("manufacturer", Company));
                post2.setEntity(new UrlEncodedFormEntity(arguments));

                HttpResponse response2 = httpClient2.execute(post2);
                HttpEntity entity2 = response2.getEntity();
                if (entity2 != null) {
                    String responseString2 = EntityUtils.toString(entity2, "UTF-8");
                    Log.d(DEBUG, responseString2.toString());
                    JSONObject dataresult = new JSONObject(responseString2);
                    JSONArray arr = dataresult.getJSONArray("test");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject up = arr.getJSONObject(i);
                        String user = up.getString("user");
                        String pass = up.getString("pass");
                        httpHelper.setPassword(pass);
                        httpHelper.setUsername(user);
                        int resp = httpHelper.getResponse();
                        Log.d(DEBUG, "USER:" + httpHelper.getUsername() + " PASS: " + httpHelper.getPassword());
                        Log.d(DEBUG, "USER:" + user + " PASS: " + pass + " RESPONSE " + resp);
                        if (resp != 401) {
                            rep.setDefaultUserName(user);
                            rep.setDefaultPassword(pass);
                            rep.setHasDefaultPassword(true);
                            publishProgress(100);
                            return rep; // has default pw
                        }
                        float percentage = ((float) i / (float) arr.length()) * 100;
                        publishProgress(Float.valueOf(percentage).intValue());
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return rep;
        }
    }

}
