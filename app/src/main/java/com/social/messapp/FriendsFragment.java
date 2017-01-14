package com.social.messapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp.adapters.FriendAdapter;
import com.social.messapp.adapters.MemberAdapter;
import com.social.messapp.constants.Constants;
import com.social.messapp.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final String TAG = FriendsFragment.class.getSimpleName();
    private ParseUser mCurrentUser;
    private FriendAdapter mAdapter;

    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser = ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeFace);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.friends_recycler_view);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = mRecyclerView.findChildViewUnder(e.getX(),e.getY());

                if(child!=null ){
                    int position = mRecyclerView.getChildAdapterPosition(child);
                    //Toast.makeText(getContext(),"The Item Clicked is: "+position,Toast.LENGTH_SHORT).show();
                    mAdapter = (FriendAdapter) mRecyclerView.getAdapter();
                    ParseUser friend = mAdapter.getFriend(position);
                    Log.d(TAG, friend.getObjectId());
                    Toast.makeText(getContext(), friend.getObjectId(), Toast.LENGTH_LONG).show();
                    Intent intent =  new Intent(getActivity(), MessageActivity.class);
                    intent.putExtra(Constants.USER_ID, friend.getObjectId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                    return true;

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchFriendList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void fetchFriendList(){
        final List<ParseUser> friends = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.FRIENDS_TABLE);
        query.whereEqualTo(Constants.SENDER, mCurrentUser);
        query.whereEqualTo(Constants.CHECKED, true);
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> friendList, ParseException e) {
                if (e == null) {
                    if (friendList.isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        for(ParseObject row : friendList){
                            friends.add((ParseUser) row.get(Constants.RECEIVER));
                        }
                        mAdapter = new FriendAdapter(getContext(), friends);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    Log.d(TAG, "Retrieved " + friendList.size() + " friends");
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

}
