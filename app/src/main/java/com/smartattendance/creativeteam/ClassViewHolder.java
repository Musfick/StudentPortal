package com.smartattendance.creativeteam;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

public class ClassViewHolder extends RecyclerView.ViewHolder {

    public TextView mTitle;
    public TextView mCode;
    public TextView mSemester;
    public TextView mSection;
    public ImageButton mOption;
    public CardView mCardView;
    public MaterialRippleLayout mRipple;

    public ClassViewHolder(@NonNull View itemView) {
        super(itemView);
        mTitle = (TextView)itemView.findViewById(R.id.title);
        mCode = (TextView)itemView.findViewById(R.id.code);
        mSemester = (TextView)itemView.findViewById(R.id.semester);
        mSection = (TextView)itemView.findViewById(R.id.section);
        mOption = (ImageButton)itemView.findViewById(R.id.option);
        mCardView = (CardView)itemView.findViewById(R.id.card);
        mRipple = (MaterialRippleLayout)itemView.findViewById(R.id.ripple);
    }
}
