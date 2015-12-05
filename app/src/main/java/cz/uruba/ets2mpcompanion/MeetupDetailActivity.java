package cz.uruba.ets2mpcompanion;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.uruba.ets2mpcompanion.constants.URL;
import cz.uruba.ets2mpcompanion.interfaces.DataReceiver;
import cz.uruba.ets2mpcompanion.interfaces.ThemedActivity;
import cz.uruba.ets2mpcompanion.model.MeetupDetail;
import cz.uruba.ets2mpcompanion.tasks.FetchJsoupDataTask;

public class MeetupDetailActivity extends ThemedActivity implements View.OnClickListener, DataReceiver<Document> {
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.loading_progress) ProgressBar loadingProgressIndicator;
    @Bind(R.id.webview) WebView webView;

    public static final String INTENT_EXTRA_URL = "intentURL";

    private String meetupPageURL;
    private MeetupDetail meetupDetail;

    private MenuItem menuCreateReminderItem;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meetup_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ignored) {
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            meetupPageURL = extras.getString(INTENT_EXTRA_URL);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    loadingProgressIndicator.setVisibility(ProgressBar.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    loadingProgressIndicator.setVisibility(ProgressBar.GONE);

                    if (url.contains(URL.MEETUP_LIST)) {
                        if (url.equals(meetupPageURL)) {
                            String cookies = CookieManager.getInstance().getCookie(url);
                            Map<String, String> cookieMap = new HashMap<>();
                            for (String cookie : cookies.split("; ")) {
                                String[] splitCookie = cookie.split("=", 2);
                                cookieMap.put(splitCookie[0], splitCookie[1]);
                            }

                            new FetchJsoupDataTask(MeetupDetailActivity.this, url, cookieMap, false).execute();
                        } else if (url.equals(URL.MEETUP_LIST)) {
                            view.loadUrl(meetupPageURL);
                        }

                        view.loadUrl("javascript:$('#chat').toggleClass('hidden');$('.content, .form').width('100%');$('.content').css({'box-sizing': 'border-box', 'padding': '0 20px'});$('.form textarea, #chat').width('95%');$('.row').css('margin-bottom', '48px');$('.row label').css('float', 'none');$('.row small').css('margin-left', '0px');");
                    }
                }
            });

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);


            webView.loadUrl(meetupPageURL);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meetup_detail, menu);

        menuCreateReminderItem = menu.findItem(R.id.create_reminder);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void processData(Document data, boolean notifyUser) {
        Element elem_form = data.select(".form").first();

        try {
            Elements elem_data = elem_form.children();
            int iterCount = 0;
            String organiser, server, location, destination, language;
            boolean trailerRequired;
            Date meetupDate;

            organiser = server = location = destination = language = "";
            trailerRequired = false;
            meetupDate = null;

            for (Element elem : elem_data) {
                iterCount++;

                String elemContent = elem.select(".desc").first().text().trim();
                switch (iterCount) {
                    case 1:
                        organiser = elemContent;
                        break;
                    case 2:
                        server = elemContent;
                        break;
                    case 3:
                        location = elemContent;
                        break;
                    case 4:
                        destination = elemContent;
                        break;
                    case 5:
                        trailerRequired = elemContent.toLowerCase().equals("yes");
                        break;
                    case 6:
                        meetupDate = new Date(Long.parseLong(elem.select(".desc").first().attr("data-stamp")));
                        break;
                    case 7:
                        language = elemContent;
                }
            }

            meetupDetail = new MeetupDetail(organiser, server, location, destination, trailerRequired, meetupDate, language);
        } catch (Exception e) {
            return;
        }

        if (menuCreateReminderItem != null) {
            menuCreateReminderItem.setVisible(true);
        }

        Snackbar.make((View) webView.getParent(), getString(R.string.meetup_detail_webpage_notification), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.meetup_detail_webpage_notification_action), this)
                .show();
    }

    @Override
    public void handleIOException(IOException e) {

    }

    @Override
    public Date getLastUpdated() {
        return null;
    }
}