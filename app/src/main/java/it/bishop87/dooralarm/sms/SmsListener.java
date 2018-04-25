package it.bishop87.dooralarm.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import it.bishop87.dooralarm.activity.RingtonePlayingService;
import it.bishop87.dooralarm.utility.PreferenceUtility;

public class SmsListener extends BroadcastReceiver {

    private enum Azioni {
        PORTA_APERTA,
        PORTA_CHIUSA,
        UNKNOWN
    }

    private static final String PREFISSO_ITA = "+39";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();  //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msgFrom;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msgFrom = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();

                        Log.d("SmsListener","msgFrom: " + msgFrom);
                        Log.d("SmsListener","msgBody: " + msgBody);

                        if(PreferenceUtility.numeroAbilitato(context, msgFrom, PREFISSO_ITA)){

                            switch(Azioni.valueOf(msgBody.toUpperCase())){
                                case PORTA_APERTA:
                                    Log.d("SmsListener","Messaggio riconosciuto: abortBroadcast");

                                    Intent startIntent = new Intent(context, RingtonePlayingService.class);
                                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                    startIntent.putExtra("ringtone-uri", ringtoneUri.toString());
                                    context.startService(startIntent);

                                    abortBroadcast();
                                    break;
                                case PORTA_CHIUSA:
                                    abortBroadcast();
                                    break;
                                case UNKNOWN:
                                default: Log.d("SmsListener","Messaggio non riconosciuto: no abortBroadcast");
                            }


                            abortBroadcast();
                        }
                        else{
                            Log.d("SmsListener","Mittente non riconosciuto: no abortBroadcast");
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


}
