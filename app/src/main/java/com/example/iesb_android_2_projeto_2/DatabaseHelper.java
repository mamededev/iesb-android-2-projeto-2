package com.example.iesb_android_2_projeto_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ContactsDB";
    private static final String TABLE_NAME = "contacts";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME +
                "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT, " +
                KEY_EMAIL + " TEXT, " +
                KEY_PHONE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addContact(String name, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PHONE, phone);

        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int idIndex = cursor.getColumnIndex(KEY_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int emailIndex = cursor.getColumnIndex(KEY_EMAIL);
        int phoneIndex = cursor.getColumnIndex(KEY_PHONE);

        if (idIndex < 0 || nameIndex < 0 || emailIndex < 0 || phoneIndex < 0) {
            return contactList;
        }

        try {
          if (cursor.moveToFirst()) {
            do {
              long id = cursor.getLong(idIndex);
              String name = cursor.getString(nameIndex);
              String email = cursor.getString(emailIndex);
              String phone = cursor.getString(phoneIndex);

              Contact contact = new Contact(id, name, email, phone);
              contactList.add(contact);
            } while (cursor.moveToNext());
          }
        } finally {
            cursor.close();
            db.close();
        }

        return contactList;
    }

    public void updateContact(long id, String name, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PHONE, phone);

        db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    public void deleteContact(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}