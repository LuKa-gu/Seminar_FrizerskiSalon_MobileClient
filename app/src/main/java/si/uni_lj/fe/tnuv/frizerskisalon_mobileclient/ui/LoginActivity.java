package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());

        TextView tvSignupLink = findViewById(R.id.tvSignupLink);
        tvSignupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.vnesi_ime_in_geslo, Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrofit instance iz ApiClient
        Retrofit retrofit = ApiClient.getClient(null);

        // Dinamični interface samo za login
        LoginApi api = retrofit.create(LoginApi.class);

        // request body kot map
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("Uporabnisko_ime", username);
        bodyMap.put("Geslo", password);

        Call<Map<String, String>> call = api.login(bodyMap);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (!response.isSuccessful()) {
                    ErrorHandler.showToastError(LoginActivity.this, response, null, getString(R.string.napaka_pri_prijavi));
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(LoginActivity.this, R.string.napaka_prazen_odgovor_streznika, Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, String> body = response.body();

                String token = body.get(PREFS_TOKEN_KEY);
                String successMessage = body.get("message");

                if (token != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putString(PREFS_TOKEN_KEY, token).apply();

                    if (successMessage != null) {
                        Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    }

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.napaka_vracanje_tokena, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                ErrorHandler.showToastError(LoginActivity.this, null, t, null);
            }
        });
    }

    // Inline interface za ta endpoint
    interface LoginApi {
        @POST("uporabniki/login")
        Call<Map<String, String>> login(@Body Map<String, String> body);
    }
}
