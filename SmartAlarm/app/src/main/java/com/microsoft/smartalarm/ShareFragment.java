package com.microsoft.smartalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareFragment extends Fragment {
    ShareResultListener mCallback;

    public interface ShareResultListener {
        void onShareCompleted();
    }

    public static final String SHAREABLE_URI = "shareable-uri";
    private final static int SHARE_REQUEST_CODE = 2;
    private String mShareableUri;
    private ImageView mShareableImage;

    public static ShareFragment newInstance(String shareableUri) {
        ShareFragment fragment = new ShareFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(SHAREABLE_URI, shareableUri);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shareable, container, false);
        mShareableImage = (ImageView) view.findViewById(R.id.shareable_image);

        view.findViewById(R.id.finish_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishShare();
            }
        });

        view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });

        Bundle args = getArguments();
        mShareableUri = args.getString(SHAREABLE_URI);

        Logger.init(getActivity());
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (ShareResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShareableUri != null && mShareableUri.length() > 0) {
            Uri shareableUri = Uri.parse(mShareableUri);
            mShareableImage.setImageURI(shareableUri);
        }
    }

    public void share() {
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

    public void download() {
        //TODO: implement
        Toast.makeText(getActivity(), "(Coming soon) Saved to gallery", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                finishShare();
            }
        }
    }

    public void finishShare() {
        mCallback.onShareCompleted();
    }

    public static Uri saveShareableBitmap(Context context, Bitmap bitmap) {
        File tempFile;
        try {
            tempFile = File.createTempFile("test", ".png", context.getCacheDir());
            tempFile.setReadable(true, false);
            tempFile.deleteOnExit();
        }
        catch (IOException ex) {
            Logger.trackException(ex);
            return null;
        }

        if (tempFile.canWrite()){
            try {
                FileOutputStream stream = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                stream.close();
            }
            catch (IOException ex) {
                Logger.trackException(ex);
                return null;
            }
        }
        return Uri.fromFile(tempFile);
    }
}
