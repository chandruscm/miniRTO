package com.chandruscm.minirto.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import com.chandruscm.minirto.Cards.AboutCard;
import com.chandruscm.minirto.R;
import com.github.clans.fab.FloatingActionButton;
import java.util.ArrayList;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.listener.SwipeOnScrollListener;

public class AboutActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CardListView aboutList;
    private CardArrayAdapter cardArrayAdapter;
    private ArrayList<Card> cards;
    private FloatingActionButton fab;
    private int previousVisibleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        View customToolbar = getLayoutInflater().inflate(R.layout.custom_toolbar_2,null);
        ((TextView)customToolbar.findViewById(R.id.toolbar_text)).setText(getString(R.string.about_header));

        Button button = (Button) customToolbar.findViewById(R.id.toolbar_button);
        button.setVisibility(View.GONE);

        toolbar.addView(customToolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        initCards();
        initFab();
    }

    public void initCards()
    {
        cards = new ArrayList<>();

        cards.add(new AboutCard(this,R.layout.about_card_layout_1,1));
        cards.add(new AboutCard(this,R.layout.about_card_layout_2,2));
        cards.add(new AboutCard(this,R.layout.about_card_layout_3,3));
        cards.add(new AboutCard(this,R.layout.about_card_layout_4,4));
        cards.add(new AboutCard(this,R.layout.about_card_layout_5,5));

        cardArrayAdapter = new CardArrayAdapter(this, cards);
        aboutList = (CardListView) findViewById(R.id.about_list);

        if(aboutList != null)
            aboutList.setAdapter(cardArrayAdapter);
    }

    public void initFab()
    {
        fab.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show(true);
            }
        }, 500);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.app_link_direct)));
                startActivity(intent);
            }
        });

        aboutList.setOnScrollListener(new SwipeOnScrollListener()
        {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (firstVisibleItem > previousVisibleItem)
                    fab.hide(true);
                else if (firstVisibleItem < previousVisibleItem)
                    fab.show(true);

                previousVisibleItem = firstVisibleItem;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
