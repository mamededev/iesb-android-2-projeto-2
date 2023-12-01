package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class EditContactActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        EditText contactName = findViewById(R.id.contactName);
        EditText contactEmail = findViewById(R.id.contactEmail);
        EditText contactPhone = findViewById(R.id.contactPhone);
        Button saveFormButton = findViewById(R.id.saveFormButton);

        contact = (Contact) getIntent().getSerializableExtra("contact");

        contactName.setText(contact.getName());
        contactEmail.setText(contact.getEmail());
        contactPhone.setText(contact.getPhone());

        saveFormButton.setOnClickListener(view -> {
            DatabaseHelper.getInstance(this).updateContact(contact.getId(),
                    contactName.getText().toString(),
                    contactEmail.getText().toString(),
                    contactPhone.getText().toString());

            Contact editedContact = new Contact(
                    contact.getId(),
                    contactName.getText().toString(),
                    contactEmail.getText().toString(),
                    contactPhone.getText().toString());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("editedContact", editedContact);

            setResult(RESULT_OK, resultIntent);

            finishEditing();
        });
    }

    private void openEditActivity(Contact contact) {
        Intent intent = new Intent(EditContactActivity.this, EditContactActivity.class);
        intent.putExtra("contact", contact);
        editContactLauncher.launch(intent);
    }

    private void finishEditing() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", contact);
        setResult(RESULT_OK, resultIntent);
        finishEditing();
    }
}