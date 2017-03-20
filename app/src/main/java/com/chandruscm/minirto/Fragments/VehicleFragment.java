package com.chandruscm.minirto.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.chandruscm.minirto.Activities.FetchActivity;
import com.chandruscm.minirto.Activities.MainActivity;
import com.chandruscm.minirto.Cards.VehicleCard;
import com.chandruscm.minirto.Cards.VehicleExpandCard;
import com.chandruscm.minirto.DataBase.DataBaseHandler;
import com.chandruscm.minirto.Interfaces.AsyncCaptchaResponse;
import com.chandruscm.minirto.Jsoup.GetCaptcha;
import com.chandruscm.minirto.Models.Vehicle;
import com.chandruscm.minirto.Interfaces.AsyncResponse;
import com.chandruscm.minirto.Interfaces.FragmentCallback;
import com.chandruscm.minirto.Jsoup.FetchFromVahan;
import com.chandruscm.minirto.R;
import com.chandruscm.minirto.Utils.RegEx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.listener.SwipeOnScrollListener;
import it.sephiroth.android.library.tooltip.Tooltip;

public class VehicleFragment extends Fragment
{
    private int previousVisibleItem;
    private CardListView recentSearchList;
    private CardArrayAdapter cardArrayAdapter;
    private int currentCardCount;

    private DataBaseHandler db;
    private Cursor cursor;
    private int matchPosition = -1;
    private ArrayList<Card> cards;

    private RegEx regEx = new RegEx();
    public final String INVALID = "INVALID";
    private final int OK = 200;
    private final int SOCKET_TIMEOUT = 408;
    private final int CAPTCHA_FAILED = 999;
    private final int TECHNICAL_DIFFICULTY = 888;
    public final int ACTION_NULL = -1;

    private Context context;
    private FragmentCallback fragmentCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        this.context = context;
        fragmentCallback = (FragmentCallback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.vehicle_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        recentSearchList = (CardListView) view.findViewById(R.id.about_list);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        init();
        initListeners();
    }

    private void init()
    {
        db = new DataBaseHandler(context);
        initCursor();
        cards = new ArrayList<>();
        cardArrayAdapter = new CardArrayAdapter(context, cards);
        addCards();
        currentCardCount = cards.size();
        if (recentSearchList != null)
            recentSearchList.setAdapter(cardArrayAdapter);

    }

    public void initCursor()
    {
        cursor = db.getVehicles();
    }

    private void initListeners()
    {
        recentSearchList.setOnScrollListener(new SwipeOnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                if (view.getFirstVisiblePosition() == 0
                        && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && currentCardCount < cards.size())
                {
                    cardArrayAdapter.getItem(0).doExpand();
                    currentCardCount = cards.size();
                }
                super.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (firstVisibleItem > previousVisibleItem)
                    fragmentCallback.hideFab();
                else if (firstVisibleItem < previousVisibleItem)
                    fragmentCallback.showFab();

                previousVisibleItem = firstVisibleItem;
            }
        });

        recentSearchList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener()
                {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, final View arg1,
                                                   final int pos, long id)
                    {
                        if (!cards.get(pos).isExpanded())
                            fragmentCallback.showSnackBar(R.string.remove_search, R.string.remove_button, pos, 0);
                        else
                            shareCard(pos);

                        return true;
                    }
                });

    }

    public void shareCard(final int pos)
    {
        if(cardArrayAdapter.getItem(pos).isExpanded())
        {
            cursor.moveToPosition(cursor.getCount() - pos - 1);
            final String subject = "Details of "+cursor.getString(0);
            final View arg1 = (View) cardArrayAdapter.getItem(pos).getCardView();
            final MaterialDialog dialog = new MaterialDialog.Builder(context)
                    .title(R.string.share_header)
                    .content(R.string.share_content)
                    .positiveText(R.string.text)
                    .positiveColorRes(R.color.accent)
                    .onPositive(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                        {
                            String content = cursor.getString(0)
                                    + ", " + cursor.getString(8) + "\n\n"
                                    + cursor.getString(1) + "\n"
                                    + "Fuel Type: " + cursor.getString(2) + "\n"
                                    + "Displacement: " + cursor.getString(3) + "\n"
                                    + "Engine No: " + cursor.getString(4) + "\n"
                                    + "Chasis No: " + cursor.getString(5) + "\n"
                                    + "Owner: " + cursor.getString(6) + "\n"
                                    + "Location: " + cursor.getString(7) + "\n\n"
                                    + getString(R.string.share_app_prefix) + " " + getString(R.string.app_link);

                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                            startActivity(intent);
                        }
                    })
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.accent)
                    .neutralText(R.string.image)
                    .neutralColorRes(R.color.accent)
                    .onNeutral(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                        {
                            arg1.findViewById(R.id.water_mark).setVisibility(View.VISIBLE);
                            arg1.setDrawingCacheEnabled(true);
                            arg1.buildDrawingCache();
                            Bitmap bitmap = arg1.getDrawingCache();
                            arg1.findViewById(R.id.water_mark).setVisibility(View.INVISIBLE);
                            try
                            {
                                File cachePath = new File(dialog.getContext().getCacheDir(), "images");
                                cachePath.mkdirs();
                                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                            } catch (FileNotFoundException e)
                            {
                                e.printStackTrace();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                            File imagePath = new File(dialog.getContext().getCacheDir(), "images");
                            File newFile = new File(imagePath, "image.png");
                            Uri contentUri = FileProvider.getUriForFile(dialog.getContext(), "com.chandruscm.minirto.fileprovider", newFile);

                            if (contentUri != null)
                            {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.setDataAndType(contentUri, dialog.getContext().getContentResolver().getType(contentUri));
                                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_app_prefix) + " " + getString(R.string.app_link));
                                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                            }
                        }
                    })
                    .build();

            dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
            dialog.show();
        }
    }

    public void newCardAnimation()
    {
        cardArrayAdapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (recentSearchList.getFirstVisiblePosition() != 0)
                    recentSearchList.smoothScrollToPosition(0);
                else
                {
                    cardArrayAdapter.getItem(0).doExpand();
                    currentCardCount = cards.size();

                    if(MainActivity.sharedPref.getBoolean(getString(R.string.share_card_first_run), true) == true)
                    {
                        MainActivity.sharedPref.edit().putBoolean(getString(R.string.share_card_first_run), false).commit();

                        Tooltip.make(context,
                                new Tooltip.Builder(1)
                                        .anchor((View) cardArrayAdapter.getItem(0).getCardView(), Tooltip.Gravity.BOTTOM)
                                        .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_CONSUME, -1)
                                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                        .text("Long press to share")
                                        .fadeDuration(200)
                                        .fitToScreen(true)
                                        .showDelay(400)
                                        .withOverlay(false)
                                        .build()
                        ).show();
                    }

                }
            }
        }, 10);
    }

    public void addCards()
    {
        if(cursor.moveToFirst())
        {
            do
            {
                cards.add(0, createCard(new Vehicle(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8))));
            }
            while(cursor.moveToNext());
        }
    }

    public Card createCard(Vehicle vehicle)
    {
        Card vehicleCard = new VehicleCard(context, vehicle.getNumber());
        vehicleCard.setOnLongClickListener(
                new Card.OnLongCardClickListener()
                {
                    @Override
                    public boolean onLongClick(Card card, View view)
                    {
                        return false;
                    }
                }
        );
        vehicleCard.addCardExpand(new VehicleExpandCard(context, vehicle));
        ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder()
                        .highlightView(false)
                        .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
        vehicleCard.setViewToClickToExpand(viewToClickToExpand);

        return vehicleCard;
    }

    public void addCardToList(Vehicle vehicle)
    {
        db.addVehicle(vehicle);
        if(matchPosition != -1)
            cards.remove(matchPosition);
        initCursor();
        cards.add(0, createCard(vehicle));
        newCardAnimation();
    }

    public void removeCardFromList(final int pos)
    {
        cursor.moveToPosition(cursor.getCount() - pos - 1);
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.remove_header)
                .content("Details of "+cursor.getString(0)+" will be removed permanently.")
                .positiveColorRes(R.color.accent)
                .positiveText(R.string.okay)
                .negativeColorRes(R.color.accent)
                .negativeText(R.string.cancel)
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        fragmentCallback.showFab();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        Animation anim = AnimationUtils.loadAnimation(context,
                                android.R.anim.slide_out_right);
                        anim.setDuration(300);
                        View cardView = (View) cardArrayAdapter.getItem(pos).getCardView();
                        cardView.startAnimation(anim);

                        new Handler().postDelayed(new Runnable() {

                            public void run()
                            {
                                cursor.moveToPosition(cursor.getCount() - pos - 1);
                                db.removeVehicle(cursor.getString(0));
                                initCursor();
                                cards.remove(pos);
                                cardArrayAdapter.notifyDataSetChanged();
                            }

                        }, anim.getDuration());
                    }
                })
                .build();

        dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
        dialog.show();
    }

    public void fabAction()
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.search)
                .content(R.string.search_content_vehicle)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                .positiveText(R.string.search)
                .positiveColorRes(R.color.accent)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.accent)
                .widgetColorRes(R.color.accent)
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        fragmentCallback.showFab();
                    }
                })
                .alwaysCallInputCallback()
                .input(R.string.search_hint_vehicle, R.string.search_prefill, false, new MaterialDialog.InputCallback()
                {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input)
                    {
                        final String result = regEx.validate(input);
                        if (result.equals(INVALID))
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        else
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);

                        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback()
                        {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which)
                            {
                                matchPosition = -1;
                                matchPosition = isAlreadyInDatabase(result);

                                if (isNetworkAvailable())
                                {
                                    addCardToList(new Vehicle(result, "Maruti Suzuki Ritz", "Petrol", "> 1500cc", "K12MNXXXXXXX", "K12MNXXXXXXX", "chandruscm", "Trivandrum, Kerala", "Registered on 01 Jan 2000"));
                                    fragmentCallback.showFab();
                                    //jsoup request made here
                                    //getDetails(result);
                                }
                                else
                                {
                                    fragmentCallback.showFab();
                                    fragmentCallback.showSnackBar(R.string.error_no_network, ACTION_NULL, ACTION_NULL, ACTION_NULL);
                                }
                            }
                        });
                    }
                }).build();

        dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
        dialog.show();
    }

    public void getDetails(final String result)
    {
        final MaterialDialog.Builder mdialogBuilder = new MaterialDialog.Builder(context).content(getString(R.string.connecting)).cancelable(false).progress(true, 0).widgetColorRes(R.color.accent);
        final MaterialDialog mdialog = mdialogBuilder.build();
        mdialog.show();

        new GetCaptcha(new AsyncCaptchaResponse()
        {
            @Override
            public void processFinish(final Bitmap bitmap, int statuscode)
            {
                mdialog.dismiss();

                if (bitmap != null)
                {
                    final MaterialDialog mdialog = new MaterialDialog.Builder(context)
                            .customView(R.layout.captcha_dialog_layout, true)
                            .positiveText(R.string.continue_button)
                            .positiveColorRes(R.color.accent)
                            .negativeText(R.string.cancel)
                            .negativeColorRes(R.color.accent)
                            .widgetColorRes(R.color.accent)
                            .cancelListener(new DialogInterface.OnCancelListener()
                            {
                                @Override
                                public void onCancel(DialogInterface dialog)
                                {
                                    fragmentCallback.showFab();
                                }
                            })
                            .build();

                    mdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    mdialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    final EditText captchaInput = (EditText) mdialog.getCustomView().findViewById(R.id.captcha_input);
                    MDTintHelper.setTint(captchaInput, getResources().getColor(R.color.accent));
                    captchaInput.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after)
                        {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count)
                        {
                            if (s.toString().length() == 5)
                                mdialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            else
                                mdialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

                        }

                        @Override
                        public void afterTextChanged(Editable s)
                        {

                        }
                    });

                    ImageView imageView = (ImageView) mdialog.getCustomView().findViewById(R.id.captcha);
                    imageView.setImageBitmap(bitmap);

                    mdialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    mdialog.dismiss();

                                    final MaterialDialog.Builder mdialogBuilder = new MaterialDialog.Builder(context).content(getString(R.string.wait)).cancelable(false).progress(true, 0).widgetColorRes(R.color.accent);
                                    final MaterialDialog mdialog = mdialogBuilder.build();
                                    mdialog.show();

                                    new FetchFromVahan(new AsyncResponse()
                                    {
                                        @Override
                                        public void processFinish(Vehicle vehicle, int statusCode)
                                        {
                                            mdialog.dismiss();
                                            fragmentCallback.showFab();

                                            if (statusCode == OK)
                                            {
                                                if (vehicle != null)
                                                    addCardToList(vehicle);
                                                else
                                                {
                                                    final MaterialDialog dialog = new MaterialDialog.Builder(context)
                                                            .title(result + " " + getString(R.string.not_found_header))
                                                            .content(getString(R.string.not_found_content))
                                                            .widgetColorRes(R.color.accent)
                                                            .positiveText(getString(R.string.okay))
                                                            .positiveColorRes(R.color.accent)
                                                            .neutralText(R.string.more)
                                                            .neutralColorRes(R.color.accent)
                                                            .onNeutral(new MaterialDialog.SingleButtonCallback()
                                                            {
                                                                @Override
                                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                                                                {
                                                                    Intent intent = new Intent(context, FetchActivity.class);
                                                                    context.startActivity(intent);
                                                                }
                                                            })
                                                            .build();

                                                    dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
                                                    dialog.show();
                                                }
                                            }
                                            else if (statusCode == CAPTCHA_FAILED)
                                            {
                                                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                                                        .title(R.string.error_captcha_failed)
                                                        .content(R.string.error_captcha_failed_description)
                                                        .positiveText(R.string.retry)
                                                        .positiveColorRes(R.color.accent)
                                                        .onPositive(new MaterialDialog.SingleButtonCallback()
                                                        {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                                                            {
                                                                getDetails(result);
                                                            }
                                                        })
                                                        .negativeText(R.string.cancel)
                                                        .negativeColorRes(R.color.accent)
                                                        .build();

                                                dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
                                                dialog.show();
                                            }
                                            else if (statusCode == TECHNICAL_DIFFICULTY)
                                            {
                                                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                                                        .title(R.string.error_technical_difficulty)
                                                        .content(R.string.error_technical_difficulty_description)
                                                        .positiveText(R.string.check)
                                                        .positiveColorRes(R.color.accent)
                                                        .onPositive(new MaterialDialog.SingleButtonCallback()
                                                        {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                                                            {
                                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                intent.setData(Uri.parse(getString(R.string.app_link_direct)));
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .negativeText(R.string.cancel)
                                                        .negativeColorRes(R.color.accent)
                                                        .build();

                                                dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
                                                dialog.show();
                                            }
                                            else if (statusCode == SOCKET_TIMEOUT)
                                            {
                                                fragmentCallback.showSnackBar(R.string.error_server_busy, ACTION_NULL, ACTION_NULL, ACTION_NULL);
                                            }
                                            else
                                            {
                                                fragmentCallback.showSnackBar(R.string.error_no_network, ACTION_NULL, ACTION_NULL, ACTION_NULL);
                                            }
                                        }
                                    }).execute(result.substring(0, result.length() - 4), result.substring(result.length() - 4), captchaInput.getText().toString());

                                }
                            }
                    );
                    mdialog.show();
                }
                else
                {
                    fragmentCallback.showFab();
                    fragmentCallback.showSnackBar(R.string.error_server_busy, ACTION_NULL, ACTION_NULL, ACTION_NULL);
                }
            }
        }).execute(result.substring(0, result.length() - 4), result.substring(result.length() - 4));
    }

    public int isAlreadyInDatabase(String number)
    {
        if(cursor.moveToFirst())
            do
            {
                if(cursor.getString(0).equals(number))
                    return cursor.getCount() - cursor.getPosition() - 1;
            }
            while (cursor.moveToNext());

        return -1;
    }

    private boolean isNetworkAvailable()
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
