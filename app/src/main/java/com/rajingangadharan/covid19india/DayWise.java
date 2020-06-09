package com.rajingangadharan.covid19india;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DayWise extends AppCompatActivity {

	private ListView lstDay;
	private CardAdapter cardAdapter;
	private ProgressBar progressBar;
	private MaterialTextView txtProgressDay;
	private LineChart lineChart;
	private RequestQueue requestQueue;

	private ArrayList<String> arrConDay = new ArrayList<>();
	private ArrayList<String> arrRecDay = new ArrayList<>();
	private ArrayList<String> arrDeadDay = new ArrayList<>();
	private ArrayList<String> arrConTot = new ArrayList<>();
	private ArrayList<String> arrRecTot = new ArrayList<>();
	private ArrayList<String> arrDeadTot = new ArrayList<>();
	private ArrayList<String> arrDate = new ArrayList<>();
	private ArrayList<Entry> arrGraph = new ArrayList<>();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		return true;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.refresh) {
			if ( isConnected() ) {
				preFetch();
				fetchDay();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_wise);

		if(getSupportActionBar() != null) {
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			getSupportActionBar().setCustomView(R.layout.daywise_actionbar);
		}

		lstDay = findViewById(R.id.lstDay);
		progressBar = findViewById(R.id.loadProgress);
		txtProgressDay = findViewById(R.id.txtProgressDay);
		lineChart = findViewById(R.id.lineChart);
		requestQueue = Volley.newRequestQueue(this);

		if(isConnected()) {
			preFetch();
			fetchDay();
		} else {
			Toast.makeText(getApplicationContext(), "Please Connect To Network", Toast.LENGTH_LONG).show();
		}
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

	class CardAdapter extends BaseAdapter {

		class viewHolder {
			MaterialTextView txtConDay, txtRecDay, txtDeadDay, txtConTot;
			MaterialTextView txtRecTot, txtDeadTot, txtDay;
		}

		@Override
		public int getCount() {
			return arrConDay.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			viewHolder ViewHolder;
			if(convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.daywise_card, null);
				ViewHolder = new viewHolder();
				ViewHolder.txtConDay = convertView.findViewById(R.id.txtConDay);
				ViewHolder.txtRecDay = convertView.findViewById(R.id.txtRecDay);
				ViewHolder.txtDeadDay = convertView.findViewById(R.id.txtDeadDay);
				ViewHolder.txtConTot = convertView.findViewById(R.id.txtConTot);
				ViewHolder.txtRecTot = convertView.findViewById(R.id.txtRecTot);
				ViewHolder.txtDeadTot = convertView.findViewById(R.id.txtDeadTot);
				ViewHolder.txtDay = convertView.findViewById(R.id.txtDay);
				convertView.setTag(ViewHolder);
			} else {
				ViewHolder = (viewHolder) convertView.getTag();
			}

			ViewHolder.txtDay.setText(arrDate.get(position));
			ViewHolder.txtConDay.setText(arrConDay.get(position));
			ViewHolder.txtRecDay.setText(arrRecDay.get(position));
			ViewHolder.txtDeadDay.setText(arrDeadDay.get(position));
			ViewHolder.txtConTot.setText(arrConTot.get(position));
			ViewHolder.txtRecTot.setText(arrRecTot.get(position));
			ViewHolder.txtDeadTot.setText(arrDeadTot.get(position));
			return convertView;
		}
	}

	public void preFetch() {
		arrDate.clear();
		arrConDay.clear();
		arrRecDay.clear();
		arrDeadDay.clear();
		arrConTot.clear();
		arrRecTot.clear();
		arrDeadTot.clear();
		arrGraph.clear();

		progressBar.setVisibility(View.VISIBLE);
		txtProgressDay.setVisibility(View.VISIBLE);

		lineChart.invalidate();
		lineChart.clear();
	}

	public void fetchDay() {
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/data.json", null, response -> {
			try {
				JSONArray dayWise = response.getJSONArray("cases_time_series");
				for ( int i = dayWise.length() - 1; i >= 0; i-- ) {
					JSONObject JO = dayWise.getJSONObject(i);
					arrConDay.add(JO.getString("dailyconfirmed"));
					arrRecDay.add(JO.getString("dailyrecovered"));
					arrDeadDay.add(JO.getString("dailydeceased"));
					arrDate.add(JO.getString("date").toUpperCase());
					arrConTot.add(JO.getString("totalconfirmed"));
					arrRecTot.add(JO.getString("totalrecovered"));
					arrDeadTot.add(JO.getString("totaldeceased"));
				}
				for ( int j = 0; j < dayWise.length(); j++ ) {
					JSONObject JO = dayWise.getJSONObject(j);
					arrGraph.add(new Entry(j, JO.getInt("totalconfirmed")));
				}

				progressBar.setVisibility(View.INVISIBLE);
				txtProgressDay.setVisibility(View.INVISIBLE);

				LineDataSet lineDataSet = new LineDataSet(arrGraph, "Confirmed");
				lineDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
				lineDataSet.setDrawCircles(false);
				ArrayList<ILineDataSet> arrayList = new ArrayList<>();
				arrayList.add(lineDataSet);
				LineData lineData = new LineData(arrayList);
				lineChart.setData(lineData);
				lineChart.getXAxis().setEnabled(false);
				lineChart.animateY(2000);
				lineChart.getAxisLeft().setDrawGridLines(false);
				lineChart.getAxisRight().setDrawGridLines(false);
				lineChart.getLegend().setEnabled(false);
				lineChart.getDescription().setEnabled(false);
				lineChart.getAxisLeft().setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
				lineChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
				lineChart.getAxisRight().setEnabled(false);
				lineChart.invalidate();

				cardAdapter = new CardAdapter();
				lstDay.setAdapter(cardAdapter);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
		requestQueue.add(jsonObjectRequest);
		requestQueue.getCache().clear();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
