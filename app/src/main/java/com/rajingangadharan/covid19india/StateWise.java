package com.rajingangadharan.covid19india;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.app.SearchManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StateWise extends AppCompatActivity {

    private ListView lstState;
    private CardAdapter cardAdapter;
    private ProgressBar progressBar;
    private MaterialTextView txtProgress, txtShowDis;
    private ArrayList<StateWiseData> stateWiseData = new ArrayList<>();
    ArrayList<StateWiseData> stateWiseDataArray;
    RequestQueue requestQueue;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView sv = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        assert manager != null;
        sv.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cardAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            if ( isConnected() ) {
                preFetch();
                fetchState();
            } else {
                Toast.makeText(getApplicationContext(), "Please Connect To Network", Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_wise);
        Log.d("covid19India", "State -> MainActivity Started");

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.statewise_actionbar);
        }
        Log.d("actionBar", "State -> ActionBar Text Is Set");

        lstState = findViewById(R.id.lstState);
        progressBar = findViewById(R.id.loadProgress);
        txtProgress = findViewById(R.id.txtProgressState);
        txtShowDis = findViewById(R.id.clickDis);
        requestQueue = Volley.newRequestQueue(this);

        if(isConnected()) {
            Log.d("covid19India", "State -> Internet Available, So Proceeding");
            preFetch();
            fetchState();
        } else {
            Log.d("covid19India", "State -> Internet Not Available, So Not Proceding");
            Toast.makeText(getApplicationContext(), "Please Connect To Network", Toast.LENGTH_LONG).show();
        }

        lstState.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("covid19India", "State -> Item Selected In Listview");
            if(stateWiseDataArray.get(position).getStateName().toLowerCase().equals("state unassigned")) {
                Intent intent = new Intent(StateWise.this, DistrictWise.class);
                intent.putExtra("State", stateWiseDataArray.get(position).getStateName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Intent intent = new Intent(StateWise.this, StateDetails.class);
                intent.putExtra("State", stateWiseDataArray.get(position).getStateName());
                intent.putExtra("StateCode", stateWiseDataArray.get(position).getStateCode().toLowerCase());
                intent.putExtra("confirmed", stateWiseDataArray.get(position).getStaConfirmed());
                intent.putExtra("active", stateWiseDataArray.get(position).getStaActive());
                intent.putExtra("recovered", stateWiseDataArray.get(position).getStaRecovered());
                intent.putExtra("dead", stateWiseDataArray.get(position).getStaDeceased());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if(networkCapabilities != null) {
                if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true;
                } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                } else return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }
        return false;
    }

    public void preFetch() {
        Log.d("covid19India", "State -> Applying PreFetch");
        progressBar.setVisibility(View.VISIBLE);
        txtProgress.setVisibility(View.VISIBLE);
        txtShowDis.setVisibility(View.INVISIBLE);
        stateWiseData.clear();
        Log.d("covid19India", "State -> PreFetch Completed");
    }

    public void fetchState() {
        Log.d("covid19India", "State -> Applying FetchTotal");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/data.json", null, response -> {
            try {
                Log.d("covid19India", "State -> Parsing JSON Started");
                JSONArray stateWise = response.getJSONArray("statewise");
                StateWiseData data;
                for (int i = 0; i < stateWise.length(); i++) {
                    JSONObject JO = stateWise.getJSONObject(i);
                    if (!(JO.get("state").equals("Total"))) {
                        String sta = JO.getString("state").toUpperCase();
                        String staCode = JO.getString("statecode");
                        String con = JO.getString("confirmed");
                        String act = JO.getString("active");
                        String rec = JO.getString("recovered");
                        String dead = JO.getString("deaths");
                        SimpleDateFormat formatIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat formatOut = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.getDefault());
                        Date date = formatIn.parse(JO.getString("lastupdatedtime"));
                        assert date != null;
                        String lastUp = "Last Updated: " + formatOut.format(date);
                        String delCon = "";
                        if(JO.getInt("deltaconfirmed") >= 0) {
                            delCon = "\u2191" + JO.getString("deltaconfirmed");
                        } else if(JO.getInt("deltaconfirmed") < 0) {
                            delCon = "\u2193" + Math.abs(JO.getInt("deltaconfirmed"));
                        }
                        String delRec = "";
                        if(JO.getInt("deltarecovered") >= 0) {
                            delRec = "\u2191" + JO.getString("deltarecovered");
                        } else if(JO.getInt("deltarecovered") < 0) {
                            delRec = "\u2193" + Math.abs(JO.getInt("deltarecovered"));
                        }
                        String delDead = "";
                        if(JO.getInt("deltadeaths") >= 0) {
                            delDead = "\u2191" + JO.getString("deltadeaths");
                        } else if(JO.getInt("deltadeaths") < 0) {
                            delDead = "\u2193" + Math.abs(JO.getInt("deltadeaths"));
                        }
                        data = new StateWiseData(sta, staCode, con, act, rec, dead, lastUp, delCon, delRec, delDead);
                        stateWiseData.add(data);
                    }

                    cardAdapter = new CardAdapter(stateWiseData);
                    lstState.setAdapter(cardAdapter);
                    progressBar.setVisibility(View.INVISIBLE);
                    txtProgress.setVisibility(View.INVISIBLE);
                    txtShowDis.setVisibility(View.VISIBLE);
                }
                Log.d("covid19India", "State -> Parsing JSON Ended");
            } catch ( JSONException | ParseException e ) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonObjectRequest);
        Log.d("covid19India", "State -> RequestQueue Added");
        requestQueue.getCache().clear();
        Log.d("covid19India", "State -> RequestQueCache Cleared");
    }

    class CardAdapter extends BaseAdapter implements Filterable {

        CustomFilter filter;
        ArrayList<StateWiseData> filterArray;

        public CardAdapter(ArrayList<StateWiseData> stateWiseDataArrayList) {
            stateWiseDataArray = stateWiseDataArrayList;
            this.filterArray = stateWiseDataArrayList;
        }

        @Override
        public Filter getFilter() {
            if(filter == null) {
                filter = new CustomFilter();
            }
            return filter;
        }

        class viewHolder {
            MaterialCardView cardView;
            MaterialTextView txtState, txtConf, txtAct, txtRec, txtDead, txtLastUp;
            MaterialTextView txtDeltaCon, txtDeltaRec, txtDeltaDead;
            AnimatedPieView animatedPieView;
        }

        @Override
        public int getCount() {
            return stateWiseDataArray.size();
        }

        @Override
        public Object getItem(int position) {
            return stateWiseDataArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            viewHolder ViewHolder;
            if(convertView == null) {
                Log.d("covid19India", "State -> Layout inflated Started");
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.statewise_card, parent, false);
                ViewHolder = new viewHolder();
                ViewHolder.cardView = convertView.findViewById(R.id.cardState);
                ViewHolder.txtState = convertView.findViewById(R.id.txtState);
                ViewHolder.txtConf = convertView.findViewById(R.id.staCon);
                ViewHolder.txtAct = convertView.findViewById(R.id.staAct);
                ViewHolder.txtRec = convertView.findViewById(R.id.staRec);
                ViewHolder.txtDead = convertView.findViewById(R.id.staDead);
                ViewHolder.txtLastUp = convertView.findViewById(R.id.lastUpdate);
                ViewHolder.txtDeltaCon = convertView.findViewById(R.id.deltaCon);
                ViewHolder.txtDeltaRec = convertView.findViewById(R.id.deltaRec);
                ViewHolder.txtDeltaDead = convertView.findViewById(R.id.deltaDead);
                ViewHolder.animatedPieView = convertView.findViewById(R.id.aniGraph);
                convertView.setTag(ViewHolder);
                Log.d("covid19India", "State -> Layout inflated Ended");
            } else {
                ViewHolder = (viewHolder) convertView.getTag();
            }

            Log.d("covid19India", "State -> Text Input Started");
            ViewHolder.txtState.setText(stateWiseDataArray.get(position).getStateName());
            ViewHolder.txtConf.setText(stateWiseDataArray.get(position).getStaConfirmed());
            ViewHolder.txtAct.setText(stateWiseDataArray.get(position).getStaActive());
            ViewHolder.txtRec.setText(stateWiseDataArray.get(position).getStaRecovered());
            ViewHolder.txtDead.setText(stateWiseDataArray.get(position).getStaDeceased());
            ViewHolder.txtLastUp.setText(stateWiseDataArray.get(position).getStaLastUpdate());
            ViewHolder.txtDeltaCon.setText(stateWiseDataArray.get(position).getStaDelCon());
            ViewHolder.txtDeltaRec.setText(stateWiseDataArray.get(position).getStaDelRec());
            ViewHolder.txtDeltaDead.setText(stateWiseDataArray.get(position).getStaDelDeceased());
            Log.d("covid19India", "State -> Text Input Ended");

            Log.d("covid19India", "Main -> Applying Graph Started");
            AnimatedPieViewConfig config = new AnimatedPieViewConfig();
            config.startAngle(-90);
            config.splitAngle(1);
            config.addData(new SimplePieInfo(Float.parseFloat(stateWiseDataArray.get(position).getStaActive()), ContextCompat.getColor(getApplicationContext(), R.color.colorRed), "Active"));
            config.addData(new SimplePieInfo(Float.parseFloat(stateWiseDataArray.get(position).getStaRecovered()), ContextCompat.getColor(getApplicationContext(), R.color.colorGreen), "Recovered"));
            config.addData(new SimplePieInfo(Float.parseFloat(stateWiseDataArray.get(position).getStaDeceased()), ContextCompat.getColor(getApplicationContext(), R.color.colorYellow), "Dead"));
            config.canTouch(false);
            config.strokeMode(false);
            config.duration(0);

            ViewHolder.animatedPieView.applyConfig(config);
            ViewHolder.animatedPieView.start();
            Log.d("covid19India", "Main -> Applying Graph Ended");
            return convertView;
        }

        class CustomFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if(constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    ArrayList<StateWiseData> filters = new ArrayList<>();
                    for(int i = 0; i < filterArray.size(); i++) {
                        if(filterArray.get(i).getStateName().toLowerCase().contains(constraint)) {
                            StateWiseData stateWiseData = new StateWiseData(filterArray.get(i).getStateName(), filterArray.get(i).getStateCode(), filterArray.get(i).getStaConfirmed(), filterArray.get(i).getStaActive(), filterArray.get(i).getStaRecovered(), filterArray.get(i).getStaDeceased(), filterArray.get(i).getStaLastUpdate(), filterArray.get(i).getStaDelCon(), filterArray.get(i).getStaDelRec(), filterArray.get(i).getStaDelDeceased());
                            filters.add(stateWiseData);
                        }
                    }
                    results.count = filters.size();
                    results.values = filters;
                } else {
                    results.count = filterArray.size();
                    results.values = filterArray;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                stateWiseDataArray = (ArrayList<StateWiseData>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    static class StateWiseData {
        private String stateName, stateCode, staConfirmed, staActive, staRecovered, staDeceased, staLastUpdate, staDelCon, staDelRec, staDelDeceased;

        public StateWiseData(String stateName, String stateCode, String staConfirmed, String staActive, String staRecovered, String staDeceased, String staLastUpdate, String staDelCon, String staDelRec, String staDelDeceased) {
            this.stateName = stateName;
            this.stateCode = stateCode;
            this.staConfirmed = staConfirmed;
            this.staActive = staActive;
            this.staRecovered = staRecovered;
            this.staDeceased = staDeceased;
            this.staLastUpdate = staLastUpdate;
            this.staDelCon = staDelCon;
            this.staDelRec = staDelRec;
            this.staDelDeceased = staDelDeceased;
        }

        public String getStateName() {
            return stateName;
        }

        public String getStateCode() {
            return stateCode;
        }

        public String getStaConfirmed() {
            return staConfirmed;
        }

        public String getStaActive() {
            return staActive;
        }

        public String getStaRecovered() {
            return staRecovered;
        }

        public String getStaDeceased() {
            return staDeceased;
        }

        public String getStaLastUpdate() {
            return staLastUpdate;
        }

        public String getStaDelCon() {
            return staDelCon;
        }

        public String getStaDelRec() {
            return staDelRec;
        }

        public String getStaDelDeceased() {
            return staDelDeceased;
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Log.d("covid19India", "State -> Exit");
    }
}
