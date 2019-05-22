package com.ahsailabs.simpletools.fragments;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ahsailabs.simpletools.R;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.zaitunlabs.zlcore.api.APIResponse;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.utils.CommonUtils;
import com.zaitunlabs.zlcore.utils.HttpClientUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * A placeholder fragment containing a simple view.
 */
public class LinkWAActivityFragment extends BaseFragment {
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private TextView linkwaTextView;
    private TextView linkwaShortTextView;
    private AdView mAdView;
    public LinkWAActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_link_wa, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        messageEditText = view.findViewById(R.id.messageEditText);
        linkwaTextView = view.findViewById(R.id.linkwaTextView);
        linkwaShortTextView = view.findViewById(R.id.linkwaShortTextView);
        mAdView = view.findViewById(R.id.adView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        linkwaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String linkwa = linkwaTextView.getText().toString();
                CommonUtils.copyPlainTextToClipboard(getActivity(), "linkwa", linkwa);
                CommonUtils.showSnackBar(getActivity(), "link click to chat wa already copied to clipboard");
            }
        });
        linkwaShortTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shortlinkwa = linkwaShortTextView .getText().toString();
                CommonUtils.copyPlainTextToClipboard(getActivity(), "shortlinkwa", shortlinkwa);
                CommonUtils.showSnackBar(getActivity(), "shortlink click to chat wa already copied to clipboard");
            }
        });
    }


    public boolean createLink() throws UnsupportedEncodingException {
        if (TextUtils.isEmpty(phoneNumberEditText.getText())) {
            CommonUtils.showSnackBar(getActivity(), "please insert wa number");
            return false;
        }
        if (TextUtils.isEmpty(messageEditText.getText())) {
            CommonUtils.showSnackBar(getActivity(), "please insert message");
            return false;
        }
        String phoneNumber = phoneNumberEditText.getText().toString();
        String message = messageEditText.getText().toString();
        final Snackbar loading = CommonUtils.showLoadingSnackBar(getActivity(),"Please wait...");
        AndroidNetworking.post("https://api.zaitunlabs.com/genpro/v1/waapi")
                .setOkHttpClient(HttpClientUtils.getHTTPClient(getActivity(),"v1"))
                .addBodyParameter("nowa",phoneNumber)
                .addBodyParameter("message",CommonUtils.urlEncode(message))
                .setTag("linkwa")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int status = response.optInt("status");
                        String message = response.optString("message");
                        if (status == APIResponse.GENERIC_RESPONSE.OK) {
                            JSONObject data = response.optJSONObject("data");
                            String linkwa = data.optString("linkwa");
                            linkwaTextView.setText(linkwa);

                            String shortlinkwa = data.optString("linkshort");
                            linkwaShortTextView.setText(shortlinkwa);

                            CommonUtils.showSnackBar(getActivity(), "Done, click link to copy to clipboard");
                        } else {
                            CommonUtils.showSnackBar(getActivity(), message);
                        }
                        loading.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        loading.dismiss();
                        CommonUtils.showSnackBar(getActivity(), anError.getErrorDetail());
                    }
                });
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AndroidNetworking.cancel("linkwa");
    }
}
