package com.senarios.simxx.callbacks;

import com.hdev.common.datamodels.Broadcasts;

public interface BroadcastCallback {
    default void OnClick(Broadcasts broadcast){

    }
   default void OnBroadcastClicked (Broadcasts broadcasts){

   }
}
