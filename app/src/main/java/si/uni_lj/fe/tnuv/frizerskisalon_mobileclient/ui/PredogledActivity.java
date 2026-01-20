package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Retrofit;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;

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

            // ===== podatki od response POST /razpolozljivost =====
            int trajanje = getIntent().getIntExtra("trajanje", 0);
            ArrayList<String> bloki =
                    getIntent().getStringArrayListExtra("razpolozljivi_bloki");

            // ===== izpis =====
            tvStoritve.setText("Izbrane storitve:\n- " +
                    String.join("\n- ", storitveNazivi));
            tvFrizer.setText("Izbran frizer: " + frizerIme);
            tvDan.setText("Izbran dan: " + dan);

            tvTrajanje.setText("Trajanje storitev: " + trajanje + " min");
            if (bloki == null || bloki.isEmpty()) {
                tvBloki.setText("Ni razpoložljivih blokov");
            } else {
                tvBloki.setText("Možni začetki termina:\n" +
                        String.join("\n", bloki));
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
                            "Izberi uro",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String opombe = etOpombe.getText().toString().trim();

                // TUKAJ BO kasneje POST /predogled
                Toast.makeText(this,
                        "Predogled pripravljen",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }
}

