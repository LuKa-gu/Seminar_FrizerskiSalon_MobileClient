package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import retrofit2.http.PATCH;
import retrofit2.http.Url;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.TerminiAdapter;

public class PregledActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private PregledApi pregledApi;
    private TerminiAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JWTManager.preveriJWT(this, () -> {

            setContentView(R.layout.activity_pregled);

            rv = findViewById(R.id.rvTermini);
            rv.setLayoutManager(new LinearLayoutManager(this));

            SharedPreferences prefs =
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = prefs.getString(PREFS_TOKEN_KEY, null);

            Retrofit retrofit = ApiClient.getClient(token);
            pregledApi = retrofit.create(PregledApi.class);

            Button btnNazaj = findViewById(R.id.btnNazaj);
            btnNazaj.setOnClickListener(v -> finish());

            naloziTermine();
        });
    }

    interface PregledApi {
        @GET("termini/pregled")
        Call<List<Map<String, Object>>> getPregled();
    }

    interface  PreklicApi {
        @PATCH
        Call<Map<String, Object>> preklic(@Url String url);
    }

    private void preklicTermin(Map<String, Object> termin) {

        String url = (String) termin.get("preklic_url");

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(PREFS_TOKEN_KEY, null);

        Retrofit retrofit = ApiClient.getClient(token);
        PreklicApi api = retrofit.create(PreklicApi.class);

        api.preklic(url).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response) {

                if (!response.isSuccessful()) {
                    ErrorHandler.showToastError(PregledActivity.this, response, null, getString(R.string.napaka_preklic_termina));
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(PregledActivity.this, R.string.napaka_prazen_odgovor_streznika, Toast.LENGTH_LONG).show();
                    return;
                }

                Object msgObj = response.body().get("message");
                String msg = msgObj != null ? msgObj.toString() : getString(R.string.termin_uspesno_preklican);

                new AlertDialog.Builder(PregledActivity.this)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, (d, w) -> naloziTermine())
                        .show();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                ErrorHandler.showToastError(PregledActivity.this, null, t, null);
            }
        });
    }

    private void naloziTermine() {
        pregledApi.getPregled().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(
                    Call<List<Map<String, Object>>> call,
                    Response<List<Map<String, Object>>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            PregledActivity.this,
                            R.string.napaka_nalaganje_terminov,
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                if (adapter == null) {
                    adapter = new TerminiAdapter(response.body(), termin -> {
                        new AlertDialog.Builder(PregledActivity.this)
                                .setTitle(R.string.preklic_termina)
                                .setMessage(R.string.preklic_termina_potrditev)
                                .setPositiveButton(R.string.da, (d, w) -> preklicTermin(termin))
                                .setNegativeButton(R.string.ne, null)
                                .show();
                    });
                    rv.setAdapter(adapter);
                } else {
                    adapter.updateData(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                ErrorHandler.showToastError(PregledActivity.this, null, t, null);
            }
        });
    }
}

