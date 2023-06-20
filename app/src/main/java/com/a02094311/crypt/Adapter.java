package com.a02094311.crypt;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.a02094311.crypt.databinding.CardItemBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.myViewHolder> {

    Context mContext;
    List<card> items;

    public Adapter(Context context, List<card> lst) {
        this.mContext = context;
        this.items = lst;
    }
    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardItemBinding binding = CardItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new myViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.cardTitle.setText(items.get(position).title);
        holder.usernameText.setText(items.get(position).username);
        holder.passwordText.setText(items.get(position).password);
        holder.documentId = items.get(position).documentId;
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        TextView cardTitle, usernameText, passwordText;
        ImageButton deleteButton, usernameCopy, passwordCopy;
        String documentId;
        int position;
        private FirebaseAuth mAuth = FirebaseAuth.getInstance();
        private FirebaseUser user;
        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        public myViewHolder(CardItemBinding binding) {
            super(binding.getRoot());
            cardTitle = binding.cardTitle;
            usernameText = binding.usernameText;
            passwordText = binding.passwordText;
            deleteButton = binding.deleteButton;
            usernameCopy = binding.usernameCopy;
            passwordCopy = binding.copyPassword;
            user = mAuth.getCurrentUser();

            usernameCopy.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("username", usernameText.getText().toString().replace("Username: ", ""));
                clipboard.setPrimaryClip(clip);
            });

            passwordCopy.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("password", passwordText.getText().toString().replace("Password: ", ""));
                clipboard.setPrimaryClip(clip);
            });

            deleteButton.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(binding.getRoot().getContext());
                builder.setMessage("Are you sure you wish to delete this password?")
                        .setTitle("Password Delete")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.collection(user.getUid()).document(documentId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Adapter.this.items.remove(myViewHolder.this.getAdapterPosition());
                                                Adapter.this.notifyItemRemoved(myViewHolder.this.getAdapterPosition());
                                                Log.d(TAG, "Deletion Successful");
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                builder.create().show();
            });
        }
    }
}
