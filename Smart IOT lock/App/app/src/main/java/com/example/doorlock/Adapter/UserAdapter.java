package com.example.doorlock.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doorlock.AdminAddKeys;
import com.example.doorlock.AdminViewUserPage;
import com.example.doorlock.Model.User;
import com.example.doorlock.R;
import com.google.firebase.database.DatabaseReference;
import com.example.doorlock.UserDetails;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    public DatabaseReference databaseReference;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users"); // Set the correct path
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvId.setText("NIC: " + user.getNic());
        holder.tvName.setText("Name: " + user.getName());
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvRole.setText("Role: " + user.getRole());

        // Navigate to UserDetails on button click
        holder.btnKeys.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminViewUserPage.class);
            intent.putExtra("id", user.getId());
            intent.putExtra("name", user.getName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("role", user.getRole());
            intent.putExtra("nic", user.getNic()); // Pass NIC
            context.startActivity(intent);
        });

        // Navigate to AdminAddKeys on button click
        holder.btnAddKeys.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminAddKeys.class);
            intent.putExtra("nic", user.getNic()); // Pass NIC
            context.startActivity(intent);
        });

        // Remove user on button click
        holder.btnRemoveUser.setOnClickListener(v -> showDeleteConfirmationDialog(user, position));
    }

    private void showDeleteConfirmationDialog(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you want to remove this user?");

        builder.setPositiveButton("Yes", (dialog, which) -> removeUser(user, position));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeUser(User user, int position) {
        databaseReference.child(user.getNic()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    userList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "User removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove user", Toast.LENGTH_SHORT).show());
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvEmail, tvRole;
        Button btnKeys, btnAddKeys, btnRemoveUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnKeys = itemView.findViewById(R.id.btnKeys);
            btnAddKeys = itemView.findViewById(R.id.btnAddKeys);
            btnRemoveUser = itemView.findViewById(R.id.btnRemoveUser);
        }
    }
}
