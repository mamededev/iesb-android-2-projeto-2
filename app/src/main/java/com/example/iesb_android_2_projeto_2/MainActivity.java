package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.content.Intent;
import androidx.annotation.Nullable;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private ContactAdapter contactAdapter;
    private ArrayList<Contact> contactList;
    private static final int ADD_CONTACT_REQUEST = 1;
    private static final int EDIT_CONTACT_REQUEST = 2;
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

    public void updateContactList(Contact newContact) {
        contactList.add(newContact);
        contactAdapter.notifyDataSetChanged();
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
}