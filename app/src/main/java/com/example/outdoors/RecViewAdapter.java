package com.example.outdoors;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

import static java.security.AccessController.getContext;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.InvViewHolder> {

    public static final int REC_VIEW = 1;
    public static final int SENT_VIEW = 2;

    public static final int PLAN_VIEW = 3;

    public static final int CARD_VIEW = 4;

    public static final int HIKE_INV = 5;


    ArrayList<String> IDs;
    int mode;
    Context context;
    BiMap<String, Plan> objects;

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();
    String currId = userListInst.getCurrentUserID();
    private FirebaseFirestore db = DBAuth.getInstance().getDB();

    public RecViewAdapter(Context ctx, ArrayList<String> ids, int mod, BiMap<String, Plan> objects){
        this.context = ctx;
        this.IDs = ids;
        this.mode = mod;
        if(mode == CARD_VIEW){
            IDs.add("emptyCard");
        }
        if(mode == PLAN_VIEW){
            this.objects = HashBiMap.create(objects);
        }
    }

    @NonNull
    @Override
    public InvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if(mode == CARD_VIEW) {
            view = inflater.inflate(R.layout.card_view, parent, false);
        }else if(mode == PLAN_VIEW){
            view = inflater.inflate(R.layout.plan_card, parent, false);
        }else{
            view = inflater.inflate(R.layout.rec_inv_view, parent, false);
        }
        return new InvViewHolder(view, mode);
    }

    @Override
    public void onBindViewHolder(@NonNull InvViewHolder holder, final int position) {
        switch(mode){
            case REC_VIEW:
            case SENT_VIEW:
                inviteView(holder, position);
                break;
            case PLAN_VIEW:
                planView(holder, position);
                break;
            case CARD_VIEW:
                cardView(holder, position);
                break;
            case HIKE_INV:
                hikeInvView(holder, position);
                break;
            default:
                break;
        }
//
    }

    private void inviteView(InvViewHolder holder, final int position){
        final String userID = IDs.get(position);
        User usr = userListInst.getUser(IDs.get(position));
        holder.titleTV.setText(usr.getUsername());
        if(usr.getAvatar()!=null) {
            holder.imageView.setImageBitmap(usr.getAvatar());
        }else{
            holder.imageView.setImageResource(R.drawable.def_avatar);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goToProfile(userID);
            }
        });
        holder.titleTV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goToProfile(userID);
            }
        });
        final User other = userListInst.getUser(userID);
        if(mode == REC_VIEW){
            recInvView(holder, position, userID, other);
        }else if(mode == SENT_VIEW){
            sentInvView(holder, position, userID);
        }
    }

    private void recInvView(InvViewHolder holder, final int position, final String userID, final User other){
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
    }

    private void sentInvView(InvViewHolder holder, final int position, final String userID){
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
                IDs.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, IDs.size());
            }
        });
    }

    private void planView(InvViewHolder holder, final int position){
        Plan plan = objects.get(IDs.get(position));
        holder.titleTV.setText(plan.planTitle);
        holder.descTV.setText(userListInst.getUser(plan.createdBy).getUsername());
        holder.dateTV.setText(userListInst.getFormatDate(plan.date));
        holder.timeTV.setText(userListInst.getFormatTime(plan.date));
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, PlansActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("planID", IDs.get(position));
                Log.w("PLAN ID RECVIEW", IDs.get(position));
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });
    }



    private void cardView(InvViewHolder holder, final int position){
        final String userID = IDs.get(position);
        Log.w("AAAAAAA,", "IN CARDVIEW");
        if(userID.equals("emptyCard")){
            holder.imageView.setImageResource(R.drawable.ic_baseline_add_dark_24);
            holder.titleTV.setText("Invite A Friend");
            holder.imageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Log.w("AAAAAAAAAA", String.valueOf(context));
                    Toast.makeText(context, "Add Someone", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, FriendListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("mode", FriendListActivity.FRIEND_INVITE);
                    bundle.putStringArrayList("exclude", IDs);
                    i.putExtras(bundle);
                    Log.w("AAAAA", String.valueOf(context));
                    ((Activity)context).startActivityForResult(i, PlansActivity.INVITES_ACT);

                }
            });
        }else{
            final User user = userListInst.getUser(userID);
            if(user.getAvatar()!=null) {
                holder.imageView.setImageBitmap(user.getAvatar());
            }else{
                holder.imageView.setImageResource(R.drawable.def_avatar);
            }
            holder.titleTV.setText(user.getUsername());
            holder.imageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, UserProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userID", userListInst.getUserId(user));
                    i.putExtras(bundle);
                    ((Activity)context).startActivity(i);
                }
            });
        }
    }

    private void hikeInvView(InvViewHolder holder, final int position){
        final String planID = IDs.get(position);
        Plan plan = userListInst.getPlanInvite(planID);
        holder.titleTV.setText(plan.planTitle);
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, PlansActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("planID", planID);
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });
        holder.accButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DocumentReference planRef = db.collection("plans").document(planID);
                DocumentReference userRef = db.collection("users").document(currId);
                planRef.update("confirmed", FieldValue.arrayUnion(currId));
                planRef.update("invites", FieldValue.arrayRemove(currId));
                removeHikeInvite(userRef, planID, position);
            }
        });
        holder.decButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DocumentReference userRef = db.collection("users").document(currId);
                removeHikeInvite(userRef, planID, position);
            }
        });
    }

    private void removeHikeInvite(DocumentReference userRef, String planID, int position){
        ArrayList<Invite> hikeInvites = currUser.planInvites;
        for(Invite inv : hikeInvites){
            if(inv.inviteID == planID){
                hikeInvites.remove(inv);
            }
        }
        userRef.update("planInvites", hikeInvites);
        IDs.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, IDs.size());
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
        IDs.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, IDs.size());
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
        return IDs.size();
    }

    public class InvViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV;
        ImageView imageView;
        ImageButton accButt, decButt;
        TextView descTV;
        TextView dateTV;
        TextView timeTV;

        public InvViewHolder(@NonNull View itemView, int mode) {
            super(itemView);
            Log.d("CREATE VIEW ", "CONSTRUCTOR" );
            if(mode == CARD_VIEW) {
                this.imageView = itemView.findViewById(R.id.cardImageView);
                this.titleTV = itemView.findViewById(R.id.cardTitle);
                this.descTV = itemView.findViewById(R.id.cardDesc);
            }else if(mode == PLAN_VIEW){
                this.titleTV = itemView.findViewById(R.id.planTitleTV);
                this.descTV = itemView.findViewById(R.id.planDescTV);
                this.dateTV = itemView.findViewById(R.id.planDateTV);
                this.timeTV = itemView.findViewById(R.id.planTimeTV);
            }else{
                this.titleTV = itemView.findViewById(R.id.recInvUsername);
                this.imageView = itemView.findViewById(R.id.recInvImg);
                this.accButt = itemView.findViewById(R.id.recInvAccButt);
                this.decButt = itemView.findViewById(R.id.recInvDecButt);
            }
        }
    }

}
