package com.ui;



import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.adapter.MyCursorAdapter;
import java.lang.reflect.Field;


public class ContactListActivity extends Activity {
    
    String[] projection = new String[] { Contacts._ID, Contacts.DISPLAY_NAME,
            Contacts.HAS_PHONE_NUMBER, Contacts.LOOKUP_KEY };
    private String sortOrder = Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

    private static final int EDITOR = Menu.FIRST + 1;
    private static final int DELETE = Menu.FIRST + 2;
    private static final int ADD = Menu.FIRST + 5;
    private CursorAdapter myAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listcontacts);
        listView = (ListView) findViewById(R.id.listView);
        Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI,
                projection, null, null, sortOrder);
        startManagingCursor(cursor);
        myAdapter = new MyCursorAdapter(this, cursor, false);
        listView.setAdapter(myAdapter);
        listView.setOnCreateContextMenuListener(this);
        listView.setOnItemClickListener(itemClickListener);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setOverflowShowingAlways();
    }

    private void setOverflowShowingAlways() {  
        try {  
            ViewConfiguration config = ViewConfiguration.get(this);  
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
            menuKeyField.setAccessible(true);  
            menuKeyField.setBoolean(config, false);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, ADD, 0, getString(R.string.menu_add)).setIcon(
//                android.R.drawable.ic_menu_add);
        
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_compose:
            Intent i = new Intent(Intent.ACTION_INSERT,Contacts.CONTENT_URI);
            i.setClass(ContactListActivity.this, ContactInsertActivity.class);
            startActivity(i);
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info;
        try {
            info = (AdapterContextMenuInfo) menuInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Cursor c = (Cursor) myAdapter.getItem(info.position);
        menu.setHeaderTitle(c.getString(1));
        menu.add(0, EDITOR, 0, getString(R.string.menu_edit));
        menu.add(0, DELETE, 0, getString(R.string.menu_delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info;
        try {
            info = (AdapterContextMenuInfo) item.getMenuInfo();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        getContactUri(info.position);
        Uri uri = getContactUri(info.position);
        Cursor c = (Cursor) myAdapter.getItem(info.position);
        switch (item.getItemId()) {
        case EDITOR:
            Intent i = new Intent(Intent.ACTION_EDIT, uri);
            i.setClass(ContactListActivity.this, ContactInsertActivity.class);
            startActivity(i);
            return true;

        case DELETE:
            getContentResolver().delete(getContactUri(info.position), null,
                    null);
            Toast.makeText(this, c.getString(1) + "被删除", Toast.LENGTH_SHORT)
                    .show();
            return true;
        default:
            break;
        }
        return super.onContextItemSelected(item);
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
            Log.d("zhl","position:"+position+"id"+id);
            Intent intent = new Intent();
            intent.setClass(ContactListActivity.this, ContactViewActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    };

    private Uri getContactUri(int position) {
        Cursor cursor = (Cursor) myAdapter.getItem(position);
        if (cursor == null)
            return null;
        long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
        String lookupkey = cursor.getString(cursor
                .getColumnIndex(Contacts.LOOKUP_KEY));
        Uri lookupUri = Contacts.getLookupUri(contactId, lookupkey);
        return lookupUri;
    }

}
