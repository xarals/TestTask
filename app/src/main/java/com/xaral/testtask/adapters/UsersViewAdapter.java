package com.xaral.testtask.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.xaral.testtask.R;
import com.xaral.testtask.api.User;

import java.util.List;

public class UsersViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final LayoutInflater inflater;
    private List<User> users;

    private boolean isFull = false;

    public UsersViewAdapter(Context context, List<User> users) {
        this.users = users;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == users.size())
            return 1;
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = inflater.inflate(R.layout.list_users, parent, false);
            return new UserViewHolder(view);
        }
        else {
            View view = inflater.inflate(R.layout.loading_view, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderObject, @SuppressLint("RecyclerView") int position) {
        if (position == users.size())
            return;
        UserViewHolder holder = (UserViewHolder) holderObject;
        User user = users.get(position);
        holder.name.setText(user.getName());
        holder.position.setText(user.getPosition());
        holder.email.setText(user.getEmail());
        holder.phone.setText(user.getPhone());
        Picasso.get().load(user.getPhoto()).into(holder.image);
    }

    public void addUsers(List<User> users) {
        int oldCount = this.users.size();
        this.users.addAll(users);
        Log.i("total", String.valueOf(this.users.size()));
        if (oldCount == 0)
            notifyDataSetChanged();
        else
            notifyItemRangeInserted(oldCount, users.size());
    }

    public void setFull() {
        this.isFull = true;
        Log.i("Full", "+");
        notifyItemRemoved(users.size());
    }

    @Override
    public int getItemCount() {
        if (isFull)
            return users.size();
        return users.size() + 1;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView name, position, email, phone;
        final ImageView image;
        UserViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.name);
            position = view.findViewById(R.id.position);
            email = view.findViewById(R.id.email);
            phone = view.findViewById(R.id.phone);
            image = view.findViewById(R.id.userImage);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final TextView name, position, email, phone;
        final ImageView image;
        LoadingViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.name);
            position = view.findViewById(R.id.position);
            email = view.findViewById(R.id.email);
            phone = view.findViewById(R.id.phone);
            image = view.findViewById(R.id.userImage);
        }
    }
}
