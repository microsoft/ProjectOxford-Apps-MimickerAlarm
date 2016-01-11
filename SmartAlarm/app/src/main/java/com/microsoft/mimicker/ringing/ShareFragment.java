package com.microsoft.mimicker.ringing;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareFragment extends Fragment {
    public static final String SHARE_FRAGMENT_TAG = "share_fragment";
    public static final String SHAREABLE_URI = "shareable-uri";
    private final static int SHARE_REQUEST_CODE = 2;
    private final static String MIMICKER_FILE_PREFIX = "Mimicker_";
    // The time to auto-dismiss share screen if there is no user interaction
    private static final int SHARING_FRAGMENT_STAY_DURATION = 60 * 1000;    // 1 minute
    // The time to auto-dismiss share screen after pressing save button
    private static final int SHARING_FRAGMENT_SAVE_STAY_DURATION = 60 * 1000;   // 1 minute
    // The time to auto-dismiss share screen after pressing share button
    private static final int SHARING_FRAGMENT_SHARE_STAY_DURATION = 300 * 1000; // 5 minutes
    // Delay to dismiss the toast message
    private static final long TOAST_IN_FRAGMENT_DELAY = 1500;    // 1.5 seconds

    private ShareResultListener mCallback;
    private String mShareableUri;
    private ImageView mShareableImage;

    private Handler mHandler;
    private Runnable mSharingFragmentDismissTask;
    private Runnable mToastAutoDismiss;

    public static ShareFragment newInstance(String shareableUri) {
        ShareFragment fragment = new ShareFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(SHAREABLE_URI, shareableUri);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Uri saveShareableBitmap(Context context, Bitmap bitmap, String question) {
        drawStamp(context, bitmap, question);

        File tempFile;
        try {
            tempFile = File.createTempFile("mimicker", ".jpg", context.getCacheDir());
            tempFile.setReadable(true, false);
            tempFile.deleteOnExit();
        } catch (IOException ex) {
            Logger.trackException(ex);
            return null;
        }

        if (tempFile.canWrite()) {
            try {
                FileOutputStream stream = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                stream.close();
            } catch (IOException ex) {
                Logger.trackException(ex);
                return null;
            }
        }
        return Uri.fromFile(tempFile);
    }

    private static void drawStamp(Context context, Bitmap bitmap, String question) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        float opacity = 0.7f;
        int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        int textSize = 20; // defined in SP
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 25, context.getResources().getDisplayMetrics());

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundResource(R.drawable.rounded_corners);
        layout.getBackground().setAlpha((int) (opacity * 255));
        layout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        ImageView logo = new ImageView(context);
        logo.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_launcher_no_bg));
        layout.addView(logo);

        TextView textView = new TextView(context);
        textView.setVisibility(View.VISIBLE);
        if (question != null) {
            textView.setText(question);
        } else {
            textView.setText("Mimicker");
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setPadding(horizontalPadding, 0, 0, 0);

        LinearLayout.LayoutParams centerInParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerInParent.gravity = Gravity.CENTER_VERTICAL;
        layout.addView(textView, centerInParent);

        layout.measure(canvas.getWidth(), height);
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());

        canvas.translate((canvas.getWidth() - layout.getMeasuredWidth()) / 2, (float) (canvas.getHeight() * 0.8 - height));
        layout.draw(canvas);
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
                // Reset the timer to wait sharing to complete
                mHandler.removeCallbacks(mSharingFragmentDismissTask);
                mHandler.postDelayed(mSharingFragmentDismissTask, SHARING_FRAGMENT_SHARE_STAY_DURATION);
                share();
            }
        });

        view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset the timer to wait downloading to complete
                mHandler.removeCallbacks(mSharingFragmentDismissTask);
                mHandler.postDelayed(mSharingFragmentDismissTask, SHARING_FRAGMENT_SAVE_STAY_DURATION);
                download();
            }
        });

        Bundle args = getArguments();
        mShareableUri = args.getString(SHAREABLE_URI);

        // Set up timer to dismiss the sharing fragment if there is no user interaction with the buttons
        mSharingFragmentDismissTask = new Runnable() {
            @Override
            public void run() {
                finishShare();
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mSharingFragmentDismissTask, SHARING_FRAGMENT_STAY_DURATION);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mShareableUri != null && mShareableUri.length() > 0) {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    File deleteFile = new File(mShareableUri);
                    boolean deleted = deleteFile.delete();
                    if (!deleted) {
                        Loggable.AppError appError = new Loggable.AppError(Loggable.Key.APP_ERROR, "Failed to delete shareable");
                        Logger.track(appError);
                    }
                }
            }).start();
        }
        if (mToastAutoDismiss != null) {
            mHandler.removeCallbacks(mToastAutoDismiss);
        }
    }

    public void share() {
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_SHARE);
        Logger.track(userAction);

        mCallback.onRequestLaunchShareAction();

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

    //
    // Copy the temporary mimic picture to the camera folder with a filename with timestamp
    //
    public void download() {
        File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File sourceFile = new File(mShareableUri);
        String dateTimeString = (new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())); // Example: 2015-12-31-193205
        String targetFileName = MIMICKER_FILE_PREFIX + dateTimeString + ".jpg" ; // "Mimicker_2015-12-31-193205.jpg
        File targetFile = new File(cameraDirectory.getPath(), targetFileName);

        try {
            copyFile(sourceFile, targetFile);
        } catch (IOException ex) {
            showToastInFragment(R.string.share_download_failure);
            Logger.trackException(ex);
            return;
        }

        // Inform the media store about the new file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, targetFile.getPath());
        getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        showToastInFragment(R.string.share_download_success);
    }

    public void copyFile(File sourceFile, File targetFile) throws IOException {
        InputStream inputStream = new FileInputStream(sourceFile);
        OutputStream outputStream = new FileOutputStream(targetFile);

        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
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
        mHandler.removeCallbacks(mSharingFragmentDismissTask);
        mCallback.onShareCompleted();
    }

    public interface ShareResultListener {
        void onShareCompleted();
        void onRequestLaunchShareAction();
    }

    // Display the home-made toast message with the specified string resource ID
    //
    // We create our own toast message, instead of using android.widget.toast, because
    // system toast won't work in lock screen.
    private void showToastInFragment(int resourceId) {
        String message = getResources().getString(resourceId);
        final TextView textView = (TextView)getView().findViewById(R.id.share_toast_message);
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        mToastAutoDismiss = new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.INVISIBLE);
            }
        };
        handler.postDelayed(mToastAutoDismiss, TOAST_IN_FRAGMENT_DELAY);
    }
}
