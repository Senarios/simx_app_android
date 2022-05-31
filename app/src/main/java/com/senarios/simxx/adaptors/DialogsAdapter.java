package com.senarios.simxx.adaptors;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hdev.common.datamodels.JobCandidates;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.ChatActivity;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.holder> {
    private Context context;
    private List<QBChatDialog> selectedItems = new ArrayList<>();
    private List<QBChatDialog> dialogs;
    private QBUser user;
    private SharedVM sharedVM;
    private RecyclerViewCallback listener;

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs, SharedVM sharedVM, RecyclerViewCallback listener) {
        this.context = context;
        this.dialogs = dialogs;
        this.sharedVM = sharedVM;
        this.listener = listener;
    }

    public List<QBChatDialog> getSelectedItems() {
        return selectedItems;
    }


    private int getUnreadMsgCount(QBChatDialog chatDialog) {
        Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
        if (unreadMessageCount == null) {
            unreadMessageCount = 0;
        }
        return unreadMessageCount;
    }

    private boolean isLastMessageAttachment(QBChatDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private String prepareTextLastMessage(QBChatDialog chatDialog) {
        if (isLastMessageAttachment(chatDialog)) {
            return context.getString(R.string.chat_attachment);
        } else {
            return chatDialog.getLastMessage();
        }
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void updateList(List<QBChatDialog> dialogs) {
        this.dialogs = dialogs;
        notifyDataSetChanged();
    }

    public void selectItem(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyDataSetChanged();
    }

    public void toggleSelection(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_dialog, parent, false);
        return new holder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        QBChatDialog dialog = dialogs.get(position);
            holder.dialogImageView.setBackgroundDrawable(getColoredCircleDrawable(position));
            holder.dialogImageView.setImageDrawable(null);

            holder.nameTextView.setText(getDialogName(dialog));
            holder.lastMessageTextView.setText(prepareTextLastMessage(dialog));
            QBUsers.getUser(dialog.getRecipientId()).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    if ((qbUser != null && qbUser.getLogin() != null) && user != qbUser) {
                        user = qbUser;
                        Glide.with(context).clear(holder.dialogImageView);
                        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL + user.getLogin() + ".png")
                                .into(holder.dialogImageView);
                        if (sharedVM.compareID(qbUser.getLogin())) {
                            holder.blockedImage.setVisibility(View.VISIBLE);
                        }
                        holder.rootLayout.setOnClickListener(v -> {
                            if (sharedVM.compareID(qbUser.getLogin())) {
                                Toast.makeText(context, "Unblock user first", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("d", dialogs.get(position));
                                context.startActivity(intent);
                            }
                        });
                    }
                }

                @Override
                public void onError(QBResponseException e) {
                    Glide.with(context).clear(holder.dialogImageView);
                    Glide.with(context).load(R.drawable.h2pay2)
                            .into(holder.dialogImageView);
                }
            });


            int unreadMessagesCount = getUnreadMsgCount(dialog);
            if (unreadMessagesCount == 0) {
                holder.unreadCounterTextView.setVisibility(View.GONE);
            } else {
                holder.unreadCounterTextView.setVisibility(View.VISIBLE);
                holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? "99+" : unreadMessagesCount));
            }



    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }


    private Drawable getColoredCircleDrawable(@ColorInt int color) {
        GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.shape_circle);
        drawable.setColor(color);
        return drawable;
    }

    private int getColor(@ColorRes int colorId) {
        return context.getResources().getColor(colorId);
    }

    private QBUser getUserById(int id) {
        SparseArray<QBUser> qbUserSparseArray = new SparseArray<>();
        return qbUserSparseArray.get(id);
    }

    private String getDialogName(QBChatDialog dialog) {
        // It's a private dialog, let's use qbUser's name as chat name
        Integer opponentId = dialog.getRecipientId();
        QBUser user = getUserById(opponentId);
        if (user != null) {
            return TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
        } else {
            return dialog.getName();
        }

    }

    public class holder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        ViewGroup rootLayout;
        CircleImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
        ImageView blockedImage;


        public holder(@NonNull View convertView) {
            super(convertView);
            nameTextView = convertView.findViewById(R.id.text_dialog_name);
            lastMessageTextView = convertView.findViewById(R.id.text_dialog_last_message);
            dialogImageView = convertView.findViewById(R.id.image_dialog_icon);
            unreadCounterTextView = convertView.findViewById(R.id.text_dialog_unread_count);
            rootLayout = convertView.findViewById(R.id.root);
            blockedImage = convertView.findViewById(R.id.blocked_image);
            rootLayout.setOnLongClickListener(this);
            dialogImageView.setOnClickListener(this);

        }


        @Override
        public boolean onLongClick(View v) {
            listener.onLongPressed(getAdapterPosition(),dialogs.get(getAdapterPosition()));
            return true;
        }

        @Override
        public void onClick(View v) {
            listener.onItemPictureClick(getAdapterPosition(),dialogs.get(getAdapterPosition()));

        }


    }
}