package com.mrtoad.jianting.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Interface.ILikedMusicInterface.OnClickListener;
import com.mrtoad.jianting.Interface.ILikedMusicInterface.OnLongClickListener;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.ToastUtils;

import java.util.List;

public class ILikedMusicAdapter extends RecyclerView.Adapter<ILikedMusicAdapter.ViewHolder> {

    private Context context;
    private List<ILikedMusicEntity> ILIkedMusicList;
    private LayoutInflater inflater;

    private OnClickListener onClickListener;
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private OnLongClickListener onLongClickListener;
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public ILikedMusicAdapter(Context context, List<ILikedMusicEntity> ILIkedMusicList) {
        this.context = context;
        this.ILIkedMusicList = ILIkedMusicList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ILikedMusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.i_liked_music_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ILikedMusicAdapter.ViewHolder holder, int position) {
        ILikedMusicEntity iLikedMusicEntity = ILIkedMusicList.get(position);

        // 设置基本信息
        GlobalMethodsUtils.setMusicCover(context , holder.musicCover , iLikedMusicEntity.getMusicCover());

        holder.musicName.setText(iLikedMusicEntity.getMusicName());
        holder.musicAuthor.setText(iLikedMusicEntity.getMusicAuthor());

        // 设置音乐区域点击事件
        holder.musicArea.setOnClickListener((v) -> {
            onClickListener.onClick(iLikedMusicEntity);
        });

        // 设置音乐区域长按事件
        holder.musicArea.setOnLongClickListener((v) -> {
            onLongClickListener.onLongClick(iLikedMusicEntity);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return ILIkedMusicList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView musicCover;
        private TextView musicName;
        private TextView musicAuthor;
        private LinearLayout musicArea;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            musicCover = itemView.findViewById(R.id.music_cover);
            musicName = itemView.findViewById(R.id.music_name);
            musicAuthor = itemView.findViewById(R.id.music_author);
            musicArea = itemView.findViewById(R.id.music_area);
        }
    }
}
