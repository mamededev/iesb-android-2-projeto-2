package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.content.Intent;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ContactAdapter contactAdapter;
    private ArrayList<Contact> contactList;
    private static final int ADD_CONTACT_REQUEST = 1;
    private static final int EDIT_CONTACT_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final ActivityResultLauncher<Intent> editContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Contact updatedContact = (Contact) data.getSerializableExtra("updatedContact");
                        updateContactList(updatedContact);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> addContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Contact newContact = (Contact) data.getSerializableExtra("newContact");
                        updateContactList(newContact);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Listagem de Contatos");

        if(!checkPermission()) {
            requestPermissions();
        } else {
          contactList = DatabaseHelper.getInstance(this).getAllContacts();
          contactAdapter = new ContactAdapter(this, contactList);
          ListView listView = findViewById(R.id.listView);
          listView.setAdapter(contactAdapter);
        }

        contactList = DatabaseHelper.getInstance(this).getAllContacts();
        contactAdapter = new ContactAdapter(this, contactList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(contactAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = contactList.get(position);
            openEditActivity(selectedContact);
        });

        contactAdapter.setOnDeleteClickListener(position -> {
            DatabaseHelper.getInstance(MainActivity.this).deleteContact(contactList.get(position).getId());
            contactList.remove(position);
            contactAdapter.notifyDataSetChanged();
        });

        contactAdapter.setOnEditClickListener(position -> {
            Contact selectedContact = contactList.get(position);
            openEditActivity(selectedContact);
        });

        Button createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            addContactLauncher.launch(intent);
        });
    }

    private void requestPermissions() {
        if(checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Contact newContact = (Contact) data.getSerializableExtra("newContact");
            updateContactList(newContact);
        } else if (requestCode == EDIT_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Contact updatedContact = (Contact) data.getSerializableExtra("updatedContact");
            updateContactList(updatedContact);
        }
    }

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
    
    public void updateContactList(Contact newContact) {
        if (newContact != null) {
          contactList.add(newContact);
          contactAdapter.notifyDataSetChanged();
          addContact(newContact.getName(), newContact.getPhone());
      } else {
          Log.e("MainActivity", "Contact is null");
      }
    }


    private void openEditActivity(Contact contact) {
        Intent intent = new Intent(MainActivity.this, EditContactActivity.class);
        intent.putExtra("contact", contact);
        editContactLauncher.launch(intent);
    }

    public void onEditButtonClick(View view) {
        int position = (int) view.getTag();
        openEditActivity(contactList.get(position));
    }

    @Override
protected void onResume() {
    super.onResume();
    contactList.clear();
    contactList.addAll(DatabaseHelper.getInstance(this).getAllContacts());
    contactAdapter.notifyDataSetChanged();
}
}