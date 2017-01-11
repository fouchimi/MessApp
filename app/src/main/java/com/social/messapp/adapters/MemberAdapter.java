package com.social.messapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.social.messapp.R;
import com.social.messapp.constants.Constants;
import com.social.messapp.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ousmane on 1/10/17.
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<ParseUser> members;
    private Context mContext;
    private static final String TAG = MemberAdapter.class.getSimpleName();
    public MemberAdapter(Context context, List<ParseUser> members) {
        this.mContext = context;
        this.members = members;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView personName;
        public ImageView personPhoto;
        public TextView personLocation;

        MemberViewHolder(View itemView) {
            super(itemView);
            personPhoto = (ImageView) itemView.findViewById(R.id.profile_thumbnail);
            personName = (TextView)itemView.findViewById(R.id.username);
            personLocation = (TextView) itemView.findViewById(R.id.location);
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
        ParseUser person = members.get(position);
        String username = members.get(position).getUsername();
        String[] cleanedUserName = username.split("_");
        memberViewHolder.personName.setText(cleanedUserName[0]);
        memberViewHolder.personLocation.setText(mContext.getString(R.string.lives_at) + " " + person.getString(Constants.LOCATION));
        String picture_thumbnail = members.get(position).getString(Constants.PROFILE_PICTURE);
        if(picture_thumbnail == null || picture_thumbnail.isEmpty()){
            Picasso.with(mContext).load(mContext.getString(R.string.default_profile_url)).transform(new CircleTransform()).
                    into(memberViewHolder.personPhoto);
        }else Picasso.with(mContext).load(person .getString(Constants.PROFILE_PICTURE)).
                transform(new CircleTransform()).into(memberViewHolder.personPhoto);
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
