package com.manishtaraiya.bleterm.Filter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.manishtaraiya.bleterm.R;

import mehdi.sakout.fancybuttons.FancyButton;

public class FilterActivity extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String Mac = "macKey";
    public static final String Rssi = "rssiKey";
    public static final String DB = " dBm";
    SharedPreferences sharedpreferences;
    int progress_result = 0;
    String name, mac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        //Set screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FancyButton button = (FancyButton) findViewById(R.id.filter_button);

        final EditText name_text = (EditText) findViewById(R.id.name_edit_text);
        final EditText mac_text = (EditText) findViewById(R.id.mac_edit_text);
        final TextView rssi_text = (TextView) findViewById(R.id.rssitext);
        SeekBar seekBar = (SeekBar) findViewById(R.id.SeekBar_rssi);
        sharedpreferences = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        name = sharedpreferences.getString(Name, null);
        mac = sharedpreferences.getString(Mac, null);
        progress_result = sharedpreferences.getInt(Rssi, 0);
        seekBar.setProgress(progress_result);
        rssi_text.setText((progress_result - 100) + DB);


       /* if(progress_result==0) {seekBar.setProgress(0);rssi_text.setText((-100) + DB);}
        else {seekBar.setProgress(100+progress_result);rssi_text.setText((progress_result) + DB);}
*/
        if (name != null) name_text.setText(name);
        if (mac != null) mac_text.setText(mac);

        Toast.makeText(FilterActivity.this, name + mac + (progress_result - 100), Toast.LENGTH_SHORT).show();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress_result = progress;
                rssi_text.setText((progress - 100) + DB);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(FilterActivity.this,"start tracking" +(progress_result-100),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(FilterActivity.this,"stop tracking "+ (progress_result-100),Toast.LENGTH_SHORT).show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).edit();
                name = name_text.getText().toString().trim();
                mac = mac_text.getText().toString().trim();

                editor.putString(Name, name);
                editor.putString(Mac, mac);
                editor.putInt(Rssi, progress_result);
                editor.apply();
                Toast.makeText(FilterActivity.this, name + mac + (progress_result - 100), Toast.LENGTH_SHORT).show();
               /* Intent intent = new Intent(FilterActivity.this, MainActivity.class);
                startActivity(intent);*/
                finish();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
}
