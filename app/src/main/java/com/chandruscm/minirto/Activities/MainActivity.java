package com.chandruscm.minirto.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.chandruscm.minirto.Interfaces.FragmentCallback;
import com.chandruscm.minirto.Fragments.OfficeFragment;
import com.chandruscm.minirto.Fragments.VehicleFragment;
import com.chandruscm.minirto.R;
import com.chandruscm.minirto.TabLayout.TabLayoutHelper;
import com.github.clans.fab.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity implements FragmentCallback
{
    public static String versionName = "1.0.0";

    public static int currentTab;
    private ViewPagerAdapter adapter;
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Fragment vehicleFragment;
    private Fragment officeFragment;
    private final int[] tabIcons = {R.drawable.ic_directions_car_white_24dp,
                                        R.drawable.ic_business_white_24dp};

    public final int ACTION_NULL = -1;

    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private FloatingActionButton fab;

    private MaterialTapTargetPrompt prompt;
    public static SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
        {
            officeFragment = getSupportFragmentManager().getFragment(savedInstanceState, "office");
            vehicleFragment = getSupportFragmentManager().getFragment(savedInstanceState, "vehicle");
        }
        else
        {
            officeFragment = new OfficeFragment();
            vehicleFragment = new VehicleFragment();
        }

        setContentView(R.layout.activity_main);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        try
        {
            versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch(PackageManager.NameNotFoundException e) {}

        appBar = (AppBarLayout) findViewById(R.id.top_bar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View customToolbar = getLayoutInflater().inflate(R.layout.custom_toolbar,null);
        toolbar.addView(customToolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cordinate_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setupFab();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        TabLayoutHelper.setupWithViewPager(tabLayout, viewPager);
        currentTab = tabLayout.getSelectedTabPosition();
        changeTab();

        if(sharedPref.getBoolean(getString(R.string.vehicle_fab_first_run), true) == true)
        {
            prompt = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(findViewById(R.id.fab))
                    .setPrimaryText(R.string.tap_target_vehicle_fab_title)
                    .setSecondaryText(R.string.tap_target_vehicle_fab_content)
                    .setBackgroundColourFromRes(R.color.accent)
                    .setFocalColourFromRes(R.color.card_backgroundExpand)
                    .setAutoDismiss(false)
                    .setAutoFinish(false)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                    {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                        {
                            if(tappedTarget)
                            {
                                sharedPref.edit().putBoolean(getString(R.string.vehicle_fab_first_run), false).commit();
                                prompt.finish();
                            }
                        }

                        @Override
                        public void onHidePromptComplete()
                        {

                        }
                    }).create();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    prompt.show();
                }
            }, 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "office", officeFragment);
        getSupportFragmentManager().putFragment(outState, "vehicle", vehicleFragment);
    }

    @Override
    public void showFab()
    {
        fab.show(true);
    }

    @Override
    public void hideFab()
    {
        fab.hide(true);
    }

    @Override
    public void showSnackBar(@StringRes int message, @StringRes int button, final int pos, final int action)
    {
        snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        final Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams lp = layout.getLayoutParams();
                if (lp instanceof CoordinatorLayout.LayoutParams) {
                    ((CoordinatorLayout.LayoutParams) lp).setBehavior(new DisableSwipeBehavior());
                    layout.setLayoutParams(lp);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.action_bar));

        if(action != ACTION_NULL)
        {
            snackbar.setAction(button, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ((VehicleFragment)adapter.getItem(0)).removeCardFromList(pos);
                }
            });
        }

        snackbar.show();
    }


    public class DisableSwipeBehavior extends SwipeDismissBehavior<Snackbar.SnackbarLayout>
    {
        @Override
        public boolean canSwipeDismissView(@NonNull View view) {
            return false;
        }
    }

    private void changeTab()
    {

        tabLayout.setOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener()
                {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab)
                    {
                        currentTab = tab.getPosition();
                        viewPager.setCurrentItem(currentTab);
                        appBar.setExpanded(true,true);
                        if(fab.isHidden())
                            fab.show(true);

                        if(currentTab == 0)
                            ((TextView) toolbar.findViewById(R.id.toolbar_text)).setText(getString(R.string.vehicle_tab));
                        else
                        {
                            ((TextView) toolbar.findViewById(R.id.toolbar_text)).setText(getString(R.string.office_tab));

                            if(sharedPref.getBoolean(getString(R.string.office_fab_first_run), true) == true)
                            {
                                prompt = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                                        .setTarget(findViewById(R.id.fab))
                                        .setPrimaryText(R.string.tap_target_office_fab_title)
                                        .setSecondaryText(R.string.tap_target_office_fab_content)
                                        .setBackgroundColourFromRes(R.color.accent)
                                        .setFocalColourFromRes(R.color.card_backgroundExpand)
                                        .setAutoDismiss(false)
                                        .setAutoFinish(false)
                                        .setCaptureTouchEventOutsidePrompt(true)
                                        .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                                        {
                                            @Override
                                            public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                                            {
                                                if(tappedTarget)
                                                {
                                                    sharedPref.edit().putBoolean(getString(R.string.office_fab_first_run), false).commit();
                                                    prompt.finish();
                                                }
                                            }

                                            @Override
                                            public void onHidePromptComplete()
                                            {

                                            }
                                        })
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab)
                    {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab)
                    {

                    }
                });
    }

    private void setupFab()
    {
        fab.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show(true);
            }
        }, 500);

        fab.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(final View v)
                    {
                        fab.hide(true);

                        switch (currentTab)
                        {
                            case 0:
                            {
                                ((VehicleFragment)adapter.getItem(0)).fabAction();
                                break;
                            }

                            case 1:
                            {
                                ((OfficeFragment)adapter.getItem(1)).fabAction();
                                break;
                            }
                        }
                    }
                }
        );
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(vehicleFragment, getString(R.string.vehicle_tab));
        adapter.addFragment(officeFragment, getString(R.string.office_tab));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter implements TabLayoutHelper.IconPagerAdapter{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public int getPageTitleIconRes(int position)
        {
            return tabIcons[position];
        }

        @Nullable
        @Override
        public Drawable getPageTitleIconDrawable(int position)
        {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.action_about)
        {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        return true;
    }

}
