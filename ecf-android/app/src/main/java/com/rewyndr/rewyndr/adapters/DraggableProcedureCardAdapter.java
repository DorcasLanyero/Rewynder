package com.rewyndr.rewyndr.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.interfaces.IProcedureCoordinator;
import com.rewyndr.rewyndr.model.Procedure;
import com.rewyndr.rewyndr.utility.StringUtilsKt;
import com.rewyndr.rewyndr.view.SquareImageView;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class DraggableProcedureCardAdapter extends DragItemAdapter<Procedure, DraggableProcedureCardAdapter.ProcedureCardViewHolder> {
    private Activity parentActivity;
    private IProcedureCoordinator coordinator;

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public DraggableProcedureCardAdapter(Activity a, IProcedureCoordinator coordinator, ArrayList<Procedure> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        this.parentActivity = a;
        this.mLayoutId = layoutId;
        this.mGrabHandleId = grabHandleId;
        this.mDragOnLongPress = dragOnLongPress;
        this.coordinator = coordinator;
        setItemList(list);
    }

    @Override
    public long getUniqueItemId(int position) { return this.mItemList.get(position).getId(); }

    class ProcedureCardViewHolder extends DragItemAdapter.ViewHolder {
        public LinearLayout container;
        public TextView name;
        TextView status;
        public SquareImageView image;

        ProcedureCardViewHolder(final View stepView){
            super(stepView, mGrabHandleId, mDragOnLongPress);
            this.name = stepView.findViewById(R.id.card_procedure_name);
            this.status = stepView.findViewById(R.id.card_procedure_status);
            this.image = stepView.findViewById(R.id.card_procedure_image);
            this.container = stepView.findViewById(R.id.card_procedure_container);
        }
    }

    @NonNull
    @Override
    public ProcedureCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ProcedureCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcedureCardViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final Procedure procedure = mItemList.get(position);
        holder.name.setText(procedure.getName());
        holder.status.setText(StringUtilsKt.capitalize(procedure.getStatus()));

        TextDrawable td = TextDrawable.builder().buildRect(procedure.getName().substring(0,1), R.color.brandLightBackground);
        Glide.with(parentActivity).load(procedure.getImageUrl())
                .placeholder(td).into(holder.image);

        holder.itemView.setTag(mItemList.get(position));
        holder.container.setOnClickListener(view -> coordinator.navigateToProcedure(procedure));
    }
}