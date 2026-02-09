package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;

public class FrizerAdapter extends RecyclerView.Adapter<FrizerAdapter.ViewHolder> {

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

        holder.tvImePriimek.setText(holder.itemView.getContext().getString(
                R.string.ime_priimek, f.get("Ime"), f.get("Priimek")
        ));
        Number starost = (Number) f.get("Starost");
        int starostValue = starost != null ? starost.intValue() : 0;
        holder.tvStarost.setText(holder.itemView.getContext().getString(
                R.string.label_starost, starostValue
        ));
        holder.tvMail.setText(holder.itemView.getContext().getString(
                R.string.label_mail, f.get("Mail")
        ));
        holder.tvTelefon.setText(holder.itemView.getContext().getString(
                R.string.label_telefon, f.get("Telefon")
        ));
        holder.tvOpis.setText(holder.itemView.getContext().getString(
                R.string.label_opis, f.get("Opis")
        ));

        // specializacije
        String specializacijeText;
        List<Map<String, Object>> specializacije = (List<Map<String, Object>>) f.get("specializacije");
        if (specializacije != null && !specializacije.isEmpty()) {
            StringBuilder temp = new StringBuilder();
            for (Map<String, Object> s : specializacije) {
                temp.append(s.get("naziv")).append(", ");
            }
            temp.setLength(temp.length() - 2); // odstrani zadnji ", "
            specializacijeText = temp.toString();
        } else {
            specializacijeText = holder.itemView.getContext()
                    .getString(R.string.ni_specializacij);
        }
        holder.tvSpecializacije.setText(holder.itemView.getContext().getString(
                R.string.label_specializacije, specializacijeText
        ));

        // delovniki
        StringBuilder del = new StringBuilder();
        del.append(holder.itemView.getContext().getString(R.string.label_delovniki));
        List<Map<String, Object>> delovniki = (List<Map<String, Object>>) f.get("delovniki");
        if (delovniki != null && !delovniki.isEmpty()) {
            for (Map<String, Object> d : delovniki) {
                del.append(holder.itemView.getContext().getString(
                        R.string.delovnik_format,
                        d.get("dan"),
                        d.get("zacetek"),
                        d.get("konec"))
                );
            }
        } else {
            del.append(holder.itemView.getContext().getString(R.string.ni_delovnika));
        }
        holder.tvDelovniki.setText(del.toString());
    }

    @Override
    public int getItemCount() {
        return frizerji.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvImePriimek, tvStarost, tvMail, tvTelefon, tvOpis, tvSpecializacije, tvDelovniki;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImePriimek = itemView.findViewById(R.id.tvImePriimek);
            tvStarost = itemView.findViewById(R.id.tvStarost);
            tvMail = itemView.findViewById(R.id.tvMail);
            tvTelefon = itemView.findViewById(R.id.tvTelefon);
            tvOpis = itemView.findViewById(R.id.tvOpis);
            tvSpecializacije = itemView.findViewById(R.id.tvSpecializacije);
            tvDelovniki = itemView.findViewById(R.id.tvDelovniki);
        }
    }
}
