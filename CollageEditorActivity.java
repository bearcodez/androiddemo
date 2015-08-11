package com.piggystudio.lovelyphotocollage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class CollageEditorActivity extends ActionBarActivity implements View.OnTouchListener, ImageChooserListener {

	private static final String TAG = "Touch";
    private int PICK_FROM_GALLERY = 0;
    private int GALLERY_KITKAT_INTENT_CALLED = 3;
//	// These matrices will be used to move and zoom image
//	Matrix matrix = new Matrix();
//	Matrix savedMatrix = new Matrix();
//
//	Matrix matrix1 = new Matrix();
//	Matrix savedMatrix1 = new Matrix();
//
//	// We can be in one of these 3 states
//	final int NONE = 0;
//	final int DRAG = 1;
//	final int ZOOM = 2;
//	int mode = NONE;
//
//	// Remember some things for zooming
//	PointF start = new PointF();
//	PointF mid = new PointF();
//	float oldDist = 1f;

	ImageView iv_rotate_left, iv_rotate_right;

	private static CustomImageView mImage1;
	private static CustomImageView mImage2;
    private static ImageView imgAdd1, imgAdd2;

	boolean isSelected_one = true;

	RelativeLayout collageEditorRelativeLayout;

	ImageView save;
    ImageView imgBackground;
    int imgBackgroundWidth, imgBackgroundHeight;
    ImageView imgText;

	int imgid, typeId;

    private InterstitialAd interstitial;
    String fullScreenImageURL = "";

    private ImageChooserManager imageChooserManager;
    private int chooserType;
    String filePath;
    int currentSelectedImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collage_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);

		Intent i = getIntent();
		imgid = i.getIntExtra("frameId", 0);
        typeId = i.getIntExtra("typeId", 0);

        imgBackground = (ImageView) findViewById(R.id.imgBackground);
        imgBackground.setBackgroundResource(imgid);


        ViewTreeObserver vto = imgBackground.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @SuppressLint("NewApi")
//            @SuppressWarnings("deprecation")
//            @Override
//            public void onGlobalLayout() {
//                imgBackgroundWidth = imgBackground.getWidth();
//                imgBackgroundHeight = imgBackground.getHeight();
//                GenerateUI();
//
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
//                    imgBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                else
//                    imgBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//
//
//            }
//        });

        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imgBackgroundWidth = imgBackground.getWidth();
                imgBackgroundHeight = imgBackground.getHeight();
                GenerateUI();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    imgBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                else
                    imgBackground.getViewTreeObserver().removeOnPreDrawListener(this);

                return true;
            }
        });

        collageEditorRelativeLayout = (RelativeLayout) findViewById(R.id.collageEditorRelativeLayout);
	}

    public Bitmap buildText(String text)
    {
        Bitmap myBitmap = Bitmap.createBitmap(260, 184, Bitmap.Config.ARGB_4444);;
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        //To use own font
        Typeface face = Typeface.create("Arial", Typeface.NORMAL);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(face);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setTextSize(65);
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(text, 80, 60, paint);
        return myBitmap;
    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    void GenerateUI()
    {
        int boxHeight = 0;
        int boxWidth = 0;
        FrameLayout frm = new FrameLayout(CollageEditorActivity.this);
        FrameLayout.LayoutParams frmParams;
        LinearLayout.LayoutParams params1;
        FrameLayout.LayoutParams params2;

        switch (typeId) {

            //region thumb1
            case R.drawable.thumb1:
                boxHeight = imgBackgroundHeight / 2 - ConvertDotToPixel(80);
                boxWidth = imgBackgroundWidth - ConvertDotToPixel(80);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(40));
                frm.setY(ConvertDotToPixel(40));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);


                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(40));
                frm.setY(boxHeight + ConvertDotToPixel(120));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);



                collageEditorRelativeLayout.addView(frm);


                break;
            //endregion

            //region thumb2
            case R.drawable.thumb2:
                boxHeight = imgBackgroundHeight - ConvertDotToPixel(120);
                boxWidth = imgBackgroundWidth / 2 - ConvertDotToPixel(60);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(ConvertDotToPixel(60));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);


                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(boxWidth + ConvertDotToPixel(90));
                frm.setY(ConvertDotToPixel(60));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb3
            case R.drawable.thumb3:
                boxHeight = imgBackgroundHeight / 2 - ConvertDotToPixel(50);
                boxWidth = imgBackgroundWidth / 2 - ConvertDotToPixel(50);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(ConvertDotToPixel(30));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);


                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(boxWidth + ConvertDotToPixel(70));
                frm.setY(boxHeight + ConvertDotToPixel(70));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb4
            case R.drawable.thumb4:
                boxHeight = imgBackgroundHeight / 2 - ConvertDotToPixel(50);
                boxWidth = imgBackgroundWidth / 2 - ConvertDotToPixel(50);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(boxHeight + ConvertDotToPixel(70));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);

                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(boxWidth + ConvertDotToPixel(70));
                frm.setY(ConvertDotToPixel(30));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb5
            case R.drawable.thumb5:
                boxHeight = imgBackgroundWidth / 2 + ConvertDotToPixel(10);
                boxWidth = imgBackgroundWidth / 2 + ConvertDotToPixel(10);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(boxHeight + ConvertDotToPixel(140));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);

                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(boxWidth - ConvertDotToPixel(50));
                frm.setY(ConvertDotToPixel(30));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb6
            case R.drawable.thumb6:
                boxHeight = imgBackgroundWidth / 2 - ConvertDotToPixel(10);
                boxWidth = imgBackgroundWidth / 2 + ConvertDotToPixel(10);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(ConvertDotToPixel(30));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);

                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(boxWidth - ConvertDotToPixel(50));
                frm.setY(boxHeight + ConvertDotToPixel(140));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb7
            case R.drawable.thumb7:
                boxHeight = imgBackgroundHeight / 2 - ConvertDotToPixel(120);
                boxWidth = imgBackgroundWidth - ConvertDotToPixel(60);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(ConvertDotToPixel(30));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);

                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(boxHeight + ConvertDotToPixel(100));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

            //region thumb8
            case R.drawable.thumb8:
                boxHeight = imgBackgroundHeight / 2 - ConvertDotToPixel(120);
                boxWidth = imgBackgroundWidth - ConvertDotToPixel(60);

                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(boxHeight - ConvertDotToPixel(10));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage1 = new CustomImageView(CollageEditorActivity.this);
                mImage1.setScaleType(ImageView.ScaleType.MATRIX);
                mImage1.setLayoutParams(params1);
                frm.addView(mImage1);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd1 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd1.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd1.setLayoutParams(params2);
                frm.addView(imgAdd1);

                collageEditorRelativeLayout.addView(frm);

                frm = new FrameLayout(CollageEditorActivity.this);
                frmParams = new FrameLayout.LayoutParams(boxWidth, boxHeight);
                frmParams.gravity = Gravity.CENTER;

                frm.setLayoutParams(frmParams);
                frm.setBackgroundColor(Color.WHITE);
                frm.setForegroundGravity(Gravity.CENTER);
                frm.setX(ConvertDotToPixel(30));
                frm.setY(boxHeight + ConvertDotToPixel(200));

                params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params1.gravity = Gravity.CENTER;

                mImage2 = new CustomImageView(CollageEditorActivity.this);
                mImage2.setScaleType(ImageView.ScaleType.MATRIX);
                mImage2.setLayoutParams(params1);
                frm.addView(mImage2);

                params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                imgAdd2 = new ImageView(CollageEditorActivity.this);
                params2.width = ConvertDotToPixel(30);
                params2.height = ConvertDotToPixel(30);

                imgAdd2.setImageDrawable(getResources().getDrawable(R.drawable.add));
                imgAdd2.setLayoutParams(params2);
                frm.addView(imgAdd2);

                collageEditorRelativeLayout.addView(frm);

                break;
            //endregion

        }

        //save = (ImageView) findViewById(R.id.iv_btn_save);


        //        mImage1.setOnClickListener(new View.OnClickListener() {
        //
        //			@Override
        //			public void onClick(View v) {
        //				Intent i = new Intent(
        //						Intent.ACTION_PICK,
        //						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //				startActivityForResult(i, 0);
        //			}
        //		});

        imgAdd1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentSelectedImage = 1;
                chooseImage();

//                if (Build.VERSION.SDK_INT < 19){
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    // Always show the chooser (if there are multiple options available)
//                    startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_photo)), PICK_FROM_GALLERY);
//                }
//                else
//                {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("image/*");
//                    startActivityForResult(intent, PICK_FROM_GALLERY);
//                }

            }
        });

        imgAdd2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentSelectedImage = 2;
                chooseImage();
//                Intent intent = new Intent();
//                // Show only images, no videos or anything else
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                // Always show the chooser (if there are multiple options available)
//                startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_photo)), 1);
            }
        });


        //        save.setOnClickListener(new View.OnClickListener() {
        //
        //            @Override
        //            public void onClick(View v) {
        //                String filename = captureImage();
        //            }
        //        });
    }

    int ConvertDotToPixel(int dp)
    {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }



    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float px){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED && resultCode == RESULT_OK)
        {
            File file;
            if (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                if (imageChooserManager == null) {
                    reinitializeImageChooser();
                }
                imageChooserManager.submit(requestCode, data);
            }
//            switch (requestCode) {
//            case 0:
//
//                if (resultCode == RESULT_OK && intent != null) {
//                    try {
//                        Uri originalUri = intent.getData();
//
//                        String[] filePathColumn = { MediaColumns.DATA };
//
//                        Cursor cursor = getContentResolver().query(originalUri,
//                                filePathColumn, null, null, null);
//                        cursor.moveToFirst();
//
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String picturePath = cursor.getString(columnIndex);
//
//                        //Bitmap bmp = BitmapFactory.decodeFile(picturePath);
//
//                        Matrix mat = new Matrix();
//
//
//                        Bitmap bMapRotate = null; //Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
//
//                        bMapRotate = Common.scaleImage(getApplicationContext(), originalUri);
//                        cursor.close();
//                        Bitmap bmpNew = Bitmap.createBitmap(bMapRotate, 0, 0, bMapRotate.getWidth(), bMapRotate.getHeight(), mat, true);
//                        mImage1.setImageBitmap(bmpNew);
//                        //mImage1.setOnTouchListener(this);
//                        imgAdd1.setVisibility(View.INVISIBLE);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                break;
//            case 1:
//
//                if (resultCode == RESULT_OK && intent != null) {
//                    try {
//                        Uri selectedImage = intent.getData();
//                        String[] filePathColumn = { MediaColumns.DATA };
//
//                        Cursor cursor = getContentResolver().query(selectedImage,
//                                filePathColumn, null, null, null);
//                        cursor.moveToFirst();
//
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String picturePath = cursor.getString(columnIndex);
//
//                        //Bitmap bmp = BitmapFactory.decodeFile(picturePath);
//
//                        Matrix mat = new Matrix();
//
//
//                        Bitmap bMapRotate = null; //Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
//
//                        bMapRotate = Common.scaleImage(getApplicationContext(), selectedImage);
//                        cursor.close();
//                        Bitmap bmpNew = Bitmap.createBitmap(bMapRotate, 0, 0, bMapRotate.getWidth(), bMapRotate.getHeight(), mat, true);
//                        mImage2.setImageBitmap(bmpNew);
//                        //mImage2.setOnTouchListener(this);
//                        imgAdd2.setVisibility(View.INVISIBLE);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//                break;
//
//            }
        }
	}

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

	File file;

	private String captureImage() {
		OutputStream output;

		Calendar cal = Calendar.getInstance();

		Bitmap bitmap = Bitmap.createBitmap(collageEditorRelativeLayout.getWidth(), collageEditorRelativeLayout.getHeight(),
                Config.ARGB_8888);

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, collageEditorRelativeLayout.getWidth(),
                collageEditorRelativeLayout.getHeight());

		Canvas b = new Canvas(bitmap);
        collageEditorRelativeLayout.draw(b);

        File baseDir;

        if (android.os.Build.VERSION.SDK_INT < 8) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        File dir = new File(baseDir, Common.FOLDER_NAME);

        boolean success = true;
        if (!dir.exists())
            success = dir.mkdir();

		String tempImageName = cal.getTimeInMillis() + ".jpg";

		// Create a name for the saved image
		file = new File(dir, tempImageName);

        fullScreenImageURL = Uri.fromFile(file).toString();

		try {
			output = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output);
			output.flush();
			output.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

        showSuccessDialog(getString(R.string.success), getString(R.string.successMsg));

        displayInterstitial();

		return tempImageName;

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        showAlert(getString(R.string.exit), getString(R.string.exitMsg));
		return super.onKeyDown(keyCode, event);
		
	}

    public void showSuccessDialog(String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CollageEditorActivity.this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton(getString(R.string.action_share),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(fullScreenImageURL));
                        startActivity(Intent.createChooser(share, getString(R.string.action_share)));
                        finish();
                    }
                })
                .setPositiveButton(getString(R.string.action_done),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        finish();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        // show it
        alertDialog.show();
    }
	
	public void showAlert(String title,String msg) {

	  	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
	  			CollageEditorActivity.this);

	  		// set title
	  		alertDialogBuilder.setTitle(title);

	  		// set dialog message
	  		alertDialogBuilder
	  			.setMessage(msg)
	  			.setCancelable(true)
	  			.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {

                        finish();
					}
				})
	  			.setPositiveButton("No",new DialogInterface.OnClickListener() {
	  				public void onClick(DialogInterface dialog,int id) {
	  					// if this button is clicked, close
	  					// current activity

                        dialog.cancel();
	  				
	  				}
	  			  });

	  		
	  			// create alert dialog
	  			AlertDialog alertDialog = alertDialogBuilder.create();
	  			alertDialog.setCancelable(false);
	  			// show it
	  			alertDialog.show();
	  			
	  			Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	  			Button b1 = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	  			if(b != null)
	  			        b.setTextColor(Color.RED);
	  			if(b1 != null)
	  		        b1.setTextColor(Color.RED);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collage_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            showAlert(getString(R.string.exit), getString(R.string.exitMsg));
            return true;
        }
        else if (id == R.id.action_collage_editor_done) {
            String filename = captureImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Matrix matrix1 = new Matrix();
    private Matrix savedMatrix1 = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        if (v == imgText)
        {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    lastEvent = new float[4];
                    lastEvent[0] = event.getX(0);
                    lastEvent[1] = event.getX(1);
                    lastEvent[2] = event.getY(0);
                    lastEvent[3] = event.getY(1);
                    d = rotation(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        // ...
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY()
                                - start.y);
                    } else if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist = spacing(event);
                        matrix.set(savedMatrix);
                        if (newDist > 10f) {
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            float r = newRot - d;
                            matrix.postRotate(r, view.getMeasuredWidth() / 2,
                                    view.getMeasuredHeight() / 2);
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        }
        else
            return false;
    }

    public static int getExifOrientation(String filepath) {// YOUR MEDIA PATH AS STRING
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
Log.d("degree", String.valueOf(degree));
            }
        }
        return degree;
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (image != null) {
//                    textViewFile.setText(image.getFilePathOriginal());
//                    imageViewThumbnail.setImageURI(Uri.parse(new File(image
//                            .getFileThumbnail()).toString()));
//                    imageViewThumbSmall.setImageURI(Uri.parse(new File(image
//                            .getFileThumbnailSmall()).toString()));
//Log.d("ABC", "file:/" + image.getFilePathOriginal());
                    File f = new File("file://" + image.getFilePathOriginal());

                    Uri contentUri = Uri.fromFile(f);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);

                    try {
                        if (currentSelectedImage == 1) {
                            Matrix mat = new Matrix();


                            Bitmap bMapRotate = null; //Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

                            bMapRotate = Common.scaleImage(getApplicationContext(), Uri.parse("file://" + image.getFilePathOriginal()));

                            Bitmap bmpNew = Bitmap.createBitmap(bMapRotate, 0, 0, bMapRotate.getWidth(), bMapRotate.getHeight(), mat, true);
                            mImage1.setImageBitmap(bmpNew);
                            //mImage1.setOnTouchListener(this);
                            imgAdd1.setVisibility(View.INVISIBLE);
                        } else if (currentSelectedImage == 2) {
                            Matrix mat = new Matrix();


                            Bitmap bMapRotate = null; //Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

                            bMapRotate = Common.scaleImage(getApplicationContext(), Uri.parse("file://" + image.getFilePathOriginal()));

                            Bitmap bmpNew = Bitmap.createBitmap(bMapRotate, 0, 0, bMapRotate.getWidth(), bMapRotate.getHeight(), mat, true);
                            mImage2.setImageBitmap(bmpNew);
                            //mImage1.setOnTouchListener(this);
                            imgAdd2.setVisibility(View.INVISIBLE);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onError(String s) {
        Log.d("ABC", s);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("chooser_type")) {
            chooserType = savedInstanceState.getInt("chooser_type");
        }

        if (savedInstanceState.containsKey("media_path")) {
            filePath = savedInstanceState.getString("media_path");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
