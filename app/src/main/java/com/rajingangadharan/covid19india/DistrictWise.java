package com.rajingangadharan.covid19india;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.app.SearchManager;
import android.content.Context;
import android.net.ConnectivityManager;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DistrictWise extends AppCompatActivity {

    private String stateName;
    private ProgressBar progressBar;
    private MaterialTextView txtLoad;
    private ListView lstDis;
    private CardAdapter cardAdapter;
    private ArrayList<DistrictWiseData> districtWiseData = new ArrayList<>();
    private RequestQueue requestQueue;

    Map<String, String> mapZones = new HashMap<>();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            preFetch();
            fetchZones();
            fetchDistrict();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_wise);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.districtwise_actionbar);
        }

        progressBar = findViewById(R.id.loadProgressDistrict);
        txtLoad = findViewById(R.id.txtProgressDistrict);
        lstDis = findViewById(R.id.lstDistrict);
        requestQueue = Volley.newRequestQueue(this);

        stateName = getIntent().getStringExtra("State");

        if(isConnected()) {
            preFetch();
            fetchZones();
            fetchDistrict();
        }
    }

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }

    public void preFetch() {
        progressBar.setVisibility(View.VISIBLE);
        txtLoad.setVisibility(View.VISIBLE);

        districtWiseData.clear();

        mapZones.clear();
    }

    public void fetchZones() {
        Log.d("covid19India", "FetchZone Started");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/zones.json", null, response -> {
            try {
                JSONArray zones = response.getJSONArray("zones");
                for (int i = 0; i < zones.length(); i++) {
                    JSONObject jsonObject = zones.getJSONObject(i);
                    if(jsonObject.getString("state").toLowerCase().equals(stateName.toLowerCase())) {
                        mapZones.put(jsonObject.getString("district").toLowerCase(), jsonObject.getString("zone"));
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonObjectRequest);
        requestQueue.getCache().clear();
        Log.d("covid19India", "FetchZone Completed");
    }


    public void fetchDistrict() {
        Log.d("covid19India", "FetchDistrict Started");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, "https://api.covid19india.org/v2/state_district_wise.json", null, response -> {
            try {
                Log.d("covid19India", String.valueOf(response));
                DistrictWiseData data;
                for ( int i = 0; i < response.length(); i++ ) {
                    JSONObject single = response.getJSONObject(i);
                    if (single.getString("state").toLowerCase().equals(stateName.toLowerCase()) ) {
                        JSONArray districtData = single.getJSONArray("districtData");
                        Log.d("covid19India", String.valueOf(districtData.length()));
                        for ( int j = 0; j < districtData.length(); j++ ) {
                            JSONObject districts = districtData.getJSONObject(j);
                            String district = districts.getString("district").toUpperCase();
                            String active = districts.getString("active");
                            String confirm = districts.getString("confirmed");
                            String dead = districts.getString("deceased");
                            String recovered = districts.getString("recovered");
                            JSONObject delta = districts.getJSONObject("delta");
                            String delCon = "\u2191" + delta.getString("confirmed");
                            String delDead = "\u2191" + delta.getString("deceased");
                            String delRec = "\u2191" + delta.getString("recovered");

                            data = new DistrictWiseData(district, confirm, active, recovered, dead, delCon, delRec, delDead);
                            districtWiseData.add(data);

                            progressBar.setVisibility(View.INVISIBLE);
                            txtLoad.setVisibility(View.INVISIBLE);

                            cardAdapter = new CardAdapter(districtWiseData);
                            lstDis.setAdapter(cardAdapter);
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonArrayRequest);
        requestQueue.getCache().clear();
        Log.d("covid19India", "FetchDistrict Completed");
    }

    class CardAdapter extends BaseAdapter implements Filterable {

        ArrayList<DistrictWiseData> districtWiseDataArrayList;
        CustomFilter filter;
        ArrayList<DistrictWiseData> filterArray;

        public CardAdapter(ArrayList<DistrictWiseData> districtWiseDataArrayList) {
            this.districtWiseDataArrayList = districtWiseDataArrayList;
            this.filterArray = districtWiseDataArrayList;
        }

        @Override
        public Filter getFilter() {
            if(filter == null) {
                filter = new CustomFilter();
            }
            return filter;
        }

        class viewHolder {
            View view;
            MaterialCardView cardView;
            MaterialTextView txtDis, txtCon, txtAct, txtRec, txtDead;
            MaterialTextView txtDeltaCon, txtDeltaRec, txtDeltaDead;
            AnimatedPieView animatedPieView;
        }

        @Override
        public int getCount() {
            return districtWiseDataArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return districtWiseDataArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("covid19India", "Adapter Started");
            viewHolder ViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.districtwise_card, parent, false);
                ViewHolder = new viewHolder();
                ViewHolder.view = convertView.findViewById(R.id.viewLeft);
                ViewHolder.cardView = convertView.findViewById(R.id.cardDistrict);
                ViewHolder.txtDis = convertView.findViewById(R.id.txtDistrict);
                ViewHolder.txtCon = convertView.findViewById(R.id.disCon);
                ViewHolder.txtAct = convertView.findViewById(R.id.disAct);
                ViewHolder.txtRec = convertView.findViewById(R.id.disRec);
                ViewHolder.txtDead = convertView.findViewById(R.id.disDead);
                ViewHolder.txtDeltaCon = convertView.findViewById(R.id.disDeltaCon);
                ViewHolder.txtDeltaRec = convertView.findViewById(R.id.disDeltaRec);
                ViewHolder.txtDeltaDead = convertView.findViewById(R.id.disDeltaDead);
                ViewHolder.animatedPieView = convertView.findViewById(R.id.disGraph);
                convertView.setTag(ViewHolder);
            } else {
                ViewHolder = (viewHolder) convertView.getTag();
            }

            if(Objects.equals(mapZones.get(districtWiseDataArrayList.get(position).getDistrict().toLowerCase()), "Green"))
                ViewHolder.view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDarkGreen));
            else if(Objects.equals(mapZones.get(districtWiseDataArrayList.get(position).getDistrict().toLowerCase()), "Red")) {
                ViewHolder.view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDarkRed));
            } else if(Objects.equals(mapZones.get(districtWiseDataArrayList.get(position).getDistrict().toLowerCase()), "Orange")) {
                ViewHolder.view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorOrange));
            } else {
                ViewHolder.view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
            }

            ViewHolder.txtDis.setText(districtWiseDataArrayList.get(position).getDistrict());
            ViewHolder.txtCon.setText(districtWiseDataArrayList.get(position).getDisConfirm());
            ViewHolder.txtAct.setText(districtWiseDataArrayList.get(position).getDisActive());
            ViewHolder.txtRec.setText(districtWiseDataArrayList.get(position).getDisRecover());
            ViewHolder.txtDead.setText(districtWiseDataArrayList.get(position).getDisDead());
            ViewHolder.txtDeltaCon.setText(districtWiseDataArrayList.get(position).getDisDelCon());
            ViewHolder.txtDeltaRec.setText(districtWiseDataArrayList.get(position).getDisDelRec());
            ViewHolder.txtDeltaDead.setText(districtWiseDataArrayList.get(position).getDisDelDead());

            AnimatedPieViewConfig config = new AnimatedPieViewConfig();
            config.startAngle(-90);
            config.splitAngle(1);
            config.addData(new SimplePieInfo(Float.parseFloat(districtWiseDataArrayList.get(position).getDisActive()), ContextCompat.getColor(getApplicationContext(), R.color.colorRed), "Active"));
            config.addData(new SimplePieInfo(Float.parseFloat(districtWiseDataArrayList.get(position).getDisRecover()), ContextCompat.getColor(getApplicationContext(), R.color.colorGreen), "Recovered"));
            config.addData(new SimplePieInfo(Float.parseFloat(districtWiseDataArrayList.get(position).getDisDead()), ContextCompat.getColor(getApplicationContext(), R.color.colorYellow), "Dead"));
            config.canTouch(false);
            config.strokeMode(false);
            config.duration(1000);

            ViewHolder.animatedPieView.applyConfig(config);
            ViewHolder.animatedPieView.start();
            Log.d("covid19India", "Adapter Ended");
            return convertView;
        }

        class CustomFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if(constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    ArrayList<DistrictWiseData> filters = new ArrayList<>();
                    for(int i = 0; i < filterArray.size(); i++) {
                        if(filterArray.get(i).getDistrict().toLowerCase().contains(constraint)) {
                            DistrictWiseData districtWiseData = new DistrictWiseData(filterArray.get(i).getDistrict(), filterArray.get(i).getDisConfirm(), filterArray.get(i).getDisActive(), filterArray.get(i).getDisRecover(), filterArray.get(i).getDisDead(), filterArray.get(i).getDisDelCon(), filterArray.get(i).getDisDelRec(), filterArray.get(i).getDisDelDead());
                            filters.add(districtWiseData);
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
                districtWiseDataArrayList = (ArrayList<DistrictWiseData>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    static class DistrictWiseData {
        private String district, disConfirm, disActive, disRecover, disDead, disDelCon, disDelRec, disDelDead;

        public DistrictWiseData(String district, String disConfirm, String disActive, String disRecover, String disDead, String disDelCon, String disDelRec, String disDelDead) {
            this.district = district;
            this.disConfirm = disConfirm;
            this.disActive = disActive;
            this.disRecover = disRecover;
            this.disDead = disDead;
            this.disDelCon = disDelCon;
            this.disDelRec = disDelRec;
            this.disDelDead = disDelDead;
        }

        public String getDistrict() {
            return district;
        }

        public String getDisConfirm() {
            return disConfirm;
        }

        public String getDisActive() {
            return disActive;
        }

        public String getDisRecover() {
            return disRecover;
        }

        public String getDisDead() {
            return disDead;
        }

        public String getDisDelCon() {
            return disDelCon;
        }

        public String getDisDelRec() {
            return disDelRec;
        }

        public String getDisDelDead() {
            return disDelDead;
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Log.d("covid19India", "District -> Exit");
    }
}
