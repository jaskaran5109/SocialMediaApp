package com.example.socialmediaapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessageList;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    public MessagesAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderMessagePicture, receiverMessagePicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            senderMessagePicture = (ImageView) itemView.findViewById(R.id.sender_message_image);
            receiverMessagePicture = (ImageView) itemView.findViewById(R.id.receiver_message_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_users, parent, false);
        auth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String messageSenderId = auth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserId = messages.getFrom();//receiver Id
        String fromMessageType = messages.getType();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(fromUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String image = snapshot.child("profile_picture").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.INVISIBLE);

        holder.senderMessagePicture.setVisibility(View.GONE);
        holder.receiverMessagePicture.setVisibility(View.GONE);
        if (fromMessageType.equals("text")) {

            if (fromUserId.equals(messageSenderId)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_bg);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setText(messages.getMessage());
            } else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_bg);
                holder.receiverMessageText.setTextColor(Color.WHITE);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.senderMessagePicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.senderMessagePicture);
            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessagePicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.receiverMessagePicture);
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.senderMessagePicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=b9fc76dc-5fb3-4602-9022-34d4311373d5")
                        .into(holder.senderMessagePicture);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url =userMessageList.get(position).getMessageId();
                        String url2="https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2F"+url+".pdf?alt=media&token=c9b6f37c-b8ef-4288-8f3d-67fef380447d";

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        Uri fileuri =  Uri.parse(url2) ;
                        intent.setDataAndType(fileuri,"application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Intent in = Intent.createChooser(intent,"open file");
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        holder.itemView.getContext().startActivity(in);
                    }

            });

        } else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessagePicture.setVisibility(View.VISIBLE);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=c1672c5d-c353-4908-a2a0-fec70ba9431f")
                    .into(holder.receiverMessagePicture);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url =userMessageList.get(position).getMessageId();
                    String url2="https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2F"+url+".pdf?alt=media&token=c9b6f37c-b8ef-4288-8f3d-67fef380447d";

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Uri fileuri =  Uri.parse(url2) ;
                    intent.setDataAndType(fileuri,"application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent in = Intent.createChooser(intent,"open file");
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.itemView.getContext().startActivity(in);
                }
            });

        }
    }
        if(fromUserId.equals(messageSenderId))

    {
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "Download and view this document",
                                    "Cancel",
                                    "Delete from everyone"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteSendMessage(position, holder);

                            } else if (i == 1) {
                                String url =userMessageList.get(position).getMessageId();
                                String url2="https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2F"+url+".pdf?alt=media&token=c9b6f37c-b8ef-4288-8f3d-67fef380447d";

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                Uri fileuri =  Uri.parse(url2) ;
                                intent.setDataAndType(fileuri,"application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Intent in = Intent.createChooser(intent,"open file");
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                holder.itemView.getContext().startActivity(in);
                            } else if (i == 2) {
                                builder.setCancelable(true);
                            } else if (i == 3) {
                                deleteEveryoneMessage(position, holder);
                            }
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("image")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "View this Image",
                                    "Cancel",
                                    "Delete from everyone"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteSendMessage(position, holder);
                            } else if (i == 1) {
                                Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                intent.putExtra("url", userMessageList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            } else if (i == 2) {
                                builder.setCancelable(true);
                            } else if (i == 3) {
                                deleteEveryoneMessage(position, holder);
                            }
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("text")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "Cancel",
                                    "Delete from everyone"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteSendMessage(position, holder);
                            } else if (i == 1) {
                                builder.setCancelable(true);
                            } else if (i == 2) {
                                deleteEveryoneMessage(position, holder);
                            }
                        }
                    });
                    builder.show();
                }
                return false;
            }
        });

    }
        else

    {
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "Download and view this document",
                                    "Cancel",
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteReceiveMessage(position, holder);
                            } else if (i == 1) {
                                String url =userMessageList.get(position).getMessageId();
                                String url2="https://firebasestorage.googleapis.com/v0/b/socialmediaapp-cf869.appspot.com/o/Document%20Files%2F"+url+".pdf?alt=media&token=c9b6f37c-b8ef-4288-8f3d-67fef380447d";

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                Uri fileuri =  Uri.parse(url2) ;
                                intent.setDataAndType(fileuri,"application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Intent in = Intent.createChooser(intent,"open file");
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                holder.itemView.getContext().startActivity(in);
                            } else if (i == 2) {
                                builder.setCancelable(true);
                            }

                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("image")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "View this Image",
                                    "Cancel",
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteReceiveMessage(position, holder);
                            } else if (i == 1) {
                                Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                intent.putExtra("url", userMessageList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            } else if (i == 2) {
                                builder.setCancelable(true);
                            }

                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("text")) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete From me",
                                    "Cancel",
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteReceiveMessage(position, holder);
                            } else if (i == 1) {
                                builder.setCancelable(true);
                            }

                        }
                    });
                    builder.show();
                }
                return false;
            }

        });

    }

}

    private void deleteSendMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Succesfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteEveryoneMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(holder.itemView.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }
}
