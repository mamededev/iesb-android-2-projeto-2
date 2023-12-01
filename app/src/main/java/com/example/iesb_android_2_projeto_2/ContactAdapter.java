package com.example.iesb_android_2_projeto_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Contact> contactList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnEditClickListener onEditClickListener;

    public ContactAdapter(Context context, ArrayList<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView phoneTextView;
        TextView deleteButton;
        Button editButton;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.nameTextView = convertView.findViewById(R.id.nameTextView);
            viewHolder.emailTextView = convertView.findViewById(R.id.emailTextView);
            viewHolder.phoneTextView = convertView.findViewById(R.id.phoneTextView);
            viewHolder.deleteButton = convertView.findViewById(R.id.deleteButton);
            viewHolder.editButton = convertView.findViewById(R.id.editButton);  // Atribui o botão de edição

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contactList.get(position);

        viewHolder.nameTextView.setText(contact.getName());
        viewHolder.emailTextView.setText(contact.getEmail());
        viewHolder.phoneTextView.setText(contact.getPhone());

        viewHolder.editButton.setOnClickListener((View v) -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position);
            }
        });

        viewHolder.deleteButton.setOnClickListener(view -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });

        return convertView;
    }
}