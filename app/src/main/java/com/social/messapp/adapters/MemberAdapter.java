package com.social.messapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.social.messapp.R;
import com.social.messapp.constants.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ousmane on 1/10/17.
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<ParseUser> members;
    private Context mContext;

    public MemberAdapter(Context context, List<ParseUser> members) {
        this.mContext = context;
        this.members = members;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView personName;
        public ImageView personPhoto;

        MemberViewHolder(View itemView) {
            super(itemView);
            personPhoto = (ImageView) itemView.findViewById(R.id.profile_thumbnail);
            personName = (TextView)itemView.findViewById(R.id.username);
        }
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_layout, viewGroup, false);
        MemberViewHolder pvh = new MemberViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(MemberViewHolder memberViewHolder, int position) {
        memberViewHolder.personName.setText(members.get(position).getUsername());
        Picasso.with(mContext).load(members.get(position).getString(Constants.PROFILE_PICTURE)).into(memberViewHolder.personPhoto);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
