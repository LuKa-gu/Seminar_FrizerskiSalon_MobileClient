package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Preveri JWT preko helperja
        JWTManager.preveriJWT(this, () -> {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            finish();
        });
        //Če token ni veljaven, JWTManager sam pošlje uporabnika na LoginActivity
    }
}
