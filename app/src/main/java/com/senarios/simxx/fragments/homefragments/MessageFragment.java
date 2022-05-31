package com.senarios.simxx.fragments.homefragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.adaptors.DialogsAdapter;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentMessagesBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends BaseFragment implements RecyclerViewCallback,SwipeRefreshLayout.OnRefreshListener,Constants.Messages,Constants.QB ,Constants.SharedPreference{
    private FragmentMessagesBinding binding;
    private ArrayList<QBChatDialog> finalDialogList;

    public MessageFragment() {
        // Required empty public constructor
    }




    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_messages, container, false);
        binding= DataBindingUtil.bind(view);
        binding.swipe.setRefreshing(true);
        binding.swipe.setOnRefreshListener(this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (getContext()!=null) {
                initChatList();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void initChatList(){
        binding.swipe.setRefreshing(true);
        finalDialogList=new ArrayList<>();
        if (QBChatService.getInstance().isLoggedIn()){
            initmessageslist();
        }
        else{
            QBChatService.getInstance().login(new Gson().fromJson(getViewModel().getSharedPreference().getString(QB_USER, ""), QBUser.class), new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {
                    initmessageslist();
                }

                @Override
                public void onError(QBResponseException e) {
                    Utility.showELog(e);
                    binding.swipe.setRefreshing(false);
                    initmessageslist();
                }
            });
        }
    }

    void removeDialogs(ArrayList<QBChatDialog> result)
    {
        for (int i=0;i<result.size();i++)
        {
//            if(result.get(i).getLastMessage() != null)
//            {
               finalDialogList.add(result.get(i));
//            }
        }
    }

    private void initmessageslist() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(
                new QBEntityCallback<ArrayList<QBChatDialog>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatDialog> result, Bundle params) {
                        removeDialogs(result);
                        binding.swipe.setRefreshing(false);
                        binding.rvMessages.setAdapter(new DialogsAdapter(getContext(),finalDialogList,getViewModel(),MessageFragment.this));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Utility.showELog(e);
                        binding.swipe.setRefreshing(false);
                        Toast.makeText(getContext(),ERROR , Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public void onRefresh() {
            initChatList();
    }

    @Override
    public void onLongPressed(int adapterPosition, Object model) {
        QBChatDialog dialog=(QBChatDialog)model;
        Utility.getAlertDialoge(requireContext(),"Delete Conversation?","Do you really want to delete this conversation?")
                .setPositiveButton("Yes", (dialog1, which) -> {
                    QBRestChatService.deleteDialog(dialog.getDialogId(),true).performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            initChatList();
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Utility.showELog(e);
                        }
                    });
                })
                .setNegativeButton("No", (dialog12, which) -> {
                    dialog12.dismiss();
                })
                .show();
    }

    @Override
    public void onItemPictureClick(int position, Object model) {
        binding.swipe.setRefreshing(true);
        QBUsers.getUser(((QBChatDialog)model).getRecipientId()).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (isVisible()) {
                    binding.swipe.setRefreshing(false);
                    DatabaseReference user = FirebaseDatabase.getInstance().getReference();
                    user.child("UserNames").orderByChild("email")
                            .equalTo(qbUser.getLogin()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot child: snapshot.getChildren()) {
                                startActivity(new Intent(requireContext(), OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID, child.getKey()));
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
                binding.swipe.setRefreshing(false);
                Utility.showELog(e);
            }
        });
    }





}
