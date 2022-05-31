package com.senarios.simxx.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.AttachClickListener;
import com.senarios.simxx.callbacks.MessageLinkClickListener;
import com.senarios.simxx.callbacks.PaginationHistoryListener;
import com.senarios.simxx.qbhelpers.MessageTextClickMovement;
import com.senarios.simxx.qbhelpers.TimeUtils;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;

    private static final int CUSTOM_VIEW_TYPE = -1;

    private static final int TYPE_TEXT_RIGHT = 1;
    private static final int TYPE_TEXT_LEFT = 2;
    private static final int TYPE_ATTACH_RIGHT = 3;
    private static final int TYPE_ATTACH_LEFT = 4;
    private static final int TYPE_NOTIFICATION_CENTER = 5;

    //Message TextView click listener
    //
    private MessageLinkClickListener messageTextViewLinkClickListener;
    private boolean overrideOnClick;

    private AttachClickListener attachImageClickListener;

    private SparseIntArray containerLayoutRes = new SparseIntArray() {
        {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_text_right);
            put(TYPE_TEXT_LEFT, R.layout.list_item_text_left);
            put(TYPE_ATTACH_RIGHT, R.layout.list_item_attach_right);
            put(TYPE_ATTACH_LEFT, R.layout.list_item_attach_left);
            put(TYPE_NOTIFICATION_CENTER, R.layout.list_item_notif_center);
        }
    };

    private MessageViewHolder viewHolder;
    private static List<QBChatMessage> chatMessages;
    private LayoutInflater inflater;
    private static Context context;

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        this.chatDialog = chatDialog;
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }


    public void addList(List<QBChatMessage> items) {
        chatMessages.clear();
        chatMessages.addAll(items);
        notifyDataSetChanged();
    }

    public void addToList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
    }


    public void add(QBChatMessage item) {
        this.chatMessages.add(item);
        this.notifyItemInserted(chatMessages.size() - 1);
    }

    public List<QBChatMessage> getList() {
        return chatMessages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NOTIFICATION_CENTER:
                viewHolder = new NotificationHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message);
                return viewHolder;
            case TYPE_TEXT_RIGHT:
                viewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message, R.id.msg_link_preview, R.id.msg_text_status_message);
                return viewHolder;
            case TYPE_TEXT_LEFT:
                viewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message, R.id.msg_link_preview);
                return viewHolder;
            case TYPE_ATTACH_RIGHT:
                viewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach, R.id.msg_attach_status_message);
                return viewHolder;
            case TYPE_ATTACH_LEFT:
                viewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach);
                return viewHolder;
            default:
                return null;
        }
    }

    @Override
    public void onViewRecycled(MessageViewHolder holder) {
        if (holder.getItemViewType() == TYPE_TEXT_LEFT || holder.getItemViewType() == TYPE_TEXT_RIGHT) {
            TextMessageHolder textMessageHolder = (TextMessageHolder) holder;

            if (textMessageHolder.linkPreviewLayout.getTag() != null) {
                textMessageHolder.linkPreviewLayout.setTag(null);
            }
        }

        //abort loading avatar before setting new avatar to view
        if (containerLayoutRes.get(holder.getItemViewType()) != 0 && holder.avatar != null) {
            Glide.with(context).clear(holder.avatar);
        }

        super.onViewRecycled(holder);
    }


    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        downloadMore(position);
        QBChatMessage chatMessage = getItem(position);
        int valueType = getItemViewType(position);
        Log.d(TAG, "onBindViewHolder on position " + position);
        switch (valueType) {
            case TYPE_NOTIFICATION_CENTER:
                onBindViewNotificationHolder((NotificationHolder) holder, chatMessage, position);
                break;
            case TYPE_TEXT_RIGHT:
                onBindViewMsgRightHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_TEXT_LEFT:
                onBindViewMsgLeftHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_RIGHT:
                onBindViewAttachRightHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_LEFT:
                onBindViewAttachLeftHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            default:
                onBindViewCustomHolder(holder, chatMessage, position);
                break;
        }
    }

    private void onBindViewNotificationHolder(NotificationHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.messageTimeTextView.setText(getTime(chatMessage.getDateSent()));
    }

    private void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        fillTextMessageHolder(holder, chatMessage, position, false);
    }

    private void onBindViewMsgLeftHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTimeTextView.setVisibility(View.VISIBLE);

        TextView customMessageTimeTextView = holder.itemView.findViewById(R.id.custom_msg_text_time_message);
        /*customMessageTimeTextView.setText(getTime(chatMessage.getDateSent()));*/

        fillTextMessageHolder(holder, chatMessage, position, true);
    }

    private void onBindViewAttachRightHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        fillAttachmentHolder(holder, chatMessage, position, false);
    }

    private void onBindViewAttachLeftHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        fillAttachmentHolder(holder, chatMessage, position, true);
        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);
    }

    private void onBindViewCustomHolder(MessageViewHolder holder, QBChatMessage chatMessage, int position) {
    }

    private void fillAttachmentHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position, boolean isLeftMessage) {
        setDateSentAttach(holder, chatMessage);
        displayAttachment(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);

        if (!isLeftMessage) {
            boolean read = isRead(chatMessage);
            boolean delivered = isDelivered(chatMessage);
            if (read) {
                holder.attachStatusTextView.setText(R.string.statuses_read);
            } else if (delivered) {
                holder.attachStatusTextView.setText(R.string.statuses_delivered);
            } else {
                holder.attachStatusTextView.setText(R.string.statuses_sent);
            }
        }
    }

    private void fillTextMessageHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position, boolean isLeftMessage) {
        holder.linkPreviewLayout.setVisibility(View.GONE);
        holder.messageTextView.setText(chatMessage.getBody());
        holder.messageTimeTextView.setText(getTime(chatMessage.getDateSent()));

        setMessageTextViewLinkClickListener(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        List<String> urlsList = extractUrls(chatMessage.getBody());
        if (urlsList.isEmpty()) {
            holder.messageTextView.setMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
        } else {
            holder.messageTextView.setMaxWidth((int) context.getResources().getDimension(R.dimen.link_preview_width));
            holder.linkPreviewLayout.setTag(chatMessage.getId());
        }

        if (!isLeftMessage) {
            boolean read = isRead(chatMessage);
            boolean delivered = isDelivered(chatMessage);
            if (read) {
                holder.messageStatusTextView.setText(R.string.statuses_read);
            } else if (delivered) {
                holder.messageStatusTextView.setText(R.string.statuses_delivered);
            } else {
                holder.messageStatusTextView.setText(R.string.statuses_sent);
            }
        }
    }

    private boolean isReadByCurrentUser(QBChatMessage chatMessage) {
        Integer currentUserId = QBChatService.getInstance().getUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private boolean isRead(QBChatMessage chatMessage) {
        boolean read = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = QBChatService.getInstance().getUser().getId();
        Collection<Integer> readIds = chatMessage.getReadIds();
        if (readIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && readIds.contains(recipientId)) {
            read = true;
        } else if (readIds.size() == 1 && readIds.contains(currentUserId)) {
            read = false;
        } else if (readIds.size() > 0) {
            read = true;
        }
        return read;
    }

    private boolean isDelivered(QBChatMessage chatMessage) {
        boolean delivered = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = QBChatService.getInstance().getUser().getId();
        Collection<Integer> deliveredIds = chatMessage.getDeliveredIds();
        if (deliveredIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && deliveredIds.contains(recipientId)) {
            delivered = true;
        } else if (deliveredIds.size() == 1 && deliveredIds.contains(currentUserId)) {
            delivered = false;
        } else if (deliveredIds.size() > 0) {
            delivered = true;
        }
        return delivered;
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getItemCount() != previousGetCount) {
                if (paginationListener != null) {
                    paginationListener.downloadMore();
                    previousGetCount = getItemCount();
                }
            }
        }
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.view_chat_message_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        TextView dateTextView = view.findViewById(R.id.header_date_textview);

        QBChatMessage chatMessage = getItem(position);
        dateTextView.setText(getDate(chatMessage.getDateSent()));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        lp.topMargin = 0;
        dateTextView.setLayoutParams(lp);
    }

    private void setMessageTextViewLinkClickListener(TextMessageHolder holder, int position) {
        if (messageTextViewLinkClickListener != null) {
            MessageTextClickMovement customClickMovement =
                    new MessageTextClickMovement(messageTextViewLinkClickListener, overrideOnClick, context);
            customClickMovement.setPositionInAdapter(position);

            holder.messageTextView.setMovementMethod(customClickMovement);
        }
    }

    private AttachClickListener getAttachListenerByType(int position) {
        QBAttachment attachment = getAttachment(position);
        if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType()) ||
                QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
            return attachImageClickListener;
        }
        return null;
    }

    private void setDateSentAttach(ImageAttachHolder holder, QBChatMessage chatMessage) {
        holder.attachTimeTextView.setText(getTime(chatMessage.getDateSent()));
    }

    @Nullable
    private String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
        return null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    private QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (chatMessage.getProperty("notification_type") != null) {
            return TYPE_NOTIFICATION_CENTER;
        }

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = getAttachment(position);

            if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType()) ||
                    QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
                return isIncoming(chatMessage) ? TYPE_ATTACH_LEFT : TYPE_ATTACH_RIGHT;
            }
        } else {
            return isIncoming(chatMessage) ? TYPE_TEXT_LEFT : TYPE_TEXT_RIGHT;
        }
        return customViewType(position);
    }

    private int customViewType(int position) {
        return CUSTOM_VIEW_TYPE;
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>10:15</b>
     */
    private String getTime(long seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(seconds * 1000));
    }

    /**
     * @return string in "Month Day" format, i.e. <b>APRIL 25</b>
     */
    public static String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds * 1000));
    }

    private void displayAttachment(MessageViewHolder holder, int position) {
        QBAttachment attachment = getAttachment(position);

        if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType()) ||
                QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
            showPhotoAttach(holder, position);
        }
    }

    private void showPhotoAttach(MessageViewHolder holder, int position) {
        String imageUrl = getImageUrl(position);
        showImageByURL(holder, imageUrl, position);
    }

    private String getImageUrl(int position) {
        QBAttachment attachment = getAttachment(position);
        return QBFile.getPrivateUrlForUID(attachment.getId());
    }

    private QBAttachment getAttachment(int position) {
        QBChatMessage chatMessage = getItem(position);
        return chatMessage.getAttachments().iterator().next();
    }

    private void showImageByURL(MessageViewHolder holder, String url, int position) {
        int preferredImageWidth = (int) context.getResources().getDimension(R.dimen.attach_image_width_preview);
        int preferredImageHeight = (int) context.getResources().getDimension(R.dimen.attach_image_height_preview);

        ((ImageAttachHolder)holder).attachImageView.setOnClickListener(v -> {
                Utility.loadFullScreenImageView(context,url);
        });

        Glide.with(context)
                .load(url)
                .override(preferredImageWidth, preferredImageHeight)
                .dontTransform()
                .error(R.drawable.ic_error)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(((ImageAttachHolder) holder).attachImageView);
    }


    private void displayAvatarImage(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .dontAnimate()
                .into(imageView);
    }

    private void setItemAttachClickListener(AttachClickListener listener, MessageViewHolder holder, QBAttachment qbAttachment, int position) {
        if (listener != null) {
            holder.bubbleFrame.setOnClickListener(new ItemClickListenerFilter(listener, qbAttachment, position));
        }
    }

    private static class NotificationHolder extends MessageViewHolder {
        private TextView messageTextView;
        private TextView messageTimeTextView;

        private NotificationHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
        }
    }

    private static class TextMessageHolder extends MessageViewHolder {
        private View linkPreviewLayout;
        private TextView messageTextView;
        private TextView messageTimeTextView;
        private TextView messageStatusTextView;

        private TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int linkPreviewLayoutId, @IdRes int statusId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId);
            messageStatusTextView = itemView.findViewById(statusId);
        }

        private TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int linkPreviewLayoutId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId);
        }
    }

    private static class ImageAttachHolder extends MessageViewHolder {
        private ImageView attachImageView;
        private ProgressBar attachProgressBar;
        private TextView attachTimeTextView;
        private TextView attachStatusTextView;

        private ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId, @IdRes int timeId, @IdRes int signId) {
            super(itemView);
            attachImageView = itemView.findViewById(attachId);
            attachProgressBar = itemView.findViewById(progressBarId);
            attachTimeTextView = itemView.findViewById(timeId);
        }

        private ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId, @IdRes int timeId, @IdRes int signId, @IdRes int statusId) {
            super(itemView);
            attachImageView = itemView.findViewById(attachId);
            attachProgressBar = itemView.findViewById(progressBarId);
            attachTimeTextView = itemView.findViewById(timeId);
            attachStatusTextView = itemView.findViewById(statusId);

        }
    }

    public abstract static class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public View bubbleFrame;


        private MessageViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.msg_image_avatar);
            bubbleFrame = itemView.findViewById(R.id.msg_bubble_background);

        }
    }


    private class ItemClickListenerFilter implements View.OnClickListener {
        protected int position;
        private QBAttachment attachment;
        private AttachClickListener chatAttachClickListener;

        ItemClickListenerFilter(AttachClickListener attachClickListener, QBAttachment attachment, int position) {
            this.position = position;
            this.attachment = attachment;
            this.chatAttachClickListener = attachClickListener;
        }

        @Override
        public void onClick(View view) {
            chatAttachClickListener.onLinkClicked(attachment, position);
        }
    }


    private List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        Pattern pattern = Patterns.WEB_URL;
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }
        return containedUrls;
    }


}