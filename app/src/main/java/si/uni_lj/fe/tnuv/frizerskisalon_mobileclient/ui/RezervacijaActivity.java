package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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

public class RezervacijaActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private int frizerId;
    private String dan;
    private String ura;
    private ArrayList<Integer> storitveIds;
    private String opombe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JWTManager.preveriJWT(this, () -> {

            setContentView(R.layout.activity_rezervacija);
            TextView tvFrizer = findViewById(R.id.tvFrizer);
            TextView tvZacetek = findViewById(R.id.tvZacetek);
            TextView tvKonec = findViewById(R.id.tvKonec);
            TextView tvStoritve = findViewById(R.id.tvStoritve);
            TextView tvSkTrajanje = findViewById(R.id.tvSkTrajanje);
            TextView tvSkCena = findViewById(R.id.tvSkCena);
            TextView tvOpombe = findViewById(R.id.tvOpombe);

            Button btnPotrdi = findViewById(R.id.btnPotrdi);

            Button btnPreklici = findViewById(R.id.btnPreklici);
            btnPreklici.setOnClickListener(v -> finish());

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = prefs.getString(PREFS_TOKEN_KEY, null);

            Retrofit retrofit = ApiClient.getClient(token);

            // ===== podatki od response POST /rezervacija =====
            String frizerIme = getIntent().getStringExtra("frizer");
            String zacetek = getIntent().getStringExtra("zacetek");
            String konec = getIntent().getStringExtra("konec");
            int skTrajanje = getIntent().getIntExtra("skupno_trajanje", 0);
            double skCena = getIntent().getDoubleExtra("skupna_cena", 0.0);
            opombe = getIntent().getStringExtra("opombe");

            ArrayList<Map<String, Object>> storitve =
                    (ArrayList<Map<String, Object>>) getIntent().getSerializableExtra("storitve");

            frizerId = getIntent().getIntExtra("frizer_ID", -1);
            dan = getIntent().getStringExtra("dan");
            ura = getIntent().getStringExtra("ura");
            storitveIds =
                    (ArrayList<Integer>) getIntent().getSerializableExtra("storitveIds");

            // Formatiranje storitev
            StringBuilder storitveText = new StringBuilder();

            for (Map<String, Object> s : storitve) {
                String naziv = (String) s.get("naziv");
                int trajanje = (int) Double.parseDouble(s.get("trajanje").toString());
                double cena = Double.parseDouble(s.get("cena").toString());

                storitveText.append(getString(R.string.storitev_format, naziv, trajanje, cena));
            }

            // Odstrani zadnji \n
            if (storitveText.length() > 0) {
                storitveText.setLength(storitveText.length() - 1);
            }

            // ===== izpis =====
            tvFrizer.setText(getString(
                    R.string.label_izbran_frizer, frizerIme
            ));
            tvStoritve.setText(getString(
                    R.string.label_izbrane_storitve_tri, storitveText.toString()
            ));
            tvZacetek.setText(getString(
                    R.string.label_zacetek, zacetek
            ));
            tvKonec.setText(getString(
                    R.string.label_konec, konec
            ));

            tvSkTrajanje.setText(getString(
                    R.string.label_trajanje, skTrajanje
            ));
            tvSkCena.setText(getString(
                    R.string.label_cena, skCena
            ));

            tvOpombe.setText(
                    opombe != null
                            ? getString(R.string.label_opombe, opombe)
                            : getString(R.string.opombe_prazno)
            );

            btnPotrdi.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.potrditev_rezervacije)
                        .setMessage(R.string.potrditev_rezervacije_potrditev)
                        .setPositiveButton(R.string.da, (dialog, which) -> {

                            posljiRezervacijo(retrofit);
                        })
                        .setNegativeButton(R.string.ne, null)
                        .show();
            });

        });
    }

    public interface RezervacijaApi {
        @POST("termini/rezervacija")
        Call<Map<String, Object>> ustvariRezervacijo(@Body Map<String, Object> body);
    }

    /* =======================
    Pomožna metoda za rezervacijo storitve
    ======================= */
    private void posljiRezervacijo(Retrofit retrofit) {

        Map<String, Object> body = new HashMap<>();
        body.put("frizer_ID", frizerId);
        body.put("dan", dan);
        body.put("ura", ura);
        body.put("storitve", storitveIds);
        body.put("opombe", opombe);

        RezervacijaApi api = retrofit.create(RezervacijaApi.class);

        api.ustvariRezervacijo(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (!response.isSuccessful()) {
                    ErrorHandler.showToastError(RezervacijaActivity.this, response, null, getString(R.string.napaka_rezervacija_termina));
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(RezervacijaActivity.this, R.string.napaka_prazen_odgovor_streznika, Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, Object> json = response.body();
                String message = json.get("message").toString();

                // SUCCESS popup
                new androidx.appcompat.app.AlertDialog.Builder(RezervacijaActivity.this)
                        .setTitle(R.string.uspeh)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, (d, w) -> {
                            Intent intent = new Intent(
                                    RezervacijaActivity.this,
                                    MainActivity.class
                            );
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_NEW_TASK
                            );
                            startActivity(intent);
                            finish();
                        })
                        .show();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                ErrorHandler.showToastError(RezervacijaActivity.this, null, t, null);
            }
        });
    }
}
