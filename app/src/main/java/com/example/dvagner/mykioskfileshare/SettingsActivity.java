package com.example.dvagner.mykioskfileshare;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText etText;
    EditText etDomain;
    EditText etUsername;
    EditText etPassword;
    EditText etTimer;
    private Integer timer_settings;
    final String URL = "saved_url";
    final String DOMAIN = "saved_domain";
    final String USERNAME = "saved_username";
    final String PASSWORD = "saved_password";
    final String Timmer = "saved_timer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etText = (EditText) findViewById(R.id.editUrl);
        etTimer = (EditText) findViewById(R.id.editTimer);
        etDomain = (EditText) findViewById(R.id.editDomain);
        etUsername = (EditText) findViewById(R.id.editUserName);
        etPassword = (EditText) findViewById(R.id.editPassword);
    }
    @Override
    protected void onResume() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedText = preferences.getString(URL, null);
        String savedDomain = preferences.getString(DOMAIN, null);
        String savedUsername = preferences.getString(USERNAME, null);
        String savedPassword = preferences.getString(PASSWORD, null);
        timer_settings = Integer.parseInt( preferences.getString(Timmer, "0"));
        etText.setText(savedText);
        etTimer.setText(timer_settings.toString());
        etDomain.setText(savedDomain);
        etUsername.setText(savedUsername);
        etPassword.setText(savedPassword);
        Toast.makeText(this, "Settings loaded", Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL, etText.getText().toString());
        editor.putString(Timmer, etTimer.getText().toString());
        editor.putString(DOMAIN,etDomain.getText().toString());
        editor.putString(USERNAME,etUsername.getText().toString());
        editor.putString(PASSWORD,etPassword.getText().toString());
        editor.commit();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        super.onPause();
    }
}
