package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.ApiClient;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api.JWTManager;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.FrizerAdapter;

public class FrizerjiActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";
    private RecyclerView recyclerView;
    private FrizerAdapter adapter;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JWTManager.preveriJWT(this, () -> {

            setContentView(R.layout.activity_frizerji);

            recyclerView = findViewById(R.id.recyclerViewFrizerji);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adapter = new FrizerAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> finish());

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = prefs.getString(PREFS_TOKEN_KEY, null);

            Retrofit retrofit = ApiClient.getClient(token);
            FrizerjiInfoApi api = retrofit.create(FrizerjiInfoApi.class);

            api.getFrizerjiInfo().enqueue(new Callback<List<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateData(response.body());
                    } else {
                        Toast.makeText(
                                FrizerjiActivity.this,
                                R.string.napaka_nalaganje_frizerjev,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    ErrorHandler.showToastError(FrizerjiActivity.this, null, t, null);
                }
            });
        });
    }

    interface FrizerjiInfoApi {
        @GET("frizerji/info")
        Call<List<Map<String, Object>>> getFrizerjiInfo();
    }
}
