package com.example.sms;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import android.content.Intent;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int FILE_SELECT_CODE = 0;
    private List<String[]> contacts = new ArrayList<>();
    private EditText messageEditText, delayEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageEditText = findViewById(R.id.message);
        delayEditText = findViewById(R.id.delay);
        Button upload = findViewById(R.id.upload);
        Button send = findViewById(R.id.send);

        upload.setOnClickListener(v -> openFileSelector());
        send.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
            } else {
                sendMessages();
            }
        });
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, "Select CSV"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
                contacts.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 3) contacts.add(fields);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessages() {
        String template = messageEditText.getText().toString();
        int delay = Integer.parseInt(delayEditText.getText().toString());
        new Thread(() -> {
            SmsManager sms = SmsManager.getDefault();
            for (String[] contact : contacts) {
                String msg = template.replace("{name}", contact[0])
                        .replace("{phone}", contact[1])
                        .replace("{address}", contact[2]);
                sms.sendTextMessage(contact[1], null, msg, null, null);
                try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
            }
        }).start();
    }
}
