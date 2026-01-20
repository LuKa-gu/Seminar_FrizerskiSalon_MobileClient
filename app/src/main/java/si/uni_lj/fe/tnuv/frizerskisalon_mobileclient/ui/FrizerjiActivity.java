package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
                                "Napaka pri nalaganju frizerjev",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    Toast.makeText(
                            FrizerjiActivity.this,
                            "Napaka: " + t.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });
    }

    interface FrizerjiInfoApi {
        @GET("frizerji/info")
        Call<List<Map<String, Object>>> getFrizerjiInfo();
    }

    // ===== Inner Adapter =====
    private static class FrizerAdapter extends RecyclerView.Adapter<FrizerAdapter.ViewHolder> {

        private List<Map<String, Object>> frizerji;

        public FrizerAdapter(List<Map<String, Object>> frizerji) {
            this.frizerji = frizerji;
        }

        public void updateData(List<Map<String, Object>> novi) {
            this.frizerji = novi;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_frizer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> f = frizerji.get(position);

            holder.tvImePriimek.setText(f.get("Ime") + " " + f.get("Priimek"));
            holder.tvStarostMail.setText("Starost: " + f.get("Starost") + " | Mail: " + f.get("Mail"));
            holder.tvTelefonOpis.setText("Telefon: " + f.get("Telefon") + " | Opis: " + f.get("Opis"));

            // specializacije
            StringBuilder spec = new StringBuilder("Specializacije: ");
            List<Map<String, Object>> specializacije = (List<Map<String, Object>>) f.get("specializacije");
            if (specializacije != null && !specializacije.isEmpty()) {
                for (Map<String, Object> s : specializacije) {
                    spec.append(s.get("naziv")).append(", ");
                }
                spec.setLength(spec.length() - 2); // odstrani zadnji ", "
            } else {
                spec.append("nobena");
            }
            holder.tvSpecializacije.setText(spec.toString());

            // delovniki
            StringBuilder del = new StringBuilder("Delovniki: ");
            List<Map<String, Object>> delovniki = (List<Map<String, Object>>) f.get("delovniki");
            if (delovniki != null && !delovniki.isEmpty()) {
                for (Map<String, Object> d : delovniki) {
                    del.append(d.get("dan")).append(" ").append(d.get("zacetek"))
                            .append("-").append(d.get("konec")).append("; ");
                }
                del.setLength(del.length() - 2);
            } else {
                del.append("noben");
            }
            holder.tvDelovniki.setText(del.toString());
        }

        @Override
        public int getItemCount() {
            return frizerji.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvImePriimek, tvStarostMail, tvTelefonOpis, tvSpecializacije, tvDelovniki;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvImePriimek = itemView.findViewById(R.id.tvImePriimek);
                tvStarostMail = itemView.findViewById(R.id.tvStarostMail);
                tvTelefonOpis = itemView.findViewById(R.id.tvTelefonOpis);
                tvSpecializacije = itemView.findViewById(R.id.tvSpecializacije);
                tvDelovniki = itemView.findViewById(R.id.tvDelovniki);
            }
        }
    }
}
