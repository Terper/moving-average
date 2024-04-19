package fi.arcada.sos_projekt_chart_sma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    String currency, startDate, endDate;
    ToggleButton sma10;
    ToggleButton sma30;
    TextView chartInfo;
    LineChart chart;
    FloatingActionButton settingsButton;
    LineData lineData;
    LineDataSet currencyLineData;
    LineDataSet sma10LineData;
    LineDataSet sma30LineData;
    ArrayList<Double> currencyValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Växelkurs i Euro");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        SharedPreferences pref = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        // hämtar sparad data, ifall datan inte har blivit sparad ges temporära värden
        currency = pref.getString("currency", "USD");
        startDate = pref.getString("startDate", "2024-01-01");
        endDate = pref.getString("endDate", "2024-03-31");

        // ifall datan inte är sparad spara nuvarande
        if (!pref.contains("currency")) {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.putString("currency", currency);
            editor.putString("startDate", startDate);
            editor.putString("endDate", endDate);
            editor.apply();
        }

        updateCurrencyLineData();

        updateChartInfo();

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        });

        sma10 = findViewById(R.id.sma10);
        sma10.setOnClickListener(view -> {
            if (sma10.isChecked()) {
                chart = findViewById(R.id.chart);
                ArrayList<Double> sma10Values = getCurrencyValues("USD", startDate, endDate);
                List<Entry> entries = new ArrayList<Entry>();
                for (int i = 0; i < sma10Values.size(); i++) {
                    entries.add(new Entry(i, sma10Values.get(i).floatValue()));
                }
                sma10LineData = new LineDataSet(entries, "SMA10");
                sma10LineData.setDrawCircles(false);
                sma10LineData.setDrawValues(false);
                sma10LineData.setLineWidth(2);
                sma10LineData.setColor(Color.GREEN);

                lineData.addDataSet(sma10LineData);
                chart.notifyDataSetChanged();
                chart.invalidate();
            } else {
                lineData.removeDataSet(sma10LineData);
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });

        sma30 = findViewById(R.id.sma30);
        sma30.setOnClickListener(view  -> {
            if (sma30.isChecked()) {
                chart = findViewById(R.id.chart);
                ArrayList<Double> sma30Values = getCurrencyValues("GBP", startDate, endDate);
                List<Entry> entries = new ArrayList<Entry>();
                for (int i = 0; i < sma30Values.size(); i++) {
                    entries.add(new Entry(i, sma30Values.get(i).floatValue()));
                }
                sma30LineData = new LineDataSet(entries, "SMA30");
                sma30LineData.setDrawCircles(false);
                sma30LineData.setDrawValues(false);
                sma30LineData.setLineWidth(2);
                sma30LineData.setColor(Color.RED);

                lineData.addDataSet(sma30LineData);
                chart.notifyDataSetChanged();
                chart.invalidate();
            } else {
                lineData.removeDataSet(sma30LineData);
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences pref = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        sma10 = findViewById(R.id.sma10);
        sma10.setChecked(false);
        sma30 = findViewById(R.id.sma30);
        sma30.setChecked(false);

        // uppdaterar variablerna
        currency = pref.getString("currency", "USD");
        startDate = pref.getString("startDate", "2024-01-01");
        endDate = pref.getString("endDate", "2024-03-31");

        updateChartInfo();
        updateCurrencyLineData();

    }


    private void updateCurrencyLineData() {
        chart = findViewById(R.id.chart);
        List<Entry> entries = new ArrayList<Entry>();

        // Hämta växelkurser från API
        currencyValues = getCurrencyValues(currency, startDate, endDate);
        // Skriv ut dem i konsolen (Logcat)
        System.out.println("CurrencyValues: " + currencyValues.toString());

        // skapar en entry för varje värde
        for (int i = 0; i < currencyValues.size(); i++) {
            entries.add(new Entry(i, currencyValues.get(i).floatValue()));
        }

        // skapar en dataset med alla entries
        currencyLineData = new LineDataSet(entries, currency);

        // dataset styling
        currencyLineData.setDrawCircles(false);
        currencyLineData.setDrawValues(false);
        currencyLineData.setLineWidth(2);
        currencyLineData.setColor(R.color.purple_500);

        // skapar en lineData med datasetet
        lineData = new LineData(currencyLineData);
        // lägger in lineData i diagrammet
        chart.setData(lineData);
        // refreshar diagrammet
        chart.invalidate();
    }

    private void updateChartInfo () {
        chartInfo = findViewById(R.id.chartInfo);
        chartInfo.setText(String.format("%s | %s — %s", currency, startDate, endDate));
    }

    // Färdig metod som hämtar växelkursdata
    public ArrayList<Double> getCurrencyValues(String currency, String from, String to) {

        CurrencyApi api = new CurrencyApi();
        ArrayList<Double> currencyData = null;

        String urlString = String.format("https://api.frankfurter.app/%s..%s",
                from.trim(),
                to.trim());

        try {
            String jsonData = api.execute(urlString).get();

            if (jsonData != null) {
                currencyData = api.getCurrencyData(jsonData, currency.trim());
                Toast.makeText(getApplicationContext(), String.format("Hämtade %s valutakursvärden från servern", currencyData.size()), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Kunde inte hämta växelkursdata från servern: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return currencyData;
    }
}