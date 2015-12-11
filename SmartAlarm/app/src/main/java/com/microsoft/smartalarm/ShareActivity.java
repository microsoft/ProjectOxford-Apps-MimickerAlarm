package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ShareActivity extends Activity{

    private String mShareableUri;
    private final static int SHARE_REQUEST_CODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shareable);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mShareableUri = extras.getString(GameFactory.SHAREABLE_URI);
        Logger.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mShareableUri != null && mShareableUri.length() > 0) {
            Uri shareableUri = Uri.parse(mShareableUri);
            ImageView imageView = (ImageView) findViewById(R.id.shareable_image);
            imageView.setImageURI(shareableUri);
        }
    }

    public void share(View view) {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_SHARE);
        Logger.track(userAction);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        File file = new File(mShareableUri);
        Uri uri = Uri.fromFile(file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text_template));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_template));
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(Intent.createChooser(shareIntent, getResources().getString(R.string.share_action_description)), SHARE_REQUEST_CODE);
    }

    public void download(View view) {
        //TODO: implement
        Toast.makeText(this, "(Coming soon) Saved to gallery", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finishShare(null);
            }
        }
    }

    public void finishShare(View view) {
        finish();
    }
}
