package com.activeandroid.test.content;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.test.MockModel;

/**
 * {@link AndroidTestCase} for {@link ContentProvider}.
 * 
 * <p>This case is not based on {@link android.test.ProviderTestCase2} since it's mock {@link Context} cannot be stubbed. 
 * 
 * @author pelzrutsche
 *
 */
public class ContentProviderTest extends AndroidTestCase {

	private static final String AUTHORITY = "com.activeandroid.test";

	private ContentProvider mProvider;
	private MockContentResolver mMockResolver;
	private IsolatedContext mMockContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mMockResolver = new MockContentResolver();
		Context delegatingContext = new ContextWrapper(getContext()) {
			@Override
			public String getPackageName() {
				return AUTHORITY;
			}
		};
		
		mProvider = new ContentProvider();
		
		mMockContext = new IsolatedContext(mMockResolver, delegatingContext);
		mMockResolver.addProvider(AUTHORITY, mProvider);
		mProvider.attachInfo(mMockContext, null);
	}
	
	/**
	 * Tests the creation of a URI for {@link MockModel} and type resolution through {@link ContentProvider#getType(Uri)}
	 */
	public void testCreateUri() {
		Uri uri = ContentProvider.createUri(MockModel.class, null);
		assertEquals("unexpected authority", AUTHORITY, uri.getAuthority());
		assertEquals("unexpected scheme", ContentResolver.SCHEME_CONTENT, uri.getScheme());
		
		String type = mProvider.getType(uri);
		
		assertTrue("unexpected path", type.contains(MockModel.class.getName()));
	}
	
	/**
	 * Tests inserting a {@link MockModel}.
	 */
	public void testInsert() {
		ContentValues values = new ContentValues();
		final String name = getName();
		values.put(MockModel.Columns.NAME, name);
		final Uri result = mProvider.insert(ContentProvider.createUri(MockModel.class, null), values);
		
		final long id = ContentUris.parseId(result);
		assertTrue("expected ID in URI", id > -1);
		
		final MockModel model = Model.load(MockModel.class, id);
		assertEquals("unexpected name", name, model.getName());
	}
	
	/**
	 * Tests deleting a {@link MockModel}.
	 */
	public void testDelete() {
		MockModel model = new MockModel();
		model.setName(getName());
		model.save();
		
		model = Model.load(MockModel.class, model.getId());
		
		assertNotNull("failed to save model", model);
		
		mProvider.delete(ContentProvider.createUri(MockModel.class, model.getId()), null, null);
		
		model = Model.load(MockModel.class, model.getId());
		
		assertNull("failed to delete model", model);
	}
	
	/**
	 * Tests querying for a {@link MockModel}.
	 */
	public void testQuery() {
		MockModel model = new MockModel();
		model.setName(getName());
		model.save();
		
		model = Model.load(MockModel.class, model.getId());
		
		assertNotNull("failed to save model", model);
		
		String[] projection = new String[]{ MockModel.Columns.ID };
		String[] selectionArgs = new String[]{ Long.toString(model.getId()) };
		Cursor cursor = mProvider.query(ContentProvider.createUri(MockModel.class, model.getId()), projection, MockModel.Columns.ID + " = ?", selectionArgs, null);
		
		if (!cursor.moveToNext()) {
			fail("expected one row");
		}
	}

}
