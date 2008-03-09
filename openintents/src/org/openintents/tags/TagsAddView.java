/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.tags;

import org.openintents.R;
import org.openintents.provider.Tag;
import org.openintents.provider.ContentIndex.Dir;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;
import org.openintents.tags.content.ContentListRow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Displays selected content and asks for tag to be added.
 * 
 * When this activity is called, getStringExtra(Tags.QUERY_URI)
 * should contain the URI to be tagged.
 */
public class TagsAddView extends Activity {

	/** tag for logging */
	private static final String TAG = "tagView";

	private static final int MENU_VIEW_TAG = 1;

	private static final int REQUEST_DIR_PICK = 1;
	private static final int REQUEST_CONTENT_PICK = REQUEST_DIR_PICK + 1;

	/**
	 * Contents fields to be used in SQL queries.
	 */
	private static final String[] TAGS_PROJECTION = new String[] {
    	Contents._ID, 
    	Contents.URI, 
    	Contents.TYPE
    };

	/**
	 * The column in the cursor c that contains Contents.URI.
	 * (Careful: counting is related to the java list above
	 *           and starts with 0).
	 */
	private static int column_Contents_URI = 1;
	
	private ListView mTagsListView;
	private AutoCompleteTextView mTagFilter;
	private String mFilter = null;
	private ContentListRow mContentRow;
	private TextView mTagsList;
	private String mUri;

	private Tag mTag;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tags_add);

		mTag = new Tag(this);
		mTagFilter = (AutoCompleteTextView) findViewById(R.id.tag_filter);
		mTagsList = (TextView) findViewById(R.id.tag_filter);

		RelativeLayout content = (RelativeLayout) findViewById(R.id.content);
		mContentRow = new ContentListRow(this);
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_WITH_PARENT_LEFT);
		content.addView(mContentRow, layout);
		
		mUri = getIntent().getStringExtra(Tags.QUERY_URI);

		if (mUri != null) {			
			mContentRow.updateContentFrom(mUri);
		}

		String tag = getIntent().getStringExtra(Tags.QUERY_TAG);
		if (tag != null) {
			mTagFilter.setText(tag);
		}

		Button button = (Button) findViewById(R.id.tags_ok_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mTag.insertTag(mTagFilter.getText().toString(), mUri);
				setResult(Activity.RESULT_OK);
				finish();
			}

		});

		ImageButton searchButton = (ImageButton) findViewById(R.id.tags_search_button);
		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				Intent intent = new Intent(Intent.PICK_ACTION, Dir.CONTENT_URI);
				startSubActivity(intent, REQUEST_DIR_PICK);
			}

		});

		fillDataTagFilter();
	}

	private void fillDataTagFilter() {
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Contents.CONTENT_URI,
				TAGS_PROJECTION,
				Contents.TYPE + " like 'TAG%'", null, Contents.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mTagFilter.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		/*
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, c,
				new String[] { Contents.URI },
				new int[] { android.R.id.text1 });
				*/
		TagsCursorAdapter adapter = new TagsCursorAdapter(c, this);
		mTagFilter.setAdapter(adapter);

	}


    // XXX compiler bug in javac 1.5.0_07-164, we need to implement Filterable
    // to make compilation work
    public static class TagsCursorAdapter
            extends CursorAdapter implements Filterable {
    	
        public TagsCursorAdapter(Cursor c, Context context) {
            super(c, context);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
        	// TODO TextView too small to be clickable. If new AutoCompleteTextView
        	//      ApiDemo is available, the following should be updated.
        	//      (look at AutoComplete4.java)
            /*
        	TextView view = new TextView(context);
            view.setText(cursor, column_Contents_URI);
            */
        	
            ViewInflate inf = 
    			(ViewInflate)context.getSystemService(INFLATE_SERVICE); 
    		View rowView = inf.inflate(android.R.layout.simple_list_item_1, null, null); 
    		
    		TextView view = (TextView) rowView.findViewById(android.R.id.text1);
            view.setText(cursor, column_Contents_URI);
            
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor, column_Contents_URI);
        }

        @Override
        protected String convertToString(Cursor cursor) {
            return cursor.getString(column_Contents_URI);
        }

        @Override
        protected Cursor runQuery(CharSequence constraint) {
            StringBuilder buffer = null;
            String[] args = null;
            if (constraint != null) {
                buffer = new StringBuilder();
                buffer.append("UPPER(");
                buffer.append(Contents.URI);
                buffer.append(") GLOB ?");
                args = new String[] { constraint.toString().toUpperCase() + "*" };
            }
            if (buffer == null) {
            	buffer = new StringBuilder();
            } else {
            	buffer.append(" AND ");
            }
            // After the last if-construct, buffer is always != null.
            buffer.append (Contents.TYPE + " like 'TAG%'");
            
            // "buffer == null ?" kept below only for possible future changes.
            return mContent.query(Contents.CONTENT_URI, TAGS_PROJECTION,
                    buffer == null ? null : buffer.toString(), args,
                    Contents.DEFAULT_SORT_ORDER);
        }

        private ContentResolver mContent;        
    }

    
	
	/*
	private void fillDataTags() {

		String filter = null;
		String[] filterArray = null;
		if (mFilter != null) {
			filter = "uri_2 = ?";
			filterArray = new String[] { mFilter };
		}
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(
				Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.TAG_ID, Tags.CONTENT_ID,
						Tags.URI_1, Tags.URI_2 }, filter, filterArray,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mTagsListView.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		StringBuilder sb = new StringBuilder();
		while (c.next()) {
			sb.append(c.getString(3)).append(" ");
		}

		// remove extra blank at end of string.
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		mTagsList.setText(sb.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_VIEW_TAG, R.string.tags_view_tag);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, Item item) {

		super.onMenuItemSelected(featureId, item);

		switch (featureId) {
		case MENU_VIEW_TAG:
			Intent intent = new Intent(Intent.VIEW_ACTION, Tags.CONTENT_URI)
					.putExtra(Tags.QUERY_TAG, mTagFilter.getText().toString());
			startActivity(intent);
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle bundle) {
		super.onActivityResult(requestCode, resultCode, data, bundle);

		switch (requestCode) {
		case REQUEST_DIR_PICK:
			if (data != null) {
				// data is the picked content directory
				Uri uri = Uri.parse(data);
				Intent intent = new Intent(Intent.PICK_ACTION, uri);
				if (getPackageManager().resolveActivity(intent, 0) != null) {
					startSubActivity(intent, REQUEST_CONTENT_PICK);
				} else {
					AlertDialog.show(this, "info", 0, "no pick activity for "
							+ data, "ok", true);
				}
			}
			break;
		case REQUEST_CONTENT_PICK:
			if (data != null){
				mUri = data;
				mContentRow.updateContentFrom(mUri);				
			}

		}

	}
	*/
}