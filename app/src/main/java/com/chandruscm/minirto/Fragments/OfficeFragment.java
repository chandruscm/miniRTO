package com.chandruscm.minirto.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chandruscm.minirto.DataBase.DataBaseHelper;
import com.chandruscm.minirto.Interfaces.FragmentCallback;
import com.chandruscm.minirto.R;
import com.chandruscm.minirto.Recycler.OfficeAdapter;
import com.chandruscm.minirto.Recycler.RecyclerItemClickListener;
import com.chandruscm.minirto.ScrollBar.FastScrollRecyclerView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

public class OfficeFragment extends Fragment
{
    private FastScrollRecyclerView recyclerView;
    private DataBaseHelper db;
    private Cursor cursor;
    private Cursor matchCursor;
    private final String NOT_AVAILABLE = "n/a";
    private LinearLayoutManager layoutManager;
    private boolean isItUser = true;
    private StickyRecyclerHeadersDecoration headersDecor;
    private OfficeAdapter officeAdapter;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        db = new DataBaseHelper(context);
        cursor = db.getOffices();

        init();
        initListeners();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.office_fragment_layout, container, false);
        recyclerView = (FastScrollRecyclerView) rootView.findViewById(R.id.office_recycler_view);
        return rootView;
    }

    public void copyToClipBoard(String attribute)
    {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label",attribute);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, getString(R.string.clipboard_acknowledge), Toast.LENGTH_SHORT).show();
    }

    public void fabAction()
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.search)
                .content(R.string.search_content_office)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .positiveText("")
                .positiveColorRes(R.color.accent)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.accent)
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        fragmentCallback.showFab();
                    }
                })
                .widgetColorRes(R.color.accent)
                .alwaysCallInputCallback()
                .input(R.string.search_hint_office, R.string.search_prefill, false, new MaterialDialog.InputCallback()
                {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
                    {
                        if (input.length() > 0)
                        {
                            matchCursor = db.matchOffice(input);

                            if(matchCursor.moveToFirst())
                            {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                dialog.getActionButton(DialogAction.POSITIVE).setText(matchCursor.getString(1));
                            }
                            else
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .build();

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                popupDetails();
            }
        });

        dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
        dialog.show();
    }

    public void popupDetails()
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.office_dialog_result_layout,true)
                .positiveText(R.string.share_header)
                .positiveColorRes(R.color.accent)
                .negativeText(R.string.dismiss)
                .negativeColorRes(R.color.accent)
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        fragmentCallback.showFab();
                    }
                })
                .build();

        if(matchCursor.getString(4).equals(NOT_AVAILABLE))
            dialog.getCustomView().findViewById(R.id.phone).setVisibility(View.GONE);

        if(matchCursor.getString(3).equals(NOT_AVAILABLE))
            dialog.getCustomView().findViewById(R.id.direction).setVisibility(View.GONE);

        ((TextView) dialog.getCustomView().findViewById(R.id.office_code)).setText(matchCursor.getString(0));
        ((TextView) dialog.getCustomView().findViewById(R.id.office_district)).setText(matchCursor.getString(1));
        ((TextView) dialog.getCustomView().findViewById(R.id.office_address)).setText(matchCursor.getString(3));
        ((TextView) dialog.getCustomView().findViewById(R.id.office_phone)).setText(matchCursor.getString(4));

        dialog.getTitleView().setTypeface(null, Typeface.NORMAL);
        dialog.show();

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String content = matchCursor.getString(1) + " RTO\n\n"
                        + "Address: " + matchCursor.getString(3) + "\n"
                        + (matchCursor.getString(4).equals(NOT_AVAILABLE) ? "" :
                        "Phone: " + matchCursor.getString(4) + "\n")
                        + "Website: " + matchCursor.getString(5) + "\n\n"
                        + getString(R.string.share_app_prefix) + " " + getString(R.string.app_link);

                String subject = matchCursor.getString(1) + " RTO Details";
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                startActivity(intent);
            }
        });

        dialog.getCustomView().findViewById(R.id.direction)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+matchCursor.getString(3));
                        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        PackageManager packageManager = context.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null)
                            startActivity(intent);
                    }
                });

        dialog.getCustomView().findViewById(R.id.phone)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:+"+matchCursor.getString(4)));
                        startActivity(intent);
                    }
                });

        dialog.getCustomView().findViewById(R.id.website)
                .setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(matchCursor.getString(5)));
                                            startActivity(intent);
                                        }
                                    }
                );

        dialog.getCustomView().findViewById(R.id.direction)
                .setOnLongClickListener(new View.OnLongClickListener()
                                        {
                                            @Override
                                            public boolean onLongClick(View v)
                                            {
                                                copyToClipBoard(matchCursor.getString(3));
                                                return true;
                                            }
                                        }
                );

        dialog.getCustomView().findViewById(R.id.phone)
                .setOnLongClickListener(new View.OnLongClickListener()
                                        {
                                            @Override
                                            public boolean onLongClick(View v)
                                            {
                                                copyToClipBoard(matchCursor.getString(4));
                                                return true;
                                            }
                                        }
                );

        dialog.getCustomView().findViewById(R.id.website)
                .setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        copyToClipBoard(matchCursor.getString(5));
                        return true;
                    }
                });

    }

    public void init()
    {
        cursor.moveToFirst();
        officeAdapter = new OfficeAdapter(cursor);
        recyclerView.setAdapter(officeAdapter);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        headersDecor = new StickyRecyclerHeadersDecoration(officeAdapter);
        recyclerView.addItemDecoration(headersDecor);
    }

    public void initListeners()
    {

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(View view, int position)
                    {
                        cursor.moveToPosition(position);
                        matchCursor = db.matchOffice(cursor.getString(1));
                        matchCursor.moveToFirst();
                        popupDetails();
                    }
                })
        );

        recyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener()
                {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState)
                    {
                        super.onScrollStateChanged(recyclerView, newState);

                        if(newState != RecyclerView.SCROLL_STATE_IDLE)
                            isItUser = true;
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                    {
                        if(isItUser)
                        {
                            if (dy > 10)
                                fragmentCallback.hideFab();
                            else if (dy < -10)
                                fragmentCallback.showFab();
                        }

                        super.onScrolled(recyclerView, dx, dy);
                    }
                }
        );
    }
}