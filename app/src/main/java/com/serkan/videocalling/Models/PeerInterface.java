package com.serkan.videocalling.Models;

import android.webkit.JavascriptInterface;

import com.serkan.videocalling.Activities.CallActivity;

public class PeerInterface {

    CallActivity callActivity ;

    public PeerInterface(CallActivity callActivity){
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
                CallActivity.onPeerConnected();
    }

}
