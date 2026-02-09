package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;

public class PredogledActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private String izbranaUra = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JWTManager.preveriJWT(this, () -> {

            setContentView(R.layout.activity_predogled);

            TextView tvFrizer = findViewById(R.id.tvFrizer);
            TextView tvDan = findViewById(R.id.tvDan);
            TextView tvStoritve = findViewById(R.id.tvStoritve);
            TextView tvTrajanje = findViewById(R.id.tvTrajanje);
            TextView tvBloki = findViewById(R.id.tvBloki);

            EditText etUra = findViewById(R.id.etUra);
            EditText etOpombe = findViewById(R.id.etOpombe);
            Button btnPotrdi = findViewById(R.id.btnPotrdi);

            Button btnNazaj = findViewById(R.id.btnNazaj);
            btnNazaj.setOnClickListener(v -> finish());

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = prefs.getString(PREFS_TOKEN_KEY, null);

            Retrofit retrofit = ApiClient.getClient(token);

            // ===== podatki iz MainActivity =====
            ArrayList<String> storitveNazivi =
                    getIntent().getStringArrayListExtra("storitveNazivi");
            String frizerIme = getIntent().getStringExtra("frizerIme");
            String dan = getIntent().getStringExtra("dan");

            // ===== podatki od POST /razpolozljivost =====
            int trajanje = getIntent().getIntExtra("trajanje", 0);
            ArrayList<String> bloki =
                    getIntent().getStringArrayListExtra("razpolozljivi_bloki");

            List<String> formatirane = new ArrayList<>();
            for (String naziv : storitveNazivi) {
                formatirane.add(getString(R.string.item_bullet, naziv));
            }

            // ===== izpis =====
            tvStoritve.setText(getString(
                    R.string.label_izbrane_storitve_tri,
                    TextUtils.join("\n", formatirane)
            ));
            tvFrizer.setText(getString(
                    R.string.label_izbran_frizer, frizerIme
            ));
            tvDan.setText(getString(
                    R.string.label_izbran_dan, dan
            ));

            tvTrajanje.setText(getString(
                    R.string.label_trajanje, trajanje
            ));
            if (bloki == null || bloki.isEmpty()) {
                tvBloki.setText(R.string.ni_blokov);
            } else {
                tvBloki.setText(getString(
                        R.string.mozni_zacetki_termina,
                        String.join("\n", bloki)
                ));
            }

            // ===== TimePicker =====
            etUra.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();

                TimePickerDialog dialog = new TimePickerDialog(
                        PredogledActivity.this,
                        (view, hourOfDay, minute) -> {
                            String ura = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    hourOfDay,
                                    minute
                            );
                            // nastavimo EditText
                            etUra.setText(ura);
                            // nastavimo izbranaUra, da lahko gremo naprej
                            izbranaUra = ura;
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                dialog.show();
            });

            // ===== Potrdi =====
            btnPotrdi.setOnClickListener(v -> {
                if (izbranaUra == null) {
                    Toast.makeText(this,
                            R.string.izberi_uro,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int frizerId = getIntent().getIntExtra("frizerId", -1);
                ArrayList<Integer> storitveIds =
                        getIntent().getIntegerArrayListExtra("storitveIds");

                String opombe = etOpombe.getText().toString().trim();

                Map<String, Object> body = new HashMap<>();
                body.put("frizer_ID", frizerId);
                body.put("dan", dan);
                body.put("ura", izbranaUra);
                body.put("storitve", storitveIds);
                body.put("opombe", opombe.isEmpty() ? null : opombe);

                PredogledApi api = retrofit.create(PredogledApi.class);

                api.posljiPredogled(body).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {

                        if (!response.isSuccessful()) {
                            ErrorHandler.showToastError(PredogledActivity.this, response, null, getString(R.string.napaka_priprava_predogleda));
                            return;
                        }

                        if (response.body() == null) {
                            Toast.makeText(PredogledActivity.this, R.string.napaka_prazen_odgovor_streznika, Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Object> json = response.body();

                        // ➜ prehod v RezervacijaActivity
                        Intent intent = new Intent(
                                PredogledActivity.this,
                                RezervacijaActivity.class
                        );

                        intent.putExtra("frizer", (String) json.get("frizer"));
                        intent.putExtra("zacetek", (String)
                                json.get("zacetek_termina"));
                        intent.putExtra("konec", (String)
                                json.get("konec_termina"));

                        intent.putExtra("skupno_trajanje", ((Number)
                                json.get("skupno_trajanje")).intValue());
                        intent.putExtra("skupna_cena", ((Number)
                                json.get("skupna_cena")).doubleValue());

                        // storitve kot ArrayList
                        ArrayList<Map<String, Object>> storitve =
                                (ArrayList<Map<String, Object>>) json.get("storitve");

                        intent.putExtra("storitve", storitve);

                        Object opombeObj = json.get("opombe");
                        intent.putExtra("opombe",
                                opombeObj != null ? (String) opombeObj : null
                        );

                        intent.putExtra("frizer_ID", frizerId);
                        intent.putExtra("dan", dan);
                        intent.putExtra("ura", izbranaUra);
                        intent.putIntegerArrayListExtra("storitveIds", storitveIds);

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        ErrorHandler.showToastError(PredogledActivity.this, null, t, null);
                    }
                });
            });
        });
    }
    interface PredogledApi {
        @POST("termini/predogled")
        Call<Map<String, Object>> posljiPredogled(
                @Body Map<String, Object> body
        );
    }
}

