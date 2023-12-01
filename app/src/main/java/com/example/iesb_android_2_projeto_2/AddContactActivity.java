package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

public class AddContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        EditText contactName = findViewById(R.id.contactName);
        EditText contactEmail = findViewById(R.id.contactEmail);
        EditText contactPhone = findViewById(R.id.contactPhone);
        Button saveFormButton = findViewById(R.id.saveFormButton);

        saveFormButton.setOnClickListener(view -> {
            String name = contactName.getText().toString();
            String email = contactEmail.getText().toString();
            String phone = contactPhone.getText().toString();

            long contactId = DatabaseHelper.getInstance(this).addContact(name, email, phone);

            Contact newContact = new Contact(contactId, name, email, phone);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("newContact", newContact);
            setResult(RESULT_OK, resultIntent);

            finish();
        });
    }
}