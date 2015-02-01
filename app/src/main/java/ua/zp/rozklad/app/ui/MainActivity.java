package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.account.GroupAuthenticator;
import ua.zp.rozklad.app.provider.ScheduleContract;


public class MainActivity extends ActionBarActivity
        implements ScheduleFragment.OnScheduleItemClickListener{

    public static interface EXTRA_KEY {
        String SELECTED_NAV_DRAWER_ITEM_ID = "SELECTED_NAV_DRAWER_ITEM_ID";
    }

    private static final int NAV_DRAWER_CLOSE_DELAY = 300;

    private static final int NAV_DRAWER_ITEM_SCHEDULE = 0;
    private static final int NAV_DRAWER_ITEM_SUBJECTS = 1;
    private static final int NAV_DRAWER_ITEM_LECTURERS = 2;
    private static final int NAV_DRAWER_ITEM_SETTINGS = 3;
    private static final int NAV_DRAWER_ITEM_INFO_RECALL = 4;
    private static final int NAV_DRAWER_ITEM_SEPARATOR = -1;

    private static final int[] NAV_DRAWER_TITLE_RES_ID = {
            R.string.nav_drawer_item_schedule,
            R.string.nav_drawer_item_subjects,
            R.string.nav_drawer_item_lecturers,
            R.string.nav_drawer_item_settings,
            R.string.nav_drawer_item_about_recall
    };

    private static final int[] NAV_DRAWER_ICON_RES_ID = {
            R.drawable.ic_query_builder_white_24dp,
            R.drawable.ic_class_white_24dp,
            R.drawable.ic_people_white_24dp,
            R.drawable.ic_settings_white_24dp,
            R.drawable.ic_help_white_24dp
    };

    private int selectedNavDrawerItemId = 0;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<Integer> navDrawerItems = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private View[] navDrawerItemViews = null;
    private View.OnClickListener changeGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            drawerLayout.closeDrawer(Gravity.START);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new MaterialDialog.Builder(MainActivity.this)
                            .content(R.string.change_group_question)
                            .positiveText(R.string.change_group)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    // TODO: Implement changing group (and switching subgroup if needed)
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    drawerLayout.openDrawer(Gravity.START);
                                }
                            }).show();
                }
            }, NAV_DRAWER_CLOSE_DELAY);
        }
    };

    private Handler handler;

    private Toolbar appBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);

        handler = new Handler();

        if (savedInstanceState != null) {
            selectedNavDrawerItemId = savedInstanceState
                    .getInt(EXTRA_KEY.SELECTED_NAV_DRAWER_ITEM_ID, NAV_DRAWER_ITEM_SCHEDULE);
        }

        setUpNavDrawer();
        onNavDrawerItemClicked(selectedNavDrawerItemId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_KEY.SELECTED_NAV_DRAWER_ITEM_ID, selectedNavDrawerItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final AccountManager manager = AccountManager.get(getApplicationContext());

        // TODO: Fix checking authorization
        Account[] accounts = manager.getAccountsByType(GroupAuthenticator.ACCOUNT_TYPE);

        if (accounts.length == 0) {
            final Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
        } else {
            String groupName = getString(R.string.nav_drawer_group_qualifier) +
                    " " + accounts[0].name;
            String departmentName = "Математичний факультет" /*getDepartment from account*/;

            ((TextView) drawerLayout.findViewById(R.id.group_name_text))
                    .setText(groupName);
            ((TextView) drawerLayout.findViewById(R.id.department_name_text))
                    .setText(departmentName);
            ContentResolver
                    .setSyncAutomatically(accounts[0], ScheduleContract.CONTENT_AUTHORITY, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case LoginActivity.RESULT_CANCELED:
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setUpNavDrawer() {
        drawerLayout.findViewById(R.id.change_group_box_indicator)
                .setOnClickListener(changeGroupClickListener);

        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, appBar,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        navDrawerItems.add(NAV_DRAWER_ITEM_SCHEDULE);
        navDrawerItems.add(NAV_DRAWER_ITEM_SUBJECTS);
        navDrawerItems.add(NAV_DRAWER_ITEM_LECTURERS);
        navDrawerItems.add(NAV_DRAWER_ITEM_SEPARATOR);
        navDrawerItems.add(NAV_DRAWER_ITEM_SETTINGS);
        navDrawerItems.add(NAV_DRAWER_ITEM_INFO_RECALL);
        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        ViewGroup navDrawerItemsListContainer =
                (ViewGroup) drawerLayout.findViewById(R.id.nav_drawer_items_list);
        if (navDrawerItemsListContainer == null) {
            return;
        }

        navDrawerItemViews = new View[navDrawerItems.size()];
        navDrawerItemsListContainer.removeAllViews();
        for (int i = 0; i < navDrawerItems.size(); i++) {
            navDrawerItemViews[i] =
                    makeNavDrawerItem(navDrawerItems.get(i), navDrawerItemsListContainer);
            navDrawerItemsListContainer.addView(navDrawerItemViews[i]);
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = itemId == selectedNavDrawerItemId;
        int layoutResId;

        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            layoutResId = R.layout.nav_drawer_separetor;
        } else {
            layoutResId = R.layout.nav_drawer_item;
        }

        View view = getLayoutInflater().inflate(layoutResId, container, false);

        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        int iconResId = itemId >= 0 && itemId < NAV_DRAWER_ICON_RES_ID.length ?
                NAV_DRAWER_ICON_RES_ID[itemId] : 0;
        int titleResId = itemId >= 0 && itemId < NAV_DRAWER_TITLE_RES_ID.length ?
                NAV_DRAWER_TITLE_RES_ID[itemId] : 0;

        if (iconResId > 0) {
            iconView.setImageResource(iconResId);
        } else {
            iconView.setVisibility(View.GONE);
        }

        titleView.setText(getString(titleResId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            return;
        }
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        view.setSelected(selected);

        titleView.setTextColor(selected ?
                getResources().getColor(R.color.nav_drawer_text_color_selected) :
                getResources().getColor(R.color.nav_drawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.nav_drawer_icon_tint_selected) :
                getResources().getColor(R.color.nav_drawer_icon_tint));
    }

    private void onNavDrawerItemClicked(int itemId) {
        switch (itemId) {
            case NAV_DRAWER_ITEM_SETTINGS:
                /*
                * Start Settings Activity
                * */
                return;
            case NAV_DRAWER_ITEM_INFO_RECALL:
                /*
                * Start Info Activity
                * */
                return;
            case NAV_DRAWER_ITEM_SCHEDULE:
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_content, ScheduleFragment.newInstance(0, 0, 0, 0))
                        .commit();
                break;
            /*
            * Change main content fragment
            * */
        }
        setSelectedNavDrawerItem(itemId);
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (navDrawerItemViews != null) {
            for (int i = 0; i < navDrawerItemViews.length; i++) {
                if (i < navDrawerItems.size()) {
                    int thisItemId = navDrawerItems.get(i);
                    formatNavDrawerItem(navDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    @Override
    public void onScheduleItemClicked(int scheduleItemId) {

    }
}
