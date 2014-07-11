package com.adapter;

import android.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter {
    private LayoutInflater mlayoutInflater;
    private ContactItem mContactItem;

    public MyCursorAdapter(Context context, Cursor c, boolean isListContacts) {
        super(context, c);
        mlayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = null;
        convertView = mlayoutInflater.inflate(
                android.R.layout.simple_list_item_2, null);
        mContactItem = new ContactItem();
        mContactItem.txName = (TextView) convertView
                .findViewById(android.R.id.text1);
        mContactItem.txNumber = (TextView) convertView
                .findViewById(android.R.id.text2);
        convertView.setTag(mContactItem);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d("zhl", "bindView");
        if (view != null) {
            mContactItem = (ContactItem) view.getTag();
            int nameIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
            if (!cursor.isNull(nameIndex)) {
                mContactItem.txName.setText(cursor.getString(nameIndex));
            }
            String contactId = cursor.getString(cursor
                    .getColumnIndex(Contacts._ID));
            String[] projection = { Phone._ID, Phone.NUMBER };
            String selection = Phone.CONTACT_ID + "=" + contactId;
            Cursor phoneCursor = context.getContentResolver().query(
                    Phone.CONTENT_URI, projection, selection, null, null);
            while (phoneCursor.moveToNext()) {
                int numberIndex = phoneCursor.getColumnIndex(Phone.NUMBER);
                if (!phoneCursor.isNull(numberIndex)) {
                    mContactItem.txNumber
                            .setText(phoneCursor.getString(numberIndex));
                }
            }
            phoneCursor.close();
        }
    }
}