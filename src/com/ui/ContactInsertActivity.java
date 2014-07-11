package com.ui;

import android.util.Log;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Data;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;

public class ContactInsertActivity extends Activity {
    
    private Spinner phoneSpinner,mailSpinner,imSpinner;
    private EditText edt,phoneEdit,mailEdit,imEdit;
    private Button btnOk,btnCancel;
    private String editName,phoneNumber,mailInfo,imInfo;
    private boolean isInsert;
    private Uri uri;
    private long dataId;
    int phoneTypePosion,emailPosion,imPosion;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_insert);
        initView();
        String action = getIntent().getAction();
        if(action.equals(Intent.ACTION_INSERT)) {
            isInsert = true;
        } else {
            uri = getIntent().getData();
            isInsert = false;
        }
      
        if(!isInsert) {
            showInfo();
        }
    }
    void initView() {
        edt = (EditText)findViewById(R.id.edit_name);
        phoneEdit  = (EditText)findViewById(R.id.edit_phone);
        mailEdit = (EditText)findViewById(R.id.edit_mail);
        imEdit  = (EditText)findViewById(R.id.edit_im);
        phoneSpinner = (Spinner)findViewById(R.id.phonespinner);
        mailSpinner = (Spinner)findViewById(R.id.mailspinner);
        imSpinner = (Spinner)findViewById(R.id.imspinner);
        btnOk = (Button)findViewById(R.id.btnOk);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
        ArrayAdapter<CharSequence> SpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.phoneTypes, android.R.layout.simple_spinner_item);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phoneSpinner.setAdapter(SpinnerAdapter);
        phoneSpinner.setOnItemSelectedListener(onItemSelectedListener);
        
        SpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.emailTypes, android.R.layout.simple_spinner_item);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mailSpinner.setAdapter(SpinnerAdapter);
        mailSpinner.setOnItemSelectedListener(onItemSelectedListener);
        
        SpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.imTypes, android.R.layout.simple_spinner_item);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imSpinner.setAdapter(SpinnerAdapter);
        imSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }
    
    void showInfo(){
        Cursor cursor =getCursor(uri);
        while (cursor.moveToNext()) {
            String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                String name = cursor
                        .getString(cursor.getColumnIndex(StructuredName.DISPLAY_NAME));
                edt.setText(name);
               
            }
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                String number = cursor
                        .getString(cursor.getColumnIndex(Phone.NUMBER));
                phoneEdit.setText(number);
                int type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
                phoneSpinner.setSelection(type-1);
            }
            
            if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                String mail = cursor
                        .getString(cursor.getColumnIndex(Email.DATA));
                mailEdit.setText(mail);
                int type = cursor.getInt(cursor.getColumnIndex(Email.TYPE));
                mailSpinner.setSelection(type-1);
            }
            
            if (Im.MIMETYPE.equals(mimeType)) {
                String im = cursor
                        .getString(cursor.getColumnIndex(Im.DATA));
                imEdit.setText(im);
                int type = cursor.getInt(cursor.getColumnIndex(Im.TYPE));
                imSpinner.setSelection(type-1);
            }
        }
    }
    
    
    
    OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            switch (parent.getId()) {
            case R.id.phonespinner:
                phoneTypePosion = position;
                break;
                
            case R.id.mailspinner:
                emailPosion= position;
                break;
                
            case R.id.imspinner:
                imPosion = position;
                break;
            default:
                break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btnOk:
                if(isInsert) {
                    save();
                } else {
                    updateContact();
                    finish();
                }
                break;
            case R.id.btnCancel:
                finish();
                break;
            default:
                break;
            }
        }
    };
    
    void save() {
        editName = edt.getText().toString();
        phoneNumber = phoneEdit.getText().toString();
        mailInfo = mailEdit.getText().toString();
        imInfo = imEdit.getText().toString();
//        addressInfo = addressEditr.getText().toString();
        if (editName == null || phoneNumber == null || ("").equals(editName)||("").equals(phoneNumber)) {
            Toast.makeText(this, "没有输入姓名或者号码，不保存", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        if (isInsert) {
            int rawContactInsertIndex = 0;
            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, editName)
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, phoneNumber)
                    .withValue(Phone.TYPE, getPhoneItemType(phoneTypePosion+1))
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                    .withValue(Email.DATA, mailInfo)
                    .withValue(Email.TYPE, getEmailItemType(emailPosion+1))
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Im.MIMETYPE)
                    .withValue(Im.DATA, imInfo)
                    .withValue(Im.TYPE, getImItemType(imPosion+1))
                    .build());
            
            
            Log.d("06500",""+getPhoneItemType(phoneTypePosion+1)+" "+ getEmailItemType(emailPosion+1)+" "+getImItemType(imPosion+1));
        }
//        } else {
//            ops.add(ContentProviderOperation
//                    .newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
//                    .withSelection(Data.RAW_CONTACT_ID+ "=?", new String[] {
//                            String.valueOf(dataId)
//                    })
//                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
//                    .withValue(StructuredName.DISPLAY_NAME, editName)
//                    .build());
//            ops.add(ContentProviderOperation
//                    .newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
//                    .withSelection(Data.RAW_CONTACT_ID + "=?", new String[] {
//                            String.valueOf(dataId)
//                    })
//                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
//                    .withValue(Phone.NUMBER, phoneNumber)
//                    .withValue(Phone.TYPE, imTypeLable)
//                    .build());
//            ops.add(ContentProviderOperation
//                    .newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
//                    .withSelection(Data.RAW_CONTACT_ID + "=?", new String[] {
//                            String.valueOf(dataId)
//                    })
//                    .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
//                    .withValue(Email.DATA, mailInfo)
//                    .withValue(Email.TYPE, mailTypeLable)
//                    .build());
//            ops.add(ContentProviderOperation
//                    .newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
//                    .withSelection(Data.RAW_CONTACT_ID+ "=?", new String[] {
//                            String.valueOf(dataId)
//                    })
//                    .withValue(Data.MIMETYPE, Im.MIMETYPE)
//                    .withValue(Im.DATA, imInfo)
//                    .withValue(Im.TYPE, imTypeLable)
//                    .build());
//        }
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            finish();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // ContentValues cValues = new ContentValues();
        // Uri insertUir = getContentResolver().insert(RawContacts.CONTENT_URI,
        // cValues);
        //
        // long rawContactsId = ContentUris.parseId(insertUir);
        // cValues.clear();
        // cValues.put(RawContacts.CONTACT_ID, rawContactsId);
        // cValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        // cValues.put(StructuredName.GIVEN_NAME,editName);
        // getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,cValues);
        //
        // cValues.put(RawContacts.CONTACT_ID, rawContactsId);
        // cValues.put(Data.MIMETYPE, Phone.MIMETYPE);
        // cValues.put(Phone.NUMBER,phoneNumber);
        // cValues.put(Phone.TYPE, phoneTypeLable);
        // getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,cValues);
        //
        // cValues.put(RawContacts.CONTACT_ID, rawContactsId);
        // cValues.put(Data.MIMETYPE,Email.MIMETYPE);
        // cValues.put(Email.ADDRESS,mailInfo);
        // cValues.put(Email.TYPE, mailTypeLable);
        // getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,cValues);
        //
        // cValues.put(RawContacts.CONTACT_ID, rawContactsId);
        // cValues.put(Data.MIMETYPE, Im.MIMETYPE);
        // cValues.put(Im.DATA,imInfo);
        // cValues.put(Im.TYPE,imTypeLable);
        // getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,cValues);

    }
    
    void updateContact() {
        editName = edt.getText().toString();
        phoneNumber = phoneEdit.getText().toString();
        mailInfo = mailEdit.getText().toString();
        imInfo = imEdit.getText().toString();
//        addressInfo = addressEditr.getText().toString();
        if (editName == null || phoneNumber == null  ) {
            Toast.makeText(this, "没有输入姓名或者号码，不保存", Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues cValues = new ContentValues();
        cValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        cValues.put(StructuredName.DISPLAY_NAME, editName);
        getContentResolver().update(android.provider.ContactsContract.Data.CONTENT_URI, cValues,
                Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
                new String[] {
                        String.valueOf(dataId), StructuredName.CONTENT_ITEM_TYPE
                });
        cValues.clear();
        
        cValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        cValues.put(Phone.NUMBER, phoneNumber);
        cValues.put(Phone.TYPE, getPhoneItemType(phoneTypePosion+1));
        getContentResolver().update(android.provider.ContactsContract.Data.CONTENT_URI, cValues,
                Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
                new String[] {
                        String.valueOf(dataId), Phone.CONTENT_ITEM_TYPE
                });
       cValues.clear();
       
      cValues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
      cValues.put(Email.ADDRESS, mailInfo);
      cValues.put(Email.TYPE, getEmailItemType(phoneTypePosion+1));
      getContentResolver().update(android.provider.ContactsContract.Data.CONTENT_URI, cValues,
              Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
              new String[] {
                      String.valueOf(dataId), Email.CONTENT_ITEM_TYPE
              });
      cValues.clear();
      cValues.put(Data.MIMETYPE, Im.MIMETYPE);
      cValues.put(Im.DATA, imInfo);
      cValues.put(Im.TYPE, getImItemType(imPosion+1));
      getContentResolver().update(android.provider.ContactsContract.Data.CONTENT_URI, cValues,
              Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
              new String[] {
                      String.valueOf(dataId), Im.MIMETYPE
              });
      
      
      Log.d("06500",""+getPhoneItemType(phoneTypePosion+1)+" "+ getEmailItemType(emailPosion+1)+" "+getImItemType(imPosion+1));
//        cValues.put(RawContacts.CONTACT_ID, dataId);
//        cValues.put(Data.MIMETYPE, Phone.MIMETYPE);
//        cValues.put(Phone.NUMBER, phoneNumber);
//        cValues.put(Phone.TYPE, phoneTypeLable);
//        getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, cValues);
//
//        cValues.put(RawContacts.CONTACT_ID, dataId);
//        cValues.put(Data.MIMETYPE, Email.MIMETYPE);
//        cValues.put(Email.ADDRESS, mailInfo);
//        cValues.put(Email.TYPE, mailTypeLable);
//        getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, cValues);
//
//        cValues.put(RawContacts.CONTACT_ID, dataId);
//        cValues.put(Data.MIMETYPE, Im.MIMETYPE);
//        cValues.put(Im.DATA, imInfo);
//        cValues.put(Im.TYPE, imTypeLable);
//        getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, cValues);
    }
    
    
    private Cursor getCursor (Uri uri) {
        long contactId = ContentUris.parseId(uri);
        dataId =  queryForRawContactId(getContentResolver(),contactId);
        Cursor cursor = getContentResolver().query(android.provider.ContactsContract.Data.CONTENT_URI,null,Data.RAW_CONTACT_ID+"=?",new String[]{String.valueOf(dataId)},null);
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
                rawContactId = rawContactIdCursor.getLong(rawContactIdCursor.getColumnIndex(RawContacts._ID));
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    
    
    public int getPhoneItemType(int position) {
        int phoneType = 0;
        switch (position) {
        case 1:
            phoneType = Phone.TYPE_HOME;
            break;
        case 2:
            phoneType = Phone.TYPE_MOBILE;
            break;
        case 3:
            phoneType = Phone.TYPE_WORK;
            break;
        case 4:
            phoneType = Phone.TYPE_FAX_WORK;
            break;
        case 5:
            phoneType = Phone.TYPE_FAX_HOME;
            break;
        case 6:
            phoneType = Phone.TYPE_PAGER;
            break;
        case 7:
            phoneType = Phone.TYPE_OTHER;
            break;
        case 8:
            phoneType = Phone.TYPE_CALLBACK;
            break;
        case 9:
            phoneType = Phone.TYPE_CAR;
            break;
        case 10:
            phoneType = Phone.TYPE_COMPANY_MAIN;
            break;
        case 11:
            phoneType = Phone.TYPE_ISDN;
            break;
        case 12:
            phoneType = Phone.TYPE_MAIN;
            break;
        case 13:
            phoneType = Phone.TYPE_OTHER_FAX;
            break;
        case 14:
            phoneType = Phone.TYPE_RADIO;
            break;
        case 15:
            phoneType = Phone.TYPE_TELEX;
            break;
        case 16:
            phoneType = Phone.TYPE_TTY_TDD;
            break;
        case 17:
            phoneType = Phone.TYPE_WORK_MOBILE;
            break;
        case 18:
            phoneType = Phone.TYPE_WORK_PAGER;
            break;
        case 19:
            phoneType = Phone.TYPE_ASSISTANT;
            break;
        case 20:// 用户自定义Lable(即data3字段)
            phoneType = Phone.TYPE_MMS;
            break;
        }
        return phoneType;
    }
    
    public int getEmailItemType(int position) {
        int emailType = 0;
        switch (position) {
        case 1:
            emailType = Email.TYPE_HOME;
            break;
        case 2:
            emailType = Email.TYPE_WORK;
            break;
        case 3:
            emailType = Email.TYPE_OTHER;
            break;
        case 4:
            emailType = Email.TYPE_MOBILE;
            break;
        case 5:// 特殊情况
            emailType = Email.TYPE_CUSTOM;
            break;
        }
        return emailType;
    }
    
    public int getImItemType(int position) {
        int imType = 0;
        switch (position) {
        case 1:
            imType = Im.PROTOCOL_AIM;
            break;
        case 2:// windows live
            imType = Im.PROTOCOL_MSN;
            break;
        case 3:
            imType = Im.PROTOCOL_YAHOO;
            break;
        case 4:
            imType = Im.PROTOCOL_SKYPE;
            break;
        case 5:
            imType = Im.PROTOCOL_QQ;
            break;
        case 6:
            imType = Im.PROTOCOL_GOOGLE_TALK;
            break;
        case 7:
            imType = Im.PROTOCOL_ICQ;
            break;
        case 8:
            imType = Im.PROTOCOL_JABBER;
            break;
        case 9:
            imType = Im.PROTOCOL_CUSTOM;
            break;
        }
        return imType;
    }

    
    
    
}
