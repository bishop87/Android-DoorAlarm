package it.bishop87.dooralarm.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import it.bishop87.dooralarm.R;
import it.bishop87.dooralarm.utility.PreferenceUtility;

public class MainActivity extends AppCompatActivity {

    private List<String> array_list_numeri;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView lvNumeriAbilitati = findViewById(R.id.lvNumeriAbilitati);

        array_list_numeri = PreferenceUtility.leggiNumeriAbilitati(MainActivity.this);
        arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.row, array_list_numeri);

        lvNumeriAbilitati.setAdapter(arrayAdapter);
        lvNumeriAbilitati.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dlg_delete_number_title).setMessage(R.string.dlg_delete_number_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                array_list_numeri.remove(position);
                                salvaListaNumeri();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

                return false;
            }
        });

        //controllo permesso do not disturb
        try{
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        //controllo permesso ricezione sms
        smsReceivePermission();
    }

    public void btnAggiungiNumeroAbilitatoClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.setTitle(R.string.dlg_add_number_title);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        dialog.setView(input);

        // Set up the buttons
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(R.string.dlg_add_number_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String numero = input.getText().toString();
                        if(!numero.isEmpty()) {
                            array_list_numeri.add(numero);
                            salvaListaNumeri();
                        }
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(R.string.dlg_add_number_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        dialog.show();
    }

    private void salvaListaNumeri(){
        PreferenceUtility.scriviNumeriAbilitati(MainActivity.this, array_list_numeri);
        arrayAdapter.notifyDataSetChanged();
    }

    public void btnReadStatusClick(View view) {
        try {
            smsSendPermission();
        }catch (IllegalArgumentException ex){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Attenzione");
            alertDialog.setMessage("Errore nell'invio dell'sms.\nControllare il numero inserito");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }


    private void smsReceivePermission() throws IllegalArgumentException {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS},2);
        }
    }


    private void smsSendPermission() throws IllegalArgumentException {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},1);

        } else {
            sendSms("STATUS", PreferenceUtility.leggiNumeriAbilitati(MainActivity.this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSms("STATUS", PreferenceUtility.leggiNumeriAbilitati(MainActivity.this));
                }
                break;
            case 2:
                if (grantResults.length < 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "ATTENZIONE: senza permessi per gli sms l'app non funzionerà!", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void sendSms(String msg, List<String> destNumbers) throws IllegalArgumentException {
        for (String number: destNumbers) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, msg, null, null);
            Log.d("Main", "Message Sent [" + number + "]: " + msg);
        }
    }

    public void btnTestStartClick(View view) {
        Intent startIntent = new Intent(MainActivity.this, RingtonePlayingService.class);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        startIntent.putExtra("ringtone-uri", ringtoneUri.toString());
        startService(startIntent);
    }


    public void btnTestStopClick(View view) {
        Intent stopIntent = new Intent(MainActivity.this, RingtonePlayingService.class);
        stopService(stopIntent);
    }
}
