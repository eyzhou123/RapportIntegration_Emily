package com.yahoo.inmind.rapport.view;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.middleware.events.MBRequest;
import com.yahoo.inmind.middleware.events.news.NewsResponseEvent;
import com.yahoo.inmind.control.util.Constants;
import com.yahoo.inmind.model.slingstone.ModelDistribution;
import com.yahoo.inmind.rapport.R;
import com.yahoo.inmind.rapport.control.SingletonApp;


/**
 * Created by oscarr on 1/3/15.
 */
public class ViewHelper {
    private static ViewHelper instance;
    private MessageBroker mMB;
    private Context mContext;


    /**
     * Some constants that you may define in order to decide what to do with the list of news articles ...
     */
    private static final int DECISION_ONE = 0;
    private static final int DECISION_TWO = 1;
    private static final int DECISION_THREE = 2;

    private ViewHelper(Context context) {
        // Controllers
        SingletonApp.getInstance( context );
        mContext = context;
        mMB = SingletonApp.mMB;
        mMB.subscribe( this );

        // By default, the news reader returns a list of 170 news articles. However,
        // you can decrease the size of this list by doing this:
        //MessageBroker.set( new MBRequest( Constants.SET_NEWS_LIST_SIZE, 170 ) );

        // By default, the news reader caches for 2 minutes the latest version of the
        // news articles list before triggering a new request to the Yahoo server.
        // This is defined in the midd_config.properties file. However, you can change
        // this value programmatically (in miliseconds):
        MessageBroker.set( new MBRequest( Constants.SET_REFRESH_TIME, 3 * 60 * 1000 ) );

        // By default, the news reader automatically triggers an event every hour to
        // check if there is any update in the Yahoo News server. If not, no notification
        // is sent.
        MessageBroker.set( new MBRequest( Constants.SET_UPDATE_TIME, 120 * 60 * 1000 ) );
    }

    public static ViewHelper getInstance(Context a) {
        if (instance == null) {
            instance = new ViewHelper(a);
        }
        return instance;
    }

    public static ViewHelper getInstance() {
        if (instance == null) {
            instance = new ViewHelper( null );
        }
        return instance;
    }


    // ****************************** CALLS TO THE NEWS READER *************************************

    /**
     * It opens the provided (default) news reader activity and shows the news articles
     */
    public void showDefaultReader( int option ){
        mMB.send(new MBRequest(Constants.MSG_LAUNCH_BASE_NEWS_ACTIVITY)
                .put( Constants.CONFIG_NEWS_RANKING_OPTION, option ) );
    }

    /**
     * This method opens a News Reader activity with a customized layout and UI components
     */
    public void showCustomizedReader(){
        MBRequest request = new MBRequest( Constants.MSG_LAUNCH_EXT_NEWS_ACTIVITY );
        request.put( Constants.BUNDLE_ACTIVITY_NAME, RapportReaderActivity.class.getCanonicalName());
        request.put( Constants.BUNDLE_DRAWER_MANAGER, RapportDrawerManager.class.getCanonicalName() );
        request.put( Constants.CONFIG_NEWS_RANKING_OPTION, 1 ); //Emma's server

        // Add as many UI components as you need (taken from your layout) to override the ones of
        // the news reader. Specify the id like R.id....
        request.put( Constants.UI_LANDSCAPE_LAYOUT, R.layout.rapport_list_item_flat );
        request.put( Constants.UI_PORTRAIT_LAYOUT, R.layout.rapport_news_list_item );

        // widgets
        request.put( Constants.UI_NEWS_RANK, R.id.rapport_rank);
        request.put( Constants.UI_NEWS_TITLE, R.id.rapport_title);
        request.put( Constants.UI_NEWS_REASON, R.id.rapport_reason);
        request.put( Constants.UI_NEWS_PUBLISHER, R.id.rapport_publisher);
        request.put( Constants.UI_NEWS_SCORE, R.id.rapport_score);
        request.put( Constants.UI_NEWS_IMG, R.id.rapport_img);
        request.put( Constants.UI_NEWS_SUMMARY, R.id.rapport_summary);
        request.put( Constants.UI_NEWS_FEAT, R.id.rapport_feat);
        request.put( Constants.UI_NEWS_FEAT2, R.id.rapport_feat2);
        request.put( Constants.UI_NEWS_SHARE_FB, R.id.rapport_btnShareFb);
        request.put( Constants.UI_NEWS_SHARE_TWITTER, R.id.rapport_btnShareTwitter);
        request.put( Constants.UI_NEWS_SHARE_TMBLR, R.id.rapport_btnShareTumblr);
        request.put( Constants.UI_NEWS_SHARE_MORE, R.id.rapport_btnShareMore);
        //like
        //dislike
        //comments
        //recommendation1
        //recommendation2

        mMB.send( request );
    }


    public void getModelDistributions(){
        MBRequest request = new MBRequest( Constants.MSG_GET_MODEL_DISTRIBUTIONS );
        ModelDistribution models = (ModelDistribution) mMB.get(request);

        // here goes your code... for instance:
        for(String[] model : models.getModels() ){
            Log.e("ViewHelper", "name: " + model[0] + " value: " + model[1] + " description: " + model[2] );
        }
    }



    // ****************************** EVENT HANDLERS ***********************************************

    /**
     * This event handler processes the most recent list of news articles requested by either
     * listNewsItems or listNewsItems2 methods.
     * @param event
     */
    public void onEvent(NewsResponseEvent event){
        Intent intent;
        switch (event.getQualifier()) {
            case DECISION_ONE:
                // once you have the news articles, process them. For instance:
                // 1. Iterate and add a prefix to each news item (modifyItems method)
                // 2. Set the results in the cache memory and specify an unique id for this object so
                // you can retrieve it later in your activity. For instance, here we use an arbitrary id.
//                mMB.addObjToCache(CONTENT_ID, modifyItems(event));
                // 3. Open a new activity and pass as reference the id of the cached object, that is,
                // the arbitrary id. This will work as a weak reference that allows you to
                // retrieve the results in the new activity (in this case, in your own implementation
                // of ListActivity, let's say NewsArticleList).
//                intent = new Intent(mActivity, NewsArticleList.class);
//                intent.putExtra(Constants.CONTENT, CONTENT_ID);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mActivity.getApplicationContext().startActivity(intent);
                break;
            case DECISION_TWO:
//                mMB.addObjToCache(CONTENT_ID, event.getNews());
//                intent = new Intent(mActivity, NewsArticleList.class);
//                intent.putExtra(Constants.CONTENT, CONTENT_ID);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mActivity.getApplicationContext().startActivity(intent);
                break;
            case DECISION_THREE:
                // your code goes here...
                break;
        }
    }

    // ****************************** EXTRAS ***********************************************

    /**
     * Do not implement this functionality in your code since it will kill all the processes.
     * This is just for testing purposes
     */
    public void exit(){
        mMB.destroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
