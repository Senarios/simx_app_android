package com.senarios.simxx.adaptors;

import android.view.View;

import com.quickblox.chat.model.QBChatDialog;

public interface RecyclerViewCallback {
   default void onItemClick(int position , Object model){

   }

   default void onItemButtonClick(int position , Object model){

   }

   default void onItemPictureClick(int position , Object model){

   }

   default void onItemDelete(int position , Object model){

   }

   default void onItemOptions(int position , Object model, View container){


   }
    default void onPicClick(String videocvpath){


    }

    default void onItemUserAction(){

    }

    default void onLongPressed(int adapterPosition, Object model){

    }
}
