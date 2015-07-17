package com.yahoo.inmind.rapport.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.yahoo.inmind.control.i13n.I13N;
import com.yahoo.inmind.control.reader.ReaderController;
import com.yahoo.inmind.model.i13n.Event;
import com.yahoo.inmind.rapport.R;
import com.yahoo.inmind.view.reader.DrawerItem;
import com.yahoo.inmind.view.reader.DrawerManager;

import java.util.ArrayList;

public class RapportDrawerManager extends DrawerManager{

	private static int[] drawer_icons = {R.drawable.ic_launcher, R.drawable.login_square,
        R.drawable.assistant_square, R.drawable.news_square, R.drawable.both_square, R.drawable.camera_square };

	public RapportDrawerManager(Context act) {
		super(act);
		Log.e("", "inside RapportDrawerManager - Context");
	}

	public RapportDrawerManager(RapportReaderActivity act) {
		super(act);
		Log.e("", "inside RapportDrawerManager - RapportReaderActivity");
	}

	public static RapportDrawerManager getInstance( Context context ) {
		if( context != null ) {
			if (instance == null) {
				if (context instanceof RapportReaderActivity) {
					instance = new RapportDrawerManager((RapportReaderActivity) context);
				} else {
					instance = new RapportDrawerManager(context);
				}
			} else {
				if (context instanceof RapportReaderActivity) {
					instance = new RapportDrawerManager((RapportReaderActivity) context);
				}
			}
		}
		return (RapportDrawerManager)instance;
	}


	@Override
	public void initializeBase( Context context ){
		super.initializeBase( context );

		DrawerItem drawerItem = new DrawerItem();
        drawerItem.name = "Assistant View";
        addItem(drawerItem);

        drawerItem = new DrawerItem();
        drawerItem.name = "News View";
        addItem(drawerItem);

        drawerItem = new DrawerItem();
        drawerItem.name = "Both View";
        addItem(drawerItem);

        drawerItem = new DrawerItem();
        drawerItem.name = "WoZ View";
        addItem(drawerItem);
	}

    @Override
    /**
     * This function is used to handle selection event in the drawer
     */
    public void selectItem(int pos, int messageId) {
        DrawerItem item = mItems.get(pos);

    	// Handle switching views (assistant, news, both)
        if (item.name.equals(mAct.getString(R.string.assistant_view))) {
            RapportReaderActivity.clicked_assistant_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }

        if (item.name.equals(mAct.getString(R.string.news_view))) {
            RapportReaderActivity.clicked_news_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }

        if (item.name.equals(mAct.getString(R.string.both_view))) {
            RapportReaderActivity.clicked_both_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }

        if (item.name.equals(mAct.getString(R.string.woz_view))) {
            RapportReaderActivity.woz_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }
        super.selectItem(pos, messageId);
    }

    @Override
    public void prepareForExtension()
    {
        //Clean up all extended items
        for (int i = 2; i < mItems.size() ; i++)
        {
            DrawerItem item = mItems.get(i);
            item.cancelLoadAsync();
            if (item.frag != null)
                item.frag.clearAdapter();
        }
		/* EZ: changed 2 to 6, as the rapport reader requires more options */
        mItems = new ArrayList<DrawerItem>(mItems.subList(0, 6));
    }

    @Override
    public void onCreateDrawer(Bundle savedInstanceState) {
        mDrawerList = (ListView) mAct.findViewById(ReaderController.news_left_drawer);
        ReaderController.mDrawerList = mDrawerList;
        super.onCreateDrawer(savedInstanceState);
        mTitle = mDrawerTitle = mAct.getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(
                mAct,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        )
        {
            public void onDrawerClosed(View view) {
//                mAct.getActionBar().setTitle(mTitle);
                mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                I13N.get().log(new Event(pkgName, "onDrawerClosed"));
            }

            public void onDrawerOpened(View drawerView) {
                Log.d("DrawerManager", "Number of drawer items: " + Integer.toString(mDrawerList.getCount()));
                for (int index = 0; index < mDrawerList.getCount(); index++) {
                    View row = mDrawerList.getChildAt(index);
                    ImageView imageView = ((ImageView) row.findViewById( R.id.rapport_drawer_icon ));
                    imageView.setImageResource(drawer_icons[index]);
                }

//                mAct.getActionBar().setTitle(mDrawerTitle);
                mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                I13N.get().log(new Event(pkgName, "onDrawerOpened"));
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
}
