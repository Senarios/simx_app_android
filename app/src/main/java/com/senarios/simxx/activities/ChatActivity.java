package com.senarios.simxx.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Users;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.ChatAdapter;
import com.senarios.simxx.callbacks.PaginationHistoryListener;
import com.senarios.simxx.databinding.ActivityChatBinding;
import com.senarios.simxx.databinding.CustomToolbarBinding;
import com.senarios.simxx.viewmodels.SharedVM;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.jivesoftware.smack.SmackException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements PaginationHistoryListener, QBEntityCallback<QBUser> {
    private ActivityChatBinding binding;
    private EditText messageEditText;
    private RecyclerView messagesRecyclerView;
    private QBChatDialog qbChatDialog;
    private ChatAdapter chatAdaptor;
    private List<QBChatMessage> messagesList;
    private SharedVM sharedVM;
    private int skipPagination = 0;
    private final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";
    private final int GALLERY_PICTURE = 2;
    private final int TAKE_PICTURE = 1;
    private File file;
    private TextView opponent_name;
    private ImageView opponent_image;

    @Override
    public ViewDataBinding binding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        return binding;
    }

    @Override
    public void init() {

        //init views
        opponent_image = findViewById(R.id.opponent_image);
        opponent_name = findViewById(R.id.opponent_name);
        messageEditText = findViewById(R.id.edit_chat_message);
        messagesRecyclerView = findViewById(R.id.list_chat_messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);

        binding.swipe.setEnabled(false);
        binding.swipe.setRefreshing(true);

        if (getIntent().getSerializableExtra("d") != null) {
            qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra("d");
        }

        if (QBChatService.getInstance().isLoggedIn()) {
            initqbChatdialoge();
        } else {
            Toast.makeText(this, ERROR, Toast.LENGTH_SHORT).show();
        }

        //settopbarview

        if (qbChatDialog != null) {
            if (qbChatDialog.getRecipientId() != null) {
                QBUsers.getUser(qbChatDialog.getRecipientId()).performAsync(this);
            }
        }


    }

    @SuppressLint("WrongConstant")
    private void initCustomToolbar(QBUser qbUser, String opponentid) {
        setSupportActionBar(binding.topbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = getLayoutInflater().inflate(R.layout.custom_toolbar, null);
        CustomToolbarBinding binding = DataBindingUtil.bind(view);
        getSupportActionBar().setCustomView(binding.getRoot());
        Glide.with(this).load(Constants.DreamFactory.GET_IMAGE_URL + qbUser.getLogin() + ".png").into(binding.profilePicture);
        binding.name.setText("" + qbUser.getFullName());

        SharedPreferences preferences = getSharedPreferences("notificationString", MODE_PRIVATE);
        String sendNotificationString = preferences.getString(SharedPreference.CALL_MSG, "");

        switch (sendNotificationString) {
            case "msg":
                binding.phone.setVisibility(View.GONE);
                break;
            case "call":
            case "both":
                binding.phone.setVisibility(View.VISIBLE);
                break;
        }

        binding.navigation.setOnClickListener(v -> {
            qbChatDialog = null;
            sharedVM = null;
            chatAdaptor = null;
            finish();
        });
        binding.profilePicture.setOnClickListener(v -> {
            Utility.loadFullScreenImageView(this, Constants.DreamFactory.GET_IMAGE_URL + qbUser.getLogin() + ".png");
        });
        binding.phone.setOnClickListener(v -> {
            if (this.binding.getUser() != null && !this.binding.getUser().getQbid().equalsIgnoreCase("NA")) {
//                if (this.binding.getUser().getRate().isEmpty() || Integer.parseInt(this.binding.getUser().getRate()) == 0) {
                    getViewModel().getSharedPreference().edit().putInt(QB_OPPONENT_USER, Integer.parseInt(this.binding.getUser().getQbid())).apply();
                    Intent intent = new Intent(this, CallActivity.class);
                    intent.putExtra(QB_OPPONENT_USER, this.binding.getUser());
                    startActivity(intent);
//                } else if (Math.round(getViewModel().getLoggedUser().getCredit()) > 0) {
//                    getViewModel().getSharedPreference().edit().putInt(QB_OPPONENT_USER, Integer.parseInt(this.binding.getUser().getQbid())).apply();
//                    Intent intent = new Intent(this, CallActivity.class);
//                    intent.putExtra(QB_OPPONENT_USER, this.binding.getUser());
//                    startActivity(intent);
//                } else {
//                    Utility.getAlertDialoge(this, "Insufficient Balance", getString(R.string.message_low_balance))
//                            .setPositiveButton("Yes", (dialog, which) ->
//                                    startActivity(new Intent(ChatActivity.this, PaymentTestActivity.class)))
//                            .setNegativeButton("No", (dialog, which) -> {
//                                dialog.dismiss();
//                            }).show();
//                }
            }
        });
        binding.info.setOnClickListener(v -> {
            startActivity(new Intent(this, OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID, opponentid));
        });
    }


    private void initqbChatdialoge() {
        if (qbChatDialog != null) {
            if (QBChatService.getInstance() != null) {
                qbChatDialog.initForChat(QBChatService.getInstance());
            }

            qbChatDialog.addMessageListener(new QBChatDialogMessageListener() {
                @Override
                public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
                    if (chatAdaptor != null) {
                        chatAdaptor.add(qbChatMessage);
                        scrollMessageListDown();
                        chatAdaptor.setPaginationHistoryListener(ChatActivity.this);
                    }

                }

                @Override
                public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

                }
            });

            QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
            messageGetBuilder.setLimit(500);
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    binding.swipe.setRefreshing(false);
                    if (chatAdaptor == null) {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        layoutManager.setStackFromEnd(true);
                        messagesRecyclerView.setLayoutManager(layoutManager);
                        messagesList = qbChatMessages;
                        chatAdaptor = new ChatAdapter(ChatActivity.this, qbChatDialog, messagesList);
                        messagesRecyclerView.setAdapter(chatAdaptor);
                        messagesRecyclerView.addItemDecoration(
                                new StickyRecyclerHeadersDecoration(chatAdaptor));
                        scrollMessageListDown();
                    }
                }

                @Override
                public void onError(QBResponseException e) {
                    binding.swipe.setRefreshing(false);
                }
            });
        }

    }

    //direct functions onclick from xml
    public void onAttachmentsClick(View view) {
        AlertDialog.Builder myAlertDialog;
        myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to send image?");
        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_PICTURE);
                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, TAKE_PICTURE);
                });
        myAlertDialog.show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            createFile(photo);
        } else if (requestCode == GALLERY_PICTURE && data != null && data.getData() != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                createFile(photo);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Problem Uploading File.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadtoQB(File file) {
        QBContent.uploadFileTask(file, false, null, i -> {
        }).performAsync(new QBEntityCallback<QBFile>() {

            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                // attach a photo
                QBAttachment attachment = new QBAttachment(QBAttachment.IMAGE_TYPE);
                attachment.setId(qbFile.getUid());
                attachment.setContentType(qbFile.getContentType());
                attachment.setName(qbFile.getName());
                attachment.setSize(qbFile.getSize());
                attachment.setUrl(qbFile.getPrivateUrl());
                sendChatMessage(null, attachment);
                binding.swipe.setRefreshing(false);

            }

            @Override
            public void onError(QBResponseException e) {
                binding.swipe.setRefreshing(false);
                Toast.makeText(ChatActivity.this, ERROR, Toast.LENGTH_SHORT).show();
            }
        });


    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createFile(Bitmap bitmap) {
        try {
            binding.swipe.setRefreshing(true);
            //create a file to write bitmap data
            file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
            file.createNewFile();
//Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            uploadtoQB(file);
        } catch (Exception e) {
            binding.swipe.setRefreshing(false);
            Toast.makeText(getApplicationContext(), "Error: Uploading file.", Toast.LENGTH_SHORT).show();
        }

    }


    public void onSendChatClick(View view) {
        String text = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }

    }

    private void sendChatMessage(final String text, final QBAttachment attachment) {
        if (QBChatService.getInstance().isLoggedIn()) {
            QBChatMessage chatMessage = new QBChatMessage();
            if (text != null && !text.isEmpty()) {
                chatMessage.setBody(text);
            } else {
                chatMessage.addAttachment(attachment);
            }
            chatMessage.setSaveToHistory(true);
            chatMessage.setDateSent(System.currentTimeMillis() / 1000);
            chatMessage.setMarkable(true);
            chatMessage.setSenderId(Integer.valueOf(sharedVM.getLoggedUser().getQbid()));

            try {
                qbChatDialog.sendMessage(chatMessage);
                chatMessage.setRecipientId(Integer.valueOf(sharedVM.getLoggedUser().getQbid()));
                //   QBChatService.getInstance().getSystemMessagesManager().sendSystemMessage(chatMessage);
                showMessage(chatMessage);
                messageEditText.setText("");
            } catch (SmackException.NotConnectedException e) {
            }
        }
    }


    private void showMessage(QBChatMessage message) {
        if (chatAdaptor != null) {
            chatAdaptor.add(message);
            scrollMessageListDown();
        }
    }

    private void scrollMessageListDown() {
        messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
    }


    ///Activity Callbacks
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        qbChatDialog = null;
        sharedVM = null;
        chatAdaptor = null;
        finish();
    }

    @Override
    public void downloadMore() {
        loadChatHistory();
    }

    private void loadChatHistory() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setSkip(skipPagination);
        messageGetBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        messageGetBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);
        messageGetBuilder.markAsRead(false);

        QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle bundle) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);
                if (chatAdaptor != null) {
                    chatAdaptor.addList(messages);
                }
                if (skipPagination == 0) {
                    scrollMessageListDown();
                }
                skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE;
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            qbChatDialog.leave();
        } catch (Exception e) {

        }
    }

    @Override
    public void onSuccess(QBUser qbUser, Bundle bundle) {
        if (qbUser != null) {
            DatabaseReference user = FirebaseDatabase.getInstance().getReference();
            user.child("UserNames").orderByChild("email")
                    .equalTo(qbUser.getLogin()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
//                        if (!this.isFinishing()) {
                        getUser(qbUser, child.getKey());
//                        }
                        Log.wtf("userid", child.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    @Override
    public void onError(QBResponseException e) {

    }

    private void getUser(QBUser qbUser, String id) {
        if (getIntent().hasExtra(DataConstants.USER)) {
            binding.setUser((Users) getIntent().getSerializableExtra(DataConstants.USER));
            initCustomToolbar(qbUser, id);
        } else {
            if (Utility.isNetworkAvailable(this)) {
                binding.swipe.setRefreshing(true);
                Utility.getService(Constants.DreamFactory.URL)
                        .getUser(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Response<Users>>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onSuccess(Response<Users> response) {
                                binding.swipe.setRefreshing(false);
                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        if (response.body() != null) {
                                            binding.setUser(response.body());
                                            initCustomToolbar(qbUser, id);
                                        }
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(ChatActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.swipe.setRefreshing(false);
                                Toast.makeText(ChatActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        });
            } else {
                Toast.makeText(this, "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
