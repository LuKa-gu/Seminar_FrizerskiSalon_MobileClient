package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;

public class StoritveAdapter extends RecyclerView.Adapter<StoritveAdapter.ViewHolder> {

    private final List<Map<String, Object>> storitveList;
    private final boolean[] selected;
    private final Context context;
    private final OnPlusClickListener plusClickListener;

    public interface OnPlusClickListener {
        void onPlusClick(String url);
    }

    public StoritveAdapter(Context context, List<Map<String, Object>> storitveList, boolean[] selected,
                           OnPlusClickListener listener) {
        this.context = context;
        this.storitveList = storitveList;
        this.selected = selected;
        this.plusClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_storitev, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> storitev = storitveList.get(position);
        holder.checkBox.setText((String) storitev.get("naziv"));
        holder.checkBox.setChecked(selected[position]);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> selected[position] = isChecked);

        holder.btnPlus.setOnClickListener(v -> {
            if (plusClickListener != null) {
                plusClickListener.onPlusClick((String) storitev.get("url"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return storitveList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        MaterialButton btnPlus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbStoritve);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}

