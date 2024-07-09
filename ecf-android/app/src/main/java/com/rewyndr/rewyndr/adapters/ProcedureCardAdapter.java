package com.rewyndr.rewyndr.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.interfaces.IProcedureCoordinator;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.view.SquareImageView;

import java.util.ArrayList;

import static android.view.View.GONE;

public class ProcedureCardAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Activity parentActivity;
    private IProcedureCoordinator coordinator;
    private ArrayList<Procedure> procedures;

    public ProcedureCardAdapter(Activity a, IProcedureCoordinator coordinator, ArrayList<Procedure> procedures){
        this.parentActivity = a;
        this.procedures = procedures != null ? procedures : new ArrayList<>();
        this.coordinator = coordinator;
        this.mInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return procedures.size();
    }

    @Override
    public Object getItem(int item) {
        return item;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    private class ProcedureCardViewHolder {
        public LinearLayout container;
        LinearLayout header;
        public TextView name;
        public TextView status;
        public SquareImageView image;
        public int position;
        LinearLayout handle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_procedure, parentView, false);
            final ProcedureCardViewHolder holder = new ProcedureCardViewHolder();

            holder.name = convertView.findViewById(R.id.card_procedure_name);
            holder.status = convertView.findViewById(R.id.card_procedure_status);
            holder.container = convertView.findViewById(R.id.card_procedure_container);
            holder.header = convertView.findViewById(R.id.card_procedure_header);
            holder.image = convertView.findViewById(R.id.card_procedure_image);
            holder.handle = convertView.findViewById(R.id.card_procedure_handle);

            convertView.setTag(holder);
        }

        final ProcedureCardViewHolder holder = (ProcedureCardViewHolder) convertView.getTag();
        final Procedure procedure = procedures.get(position);

        holder.position = position;
        holder.name.setText(procedure.getName());
        holder.status.setText(procedure.getStatus().toUpperCase());
        holder.handle.setVisibility(GONE);

        if(procedure.isPublished()) {
            holder.status.setTextColor(ContextCompat.getColor(parentActivity, R.color.white));
            holder.status.setBackgroundResource(R.color.brandBackground);
        }

        TextDrawable td = TextDrawable.builder().buildRect(procedure.getName().substring(0,1), R.color.brandLightBackground);
        Glide.with(parentActivity).load(procedure.getImageUrl())
                .placeholder(td).into(holder.image);

        holder.container.setOnClickListener(v -> coordinator.navigateToProcedure(procedure));

        return convertView;
    }
}