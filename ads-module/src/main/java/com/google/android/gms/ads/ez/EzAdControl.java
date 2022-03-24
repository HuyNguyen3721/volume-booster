package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics;
import com.google.android.gms.ads.ez.applovin.ApplovinUtils;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.ez.observer.MySubject;
import com.google.android.gms.ads.ez.irc.IronsourceUtils;
import com.google.android.gms.ads.ez.unity.UnityUtils;
import com.google.android.gms.ads.ez.utils.StateOption;

import java.util.ArrayList;
import java.util.List;

public class EzAdControl {

    private static EzAdControl INSTANCE;
    public static final String KEY_CLOSE_ADS = "close_ads";
    private StateOption stateOption = StateOption.getInstance();

    public static EzAdControl getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new EzAdControl(context);
        } else {
            INSTANCE.mContext = context;
        }
        return INSTANCE;
    }

    private Activity mContext;
    //    private AdFactoryListener adListener;
    private LoadAdCallback loadAdCallback;
    private ShowAdCallback showAdCallback;
    private AdChecker adChecker;
    private List<String> listAds;
    private boolean isCapping = true;

    public EzAdControl(Activity context) {
        mContext = context;
        adChecker = new AdChecker(mContext);
    }

    public static void initAd(Activity context) {
        LogUtils.logString(EzAdControl.class, "Init Ad");

        getInstance(context);


        AdmobUtils.getInstance(context).init();
        AudienceNetworkAds.initialize(context);
        IronsourceUtils.getInstance(context).init();
        UnityUtils.getInstance(context).init();
        ApplovinUtils.getInstance(context).init();
    }

    public static void initFlurry(Context context, String flurryId) {
        FlurryAnalytics.init(context, flurryId);
    }

    public EzAdControl setLoadAdCallback(LoadAdCallback callback) {
        LogUtils.logString(EzAdControl.class, "setLoadAdCallback");
        loadAdCallback = callback;
        if (!isloading()) {
            new CountDownTimer(500, 500) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if (isLoaded()) {
                        if (loadAdCallback != null) {
                            loadAdCallback.onLoaded();
                            loadAdCallback = null;
                        }
                    } else {
                        if (loadAdCallback != null) {
                            loadAdCallback.onError();
                            loadAdCallback = null;
                        }
                    }
                }
            }.start();
        }
        return this;
    }

    public EzAdControl setShowAdCallback(ShowAdCallback showAdCallback) {
        this.showAdCallback = showAdCallback;
        return this;
    }

//    public EzAdControl setAdListener(AdFactoryListener adListener) {
//        LogUtils.logString(EzAdControl.class, "setAdListener " + adListener);
//        this.adListener = adListener;
//        if (stateOption.isLoaded() && !stateOption.isLoading() && !stateOption.isShowed()) {
//            // khi o man GetStart neu da loaded roi thi khi sang splash set listener kiem tra xem co loaded chua roi thi ban ve onloaded luon
//            new CountDownTimer(1000, 1000) {
//
//                @Override
//                public void onTick(long millisUntilFinished) {
//
//                }
//
//                @Override
//                public void onFinish() {
//                    setLoadAdSuccess();
//                }
//            }.start();
//
//        }
//
//        if (!stateOption.isLoaded() && !stateOption.isLoading() && !stateOption.isShowed()) {
//            // khi o man GetStart neu da loaded roi thi khi sang splash set listener kiem tra xem co loaded chua roi thi ban ve onloaded luon
//
//            new CountDownTimer(1000, 1000) {
//
//                @Override
//                public void onTick(long millisUntilFinished) {
//
//                }
//
//                @Override
//                public void onFinish() {
//                    setLoadAdSuccess();
//                }
//            }.start();
//        }
//
//        return this;
//    }

    public void loadAd() {
        LogUtils.logString(EzAdControl.class, "Call load ad");
        if(isloading()){
            return;
        }
        loadAdx();
        loadAdmob();
        loadUnity();
        loadIrc();
        loadApplovin();
        stateOption.setOnLoading();

        new CountDownTimer(5000, 5000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                LogUtils.logString(EzAdControl.class, "Request time out adLoaded = " + isLoaded());
                if (!stateOption.isShowed()) {
                    if (isLoaded()) {
                        if (loadAdCallback != null) {
                            loadAdCallback.onLoaded();
                            loadAdCallback = null;
                        }
                    } else {
                        if (loadAdCallback != null) {
                            loadAdCallback.onError();
                            loadAdCallback = null;
                        }
                    }
                }
            }
        }.start();
    }


    private void loadUnity() {
        LogUtils.logString(EzAdControl.class, "Load Unity");
        UnityUtils.getInstance(mContext)
                .setListener(new AdFactoryListener() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Unity onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Unity loaded");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onDisplay() {
                        super.onDisplay();
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onClosed() {
                        super.onClosed();
                        setOnAdClosed();
                    }
                })
                .loadAds();
    }

    private void loadApplovin() {
        LogUtils.logString(EzAdControl.class, "Load  Applovin");
        ApplovinUtils.getInstance(mContext)
                .setListener(new AdFactoryListener() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, " Applovin onError " + ApplovinUtils.getInstance(mContext).isLoaded()+"  " + AdmobUtils.getInstance(mContext).isLoaded());
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, " Applovin loaded");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onDisplay() {
                        super.onDisplay();
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onClosed() {
                        super.onClosed();
                        setOnAdClosed();
                    }
                })
                .loadAds();
    }

    private void loadIrc() {
        LogUtils.logString(EzAdControl.class, "Load Ironsource");
        IronsourceUtils.getInstance(mContext)
                .setListener(new AdFactoryListener() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Ironsource onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Ironsource loaded");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onDisplay() {
                        super.onDisplay();
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onClosed() {
                        super.onClosed();
                        setOnAdClosed();
                    }
                })
                .loadAds();
    }

    private void loadAdmob() {
        LogUtils.logString(EzAdControl.class, "Load Admob ");
        AdmobUtils.getInstance(mContext)
                .setListener(new AdFactoryListener() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Admob onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Admob Loaded");
                        LogUtils.logString(EzAdControl.class, " Applovin onError " + ApplovinUtils.getInstance(mContext).isLoaded()+"  " + AdmobUtils.getInstance(mContext).isLoaded());
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onDisplay() {
                        super.onDisplay();
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onClosed() {
                        super.onClosed();
                        setOnAdClosed();
                    }
                })
                .loadAds();
//        LogUtils.logString(EzAdControl.class, "Load Admob " + AdmobUtils.getInstance(mContext).isLoading());
    }

    private void loadAdx() {
        LogUtils.logString(EzAdControl.class, "Load Adx");
        AdxUtils.getInstance(mContext)
                .setListener(new AdFactoryListener() {
                    @Override
                    public void onError() {
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Adx Loaded");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onDisplay() {
                        super.onDisplay();
                        LogUtils.logString(EzAdControl.class, "Adx display");
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onClosed() {
                        super.onClosed();
                        setOnAdClosed();
                    }
                })
                .loadAds();
    }


    public void showAds() {
        Message message = mHandler1.obtainMessage();
        message.arg1 = 1;
        message.sendToTarget();
    }

    public void showAdsWithoutCapping() {
        this.isCapping = false;
        showAds();
    }


    private boolean isloading() {
        return AdmobUtils.getInstance(mContext).isLoading() || ApplovinUtils.getInstance(mContext).isLoading();
    }

    private boolean isLoaded() {
        return AdmobUtils.getInstance(mContext).isLoaded() || ApplovinUtils.getInstance(mContext).isLoaded();
    }


    final Handler mHandler1 = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            showAdss();
        }
    };

    private boolean showAdss() {

        LogUtils.logString(EzAdControl.class, "Call Show Ad");
        if (isCapping) {
            if (!adChecker.checkShowAds()) {
                LogUtils.logString(EzAdControl.class, "Ad Checker false");
                setOnAdDisplayFaild();
                return false;
            }
        } else {
            isCapping = true;
        }


        listAds = new ArrayList<>();

        String[] array = AdUnit.getMasterAdsNetwork().split(",", -1);
        LogUtils.logString(EzAdControl.class, AdUnit.getMasterAdsNetwork());
        for (String s : array) {

            listAds.add(s);
        }

        showAds2();
//        loadAd();
        return false;

    }

    private boolean showAds2() {
        // neu het phan tu thi return ve 0, luc nay da load qua 4 mang nhung k show dc
        if (listAds == null || listAds.size() == 0) {
            setOnAdDisplayFaild();
            return false;
        }
        String network = listAds.get(0);

        listAds.remove(0);


        LogUtils.logString(EzAdControl.class, network);
        switch (network) {
            case "admob":
                if (AdmobUtils.getInstance(mContext).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Admob Success");
                    adChecker.setShowAds();
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "irc":
                if (IronsourceUtils.getInstance(mContext).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Irc Success");
                    adChecker.setShowAds();
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "max":
                if (ApplovinUtils.getInstance(mContext).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show applovin Success");
                    adChecker.setShowAds();
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;

            case "unity":
                if (UnityUtils.getInstance(mContext).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Unity Success");
                    adChecker.setShowAds();
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "adx":

                if (AdxUtils.getInstance(mContext).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Adx Success ");
                    adChecker.setShowAds();
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            default:
                if (showAds2()) {
                    return true;
                }
                break;
        }

        setOnAdDisplayFaild();


        return false;
    }

    public static Context getContext() {
        if (INSTANCE != null) {
            return INSTANCE.mContext;
        }
        return null;
    }


    private void setLoadAdSuccess() {


//        if (!isloading()) {
//            LogUtils.logString(EzAdControl.class, "Load ad success " + isLoaded());
//            if (isLoaded()) {
//                if (loadAdCallback != null) {
//                    loadAdCallback.onLoaded();
//                    loadAdCallback = null;
//                }
//                stateOption.setOnLoaded();
//            } else {
//                if (loadAdCallback != null) {
//                    loadAdCallback.onError();
//                    loadAdCallback = null;
//                }
//                stateOption.setOnFailed();
//            }
//        }
    }


    private void setOnAdDisplayed() {
        LogUtils.logString(EzAdControl.class, "setOnAdDisplayed ");
        if (showAdCallback != null) {
            showAdCallback.onDisplay();
        }
        stateOption.setShowAd();
    }

    private void setOnAdClosed() {
        loadAd();
        if (showAdCallback != null) {
            showAdCallback.onClosed();
            Log.e("TAG", "onFinishxx: 4");
            showAdCallback = null;
        }
        stateOption.setDismisAd();
        MySubject.getInstance().notifyChange(KEY_CLOSE_ADS);
    }

    private void setOnAdDisplayFaild() {
        if (showAdCallback != null) {
            showAdCallback.onDisplayFaild();
            Log.e("TAG", "onFinishxx: 5");
            showAdCallback = null;
        }
    }
}
