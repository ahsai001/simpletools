package com.ahsailabs.simpletools.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ahsailabs.simpletools.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.utils.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A placeholder fragment containing a simple view.
 */
public class SendWAActivityFragment extends BaseFragment {
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private AdView mAdView;
    public SendWAActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_wa, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        messageEditText = view.findViewById(R.id.messageEditText);

        mAdView = view.findViewById(R.id.adView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public boolean sendWA() throws UnsupportedEncodingException {
        if(TextUtils.isEmpty(phoneNumberEditText.getText())){
            CommonUtils.showSnackBar(getActivity(),"please insert recipient wa number");
            return false;
        }
        if(TextUtils.isEmpty(messageEditText.getText())){
            CommonUtils.showSnackBar(getActivity(),"please insert message");
            return false;
        }

        String message = messageEditText.getText().toString();
        String url = "https://api.whatsapp.com/send?phone="+ phoneNumberEditText.getText()
                +"&text=" +  URLEncoder.encode(message, "UTF-8");

        return CommonUtils.openUrlWithPackageName(getActivity(),url,"com.whatsapp");
    }
}
