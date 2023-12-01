package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

public class EditContactActivity extends AppCompatActivity {
    private Contact oldContact;
    private Contact contact;
    private static final int EDIT_CONTACT_REQUEST = 2;

    private final ActivityResultLauncher<Intent> editContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Contact editedContact = (Contact) data.getSerializableExtra("editedContact");

                            ((MainActivity) getParent()).updateContactList(editedContact);
                        }
                    }
                }
            }
    );

    private void addContact(String name, String phone) {
      ContentResolver contentResolver = getContentResolver();
      ArrayList<ContentProviderOperation> contentProviderResults = new ArrayList<>();

      contentProviderResults.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
              .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
              .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
              .build());

      contentProviderResults.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
              .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
              .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
              .build());

      contentProviderResults.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
              .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
              .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
              .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
              .build());

      try {
          contentResolver.applyBatch(ContactsContract.AUTHORITY, contentProviderResults);
          Toast.makeText(this, "Contato adicionado com sucesso", Toast.LENGTH_LONG).show();
      } catch (RemoteException | OperationApplicationException e) {
          Toast.makeText(this, "Erro ao adicionar contato", Toast.LENGTH_LONG).show();
      }
  }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        EditText contactName = findViewById(R.id.contactName);
        EditText contactEmail = findViewById(R.id.contactEmail);
        EditText contactPhone = findViewById(R.id.contactPhone);
        Button saveFormButton = findViewById(R.id.saveFormButton);

        contact = (Contact) getIntent().getSerializableExtra("contact");
        oldContact = new Contact(contact.getId(), contact.getName(), contact.getEmail(), contact.getPhone());

        contactName.setText(contact.getName());
        contactEmail.setText(contact.getEmail());
        contactPhone.setText(contact.getPhone());
        saveFormButton.setOnClickListener(view -> {
          deleteContact(oldContact.getPhone());
      
          DatabaseHelper.getInstance(this).updateContact(contact.getId(),
                  contactName.getText().toString(),
                  contactEmail.getText().toString(),
                  contactPhone.getText().toString());
      
          contact.setName(contactName.getText().toString());
          contact.setEmail(contactEmail.getText().toString());
          contact.setPhone(contactPhone.getText().toString());
      
          Contact newContact = new Contact(contact.getId(), contact.getName(), contact.getEmail(), contact.getPhone());
      
          updateContact(oldContact, newContact);
      
          Intent resultIntent = new Intent();
          resultIntent.putExtra("editedContact", contact);
      
          setResult(RESULT_OK, resultIntent);
          finish();
      });
    }

    private void deleteContact(String phoneNumber) {
      ContentResolver contentResolver = getContentResolver();
      ArrayList<ContentProviderOperation> ops = new ArrayList<>();
  
      String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
      String[] params = new String[]{getRawContactId(phoneNumber), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
  
      ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
              .withSelection(where, params)
              .build());
  
      try {
          contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
  
  private String getRawContactId(String phoneNumber) {
      String contactId = null;
      ContentResolver contentResolver = getContentResolver();
      Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
      String[] projection = new String[]{ContactsContract.PhoneLookup._ID};
      Cursor cursor = contentResolver.query(uri, projection, null, null, null);
  
      if (cursor != null) {
          while (cursor.moveToNext()) {
              contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
          }
          cursor.close();
      }
  
      return contactId;
  }
  
  private void updateContact(Contact oldContact, Contact newContact) {
    // Delete the old contact
    Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(oldContact.getPhone()));
    Cursor cur = getContentResolver().query(contactUri, null, null, null, null);
    try {
        if (cur.moveToFirst()) {
            do {
                if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(oldContact.getName())) {
                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    getContentResolver().delete(uri, null, null);
                }
            } while (cur.moveToNext());
        }
    } catch (Exception e) {
        System.out.println(e.getStackTrace());
    } finally {
        if (cur != null) {
            cur.close();
        }
    }

    // Add the new contact
    addContact(newContact.getName(), newContact.getPhone());
}

    private void openEditActivity(Contact contact) {
        Intent intent = new Intent(EditContactActivity.this, EditContactActivity.class);
        intent.putExtra("contact", contact);
        editContactLauncher.launch(intent);
    }

    private void finishEditing() {
      updateContact(oldContact, contact);
  
      Intent resultIntent = new Intent();
      resultIntent.putExtra("updatedContact", contact);
      setResult(RESULT_OK, resultIntent);
      finish();
  }
}