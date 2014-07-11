package com.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactViewActivity extends Activity {
    LinearLayout phoneLayout, mailLayout, imLayout, addressLayout;
    TextView nameTx, phoneTx, numberTx, mailTx, mailInfo, imTx, imInfo,
            addressTx, address_info;
    Uri uri;
    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_view);
        uri = getIntent().getData();
        initView();
        mCursor = getCursor(uri);
        getContactInfo();
    }

    void initView() {
        phoneLayout = (LinearLayout) findViewById(R.id.phonelayout);
        mailLayout = (LinearLayout) findViewById(R.id.maillayout);
        imLayout = (LinearLayout) findViewById(R.id.imlayout);
        nameTx = (TextView) findViewById(R.id.txt_name);
        phoneTx = (TextView) findViewById(R.id.phonetx);
        numberTx = (TextView) findViewById(R.id.numbertx);
        mailTx = (TextView) findViewById(R.id.mailTx);
        mailInfo = (TextView) findViewById(R.id.mailinfo);
        imTx = (TextView) findViewById(R.id.imTx);
        imInfo = (TextView) findViewById(R.id.im_info);
    }

    private Cursor getCursor(Uri uri) {
        long contactId = ContentUris.parseId(uri);
        long raw_contact_id = queryForRawContactId(getContentResolver(),
                contactId);
        Cursor cursor = getContentResolver().query(
                android.provider.ContactsContract.Data.CONTENT_URI, null,
                Data.RAW_CONTACT_ID + "=?",
                new String[] { String.valueOf(raw_contact_id) }, null);
        return cursor;
    }

    private long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] { RawContacts._ID }, RawContacts.CONTACT_ID
                            + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(rawContactIdCursor
                        .getColumnIndex(RawContacts._ID));
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }

    void getContactInfo() {
        String im = null;
        String mail = null;
        while (mCursor.moveToNext()) {
            String mimeType = mCursor.getString(mCursor
                    .getColumnIndex(Data.MIMETYPE));
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                String name = mCursor.getString(mCursor
                        .getColumnIndex(StructuredName.DISPLAY_NAME));
                nameTx.setText(name);

            }
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                String number = mCursor.getString(mCursor
                        .getColumnIndex(Phone.NUMBER));
                numberTx.setText(number);
                int type = mCursor.getInt(mCursor.getColumnIndex(Phone.TYPE));
                phoneTx.setText(getResources().getString(
                        getStringFromPhoneType(type)));
            }

            if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                mail = mCursor.getString(mCursor.getColumnIndex(Email.DATA));
                mailInfo.setText(mail);
                int type = mCursor.getInt(mCursor.getColumnIndex(Email.TYPE));
                mailTx.setText(getResources().getString(
                        getStringFromEmailType(type)));
            }

            if (Im.MIMETYPE.equals(mimeType)) {
                im = mCursor.getString(mCursor.getColumnIndex(Im.DATA));
                imInfo.setText(im);
                int type = mCursor.getInt(mCursor.getColumnIndex(Im.TYPE));
                imTx.setText(getResources()
                        .getString(getStringFromImType(type)));

            }
        }
        if (mail == null || ("").equals(mail)) {
            mailLayout.setVisibility(View.INVISIBLE);
        } else {
            mailLayout.setVisibility(View.VISIBLE);
        }
        if (im == null || ("").equals(im)) {
            imLayout.setVisibility(View.INVISIBLE);
        } else {
            mailLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 根据数据的类型返回对应的字符串
     */
    private int getStringFromEmailType(int type) {
        switch (type) {
        case Email.TYPE_CUSTOM:// 特殊情况
            break;
        // 如果是上述情况，则用户自定义Lable(即data3字段)
        case Email.TYPE_HOME:
            type = R.string.email_home;
            break;
        case Email.TYPE_WORK:
            type = R.string.email_work;
            break;
        case Email.TYPE_MOBILE:
            type = R.string.email_mobile;
            break;
        case Email.TYPE_OTHER:
            type = R.string.email_other;
            break;
        }
        return type;
    }

    /**
     * 根据数据的类型返回对应的字符串
     */
    private int getStringFromImType(int protocol) {
        switch (protocol) {
        // Im.PROTOCOL_NETMEETING与Im.PROTOCOL_CUSTOM
        case Im.PROTOCOL_CUSTOM:// 特殊情况
            break;
        // 如果是上述情况，则用户自定义Custom_Protocol(即data6字段)
        case Im.PROTOCOL_AIM:
            protocol = R.string.chat_aim;
            break;
        case Im.PROTOCOL_GOOGLE_TALK:
            protocol = R.string.chat_gtalk;
            break;
        case Im.PROTOCOL_ICQ:
            protocol = R.string.chat_icq;
            break;
        case Im.PROTOCOL_JABBER:
            protocol = R.string.chat_jabber;
            break;
        case Im.PROTOCOL_MSN:
            protocol = R.string.chat_msn;
            break;
        case Im.PROTOCOL_QQ:
            protocol = R.string.chat_qq;
            break;
        case Im.PROTOCOL_SKYPE:
            protocol = R.string.chat_skype;
            break;
        case Im.PROTOCOL_YAHOO:
            protocol = R.string.chat_yahoo;
            break;
        }
        return protocol;
    }

    /**
     * 根据数据的类型返回对应的字符串
     */
    private int getStringFromPhoneType(int type) {
        switch (type) {
        case Phone.TYPE_CUSTOM:// 特殊情况
            break;
        case Phone.TYPE_ASSISTANT:// 特殊情况
            break;
        // 如果是上述两种情况，则用户自定义Lable(即data3字段)
        case Phone.TYPE_HOME:
            type = R.string.call_home;
            break;
        case Phone.TYPE_CALLBACK:
            type = R.string.call_callback;
            break;
        case Phone.TYPE_CAR:
            type = R.string.call_car;
            break;
        case Phone.TYPE_COMPANY_MAIN:
            type = R.string.call_company_main;
            break;
        case Phone.TYPE_FAX_HOME:
            type = R.string.call_fax_home;
            break;
        case Phone.TYPE_FAX_WORK:
            type = R.string.call_fax_work;
            break;
        case Phone.TYPE_ISDN:
            type = R.string.call_isdn;
            break;
        case Phone.TYPE_MAIN:
            type = R.string.call_main;
            break;
        case Phone.TYPE_MMS:
            type = R.string.call_mms;
            break;
        case Phone.TYPE_MOBILE:
            type = R.string.call_mobile;
            break;
        case Phone.TYPE_OTHER:
            type = R.string.call_other;
            break;
        case Phone.TYPE_OTHER_FAX:
            type = R.string.call_other_fax;
            break;
        case Phone.TYPE_PAGER:
            type = R.string.call_pager;
            break;
        case Phone.TYPE_RADIO:
            type = R.string.call_radio;
            break;
        case Phone.TYPE_TELEX:
            type = R.string.call_telex;
            break;
        case Phone.TYPE_TTY_TDD:
            type = R.string.call_tty_tdd;
            break;
        case Phone.TYPE_WORK:
            type = R.string.call_work;
            break;
        case Phone.TYPE_WORK_MOBILE:
            type = R.string.call_work_mobile;
            break;
        case Phone.TYPE_WORK_PAGER:
            type = R.string.call_work_pager;
            break;
        }
        return type;
    }

}
