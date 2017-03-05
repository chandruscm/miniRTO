package com.chandruscm.minirto.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chandruscm.minirto.R;

public class FetchActivity extends AppCompatActivity
{
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        View customToolbar = getLayoutInflater().inflate(R.layout.custom_toolbar_2,null);
        ((TextView)customToolbar.findViewById(R.id.toolbar_text)).setText(R.string.fetch_header);

        Button button = (Button) customToolbar.findViewById(R.id.toolbar_button);
        button.setText(getString(R.string.fetch_website_button));
        button.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.fetch_website)));
                        startActivity(intent);
                    }
                }
        );

        toolbar.addView(customToolbar);
        ((TextView)findViewById(R.id.fetch_content)).setText(Html.fromHtml(getString(R.string.fetch_content)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
