package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;

public class TerminiAdapter
        extends RecyclerView.Adapter<TerminiAdapter.ViewHolder> {

    public interface OnPreklicClickListener {
        void onPreklic(Map<String, Object> termin);
    }

    private final List<Map<String, Object>> termini;
    private final OnPreklicClickListener listener;

    public TerminiAdapter(
            List<Map<String, Object>> termini,
            OnPreklicClickListener listener
    ) {
        this.termini = termini;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_termin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int position) {

        Map<String, Object> t = termini.get(position);

        h.tvFrizer.setText("Frizer: " + t.get("frizer"));

        // storitve
        StringBuilder sb = new StringBuilder("Storitve:\n");
        List<Map<String, Object>> storitve =
                (List<Map<String, Object>>) t.get("storitve");

        for (Map<String, Object> s : storitve) {
            sb.append("- ")
                    .append(s.get("naziv"))
                    .append(" (")
                    .append(((Number)s.get("trajanje")).intValue())
                    .append(" min, ")
                    .append(((Number)s.get("cena")).doubleValue())
                    .append(" €)\n");
        }

        h.tvStoritve.setText(sb.toString());

        h.tvZacetek.setText(
                "Začetek termina: " +
                        t.get("zacetek_termina")
        );

        h.tvKonec.setText(
                "Konec termina: " +
                        t.get("konec_termina")
        );

        h.tvTrajanje.setText(
                "Trajanje storitev: " +
                        ((Number)t.get("skupno_trajanje")).doubleValue() + " min"
        );

        h.tvCena.setText(
                String.format(
                    "Cena storitev: %.2f €",
                        ((Number)t.get("skupna_cena")).doubleValue()
                )
        );

        h.tvOpombe.setText("Opombe: " + t.get("opombe"));

        h.tvStatus.setText("Status: " + t.get("status"));

        if (!"Rezervirano".equals(t.get("status"))) {
            h.btnPreklic.setVisibility(View.GONE);
        }

        h.btnPreklic.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPreklic(t);
            }
        });

    }

    public void updateData(List<Map<String, Object>> novi) {
        termini.clear();
        termini.addAll(novi);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return termini.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrizer, tvStoritve,tvZacetek, tvKonec, tvTrajanje, tvCena, tvOpombe, tvStatus;
        Button btnPreklic;

        ViewHolder(View v) {
            super(v);
            tvFrizer = v.findViewById(R.id.tvFrizer);
            tvStoritve = v.findViewById(R.id.tvStoritve);
            tvZacetek = v.findViewById(R.id.tvZacetek);
            tvKonec = v.findViewById(R.id.tvKonec);
            tvTrajanje = v.findViewById(R.id.tvTrajanje);
            tvCena = v.findViewById(R.id.tvCena);
            tvOpombe = v.findViewById(R.id.tvOpombe);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnPreklic = v.findViewById(R.id.btnPreklic);
        }
    }
}

