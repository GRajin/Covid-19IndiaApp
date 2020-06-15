package com.rajingangadharan.covid19india;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class StateDetails extends AppCompatActivity {

    ArrayList<BarEntry> barEntries = new ArrayList<>();
    ArrayList<String> arrDistrict = new ArrayList<>();
    ArrayList<String> arrConfirm = new ArrayList<>();
    ArrayList<String> arrTopCon = new ArrayList<>();
    ArrayList<String> arrTopDis = new ArrayList<>();
    BarChart barChart;
    String stateCode;
    String json = "";
    float fltConfirmed;
    private int pop;
    private String stateName;
    RequestQueue requestQueue;
    ProgressBar progressBar;
    MaterialTextView txtProgress;
    MaterialTextView con1, con2, con3, con4, con5;
    MaterialTextView dis1, dis2, dis3, dis4, dis5;
    MaterialButton btnDis;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        Log.d("covid19India", "State -> Menu Inflated");
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            Log.d("covid19India", "State -> Item <Refresh> Selected");
            if ( isConnected() ) {
                Log.d("covid19India", "State -> Internet Available, So Proceeding");

                txtProgress.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                txtProgress.setText(R.string.loadingPleaseWait);
                btnDis.setVisibility(View.INVISIBLE);

                con1.setVisibility(View.INVISIBLE); con2.setVisibility(View.INVISIBLE); con3.setVisibility(View.INVISIBLE); con4.setVisibility(View.INVISIBLE); con5.setVisibility(View.INVISIBLE);
                dis1.setVisibility(View.INVISIBLE); dis2.setVisibility(View.INVISIBLE); dis3.setVisibility(View.INVISIBLE); dis4.setVisibility(View.INVISIBLE); dis5.setVisibility(View.INVISIBLE);

                graph();
                getTests();
                getFirstFive();
            } else {
                Log.d("covid19India", "State -> Internet Not Available, So Not Proceding");
                Toast.makeText(getApplicationContext(), "Please Connect To Network", Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_details);

        btnDis = findViewById(R.id.btnDistrict);
        MaterialTextView txtConPLak = findViewById(R.id.conPLak);
        MaterialTextView recRate = findViewById(R.id.RecRate);
        MaterialTextView deadRate = findViewById(R.id.deadRate);

        con1 = findViewById(R.id.con1);
        con2 = findViewById(R.id.con2);
        con3 = findViewById(R.id.con3);
        con4 = findViewById(R.id.con4);
        con5 = findViewById(R.id.con5);
        dis1 = findViewById(R.id.dis1);
        dis2 = findViewById(R.id.dis2);
        dis3 = findViewById(R.id.dis3);
        dis4 = findViewById(R.id.dis4);
        dis5 = findViewById(R.id.dis5);

        progressBar = findViewById(R.id.progress);
        txtProgress = findViewById(R.id.txtProgress);
        barChart = findViewById(R.id.barChart);
        requestQueue = Volley.newRequestQueue(this);

        txtProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txtProgress.setText(R.string.loadingPleaseWait);
        btnDis.setVisibility(View.INVISIBLE);

        con1.setVisibility(View.INVISIBLE); con2.setVisibility(View.INVISIBLE); con3.setVisibility(View.INVISIBLE); con4.setVisibility(View.INVISIBLE); con5.setVisibility(View.INVISIBLE);
        dis1.setVisibility(View.INVISIBLE); dis2.setVisibility(View.INVISIBLE); dis3.setVisibility(View.INVISIBLE); dis4.setVisibility(View.INVISIBLE); dis5.setVisibility(View.INVISIBLE);

        stateName = getIntent().getStringExtra("State");

        if(getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.actionbar_statedetails);
            View view = getSupportActionBar().getCustomView();
            MaterialTextView txtTitle = view.findViewById(R.id.txtStateActionBar);
            assert stateName != null;
            txtTitle.setText(stateName.toUpperCase());
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        stateCode = getIntent().getStringExtra("StateCode");
        fltConfirmed = Float.parseFloat(Objects.requireNonNull(getIntent().getStringExtra("confirmed")));
        String strActive = getIntent().getStringExtra("active");
        DecimalFormat decimalFormat = new DecimalFormat("##,##,###");
        MaterialTextView staActive = findViewById(R.id.staActive);
        staActive.setText(decimalFormat.format(Integer.parseInt(Objects.requireNonNull(strActive))));
        String strRecovered = getIntent().getStringExtra("recovered");
        MaterialTextView staRecovered = findViewById(R.id.staRecovered);
        staRecovered.setText(decimalFormat.format(Integer.parseInt(Objects.requireNonNull(strRecovered))));
        float fltRecovered = Float.parseFloat(strRecovered);
        String strDead = getIntent().getStringExtra("dead");
        MaterialTextView staDead = findViewById(R.id.staDead);
        staDead.setText(decimalFormat.format(Integer.parseInt(Objects.requireNonNull(strDead))));
        float fltDead = Float.parseFloat(strDead);
        assert stateCode != null;

        btnDis.setOnClickListener(v -> {
            Intent intent = new Intent(StateDetails.this, DistrictWise.class);
            intent.putExtra("State", stateName);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        txtConPLak.setText(getPop());
        float recR = (fltRecovered / fltConfirmed) * 100;
        float deadR = (fltDead / fltConfirmed) * 100;

        recRate.setText(String.format("%s%%", String.format(Locale.US, "%.2f", recR)));
        deadRate.setText(String.format("%s%%", String.format(Locale.US, "%.2f", deadR)));

        if(isConnected()) {
            graph();
            getTests();
            getFirstFive();
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

    private void getFirstFive() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, "https://api.covid19india.org/v2/state_district_wise.json", null, response -> {
            try {
                arrConfirm.clear();
                arrDistrict.clear();
                for(int i = 0; i < response.length(); i++) {
                    JSONObject dis = response.getJSONObject(i);
                    if(dis.getString("statecode").toLowerCase().equals(stateCode.toLowerCase())) {
                        JSONArray districtData = dis.getJSONArray("districtData");
                        for ( int j = 0; j < districtData.length(); j++ ) {
                            JSONObject districts = districtData.getJSONObject(j);
                            arrDistrict.add(districts.getString("district").toUpperCase());
                            arrConfirm.add(districts.getString("confirmed"));
                        }
                    }
                }
                ArrayList<Integer> arrIntCon = new ArrayList<>();
                arrIntCon.clear();
                arrTopDis.clear();
                arrTopCon.clear();
                for(String con : arrConfirm) {
                    try {
                        arrIntCon.add(Integer.parseInt(con));
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                Collections.sort(arrIntCon, Collections.reverseOrder());
                int length = arrIntCon.size();
                int idx = 0;
                for(int i = 0; i < 5;) {
                    if(idx >= length) {
                        break;
                    }
                    int eleIdx = arrConfirm.indexOf(Integer.toString(arrIntCon.get(idx)));
                    String text = arrDistrict.get(eleIdx).substring(0, 1).toUpperCase() + arrDistrict.get(eleIdx).substring(1).toLowerCase();
                    String con = arrConfirm.get(eleIdx);
                    if(text.toLowerCase().equals("unknown")) {
                        idx += 1;
                        continue;
                    } else if(arrTopDis.contains(text)) {
                        idx += 1;
                        continue;
                    } else {
                        arrTopDis.add(text);
                        arrTopCon.add(con);
                        idx += 1;
                        i++;
                    }
                }

                int len = arrTopDis.size();
                if(len == 0) {
                    con1.setVisibility(View.INVISIBLE); con2.setVisibility(View.INVISIBLE); con3.setVisibility(View.INVISIBLE); con4.setVisibility(View.INVISIBLE); con5.setVisibility(View.INVISIBLE);
                    dis1.setVisibility(View.INVISIBLE); dis2.setVisibility(View.INVISIBLE); dis3.setVisibility(View.INVISIBLE); dis4.setVisibility(View.INVISIBLE); dis5.setVisibility(View.INVISIBLE);
                } else if(len == 1) {
                    con1.setVisibility(View.VISIBLE); dis1.setVisibility(View.VISIBLE);
                    con1.setText(arrTopCon.get(0)); dis1.setText(arrTopDis.get(0));
                } else if(len == 2) {
                    con1.setVisibility(View.VISIBLE); dis1.setVisibility(View.VISIBLE);
                    con1.setText(arrTopCon.get(0)); dis1.setText(arrTopDis.get(0));
                    con2.setVisibility(View.VISIBLE); dis2.setVisibility(View.VISIBLE);
                    con2.setText(arrTopCon.get(1)); dis2.setText(arrTopDis.get(1));
                } else if(len == 3) {
                    con1.setVisibility(View.VISIBLE); dis1.setVisibility(View.VISIBLE);
                    con1.setText(arrTopCon.get(0)); dis1.setText(arrTopDis.get(0));
                    con2.setVisibility(View.VISIBLE); dis2.setVisibility(View.VISIBLE);
                    con2.setText(arrTopCon.get(1)); dis2.setText(arrTopDis.get(1));
                    con3.setVisibility(View.VISIBLE); dis3.setVisibility(View.VISIBLE);
                    con3.setText(arrTopCon.get(2)); dis3.setText(arrTopDis.get(2));
                } else if(len == 4) {
                    con1.setVisibility(View.VISIBLE); dis1.setVisibility(View.VISIBLE);
                    con1.setText(arrTopCon.get(0)); dis1.setText(arrTopDis.get(0));
                    con2.setVisibility(View.VISIBLE); dis2.setVisibility(View.VISIBLE);
                    con2.setText(arrTopCon.get(1)); dis2.setText(arrTopDis.get(1));
                    con3.setVisibility(View.VISIBLE); dis3.setVisibility(View.VISIBLE);
                    con3.setText(arrTopCon.get(2)); dis3.setText(arrTopDis.get(2));
                    con4.setVisibility(View.VISIBLE); dis4.setVisibility(View.VISIBLE);
                    con4.setText(arrTopCon.get(3)); dis4.setText(arrTopDis.get(3));
                } else if(len == 5) {
                    con1.setVisibility(View.VISIBLE); dis1.setVisibility(View.VISIBLE);
                    con1.setText(arrTopCon.get(0)); dis1.setText(arrTopDis.get(0));
                    con2.setVisibility(View.VISIBLE); dis2.setVisibility(View.VISIBLE);
                    con2.setText(arrTopCon.get(1)); dis2.setText(arrTopDis.get(1));
                    con3.setVisibility(View.VISIBLE); dis3.setVisibility(View.VISIBLE);
                    con3.setText(arrTopCon.get(2)); dis3.setText(arrTopDis.get(2));
                    con4.setVisibility(View.VISIBLE); dis4.setVisibility(View.VISIBLE);
                    con4.setText(arrTopCon.get(3)); dis4.setText(arrTopDis.get(3));
                    con5.setVisibility(View.VISIBLE); dis5.setVisibility(View.VISIBLE);
                    con5.setText(arrTopCon.get(4)); dis5.setText(arrTopDis.get(4));
                }

                txtProgress.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                btnDis.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonArrayRequest);
        requestQueue.getCache().clear();
    }

    private void graph() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/states_daily.json", null, response -> {
            try {
                JSONArray stateDaily = response.getJSONArray("states_daily");
                int flag = 1;
                for(int i = stateDaily.length() - 1; i >= 0; i--) {
                    JSONObject confirmed = stateDaily.getJSONObject(i);
                    if(flag <= 20) {
                        if ((confirmed.getString("status").equals("Confirmed"))) {
                            String confirm = confirmed.getString(stateCode);
                            barEntries.add(new BarEntry(flag, Integer.parseInt(confirm)));
                            flag = flag + 1;
                        }
                    }
                }
                BarDataSet barDataSet = new BarDataSet(barEntries, "Confirmed");
                barDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                barDataSet.setBarBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDarkRed));
                barDataSet.setDrawValues(false);
                CustomBarChartRender chartRender = new CustomBarChartRender(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
                chartRender.setRadius();
                barChart.setRenderer(chartRender);
                BarData barData = new BarData();
                barData.addDataSet(barDataSet);
                barChart.setData(barData);
                barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart.getXAxis().setDrawGridLines(false);
                barChart.getXAxis().setDrawLabels(false);
                barChart.getXAxis().setAxisLineWidth(2f);
                barChart.getXAxis().setAxisLineColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
                barChart.getAxisLeft().setDrawGridLines(false);
                barChart.getAxisLeft().setAxisLineWidth(2f);
                barChart.getAxisLeft().setLabelCount(4, true);
                barChart.getAxisLeft().setAxisMinimum(0);
                barChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
                barChart.animateY(3000);
                barChart.getLegend().setEnabled(false);
                barChart.getDescription().setEnabled(false);
                barChart.getAxisRight().setEnabled(false);
                barChart.invalidate();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonObjectRequest);
        requestQueue.getCache().clear();
    }

    private String getPop() {
        float conPL = 0;
        try {
            InputStream inputStream = getAssets().open("population.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                json = String.format("%s%s", json, line);
            }

            json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            JSONObject jsonObject = new JSONObject(json);
            pop = jsonObject.getInt(stateCode);
            conPL = (fltConfirmed / pop) * 100000;

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return String.format(Locale.US, "%.2f", conPL);
    }

    private void getTests() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/state_test_data.json", null, response -> {
            try {
                double dblTest = 0;
                JSONArray stateTest = response.getJSONArray("states_tested_data");
                for(int i = 0; i < stateTest.length(); i++) {
                    JSONObject state = stateTest.getJSONObject(i);
                    if(state.getString("state").toLowerCase().equals(stateName.toLowerCase())) {
                        if(state.getString("totaltested").equals("")) {
                            break;
                        } else {
                            dblTest = state.getDouble("totaltested");
                        }
                    }

                    MaterialTextView testPerLakh = findViewById(R.id.testRate);
                    double dblTestPerLakh = ((float) dblTest / pop) * 100000;
                    testPerLakh.setText(String.format(Locale.US, "%.2f", dblTestPerLakh));

                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show());
        requestQueue.add(jsonObjectRequest);
        requestQueue.getCache().clear();
    }

    public class CustomBarChartRender extends BarChartRenderer {

        private int mRadius;
        private RectF mBarShadowRectBuffer = new RectF();

        CustomBarChartRender(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
        }

        void setRadius() {
            this.mRadius = 5;
        }

        @Override
        protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            Transformer transformer = barChart.getTransformer(dataSet.getAxisDependency());
            mBarBorderPaint.setColor(dataSet.getBarBorderColor());
            mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));
            mShadowPaint.setColor(dataSet.getBarShadowColor());
            boolean drawBorder = dataSet.getBarBorderWidth() > 0f;
            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();

            if(mChart.isDrawBarShadowEnabled()) {
                mShadowPaint.setColor(dataSet.getBarShadowColor());
                BarData barData = mChart.getBarData();
                float barWidth = barData.getBarWidth();
                float barWidthHalf = barWidth / 2.0f;
                float x;
                int i = 0;
                double count = Math.min(Math.ceil((int) (double) ((float) dataSet.getEntryCount() * phaseX)), dataSet.getEntryCount());
                while (i < count) {
                    BarEntry e = dataSet.getEntryForIndex(i);
                    x = e.getX();
                    mBarShadowRectBuffer.left = x - barWidthHalf;
                    mBarShadowRectBuffer.right = x + barWidthHalf;
                    transformer.rectValueToPixel(mBarShadowRectBuffer);
                    if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
                        i++;
                        continue;
                    }
                    if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                        break;
                    mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
                    mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();
                    c.drawRoundRect(mBarRect, mRadius, mRadius, mShadowPaint);
                    i++;
                }
            }
            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.setBarWidth(mChart.getBarData().getBarWidth());
            buffer.feed(dataSet);
            transformer.pointValuesToPixel(buffer.buffer);
            boolean isSingleColor = dataSet.getColors().size() == 1;
            if (isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor());
            }
            int j = 0;
            while (j < buffer.size()) {
                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                    j += 4;
                    continue;
                }
                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;
                if (!isSingleColor) {
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
                }
                if (dataSet.getGradientColor() != null) {
                    GradientColor gradientColor = dataSet.getGradientColor();
                    mRenderPaint.setShader(new LinearGradient(
                            buffer.buffer[j],
                            buffer.buffer[j + 3],
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            gradientColor.getStartColor(),
                            gradientColor.getEndColor(),
                            android.graphics.Shader.TileMode.MIRROR));
                }
                if (dataSet.getGradientColors() != null) {
                    mRenderPaint.setShader(new LinearGradient(
                            buffer.buffer[j],
                            buffer.buffer[j + 3],
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            dataSet.getGradientColor(j / 4).getStartColor(),
                            dataSet.getGradientColor(j / 4).getEndColor(),
                            Shader.TileMode.MIRROR));
                }
                Path path2 = roundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3]), mRadius, mRadius);
                c.drawPath(path2, mRenderPaint);
                if (drawBorder) {
                    Path path = roundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3]), mRadius, mRadius);
                    c.drawPath(path, mBarBorderPaint);
                }
                j += 4;
            }
        }

        private Path roundRect(RectF rect, float rx, float ry) {
            float top = rect.top;
            float left = rect.left;
            float right = rect.right;
            float bottom = rect.bottom;
            Path path = new Path();
            if (rx < 0) rx = 0;
            if (ry < 0) ry = 0;
            float width = right - left;
            float height = bottom - top;
            if (rx > width / 2) rx = width / 2;
            if (ry > height / 2) ry = height / 2;
            float widthMinusCorners = (width - (2 * rx));
            float heightMinusCorners = (height - (2 * ry));
            path.moveTo(right, top + ry);
            path.rQuadTo(0, -ry, -rx, -ry);
            path.rLineTo(-widthMinusCorners, 0);
            path.rQuadTo(-rx, 0, -rx, ry);
            path.rLineTo(0, heightMinusCorners);
            path.rLineTo(0, ry);
            path.rLineTo(rx, 0);
            path.rLineTo(widthMinusCorners, 0);
            path.rLineTo(rx, 0);
            path.rLineTo(0, -ry);
            path.rLineTo(0, -heightMinusCorners);
            path.close();
            return path;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
