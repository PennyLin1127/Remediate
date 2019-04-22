package tw.net.pic.mobi.ui_old.news;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import org.owasp.encoder.Encode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;

import pic.ibon.api.ApiCall;
import pic.ibon.api.ApiHandle;
import pic.ibon.api.api_ibon_v2.model.response.C31001_get_imm_serial_num.ImmGetSerialNum;
import pic.ibon.api.api_ibon_v2.model.response.FPT001_get_free_print_desc.FreePrintDesc;
import pic.ibon.api.api_ibon_v2.model.response.Y31002_get_activity_page.ActivityGetPage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tw.net.pic.mobi.BaseActivity;
import tw.net.pic.mobi.BuildConfig;
import tw.net.pic.mobi.R;
import tw.net.pic.mobi.alertdialog.CommDialog;
import tw.net.pic.mobi.api.ApiHandleV2Impl;
import tw.net.pic.mobi.api.ApiHelper;
import tw.net.pic.mobi.data.MyConfig;
import tw.net.pic.mobi.gopage.GoPage;
import tw.net.pic.mobi.tool.GlobalClass;
import tw.net.pic.mobi.tool.MyLog;
import tw.net.pic.mobi.tool.MyTestApi;
import tw.net.pic.mobi.tool.MyWebViewClient;
import tw.net.pic.mobi.ui_old.camera.model.FreePrintDTO;
import tw.net.pic.mobi.view.Title;
import tw.net.pic.mobi.view.WebTitle;

import static tw.net.pic.mobi.unorganized.MobiWebView.stripHtml;


public class NewsMore extends BaseActivity implements WebTitle.CallBack {

    public static final String INTENT_KEY_URL = "Key_Web";
	public static final String INTENT_KEY_TITLE_TYPE = "key_title_type";
	public static final String INTENT_KEY_TITLE_ADD_SHARE = "key_title_add_share";
	public static final String INTENT_KEY_MEMBER_BENEFIT_DATA = "key_member_benefit_data";
	public static final String TITLE_TYPE_FULL = "title_type_full";
	public static final String TITLE_TYPE_IBON = "title_type_ibon";

    //region Api 取得公版活動頁 用了很多HardCode的字串
    private static final String API_EVENT_TYPE_MEMBER_BENEFIT   = "MemberBenefits";
    private static final String API_EVENT_TYPE_EC_QRCODE        = "QRCMdd";
    private static final String API_CONTROL_KIND_BUTTON         = "button";
    private static final String API_CONTROL_KIND_LEBEL          = "label";
    private static final String API_CONTROL_COLOR_BLACK         = "black";
    private static final String API_CONTROL_COLOR_RED           = "red";
    private static final String API_CONTROL_COLOR_BLUE          = "blue";
    private static final String API_CONTROL_COLOR_GREEN         = "green";
    private static final String API_CONTROL_COLOR_ORANGE        = "orange";
    private static final String API_NEXT_API_ID_C31001          = "C31001";
    //endregion


    //region 字串
    private static final String ERROR_FIELD = "欄位錯誤";
    //endregion


//    private String mTitleType;
	// 是否加上share (用別的APP開網頁的意思)
//	private boolean mIsTitleAddShare;

	private WebView view_web;
    private String mSaveUrl;
	private String invoiceWinUrl;
	private Title ibonTitle;// 之後會有另一種title給webview用(intent切換)
	private WebTitle mWebTitle;
	private View mBottomBar;
	private Button mBtnNext;
	private TextView mTvNext;


	//region EC QRCode/會員獨享/免費列印
    private boolean mIsEcQRCode;
    private boolean mIsMemberBenefit;
	private String mEcQRCodeProUrl;
	private String mEcQRCodeDevUrl;
	private String mMemberBenefitData;
    private String mImmEventType;
    private String mImmEventData;
    private String mFreePrintActivityId;
	//endregion


	// Api 取得公版活動頁
	private ApiCall<ActivityGetPage> mApiGetActivityPage;

	// Api 取得iMM序號
	private ApiCall<ImmGetSerialNum> mApiGetImmSerialNum;

    // Api 免費列印說明
    private ApiCall<FreePrintDesc> mApiGetFreePrintDesc;

    // Api Handler 取得公版活動頁
    private ApiHandleV2Impl<ActivityGetPage> mApiHandlerActivityPage;

    // Api Handler 取得iMM序號
    private ApiHandleV2Impl<ImmGetSerialNum> mApiHandlerImmSerialNum;

    // Api Handler 免費列印說明
    private ApiHandleV2Impl<FreePrintDesc> mApiHandlerFreePrintDesc;

    // 目前api要登入的是誰
    private static final int LOGIN_API_NONE                     = 0;
    private static final int LOGIN_API_GET_PUBLIC_ACTIVITY_PAGE = 1;
    private static final int LOGIN_API_GET_IMM_SERIAL_NUM       = 2;
    private static final int LOGIN_API_GET_FREE_PRINT_DESC      = 3;
    private int mCurrentLoginApi = LOGIN_API_NONE;



	@Nullable
	@Override
	public GAModel getGAModel() {
		GAModel model = new GAModel();
		model.setScreenName(MyConfig.GA_SCREEN_UNKNOWN);
		model.setCategory(MyConfig.GA_CATEGORY_UNKNOWN);
		return model;
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_more);
		// init view
		view_web  = findViewById(R.id.web);
		ibonTitle = findViewById(R.id.ibonTitle);
		mWebTitle = findViewById(R.id.webTitle);
        mBottomBar = findViewById(R.id.bottomBar);
        mBtnNext = findViewById(R.id.next_btn);
        mTvNext = findViewById(R.id.next_txt);

		ibonTitle.getBtnBack().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mWebTitle.setCallBack(this);

		invoiceWinUrl = BuildConfig.InvoiceWinURL;

		mEcQRCodeProUrl = getString(R.string.EcQrcodeURL_pro);
		mEcQRCodeDevUrl = getString(R.string.EcQrcodeURL_dev);

		// 從Intent取值
		String url = getIntent().getStringExtra(INTENT_KEY_URL);
		String titleType = getIntent().getStringExtra(INTENT_KEY_TITLE_TYPE);
		boolean isTitleAddShare = getIntent().getBooleanExtra(INTENT_KEY_TITLE_ADD_SHARE, false);
		mMemberBenefitData = getIntent().getStringExtra(INTENT_KEY_MEMBER_BENEFIT_DATA);

        MyLog.log("NewsMore... mMemberBenefitData = " + mMemberBenefitData);

        //測試攻擊用url
//		url = "https://xss-game.appspot.com/level1/frame?query=468<s<script>cript>alert()</expression()script>132&test=9we8f49&tt2=tt448";

        // 製作安全的url
        if (!TextUtils.isEmpty(url)) {
            mSaveUrl = createSafeUrl(url);
        } else {
            mSaveUrl = "";
        }


		// 設定Api處理
        settingApiGetActivityPage();
        settingApiGetImmSerialNum();
        settingApiGetFreePrintDesc();


		if (!TextUtils.isEmpty(titleType) && titleType.equals(TITLE_TYPE_FULL)) {
			mWebTitle.setVisibility(View.VISIBLE);
			ibonTitle.setVisibility(View.GONE);
		}

		if (isTitleAddShare) {
            addShareBtn();
        }

		MyLog.log("NewsMore... url = " + url);

		view_web.setInitialScale(60);
		view_web.getSettings().setSupportZoom(true);
		view_web.getSettings().setBuiltInZoomControls(true);
		view_web.getSettings().setJavaScriptEnabled(true);
		view_web.getSettings().setDomStorageEnabled(true);
		view_web.getSettings().setUseWideViewPort(true);
		view_web.setWebViewClient(new HelloWebViewClient());

		// 創建WebViewChromeClient
		WebChromeClient wvcc = new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
									 final JsResult result) {
				new CommDialog(NewsMore.this)
						.setMessage(message)
						.setPositiveText(R.string.nnr_ok2)
						.setOnPositiveListener(new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						})
						.show();

				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
									   String message, final JsResult result) {
				new CommDialog(NewsMore.this)
						.setMessage(message)
						.setPositiveText(R.string.nnr_ok2)
						.setOnPositiveListener(new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						})
						.setNegativeText(R.string.cancel)
						.setOnNegativeListener(new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						})
						.show();

				return true;
			}



		};
		view_web.setWebChromeClient(wvcc);


		if (MyTestApi.isShowEcQRcode) {
			// 11月版需求
			convertForEcQRCode(mSaveUrl);
		} else {
			view_web.loadUrl(mSaveUrl);
		}

	}


    private static String createSafeUrl(String rawUrl) {
        String urlRoot = getPathFromUrl(rawUrl);
        HashMap<String, String> qMap = getQueryString(rawUrl);
        return getSafeUrl(urlRoot, qMap);
    }


    private void addShareBtn() {
        ibonTitle.getBtnRight().setVisibility(View.VISIBLE);
        ibonTitle.setBtnRightType(Title.TYPE_UPLOAD);
        ibonTitle.getBtnRight().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(mSaveUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Create and start the chooser
                Intent chooser = Intent.createChooser(intent, "使用其他的瀏覽器開啟");
                startActivity(chooser);
            }
        });
    }


    private void hideShareBtn() {
        ibonTitle.getBtnRight().setVisibility(View.GONE);
    }


	/**
	 * 遇到Ec Qrcode的用header傳東西過去
	 * https://droidyue.com/blog/2014/07/12/load-url-with-extra-header-in-android-webview-chinese-edition/
	 */
	private void convertForEcQRCode(String url) {
		if (url != null &&
				(url.regionMatches(true, 0, mEcQRCodeProUrl, 0, mEcQRCodeProUrl.length())
						|| url.regionMatches(true, 0, mEcQRCodeDevUrl, 0, mEcQRCodeDevUrl.length()))) {
            mIsEcQRCode = true;
            // 送GA
            sendGAScreen();
            callApiGetActivityPage(url, null);

		} else if (!TextUtils.isEmpty(mMemberBenefitData)) {
            mIsMemberBenefit = true;
            // 送GA
            sendGAScreen();
            callApiGetActivityPage(null, mMemberBenefitData);

		} else {
			view_web.loadUrl(url);
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebTitle.onDestroy();
		cancelCall(mApiGetActivityPage);
		cancelCall(mApiGetImmSerialNum);
		cancelCall(mApiGetFreePrintDesc);
		detachApiHandle(mApiHandlerActivityPage);
		detachApiHandle(mApiHandlerImmSerialNum);
		detachApiHandle(mApiHandlerFreePrintDesc);
	}


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 送GA
        sendGABack();
    }


    //region Y31002 取得公版活動頁
    /**
     * 設定Api 處理: 取得公版活動頁
     */
	private void settingApiGetActivityPage() {
	    mApiHandlerActivityPage = new ApiHandleV2Impl<>();
	    mApiHandlerActivityPage.attachView(this);
        mApiHandlerActivityPage.setBodyBehaviorSuccess(new ApiHandle.ApiBodyBehavior<ActivityGetPage>() {
            @Override
            public void processApiBody(@NonNull ActivityGetPage body, int httpCode) {
                ActivityGetPage.Result result = body.getResult();
                if (result != null) {
                    final String alertMsg = result.getAlertMsg();
                    int actionType = result.getActionType();
                    ActivityGetPage.Action1Data action1 = result.getAction1Data();
                    ActivityGetPage.Action2Data action2 = result.getAction2Data();
                    ActivityGetPage.Action3Data action3 = result.getAction3Data();
                    String eventName = result.getEventName();
                    if (!TextUtils.isEmpty(alertMsg)) {
                        // 無活動, Alert message 之後去會員活動頁
                        createCommDialog()
                                .setCancelable(false)
                                .setMessage(alertMsg)
                                .setPositiveText(R.string.nnr_ok2)
                                .setOnPositiveListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendGaAlert(R.string.nnr_ok2, alertMsg);
                                        GoPage.getInstance().handleGoPage(NewsMore.this, MyConfig.MYB_11_P00, 0, null, null, null, null);
                                        finish();
                                    }
                                })
                                .show();

                    } else if (actionType == 1 && action1 != null) {
                        // IMM: 顯示WebView 按鈕有動作
                        // Title沒有分享
                        hideShareBtn();
                        processImmPage(action1, eventName);

                    } else if (actionType == 2 && action2 != null) {
                        // URL 轉址
                        // Title有分享
                        addShareBtn();
                        String url = action2.getUrl();
                        if (!TextUtils.isEmpty(url)) {
                            view_web.loadUrl(createSafeUrl(url));
                        } else {
                            // 欄位錯誤
                            showMessage(ERROR_FIELD, false, mClickFinish);
                        }

                    } else if (actionType == 3 && action3 != null) {
                        // FPT001
                        // Title沒有分享
                        hideShareBtn();
                        mFreePrintActivityId = action3.getActivityId();
                        if (!TextUtils.isEmpty(mFreePrintActivityId)) {
                            processFreePrintPage(mFreePrintActivityId);
                        } else {
                            // 欄位錯誤
                            showMessage(ERROR_FIELD, false, mClickFinish);
                        }

                    } else {
                        // 欄位錯誤
                        showMessage(ERROR_FIELD, false, mClickFinish);
                    }

                } else {
                    // 欄位錯誤
                    showMessage(ERROR_FIELD, false, mClickFinish);
                }
            }
        });
    }


    /**
     * 處理公版活動頁中, IMM的情形
     * @param data  IMM頁面資料
     */
    private void processImmPage(@NonNull ActivityGetPage.Action1Data data, @Nullable final String eventName) {
        String controlKind              = data.getControlKind();
        final boolean controlEnable     = data.isControlEnable();
        final String controlText              = data.getControlText();
        String eventContent             = data.getEventContent();
        final String nextApiId          = data.getNextApiId();
        mImmEventType                   = data.getEventType();
        mImmEventData                   = data.getEventData();

        // 送GA
        snedGaScanTrigger(eventName);

        int resColor = createColor(data.getControlColor());

        // 設定button/label
        mBottomBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(controlKind)) {
            if (controlKind.equalsIgnoreCase(API_CONTROL_KIND_BUTTON)) {
                mBtnNext.setVisibility(View.VISIBLE);
                mTvNext.setVisibility(View.GONE);
                mBtnNext.setEnabled(controlEnable);
                mBtnNext.setText(controlText);

            } else if (controlKind.equalsIgnoreCase(API_CONTROL_KIND_LEBEL)) {
                mBtnNext.setVisibility(View.GONE);
                mTvNext.setVisibility(View.VISIBLE);
                mTvNext.setTextColor(resColor);
                mTvNext.setText(controlText);

            } else {
                mBtnNext.setVisibility(View.GONE);
                mTvNext.setVisibility(View.GONE);
            }

        } else {
            mBtnNext.setVisibility(View.GONE);
            mTvNext.setVisibility(View.GONE);
        }

        // 設定WebView
        if (!TextUtils.isEmpty(eventContent)) {
            view_web.loadData(
                    stripHtml(Encode.forHtml(eventContent)),
                    "text/html; charset=UTF-8", null);
        }

        // 設定按鈕的動作
        mBtnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlEnable) {
                    sendGaBtn(controlText, eventName);
                    if (!TextUtils.isEmpty(nextApiId)) {
                        switch (nextApiId) {
                            case API_NEXT_API_ID_C31001:
                                // call api C31001
                                if (!TextUtils.isEmpty(mImmEventType) && !TextUtils.isEmpty(mImmEventData)) {
                                    callApiGetImmSerialNum(mImmEventType, mImmEventData);
                                } else {
                                    // 欄位錯誤
                                    showMessage(ERROR_FIELD, false, mClickFinish);
                                }
                                break;
                        }
                    }

                }
            }
        });

    }


    private int createColor(String controlColor) {
        if (!TextUtils.isEmpty(controlColor)) {
            switch (controlColor) {
                case API_CONTROL_COLOR_BLACK:
                    return Color.parseColor("#3E3A39");
                case API_CONTROL_COLOR_RED:
                    return Color.parseColor("#D0021B");
                case API_CONTROL_COLOR_BLUE:
                    return Color.parseColor("#0078FF");
                case API_CONTROL_COLOR_GREEN:
                    return Color.parseColor("#8EC31E");
                case API_CONTROL_COLOR_ORANGE:
                    return Color.parseColor("#EC6C00");
            }
        }
        return Color.parseColor("#3E3A39");
    }


    /**
     * 處理公版活動頁中, FreePrint的情形
     * @param activityId    免費列印活動ID
     */
    private void processFreePrintPage(@NonNull String activityId) {
        // 呼叫api 免費列印說明
        callApiGetFreePrintDesc(activityId);
    }


    /**
     * 呼叫Api 取得公版活動頁
     * @param url               EC QRCode 要帶入的參數
     * @param memberBenefitData 會員獨享 要帶入的參數
     */
    private void callApiGetActivityPage(String url, String memberBenefitData) {
        mCurrentLoginApi = LOGIN_API_GET_PUBLIC_ACTIVITY_PAGE;
	    String eventType = null;
	    String eventData = null;
        if (!TextUtils.isEmpty(url)) {
            eventType = API_EVENT_TYPE_EC_QRCODE;
            eventData = url;
        } else if (!TextUtils.isEmpty(memberBenefitData)) {
            eventType = API_EVENT_TYPE_MEMBER_BENEFIT;
            eventData = memberBenefitData;
        }

        if (!TextUtils.isEmpty(eventType) && !TextUtils.isEmpty(eventData)) {
            showProgress(true);
            cancelCall(mApiGetActivityPage);
            mApiGetActivityPage = ApiHelper.getInstance(this).getApi().getIbonApiV2().getActivityPage(eventType, eventData);
            mApiGetActivityPage.enqueue(new Callback<ActivityGetPage>() {
                @Override
                @ParametersAreNonnullByDefault
                public void onResponse(Call<ActivityGetPage> call, Response<ActivityGetPage> response) {
                    showProgress(false);
                    mApiHandlerActivityPage.processOnResponse(response.body(), response.code());
                }

                @Override
                @ParametersAreNonnullByDefault
                public void onFailure(Call<ActivityGetPage> call, Throwable t) {
                    showProgress(false);
                    mApiHandlerActivityPage.processOnError(t);
                }
            });
        }

    }
    //endregion


    //region C31001 取得iMM序號
    private void settingApiGetImmSerialNum() {
        mApiHandlerImmSerialNum = new ApiHandleV2Impl<>();
        mApiHandlerImmSerialNum.attachView(this);
        mApiHandlerImmSerialNum.setBodyBehaviorSuccess(new ApiHandle.ApiBodyBehavior<ImmGetSerialNum>() {
            @Override
            public void processApiBody(@NonNull ImmGetSerialNum body, int httpCode) {

                ImmGetSerialNum.Result result = body.getResult();
                if (result != null) {
                    String alertTitle           = result.getAlertTitle();
                    String alertMsg             = result.getAlertMsg();
                    final String featureId      = result.getFeatureId();
                    final String gaAlertMessage = alertMsg;

                    if (TextUtils.isEmpty(alertMsg)) {
                        alertMsg = "無訊息內容";
                    }
                    boolean isShowTitle = !TextUtils.isEmpty(alertTitle);
                    boolean isTwoButton = !TextUtils.isEmpty(featureId);

                    CommDialog commDialog = createCommDialog();
                    commDialog.setCancelable(false);
                    if (isShowTitle) {
                        commDialog.setTitle(alertTitle);
                    }
                    commDialog.setMessage(alertMsg);
                    if (isTwoButton) {
                        commDialog.setPositiveText(R.string.ECQRCode_go_ticket);
                        commDialog.setNegativeText(R.string.close);
                        commDialog.setOnPositiveListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendGaAlert(R.string.ECQRCode_go_ticket, gaAlertMessage);
                                GoPage.getInstance().handleGoPage(NewsMore.this, featureId, 0, null, null, null, null);
                                finish();
                            }
                        });
                        commDialog.setOnNegativeListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendGaAlert(R.string.close, gaAlertMessage);
                                finish();
                            }
                        });

                    } else {
                        commDialog.setPositiveText(R.string.confirm);
                        commDialog.setOnPositiveListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendGaAlert(R.string.confirm, gaAlertMessage);
                                finish();
                            }
                        });
                    }
                    commDialog.show();

                } else {
                    // 欄位錯誤
                    showMessage(ERROR_FIELD, false, mClickFinish);
                }
            }
        });
    }


    private void callApiGetImmSerialNum(String eventType, String eventData) {
        mCurrentLoginApi = LOGIN_API_GET_IMM_SERIAL_NUM;
        showProgress(true);
        cancelCall(mApiGetImmSerialNum);
        mApiGetImmSerialNum = ApiHelper.getInstance(this).getApi().getIbonApiV2().getImmSerialNum(eventType, eventData);
        mApiGetImmSerialNum.enqueue(new Callback<ImmGetSerialNum>() {
            @Override
            @ParametersAreNonnullByDefault
            public void onResponse(Call<ImmGetSerialNum> call, Response<ImmGetSerialNum> response) {
                showProgress(false);
                mApiHandlerImmSerialNum.processOnResponse(response.body(), response.code());
            }

            @Override
            @ParametersAreNonnullByDefault
            public void onFailure(Call<ImmGetSerialNum> call, Throwable t) {
                showProgress(false);
                mApiHandlerImmSerialNum.processOnError(t);
            }
        });
    }
    //endregion


    //region FPT001 免費列印說明
    /**
     * 設定Api 處理: 免費列印說明
     */
    private void settingApiGetFreePrintDesc() {
        mApiHandlerFreePrintDesc = new ApiHandleV2Impl<>();
        mApiHandlerFreePrintDesc.attachView(this);
    }


    private class MyFreePrintSuccess implements ApiHandle.ApiBodyBehavior<FreePrintDesc> {
        private String mActivityId;

        MyFreePrintSuccess(String activityId) {
            this.mActivityId = activityId;
        }

        @Override
        public void processApiBody(@NonNull FreePrintDesc body, int httpCode) {
            final FreePrintDesc.Result result = body.getResult();
            if (result != null) {
                mBottomBar.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
                mTvNext.setVisibility(View.GONE);

                view_web.loadData(
                        stripHtml(Encode.forHtml(result.getEventContent())),
                        "text/html; charset=UTF-8", null);
                mBtnNext.setText(result.getButtonText());
                mBtnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String eventTypeFromApi         = result.getEventType();
                        int    uploadLimitFromApi       = result.getUploadLimit();
                        String postcardButtonIdFromApi  = result.getPostcardButtonId();
                        String postcardBrandIdFromApi   = result.getPostcardBrandId();
                        String functionIdFromApi        = result.getFunctionId();


                        FreePrintDTO freePrintDTO = new FreePrintDTO();
                        freePrintDTO.setActivityId(mActivityId);

                        if (uploadLimitFromApi > 0) {
                            freePrintDTO.setMaxCount(uploadLimitFromApi);
                        }

                        if (!TextUtils.isEmpty(functionIdFromApi)) {
                            freePrintDTO.setFunctionId(functionIdFromApi);
                        }

                        if (!TextUtils.isEmpty(postcardButtonIdFromApi)) {
                            freePrintDTO.setPostcardButtonId(postcardButtonIdFromApi);
                        }

                        if (!TextUtils.isEmpty(postcardBrandIdFromApi)) {
                            freePrintDTO.setPostcardBrandId(postcardBrandIdFromApi);
                        }

                        switch (result.getEventStatus()) {
                            // 1: 尚有餘額
                            case "1":

                                if (!TextUtils.isEmpty(eventTypeFromApi)) {
                                    switch (eventTypeFromApi) {
                                        case "1": {
                                            // 檔案上傳
                                            GoPage.getInstance().GotoPrintImage(NewsMore.this, null, freePrintDTO);
                                            finish();
                                        }
                                        break;
                                        case "2": {
                                            // 卡片列印
                                            GoPage.getInstance().GotoPrintCardAll(NewsMore.this, 0, null, freePrintDTO,
                                                    postcardButtonIdFromApi, postcardBrandIdFromApi);
                                            finish();
                                        }
                                        break;
                                    }
                                }
                                break;
                            // 2: 活動期間不符
                            // 3: 已無活動總額
                            // 4: 已無今日額度
                            case "2":
                            case "3":
                            case "4":

                                if (!TextUtils.isEmpty(eventTypeFromApi)) {
                                    switch (eventTypeFromApi) {
                                        case "1":
                                            // 檔案上傳
                                            GoPage.getInstance().GotoPrintImage(NewsMore.this, null, null);
                                            finish();
                                            break;
                                        case "2":
                                            // 卡片列印
                                            GoPage.getInstance().GotoPrintCardAll(NewsMore.this, 0, null, null
                                                    , postcardButtonIdFromApi, postcardBrandIdFromApi);
                                            finish();
                                            break;
                                    }
                                }
                                break;
                        }

                    }
                });

            } else {
                // 欄位錯誤
                showMessage(ERROR_FIELD, false, mClickFinish);
            }
        }
    }


    /**
     * 呼叫Api 免費列印說明
     * @param activityId 活動Id
     */
    private void callApiGetFreePrintDesc(final String activityId) {
        mCurrentLoginApi = LOGIN_API_GET_FREE_PRINT_DESC;
        showProgress(true);
        cancelCall(mApiGetFreePrintDesc);
        mApiGetFreePrintDesc = ApiHelper.getInstance(this).getApi().getIbonApiV2().getFreePrintDesc(activityId);
        mApiGetFreePrintDesc.enqueue(new Callback<FreePrintDesc>() {
            @Override
            @ParametersAreNonnullByDefault
            public void onResponse(Call<FreePrintDesc> call, Response<FreePrintDesc> response) {
                showProgress(false);
                mApiHandlerFreePrintDesc.setBodyBehaviorSuccess(new MyFreePrintSuccess(activityId));
                mApiHandlerFreePrintDesc.processOnResponse(response.body(), response.code());
            }

            @Override
            @ParametersAreNonnullByDefault
            public void onFailure(Call<FreePrintDesc> call, Throwable t) {
                showProgress(false);
                mApiHandlerFreePrintDesc.processOnError(t);
            }
        });
    }
    //endregion


    //region 送GA
    private void sendGAScreen() {
        try {
            if (mIsEcQRCode) {
                GlobalClass.sendGAScreen(MyConfig.GA_SCREEN_EC_QRCODE);
            } else if (mIsMemberBenefit) {
                GlobalClass.sendGAScreen(MyConfig.GA_SCREEN_MEMBER_BENEFIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendGABack() {
        try {
            if (mIsEcQRCode) {
                GlobalClass.sendGABack(MyConfig.GA_SCREEN_EC_QRCODE);
            } else if (mIsMemberBenefit) {
                GlobalClass.sendGABack(MyConfig.GA_SCREEN_MEMBER_BENEFIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendGaAlert(@StringRes int btnStr, String alertMsg) {
        try {
            String str = getString(btnStr);
            if (mIsEcQRCode) {
                GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_EC_QRCODE, MyConfig.GA_ACTION_EC_QRCODE_CLICK_ALERT + str, alertMsg, 0, false);
            } else if (mIsMemberBenefit) {
                GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_MEMBER_BENEFIT, MyConfig.GA_ACTION_EC_QRCODE_CLICK_ALERT + str, alertMsg, 0, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendGaBtn(String btnText, String eventName) {
        // 送GA-Button Click
        try {
            if (!TextUtils.isEmpty(btnText) && !TextUtils.isEmpty(eventName)) {
                if (mIsEcQRCode) {
                    GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_EC_QRCODE, MyConfig.GA_ACTION_EC_QRCODE_CLICK_BUTTON + btnText, eventName, 0, false);
                } else if (mIsMemberBenefit) {
                    GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_MEMBER_BENEFIT, MyConfig.GA_ACTION_EC_QRCODE_CLICK_BUTTON + btnText, eventName, 0, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void snedGaScanTrigger(String eventName) {
        try {
            if (!TextUtils.isEmpty(eventName)) {
                if (mIsEcQRCode) {
                    GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_SCAN_TRIGGER, MyConfig.GA_SCAN_TRIGGER_EC_QRCODE + "_" + eventName);
                } else if (mIsMemberBenefit) {
                    GlobalClass.sendGAEvent(MyConfig.GA_CATEGORY_SCAN_TRIGGER, MyConfig.GA_SCAN_TRIGGER_MEMBER_BENEFIT + "_" + eventName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion


	@Override
	protected void onUserDoNotWantGoToSettings() {
        finish();
	}

	@Override
	protected void onBackFromSettings() {
        finish();
	}

	@Override
	protected void onBackFromLoginSuccess() {
        switch (mCurrentLoginApi) {
            case LOGIN_API_GET_PUBLIC_ACTIVITY_PAGE:
                if (!TextUtils.isEmpty(mSaveUrl) || !TextUtils.isEmpty(mMemberBenefitData)) {
                    // 兩者擇一
                    callApiGetActivityPage(mSaveUrl, mMemberBenefitData);
                }
                break;
            case LOGIN_API_GET_IMM_SERIAL_NUM:
                if (!TextUtils.isEmpty(mImmEventType) && !TextUtils.isEmpty(mImmEventData)) {
                    // 兩者皆要
                    callApiGetImmSerialNum(mImmEventType, mImmEventData);
                }
                break;
            case LOGIN_API_GET_FREE_PRINT_DESC:
                if (!TextUtils.isEmpty(mFreePrintActivityId)) {
                    callApiGetFreePrintDesc(mFreePrintActivityId);
                }
                break;
        }
	}

	@Override
	protected void onBackFromLoginCancel() {
        finish();
	}

	public static String getPathFromUrl(String url) {
		return url.split("\\?")[0];
	}

	public static HashMap<String, String> getQueryString(String url) {
		Uri uri= Uri.parse(url);

		HashMap<String, String> map = new HashMap<>();
		for (String paramName : uri.getQueryParameterNames()) {
			if (paramName != null) {
				String paramValue = uri.getQueryParameter(paramName);
				if (paramValue != null) {
					map.put(paramName, paramValue);
				}
			}
		}
		return map;
	}

	public static String getSafeUrl(String urlRoot, Map<String, String> qMap){
		Uri.Builder uri = Uri.parse(urlRoot).buildUpon();
		for (Map.Entry<String, String> entry : qMap.entrySet())
		{
			String safeParam = stripXSS(entry.getValue());
			qMap.put(entry.getKey(), safeParam);

			uri.appendQueryParameter(entry.getKey(), safeParam);
		}
		String safeUrl = uri.build().toString();
		String localUrl = "about:blank";
        if (safeUrl.startsWith("file://*")) {
            safeUrl = localUrl;
        }
		return safeUrl;
	}

	private static String stripXSS(String value) {
		if (value != null) {
			// NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
			// avoid encoded attacks.
			// value = ESAPI.encoder().canonicalize(value);

			// Avoid null characters
			value = value.replaceAll("", "");

			// Avoid anything between script tags
			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid anything in a src='...' type of expression
			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Remove any lonesome </script> tag
			scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Remove any lonesome <script ...> tag
			scriptPattern = Pattern.compile("<(.*?)s(.*?)c(.*?)r(.*?)i(.*?)p(.*?)t(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid eval(...) expressions
			scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid expression(...) expressions
			scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid javascript:... expressions
			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid vbscript:... expressions
			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid onload= expressions
			scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");
		}
		return value;
	}


	private class HelloWebViewClient extends MyWebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showProgress(true);
			if (mWebTitle.getVisibility() == View.VISIBLE) {
				mWebTitle.setWebName(view.getTitle());
				mWebTitle.setWebUrl(url);
			}
		}

		public void onReceivedError(WebView view, int errorCode,
									String description, String failingUrl) {
            showProgress(false);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
            showProgress(false);
            if (!mIsEcQRCode && !mIsMemberBenefit) {
                // 非EC QRCode 也 非會員獨享
                processInvoice(view, url);
            }

			if (mWebTitle.getVisibility() == View.VISIBLE) {
				mWebTitle.setWebName(view.getTitle());
				mWebTitle.setWebUrl(url);
			}

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			MyLog.log("shouldOverrideUrlLoading, url = " + url);
			return (GlobalClass.processShouldOverrideUrlLoading(NewsMore.this, view, url));
		}

	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && view_web.canGoBack()) {
			view_web.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void processInvoice(WebView view, String url) {
		if (view != null && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(invoiceWinUrl)) {
			if (url.equals(invoiceWinUrl)) {
				String inject = hideHeaderInvoiceWin();
				view.loadUrl(inject);
			}
		}
	}

	private String hideHeaderInvoiceWin() {
		return
				"javascript:(function() { " +
						"  if ($('header.handle')[0]) {" +
						"    $('header.handle')[0].remove();" +
						"  }" +
						"})()";
	}

	@Override
	public void onWebGoBack() {
		if (view_web.canGoBack()) {
			view_web.goBack();
			return;
		}
		onBackPressed();
	}

	@Override
	public void onWebGoForward() {
		if (view_web.canGoForward()) {
			view_web.goForward();
		}
	}

	@Override
	public void onWebClose() {
		finish();
	}
}
