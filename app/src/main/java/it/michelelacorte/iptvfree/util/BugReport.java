package it.michelelacorte.iptvfree.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import it.michelelacorte.iptvfree.R;

/**
 * This class contains method to report bug (alpha).
 *
 * Created by Michele on 29/04/2016.
 */
public class BugReport {

    /**
     * This method initialize an itent to send an email to developers.
     * @param activity Activity
     */
    public static void reportBug(Activity activity)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getResources().getString(R.string.email)});
        i.putExtra(Intent.EXTRA_SUBJECT, activity.getResources().getString(R.string.email_subject_bug));
        i.putExtra(Intent.EXTRA_TEXT, Utils.getSystemInformation() + "\n" + activity.getResources().getString(R.string.email_text_bug));
        try {
            activity.startActivity(Intent.createChooser(i, activity.getResources().getString(R.string.email_send)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, activity.getResources().getString(R.string.email_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
