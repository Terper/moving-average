package fi.arcada.sos_projekt_chart_sma;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;


public class SettingsActivity extends AppCompatActivity {

    Button buttonDateRange;
    Button buttonCurrency;
    Button buttonSave;

    String startDateString;
    String endDateString;

    String[] availableCurrencies = {"CAD", "DKK", "SEK", "NOK", "GBP", "USD"};
    int selectedCurrency = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Inställningar");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buttonSave), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences pref = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);


        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(View -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("startDate", startDateString);
            editor.putString("endDate", endDateString);
            editor.putString("currency", availableCurrencies[selectedCurrency]);
            editor.apply();
            finish();
        });

        String endDate = pref.getString("endDate", "");
        String startDate = pref.getString("startDate", "");
        String currency = pref.getString("currency", "");

        // uppdaterar selectedCurrency med indexet av det sparade värdet
        for (int i = 0; i < availableCurrencies.length; i++) {
            if (Objects.equals(availableCurrencies[i], currency)) {
                selectedCurrency = i;
            }
        }

        startDateString = pref.getString("startDate", "");
        endDateString = pref.getString("endDate", "");

        buttonDateRange = findViewById(R.id.buttonDateRange);
        buttonDateRange.setText(String.format("%s — %s", startDate, endDate));
        buttonDateRange.setOnClickListener(view -> DatePickerDialog());

        buttonCurrency = findViewById(R.id.buttonCurrency);
        buttonCurrency.setText(currency);
        buttonCurrency.setOnClickListener(view -> CurrencyDialog());
    }

    // https://www.geeksforgeeks.org/how-to-implement-date-range-picker-in-android/
    private void DatePickerDialog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            startDateString = sdf.format(new Date(startDate));
            endDateString = sdf.format(new Date(endDate));

            buttonDateRange.setText(String.format("%s - %s", startDateString, endDateString));
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void CurrencyDialog() {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(SettingsActivity.this);
        alertDialog.setTitle("Valuta");
        alertDialog.setSingleChoiceItems(availableCurrencies, selectedCurrency, (dialog, which) -> {
            selectedCurrency = which;
            buttonCurrency.setText(availableCurrencies[selectedCurrency]);
            dialog.dismiss();
        });
        alertDialog.show();
    }
}