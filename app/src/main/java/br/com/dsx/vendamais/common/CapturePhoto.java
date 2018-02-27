package br.com.dsx.vendamais.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.dsx.vendamais.R;

public class CapturePhoto {

	public static final int SHOT_IMAGE = 1;
	public static final int PICK_IMAGE = 2;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private Fragment fragment;
	private String mCurrentPhotoPath;
	private int actionCode;

	public CapturePhoto(Fragment fragment) {
		this.fragment = fragment;
		mAlbumStorageDirFactory = new AlbumDirFactory();
	}

	public int getActionCode() {
		return this.actionCode;
	}

	public void dispatchTakePictureIntent(int actionCode, int requestCode) {
		Intent takePictureIntent = null;

		this.actionCode = actionCode;

		switch(actionCode) {
		case SHOT_IMAGE:
			File f = null;
			takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		case PICK_IMAGE :
			takePictureIntent = new Intent(Intent.ACTION_PICK,
				     android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			break;
		default:
			break;
		} // switch

		if(takePictureIntent != null) {
			fragment.startActivityForResult(takePictureIntent, requestCode);
		}

	}


	public Bitmap bitmapFromCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			/* Decode the JPEG file into a Bitmap */
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			galleryAddPic();
			mCurrentPhotoPath = null;
			return bmp;
		}
		return null;
	}


	public void handleCameraPhoto(ImageView imageView) {
		if (mCurrentPhotoPath != null) {

			/* Decode the JPEG file into a Bitmap */
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();

			Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			Bitmap scaledBitmap = ImageUtil.createScaledBitmap(
					bmp,
					imageView.getWidth(),
					imageView.getHeight(),
	       		 	ImageUtil.ScalingLogic.FIT,
	       		 	ImageUtil.getFileOrientation(mCurrentPhotoPath)
	       		 	);
	        bmp.recycle();

			/* Associate the Bitmap to the ImageView */
			imageView.setImageBitmap(scaledBitmap);
			imageView.setVisibility(View.VISIBLE);

			galleryAddPic();
			mCurrentPhotoPath = null;
		}
	}


	public Bitmap bitmapFromMediaPhoto(Uri targetUri) {
		try {
			Bitmap bmp = MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), targetUri);
			return bmp;
		} catch (Exception e) { }
		return null;
	}


	public void handleMediaPhoto(Uri targetUri, ImageView imageView) {

		try {
			Bitmap bmp = MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), targetUri);
			Bitmap scaledBitmap = ImageUtil.createScaledBitmap(
					bmp,
					imageView.getWidth(),
					imageView.getHeight(),
	                ImageUtil.ScalingLogic.FIT,
	                ImageUtil.getGalleryOrientation(fragment.getActivity(), targetUri));
			bmp.recycle();
			imageView.setImageBitmap(scaledBitmap);
			imageView.setVisibility(View.VISIBLE);
		} catch (Exception e) { }
	}

//	private void setPic() {
//
//		/* There isn't enough memory to open up more than a couple camera photos */
//		/* So pre-scale the target bitmap into which the file is decoded */
//
//		/* Get the size of the ImageView */
//		int targetW = imageView.getWidth();
//		int targetH = imageView.getHeight();
//
//		/* Get the size of the image */
//		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//		bmOptions.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//		int photoW = bmOptions.outWidth;
//		int photoH = bmOptions.outHeight;
//		
//		/* Figure out which way needs to be reduced less */
//		int scaleFactor = 1;
//		if ((targetW > 0) || (targetH > 0)) {
//			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
//		}
//
//		/* Set bitmap options to scale the image decode target */
//		bmOptions.inJustDecodeBounds = false;
//		bmOptions.inSampleSize = scaleFactor;
//		bmOptions.inPurgeable = true;
//
//		/* Decode the JPEG file into a Bitmap */
//		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//		
//		/* Associate the Bitmap to the ImageView */
//		imageView.setImageBitmap(bitmap);
//		imageView.setVisibility(View.VISIBLE);
//	}

	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
		fragment.getActivity().sendBroadcast(mediaScanIntent);
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private String getAlbumName() {
		return fragment.getActivity().getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(fragment.getActivity().getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		return storageDir;
	}

}
