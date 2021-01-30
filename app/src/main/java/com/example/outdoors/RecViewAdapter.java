package com.example.outdoors;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.InvViewHolder> {

    private static final int REC_VIEW = 1;
    private static final int SENT_VIEW = 2;

    ArrayList<String> usernames;
    int avatar;
    int mode;
    Context context;

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();
    String currId = userListInst.getCurrentUserID();
    private FirebaseFirestore db = DBAuth.getInstance().getDB();

    public RecViewAdapter(Context ctx, ArrayList<String> uns, int img, int mod){
        this.context = ctx;
        this.usernames = uns;
        this.avatar = img;
        this.mode = mod;
    }

    @NonNull
    @Override
    public InvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rec_inv_view, parent, false);
        return new InvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvViewHolder holder, final int position) {
        holder.usernameTV.setText(usernames.get(position));
        holder.avatarView.setImageResource(avatar);
        holder.avatarView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goToProfile(userListInst.getUserId(usernames.get(position)));
            }
        });
        holder.usernameTV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goToProfile(userListInst.getUserId(usernames.get(position)));
            }
        });
        final String userID = userListInst.getUserId(usernames.get(position));
        final User other = userListInst.getUser(userID);
        if(mode==REC_VIEW) {
            holder.accButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeRequests(other, userID, position);
                    ArrayList<String> currFriends = new ArrayList<>(currUser.friends);
                    currFriends.add(userID);
                    db.collection("users").document(currId).update("friends", currFriends);
                    ArrayList<String> otherFriends = new ArrayList<>(other.friends);
                    otherFriends.add(currId);
                    db.collection("users").document(userID).update("friends", otherFriends);
                    userListInst.updateUsers(null);
                }
            });
            holder.decButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeRequests(other, userID, position);
                    userListInst.updateUsers(null);
                }
            });
        }else if(mode == SENT_VIEW){
            holder.accButt.setVisibility(View.GONE);
            holder.decButt.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ArrayList<String> currSentReq = new ArrayList<>(currUser.sentFriendRequests);
                    currSentReq.remove(userID);
                    db.collection("users").document(currId)
                            .update("sentFriendRequests", currSentReq).addOnSuccessListener(new OnSuccessListener(){
                        @Override
                        public void onSuccess(Object o) {
                            currUser.sentFriendRequests.remove(userID);
                        }
                    });
                    ArrayList<String> otherFReq = new ArrayList<>(currUser.friendRequests);
                    otherFReq.remove(currId);
                    db.collection("users").document(userID).update("friendRequests", otherFReq);
                    usernames.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, usernames.size());
                }
            });
        }
    }

    private void removeRequests(User user, final String userID, int pos){
        ArrayList<String> currFReq = new ArrayList<>(currUser.friendRequests);
        currFReq.remove(userID);
        db.collection("users").document(currId).update("friendRequests", currFReq).addOnSuccessListener(new OnSuccessListener(){
            @Override
            public void onSuccess(Object o) {
                currUser.friendRequests.remove(userID);
            }
        });
        ArrayList<String> otherSentFR = new ArrayList<>(user.sentFriendRequests);
        otherSentFR.remove(currId);
        db.collection("users").document(userID).update("sentFriendRequests", otherSentFR);
        usernames.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, usernames.size());
    }

    private void goToProfile(String id){
        Bundle userBundle = new Bundle();
        userBundle.putString("userID", id);
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtras(userBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    public class InvViewHolder extends RecyclerView.ViewHolder{

        TextView usernameTV;
        ImageView avatarView;
        ImageButton accButt, decButt;

        public InvViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d("CREATE VIEW ", "CONSTRUCTOR" );
            this.usernameTV = itemView.findViewById(R.id.recInvUsername);
            this.avatarView = itemView.findViewById(R.id.recInvImg);
            this.accButt = itemView.findViewById(R.id.recInvAccButt);
            this.decButt = itemView.findViewById(R.id.recInvDecButt);
        }
    }

}
