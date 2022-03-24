package com.google.android.gms.ads.ez.irc;

import android.app.Activity;

import com.google.android.gms.ads.ez.AdsFactory;
import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

public class IronsourceUtils extends AdsFactory {

    public static IronsourceUtils INSTANCE;
    public static final String APP_KEY = "13a0219d9";

    public static IronsourceUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new IronsourceUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }

    public void init() {
        IronSource.init(mContext, AdUnit.getIrcAppId());
    }


    public IronsourceUtils(Activity mContext) {
        super(mContext);
    }

    @Override
    public void loadAds() {


        if (stateOption.isLoading()) {
            // neu dang loading  thi k load nua
        } else if (stateOption.isLoaded()) {
            // neu da loaded thi goi callback luon
            if (mListener != null) {
                mListener.onLoaded();
            }
        } else {
            // neu k loading cung k loaded thi goi ham load ads va dat loading = true

            LogUtils.logString(IronsourceUtils.class, "Load Ironsource: Start Loading ");
            stateOption.setOnLoading();

            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    LogUtils.logString(IronsourceUtils.class, "Ironsource loaded");
                    stateOption.setOnLoaded();
                    if (mListener != null) {
                        mListener.onLoaded();
                    }
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError error) {
                    LogUtils.logString(IronsourceUtils.class, "Ironsource Failed");
                    stateOption.setOnFailed();
                    if (mListener != null) {
                        mListener.onError();
                    }
                }

                @Override
                public void onInterstitialAdOpened() {

                    LogUtils.logString(IronsourceUtils.class, "Ironsource onInterstitialAdOpened");
                }

                @Override
                public void onInterstitialAdClosed() {
                    stateOption.setDismisAd();
                    // ad has been closed
                    LogUtils.logString(IronsourceUtils.class, "Ironsource Closed");
//                    if (mListener != null) {
//                        mListener.onClosed();
//                    }
//                    LogUtils.logString(IronsourceUtils.class, "Call Reload EzAd");
//                    EzAdControl.getInstance(mContext).loadAd();
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError error) {
                    LogUtils.logString(IronsourceUtils.class, "Ironsource onDisplayFaild");
//                    if (mListener != null) {
//                        mListener.onDisplayFaild();
//                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    // ad has showed
                    stateOption.setShowAd();
                    LogUtils.logString(IronsourceUtils.class, "Ironsource onInterstitialAdShowSucceeded");
//                    if (mListener != null) {
//                        mListener.onDisplay();
//                    }
                }
            });

            IronSource.loadInterstitial();
        }


    }

    @Override
    public boolean showAds() {

        LogUtils.logString(IronsourceUtils.class, "showAds " +   "  " + mContext + "  " + (mContext instanceof Activity) + "   " + IronSource.isInterstitialReady());
        if (stateOption.isLoaded() && mContext != null && IronSource.isInterstitialReady()) {
            IronSource.showInterstitial();
            return true;
        }

        return false;
    }
}
