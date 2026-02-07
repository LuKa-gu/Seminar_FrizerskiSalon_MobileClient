package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import retrofit2.http.GET;
import retrofit2.http.POST;

import retrofit2.http.Url;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.StoritveAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private List<Map<String, Object>> frizerjiList = new ArrayList<>();
    private List<Map<String, Object>> storitveList = new ArrayList<>();
    private boolean[] selectedStoritve; // za multiple-choice dialog
    private ArrayList<Integer> selectedStoritveIds = new ArrayList<>();
    private ArrayList<String> selectedStoritveNazivi = new ArrayList<>();
    private String[] storitveNazivi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JWTManager.preveriJWT(this, () -> {

            setContentView(R.layout.activity_main);

            Spinner spinnerFrizerji = findViewById(R.id.spinnerFrizerji);
            Button btnIzberiStoritve = findViewById(R.id.btnIzberiStoritve);
            Button btnPreveri = findViewById(R.id.btnPreveri);

            // Date picker za izbiro datuma
            EditText etDatum = findViewById(R.id.etDatum);

            etDatum.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();

                DatePickerDialog dpd = new DatePickerDialog(
                        MainActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            String datum = String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    dayOfMonth
                            );
                            etDatum.setText(datum);
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show();
            });

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = prefs.getString(PREFS_TOKEN_KEY, null);

            Retrofit retrofit = ApiClient.getClient(token);

            TextView tvPreglejFrizerje = findViewById(R.id.tvPreglejFrizerje);
            tvPreglejFrizerje.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, FrizerjiActivity.class));
            });

            Button btnPregledTerminov = findViewById(R.id.btnPregledTerminov);
            btnPregledTerminov.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        PregledActivity.class
                );
                startActivity(intent);
            });

            /* =======================
               GET frizerji
               ======================= */
            FrizerjiApi frizerjiApi = retrofit.create(FrizerjiApi.class);

            frizerjiApi.getFrizerji().enqueue(new Callback<List<Map<String, Object>>>() {

                @Override
                public void onResponse(
                        Call<List<Map<String, Object>>> call,
                        Response<List<Map<String, Object>>> response
                ) {
                    if (response.isSuccessful() && response.body() != null) {

                        frizerjiList = response.body();

                        // imena za Spinner
                        List<String> imena = new ArrayList<>();

                        for (Map<String, Object> f : frizerjiList) {
                            imena.add((String) f.get("osebno_ime"));
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        MainActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        imena
                                );

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spinnerFrizerji.setAdapter(adapter);
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Napaka pri pridobivanju frizerjev.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    ErrorHandler.showToastError(MainActivity.this, null, t, null);
                }
            });

            /* =======================
               GET storitve
               ======================= */
            StoritveApi storitveApi = retrofit.create(StoritveApi.class);
            storitveApi.getStoritve().enqueue(new Callback<List<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        storitveList = response.body();
                        storitveNazivi = new String[storitveList.size()];
                        selectedStoritve = new boolean[storitveList.size()];
                        for (int i = 0; i < storitveList.size(); i++) {
                            storitveNazivi[i] = (String) storitveList.get(i).get("naziv");
                            selectedStoritve[i] = false;
                        }
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Napaka pri pridobivanju storitev.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    ErrorHandler.showToastError(MainActivity.this, null, t, null);
                }
            });

            /* ============================
            Multi-choice dialog za storitve
               ============================ */
            btnIzberiStoritve.setOnClickListener(v -> {
                if (storitveList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Storitve še niso naložene.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Dialog layout
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_storitve, null);
                RecyclerView rvStoritve = dialogView.findViewById(R.id.rvStoritve);

                StoritveAdapter adapter = new StoritveAdapter(MainActivity.this, storitveList, selectedStoritve,
                        url -> openStoritevDetails(url));

                rvStoritve.setAdapter(adapter);
                rvStoritve.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Izberi storitve")
                        .setView(dialogView)
                        .setPositiveButton("OK", (dialog, which) -> {
                            selectedStoritveIds.clear();
                            selectedStoritveNazivi.clear();

                            TextView tvIzbraneStoritve = findViewById(R.id.tvIzbraneStoritve);

                            for (int i = 0; i < selectedStoritve.length; i++) {
                                if (selectedStoritve[i]) {
                                    Number idNumber = (Number) storitveList.get(i).get("id");
                                    selectedStoritveIds.add(idNumber.intValue());

                                    String naziv = (String) storitveList.get(i).get("naziv");
                                    selectedStoritveNazivi.add(naziv);
                                }
                            }

                            if (!selectedStoritveNazivi.isEmpty()) {
                                String prikaz = String.join(", ", selectedStoritveNazivi);
                                tvIzbraneStoritve.setText("Izbrane storitve: " + prikaz);
                            } else {
                                tvIzbraneStoritve.setText("Izbrane storitve: /");
                            }

                        })
                        .setNegativeButton("Prekliči", null)
                        .show();
            });

            /* ===========================
               Klik gumb → POST razpolozljivost
               =========================== */
            btnPreveri.setOnClickListener(v -> {

                int selectedIndex = spinnerFrizerji.getSelectedItemPosition();
                if(selectedIndex < 0 || frizerjiList.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Izberi frizerja.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                int frizerId = ((Double) frizerjiList.get(selectedIndex).get("id")).intValue();
                String dan = etDatum.getText().toString().trim();

                if (dan.isEmpty() || selectedStoritveIds.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Izberi datum in vsaj eno storitev.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                /* =======================
                   JWT + Retrofit
                   ======================= */
                RazpolozljivostApi razpApi = retrofit.create(RazpolozljivostApi.class);

                /* =======================
                   Request body
                   ======================= */
                Map<String, Object> body = new HashMap<>();
                body.put("frizer_ID", frizerId);
                body.put("dan", dan);
                body.put("storitve", selectedStoritveIds);

                /* =======================
                   API klic
                   ======================= */
                razpApi.preveriRazpolozljivost(body).enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, Object>> call,
                            Response<Map<String, Object>> response
                    ) {
                        if (!response.isSuccessful()) {
                            ErrorHandler.showToastError(MainActivity.this, response, null, "Napaka pri izračunu razpoložljivosti.");
                            return;
                        }

                        if (response.body() == null) {
                            Toast.makeText(MainActivity.this, "Prazen odgovor strežnika.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Object> data = response.body();

                        // trajanje
                        int trajanje = ((Number) data.get("trajanje_num")).intValue();

                        // bloki
                        @SuppressWarnings("unchecked")

                        ArrayList<String> bloki = new ArrayList<>();
                        Object rawBloki = data.get("razpolozljivi_bloki");

                        if (rawBloki instanceof List<?>) {
                            for (Object o : (List<?>) rawBloki) {
                                if(o instanceof Map<?, ?>) {
                                    Map<?, ?> blok = (Map<?, ?>) o;

                                    String od = blok.get("od") != null ? blok.get("od").toString() : "?";
                                    String do_ = blok.get("do") != null ? blok.get("do").toString() : "?";

                                    bloki.add(od + " - " + do_);
                                }
                            }
                        }

                        // če ni razpoložljivih blokov
                        if (bloki == null || bloki.isEmpty()) {
                            String razlog;

                            if (data.containsKey("razlog") && data.get("razlog") != null) {
                                razlog = data.get("razlog").toString();
                            } else {
                                razlog = "Frizer ne dela ta dan.";
                            }

                            Toast.makeText(
                                    MainActivity.this,
                                    razlog,
                                    Toast.LENGTH_LONG
                            ).show();

                            return; // ne gremo naprej
                        }

                        // sicer gremo v PredogledActivity
                        Intent intent = new Intent(MainActivity.this, PredogledActivity.class);

                        intent.putExtra("frizerId", frizerId);
                        intent.putExtra("frizerIme", spinnerFrizerji.getSelectedItem().toString());
                        intent.putExtra("dan", dan);
                        intent.putExtra("trajanje", trajanje);

                        intent.putStringArrayListExtra(
                                "razpolozljivi_bloki",
                                bloki
                        );

                        intent.putIntegerArrayListExtra(
                                "storitveIds",
                                new ArrayList<>(selectedStoritveIds)
                        );

                        intent.putStringArrayListExtra(
                                "storitveNazivi",
                                new ArrayList<>(selectedStoritveNazivi)
                        );

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        ErrorHandler.showToastError(MainActivity.this, null, t, null);
                    }
                });
            });
        });
    }

    /* =======================
    Interfaces
    ======================= */
    interface FrizerjiApi {
        @GET("frizerji")
        Call<List<Map<String, Object>>> getFrizerji();
    }

    interface StoritveApi {
        @GET("storitve")
        Call<List<Map<String, Object>>> getStoritve();
    }

    interface StoritevDetailsApi {
        @GET
        Call<Map<String, Object>> getStoritev(@Url String url);
    }

    interface RazpolozljivostApi {
        @POST("termini/razpolozljivost")
        Call<Map<String, Object>> preveriRazpolozljivost(
                @Body Map<String, Object> body
        );
    }

    /* =======================
    Pomožna metoda za odpiranje detajlov storitve
    ======================= */
    private void openStoritevDetails(String url) {

        Retrofit retrofit = ApiClient.getClient(null);
        // če endpoint NI zaščiten z JWT → null
        // če JE zaščiten → uporabi token

        StoritevDetailsApi api = retrofit.create(StoritevDetailsApi.class);

        api.getStoritev(url).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response
            ) {
                if (!response.isSuccessful()) {
                    ErrorHandler.showToastError(MainActivity.this, response, null, "Napaka pri pridobivanju podrobnosti storitve.");
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(MainActivity.this, "Prazen odgovor strežnika.", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, Object> s = response.body();

                String ime = String.valueOf(s.get("Ime"));
                String opis = String.valueOf(s.get("Opis"));
                int trajanje = ((Number) s.get("Trajanje")).intValue();
                String cena = String.valueOf(s.get("Cena")); // ker je Cena decimal

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(ime)
                        .setMessage(
                                "Opis:\n" + opis +
                                        "\n\nTrajanje: " + trajanje + " min" +
                                        "\n\nCena: " + cena + " €"
                        )
                        .setPositiveButton("OK", null)
                        .show();

            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                ErrorHandler.showToastError(MainActivity.this, null, t, null);
            }
        });
    }
}
