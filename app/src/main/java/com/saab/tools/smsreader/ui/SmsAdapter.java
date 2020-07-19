package com.saab.tools.smsreader.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.saab.tools.smsreader.R;
import com.saab.tools.smsreader.db.Sms;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;

public class SmsAdapter extends ArrayAdapter<Sms> {

    private SmsListViewHelper smsListViewHelper;

    public SmsAdapter(@NonNull Context context, SmsListViewHelper smsListViewHelper) {
        super(context, 0, new ArrayList<Sms>());
        this.smsListViewHelper = smsListViewHelper;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Sms sms = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sms, parent, false);
        }

        // Get the view object and populate it
        TextView textMessage = convertView.findViewById(R.id.sms_message);
        textMessage.setText(sms.getMessage());

        // Handle the buttons
        handleSyncButton(convertView, sms);
        handleResetButton(convertView, sms);

        return convertView;
    }

    private void handleSyncButton(@Nullable View convertView, Sms sms) {
        // Link this object with it's respective button
        Button syncButton = (Button) convertView.findViewById(R.id.sms_button);
        syncButton.setTag(sms);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsListViewHelper.syncButtonOnClick(v);
            }
        });

        // Define the behaviour of the sync button
        if (sms.isSynced()) {
            syncButton.setEnabled(false);

            Drawable image = MaterialDrawableBuilder.with(getContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.CLOUD_CHECK)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build();
            syncButton.setCompoundDrawables(image, null, null, null);
        } else {
            syncButton.setEnabled(true);

            Drawable image = MaterialDrawableBuilder.with(getContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.CLOUD_UPLOAD)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build();
            syncButton.setCompoundDrawables(image, null, null, null);
        }
    }

    private void handleResetButton(@Nullable View convertView, Sms sms) {
        // Link this object with it's respective button
        Button resetButton = (Button) convertView.findViewById(R.id.sms_reset);
        resetButton.setTag(sms);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsListViewHelper.resetButtonOnClick(v);
            }
        });

        // Set the image
        Drawable image = MaterialDrawableBuilder.with(getContext())
                .setIcon(MaterialDrawableBuilder.IconValue.CLOUD_SYNC)
                .setColor(Color.WHITE)
                .setToActionbarSize()
                .build();
        resetButton.setCompoundDrawables(image, null, null, null);
    }
}
