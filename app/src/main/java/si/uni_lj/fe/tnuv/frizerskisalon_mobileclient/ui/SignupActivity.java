package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;

public class SignupActivity extends AppCompatActivity {

    private EditText etIme, etPriimek, etNaslov, etStarost, etMail, etTelefon, etUporabnisko, etGeslo;
    private Spinner spSpol;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // povezava UI elementov
        etIme = findViewById(R.id.etIme);
        etPriimek = findViewById(R.id.etPriimek);
        etNaslov = findViewById(R.id.etNaslov);
        etStarost = findViewById(R.id.etStarost);
        etMail = findViewById(R.id.etMail);
        etTelefon = findViewById(R.id.etTelefon);
        etUporabnisko = findViewById(R.id.etUporabnisko);
        etGeslo = findViewById(R.id.etGeslo);
        spSpol = findViewById(R.id.spSpol);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> signup());
    }

    private void signup() {
        String spol = spSpol.getSelectedItem().toString();
        String ime = etIme.getText().toString().trim();
        String priimek = etPriimek.getText().toString().trim();
        String naslov = etNaslov.getText().toString().trim();
        String starost = etStarost.getText().toString().trim();
        String mail = etMail.getText().toString().trim();
        String telefon = etTelefon.getText().toString().trim();
        String uporabnisko = etUporabnisko.getText().toString().trim();
        String geslo = etGeslo.getText().toString().trim();

        // minimalna validacija
        if (ime.isEmpty() || priimek.isEmpty() || naslov.isEmpty() || starost.isEmpty() ||
                mail.isEmpty() || telefon.isEmpty() || uporabnisko.isEmpty() || geslo.isEmpty()) {
            Toast.makeText(this, "Vsa polja so obvezna.", Toast.LENGTH_SHORT).show();
            return;
        }

        // priprava body-ja kot Map
        Map<String, String> body = new HashMap<>();
        body.put("Spol", spol);
        body.put("Ime", ime);
        body.put("Priimek", priimek);
        body.put("Naslov", naslov);
        body.put("Starost", starost);
        body.put("Mail", mail);
        body.put("Telefon", telefon);
        body.put("Uporabnisko_ime", uporabnisko);
        body.put("Geslo", geslo);

        // Retrofit + ApiClient
        Retrofit retrofit = ApiClient.getClient(null);
        SignupApi api = retrofit.create(SignupApi.class);

        Call<Map<String, String>> call = api.signup(body);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignupActivity.this, response.body().get("message") + "Zdaj se lahko prijavite.", Toast.LENGTH_SHORT).show();
                    // po uspešni registraciji preusmeri na LoginActivity
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Napaka pri registraciji.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Napaka: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // inline interface za signup endpoint
    interface SignupApi {
        @POST("uporabniki/signup")
        Call<Map<String, String>> signup(@Body Map<String, String> body);
    }
}
