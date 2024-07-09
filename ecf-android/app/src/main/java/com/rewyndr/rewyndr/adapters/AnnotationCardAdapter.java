package com.rewyndr.rewyndr.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rewyndr.rewyndr.R;
import com.rewyndr.rewyndr.fragments.EditDialogFragment;
import com.rewyndr.rewyndr.interfaces.IFragmentDetachListener;
import com.rewyndr.rewyndr.model.Annotation;
import com.rewyndr.rewyndr.model.Tag;
import com.rewyndr.rewyndr.model.User;

import java.util.List;

public class AnnotationCardAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;

    private List<Annotation> annotations;
    private Tag currentTag;
    private final User currentUser;

    public AnnotationCardAdapter(Context context, List<Annotation> annotations, User currentUser) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.currentUser = currentUser;
        this.annotations = annotations;
    }

    public AnnotationCardAdapter(Context context, User currentUser, List<Annotation> annotations) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.currentUser = currentUser;
        this.annotations = annotations;
    }

    public void setTag(Tag t){
        currentTag = t;
    }

    public void setAnnotations(List<Annotation> annotations){
        this.annotations = annotations;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(currentTag != null) {
            return currentTag.getAnnotations().size();
        }

        return annotations.size();
    }

    @Override
    public Object getItem(int item) {
        List<Annotation> annotations = currentTag == null ? this.annotations : currentTag.getAnnotations();
        return annotations.get(item);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    private static class AnnotationCardViewHolder {
        public LinearLayout container;
        public TextView content;
        public TextView meta;
        public PlayerView player;
        public ImageView menuImage;
        public int position;

        public AnnotationCardViewHolder(View view) {
            this.container = view.findViewById(R.id.card_annotation_container);
            this.content = view.findViewById(R.id.card_annotation_content);
            this.meta = view.findViewById(R.id.card_annotation_meta);
            this.menuImage = view.findViewById(R.id.more_menu);
            this.player = view.findViewById(R.id.card_annotation_player);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        AnnotationCardViewHolder vh;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_annotation, parentView, false);
            vh = new AnnotationCardViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (AnnotationCardViewHolder) convertView.getTag();
        }

        ExoPlayer ep = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        ep.setRepeatMode(Player.REPEAT_MODE_OFF);

        Annotation annotation = (Annotation)getItem(position);

        if(currentUser.getFullName().equals(annotation.getAuthor().getFullName())){
            final int id = annotation.getId();
            vh.menuImage.setVisibility(View.VISIBLE);
            vh.menuImage.setOnClickListener(v -> {
                FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();
                EditDialogFragment frag = EditDialogFragment.newInstance(id);
                if(mContext instanceof IFragmentDetachListener){
                    frag.setListener((IFragmentDetachListener)mContext);
                }
                frag.show(fm, "EditDialogFragment");
            });
        }

        // Prepare audio player
        if(annotation.hasAudio()) {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "rewyndr"));
            MediaSource ms = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(annotation.getAttachmentUrl()));
            ep.prepare(ms);
            vh.player.setPlayer(ep);
            vh.player.setVisibility(View.VISIBLE);
        } else {
            if(vh.player.getPlayer() != null) {
                vh.player.getPlayer().release();
            }
            vh.player.setVisibility(View.GONE);
        }

        vh.position = position;
        vh.content.setText(annotation.getContent());
        vh.meta.setText(String.format("%s on %s", annotation.getAuthor().getFullName(), annotation.getFormattedCreatedAt()));

        return convertView;
    }
}