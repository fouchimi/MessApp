package com.social.messapp;

import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp.adapters.MemberAdapter;
import com.social.messapp.constants.Constants;
import com.social.messapp.utils.DividerItemDecoration;

import java.util.List;


public class MemberActivityFragment extends Fragment {

    private ParseUser mCurrentUser;
    private static final String TAG = MemberActivityFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MemberAdapter mAdapter;
    private TextView emptyTextView;

    public MemberActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser= ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        View view =  inflater.inflate(R.layout.fragment_member, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.members_recycler_view);
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeFace);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(itemDecoration);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMembers();
    }

    private void fetchMembers(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo(Constants.USERNAME, mCurrentUser.getUsername());
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> memberList, ParseException e) {
                if (e == null) {
                    if (memberList.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mAdapter = new MemberAdapter(getContext(), memberList);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    Log.d(TAG, "Retrieved " + memberList.size() + " members");
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }
}
