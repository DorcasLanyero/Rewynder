package com.rewyndr.rewyndr.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.activity.StepActivity;
import com.rewyndr.rewyndr.model.Step;
import com.rewyndr.rewyndr.utility.ToastUtility;
import com.rewyndr.rewyndr.view.SquareImageView;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.List;

public class StepCardAdapter extends DragItemAdapter<Step, StepCardAdapter.ViewHolder> {
    private Activity parentActivity;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public StepCardAdapter(Activity a, List<Step> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        this.parentActivity = a;
        this.mLayoutId = layoutId;
        this.mGrabHandleId = grabHandleId;
        this.mDragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

    public long getUniqueItemId(int position) {
        return this.mItemList.get(position).getId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final Step step = mItemList.get(position);
        holder.name.setText(String.format(parentActivity.getString(R.string.procedure_step_viewholder_text), position + 1,  step.getName()));
        holder.description.setText(step.getDescription());

        TextDrawable td = TextDrawable.builder().buildRect(step.getName().substring(0,1), R.color.brandLightBackground);
        Glide.with(parentActivity).load(step.getImageThumbnailUrl())
                .placeholder(td).into(holder.image);

        holder.itemView.setTag(mItemList.get(position));
        holder.container.setOnClickListener(view -> navigateToActivity(StepActivity.class, step));
    }

    private void navigateToActivity(Class activityClass, Step selectedStep) {
        Intent intent = new Intent(parentActivity, activityClass);
        intent.putExtra("executionId", selectedStep.getId());
        parentActivity.startActivity(intent);
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        private TextView name;
        private TextView description;
        private SquareImageView image;
        private LinearLayout container;

        ViewHolder(final View stepView) {
            super(stepView, mGrabHandleId, mDragOnLongPress);
            this.name = stepView.findViewById(R.id.card_step_name);
            this.description = stepView.findViewById(R.id.card_step_description);
            this.image = stepView.findViewById(R.id.card_step_image);
            this.container = stepView.findViewById(R.id.card_step_container);
        }

        @Override
        public void onItemClicked(View view) {
        }

        @Override
        public boolean onItemLongClicked(View view) {
            ToastUtility.popShort(view.getContext(), "Item long clicked");
            return true;
        }
    }
}