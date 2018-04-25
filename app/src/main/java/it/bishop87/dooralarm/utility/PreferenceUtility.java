package it.bishop87.dooralarm.utility;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PreferenceUtility {

    private static String NUMERI_ABILITATI = "NUMERI_ABILITATI";


    public static List<String> leggiNumeriAbilitati(Context context) {
        List<String> array_list_numeri;
        String numeri = PreferenceManager.getDefaultSharedPreferences(context).getString(NUMERI_ABILITATI, "");

        array_list_numeri = new ArrayList<>(Arrays.asList(numeri.trim().split(";")));
        System.out.println("leggiNumeriAbilitati: " + array_list_numeri.toString());

        return array_list_numeri;
    }

    public static void scriviNumeriAbilitati(Context context, List<String> array_list_numeri){
        array_list_numeri.removeAll(Arrays.asList("", null));
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PreferenceUtility.NUMERI_ABILITATI, TextUtils.join(";", array_list_numeri) ).apply();
        System.out.println("scriviNumeriAbilitati: " + TextUtils.join(";", array_list_numeri));
    }

    public static boolean numeroAbilitato(Context context, String numero, String prefisso) {
        if(numero.startsWith(prefisso)) {
            numero = numero.substring(prefisso.length());
        }
        System.out.println("numero senza prefisso [" + prefisso + "]: " + numero);
        List<String> numeri = leggiNumeriAbilitati(context);
        for(String n : numeri) {
            if(n.contains(numero))
                return true;
        }
        return false;
    }

}
