package org.openintents.safe.dialog;

import org.openintents.distribution.GetFromMarketDialog;
import org.openintents.distribution.RD;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.IntentUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class DialogHostingActivity extends Activity {

	private static final String TAG = "FilenameActivity";

	public static final int DIALOG_ID_SAVE = 1;
	public static final int DIALOG_ID_OPEN = 2;
	public static final int DIALOG_ID_NO_FILE_MANAGER_AVAILABLE = 3;
	public static final int DIALOG_ID_ALLOW_EXTERNAL_ACCESS = 4;
	public static final int DIALOG_ID_FIRST_TIME_WARNING = 5;
	
	public static final String EXTRA_DIALOG_ID = "org.openintents.notepad.extra.dialog_id";

	/**
	 * Whether dialog is simply pausing while hidden by another activity
	 * or when configuration changes.
	 * If this is false, then we can safely finish this activity if a dialog
	 * gets dismissed.
	 */
	private boolean mIsPausing = false;
	
    EditText mEditText;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i != null && savedInstanceState == null) {
			int dialogId = i.getIntExtra(EXTRA_DIALOG_ID, 0);
			switch (dialogId) {
			case DIALOG_ID_SAVE:
				Log.i(TAG, "Show Save dialog");
				saveFile();
				break;
			case DIALOG_ID_OPEN:
				Log.i(TAG, "Show Save dialog");
				openFile();
				break;
			case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
				Log.i(TAG, "Show no file manager dialog");
				showDialog(DIALOG_ID_NO_FILE_MANAGER_AVAILABLE);
			case DIALOG_ID_ALLOW_EXTERNAL_ACCESS:
				Log.i(TAG, "Show allow access dialog");
				showDialog(DIALOG_ID_ALLOW_EXTERNAL_ACCESS);
				break;
			case DIALOG_ID_FIRST_TIME_WARNING:
				Log.i(TAG, "Show first time warning dialog");
				showDialog(DIALOG_ID_FIRST_TIME_WARNING);
				break;
			}
		}
	}


	/**
	 * 
	 */
	private void saveFile() {
		
		// Check whether intent exists
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(getIntent().getData());
		if (IntentUtils.isIntentAvailable(this, intent)) {
			/*
			intent.putExtra(NotePadIntents.EXTRA_URI, getIntent().getStringExtra(NotePadIntents.EXTRA_URI));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, getText(R.string.menu_save_to_sdcard));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getText(R.string.save));
			*/
			intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			startActivity(intent);
			finish();
		} else {
			showDialog(DIALOG_ID_SAVE);
		}
	}
	

	private void openFile() {
		
		// Check whether intent exists
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(getIntent().getData());
		if (IntentUtils.isIntentAvailable(this, intent)) {
			/*
			intent.putExtra(NotePadIntents.EXTRA_URI, getIntent().getStringExtra(NotePadIntents.EXTRA_URI));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, getText(R.string.menu_open_from_sdcard));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getText(R.string.open));
			intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			*/
			startActivity(intent);
			finish();
		} else {
			showDialog(DIALOG_ID_OPEN);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ID_SAVE:
			return new FilenameDialog(this);
		case DIALOG_ID_OPEN:
			return new FilenameDialog(this);
		case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
			Log.i(TAG, "fmd - create");
			return new GetFromMarketDialog(this, 
					RD.string.filemanager_not_available,
					RD.string.filemanager_get_oi_filemanager,
					RD.string.filemanager_market_uri);
		case DIALOG_ID_ALLOW_EXTERNAL_ACCESS:
			return new AllowExternalAccessDialog(this);
		case DIALOG_ID_FIRST_TIME_WARNING:
			return new FirstTimeWarningDialog(this);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		FilenameDialog fd;
		
		dialog.setOnDismissListener(mDismissListener);
		
		switch (id) {
		case DIALOG_ID_SAVE:
			fd = (FilenameDialog) dialog;
			//fd.setTitle(R.string.menu_save_to_sdcard);
			
			break;
		case DIALOG_ID_OPEN:
			fd = (FilenameDialog) dialog;
			//fd.setTitle(R.string.menu_open_from_sdcard);
			break;
			
		case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
			Log.i(TAG, "fmd - prepare");
			/*
			GetFileManagerFromMarketDialog gd = (GetFileManagerFromMarketDialog) dialog;
			gd.setMessageResource(R.string.filemanager_not_available);
			gd.setInfoResources(R.string.filemanager_get_oi_filemanager, 
					R.string.filemanager_market_uri, 
					R.drawable.ic_launcher_folder_small, 
					R.string.update_error);
					*/
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {
		
		public void onDismiss(DialogInterface dialoginterface) {
			Log.i(TAG, "Dialog dismissed");
			if (!mIsPausing) {
				// Dialog has been dismissed by user.
				DialogHostingActivity.this.finish();
			} else {
				// Probably just a screen orientation change. Don't finish yet.
				// Dialog has been dismissed by system.
			}
		}
		
	};

	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mIsPausing = false;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mIsPausing = true;
	}
	
	
	
}
