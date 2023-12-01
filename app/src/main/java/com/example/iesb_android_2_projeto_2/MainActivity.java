package com.example.iesb_android_2_projeto_2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btnAddContact;
    private TextView title;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        List<Person> person = db.getAllPerson();

        title = findViewById(R.id.title);
        title.setText("Lista de Contatos");

        btnAddContact = findViewById(R.id.btnAddContact);
        btnAddContact.setText("Adicionar novo contato");
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
    }
}