package com.tuegum.mDiaryDemo.Common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tuegum.mDiaryDemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class diaryAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private Boolean isduoxaun;

    public static List<diary> mList = new ArrayList<>();
    public static HashMap<Integer,Boolean> isSelected = new HashMap<>();


    public diaryAdapter(List<diary> mList, Boolean isduoxaun) {
        this.mList = mList;
        this.isduoxaun = isduoxaun;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.size() <= 0){
            return -1;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        if (viewType == -1){
            View view = LayoutInflater.from(mContext).inflate(R.layout.diary_empty_item,parent,false);
            return new EmptyViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.diary_item,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder){
            if (isduoxaun){
                ((ViewHolder)holder).checkBox.setVisibility(View.VISIBLE);
                if (isSelected.containsKey(position)){
                    ((ViewHolder)holder).checkBox.setChecked(isSelected.get(position));
                }else {
                    isSelected.put(position,false);
                }
            }else{
                isSelected.clear();
                ((ViewHolder)holder).checkBox.setVisibility(View.GONE);
            }
            diary mDiary = mList.get(position);
            ((ViewHolder) holder).diaryContent.setText(mDiary.getContent());
            ((ViewHolder) holder).diaryTime.setText(mDiary.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() > 0 ? mList.size() : 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView diaryContent;
        TextView diaryTime;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView)itemView;
            diaryContent = itemView.findViewById(R.id.diary_content);
            diaryTime = itemView.findViewById(R.id.diary_time);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder{
        View empty_view;
        TextView textView;
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            empty_view = itemView;
            textView = itemView.findViewById(R.id.empty_text);
        }
    }
}
