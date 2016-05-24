package it.michelelacorte.iptvfree.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.ArrayList;
import java.util.List;
import it.michelelacorte.iptvfree.MainActivity;
import it.michelelacorte.iptvfree.R;
import it.michelelacorte.iptvfree.m3u.M3UData;
import it.michelelacorte.iptvfree.sd_reader.FileOperation;
import it.michelelacorte.iptvfree.sd_reader.FileSelector;
import it.michelelacorte.iptvfree.sd_reader.OnHandleFileListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * This class provide misc of util method.
 *
 * Created by Michele on 23/04/2016.
 */
public class Utils {

    private static final String[] mFileFilter = {".m3u", ".m3u8"};
    private static String loadedFromURLString = null;

    /**
     * This method convert two List of channelName and channelString into List of M3UData object.
     * @param channelName List<String>
     * @param channelLink List<String>
     * @return m3uDatas List<M3UData>
     */
    public static List<M3UData> convertToM3UData(List<String> channelName, List<String> channelLink)
    {
        List<M3UData> m3uDatas = new ArrayList<>();
        for(int i = 0; i < channelLink.size(); i++)
        {
           m3uDatas.add(new M3UData(channelName.get(i), channelLink.get(i)));
        }
        return m3uDatas;
    }

    /**
     * Check if device is online.
     * @param context Context
     * @return boolean
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Check if package is installed on device
     * @param packagename String
     * @param packageManager PackageManager
     * @return boolean
     */
    public static boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get system information for bug report
     * @return String
     */
    public static String getSystemInformation()
    {
        return  "SDK: " + Build.VERSION.SDK_INT + "\n" +
                "RELEASE: " + Build.VERSION.RELEASE + "\n" +
                "DEVICE: " + android.os.Build.DEVICE + "\n" +
                "OS VERSION: " + System.getProperty("os.version") + "\n" +
                "OS NAME: " + System.getProperty("os.name") + "\n" +
                "MODEL: " + android.os.Build.MODEL + "\n" +
                "PRODUCT: " + android.os.Build.PRODUCT + "\n"+
                "BRAND: " + Build.BRAND + "\n" +
                "HARDWARE: " + Build.HARDWARE + "\n" +
                "BOARD: " + Build.BOARD + "\n";
    }

    /**
     * This method is for select botch URL or Folder
     * @param activity Activity
     */
    public static void addFromURLOrFolder(final Activity activity)
    {
        final AlertDialog.Builder loaderDialog = new AlertDialog.Builder(activity, R.style.AlertDialogCustom);
        CharSequence items[] = new CharSequence[] {activity.getResources().getString(R.string.dialog_loader_choose_from_file),
                   activity.getResources().getString(R.string.dialog_loader_choose_from_url)};
        loaderDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                switch (n)
                {
                    case 0:
                        OnHandleFileListener mLoadFileListener = MainActivity.onHandleFileListener(activity);
                        new FileSelector(activity, FileOperation.LOAD, mLoadFileListener, mFileFilter).show();
                        d.dismiss();
                        break;
                    case 1:
                        loadFromURL(activity);
                        d.dismiss();
                        break;
                    default:
                        break;
                }
            }

        });
        loaderDialog.setNegativeButton(activity.getResources().getString(R.string.dialog_loader_cancel), null);
        loaderDialog.setTitle(activity.getResources().getString(R.string.dialog_loader_choose));
        loaderDialog.show();
    }

    /**
     * Load from URL
     * @param activity Activity
     */
    private static void loadFromURL(final Activity activity)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity, R.style.AlertDialogCustom);
        alert.setTitle(activity.getResources().getString(R.string.dialog_loader_choose_from_url));
        alert.setMessage(activity.getResources().getString(R.string.dialog_loader_url));

        final EditText input = new EditText(activity.getApplicationContext());
        input.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), android.R.color.black));
        input.getBackground().mutate().setColorFilter(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        alert.setView(input);

        alert.setPositiveButton(activity.getResources().getString(R.string.dialog_loader_open), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                loadedFromURLString = input.getEditableText().toString();
                MainActivity.isMyListLoadedFromURL = true;
                MainActivity.isMyListLoadedFromPath = false;
                Log.e("isMy", "" + MainActivity.isMyListLoadedFromURL + "   " + MainActivity.isMyListLoadedFromPath);
                Toast.makeText(activity, activity.getResources().getString(R.string.dialog_loader_load) + ": " + loadedFromURLString, Toast.LENGTH_SHORT).show();

            }
        });
        alert.setNegativeButton(activity.getResources().getString(R.string.dialog_loader_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    /**
     * Get URL insered from user.
     * @return String
     */
    public static String getLoadedFromURLString() {
        return loadedFromURLString;
    }

    /**
     * Clear a series of List
     * @param lists List<?>
     */
    public static void clearIfNotEmpty(List<?> ... lists)
    {
        for(List<?> list : lists)
        {
            if(list != null) {
                if (list.size() > 0) {
                    list.clear();
                    list = new ArrayList<>(list);
                }
            }
        }
    }

    /**
     * Tutorial view method
     * @param activity Activity
     * @param SHOWCASE_ID String
     * @param toolbar Toolbar
     * @param floatingActionButton Floating Action Button
     * @param tabLayout TabLayout
     * @param viewPager ViewPager
     * @param searchView MaterialSearchView
     */
    public static void tutorialView(Activity activity, String SHOWCASE_ID, Toolbar toolbar, FloatingActionButton floatingActionButton
    , TabLayout tabLayout, ViewPager viewPager, MaterialSearchView searchView)
    {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(getNavButtonInToolBar(toolbar),
                activity.getResources().getString(R.string.intro_navbar), activity.getResources().getString(R.string.intro_understand));

        sequence.addSequenceItem(floatingActionButton,
                activity.getResources().getString(R.string.intro_fab), activity.getResources().getString(R.string.intro_understand));

        sequence.addSequenceItem(tabLayout,
                activity.getResources().getString(R.string.intro_tab_1), activity.getResources().getString(R.string.intro_understand));

        sequence.addSequenceItem(tabLayout,
                activity.getResources().getString(R.string.intro_tab_2), activity.getResources().getString(R.string.intro_understand));

        sequence.addSequenceItem(searchView,
                activity.getResources().getString(R.string.intro_search), activity.getResources().getString(R.string.intro_understand));
        sequence.start();
    }

    /**
     * Get hamburger icon on navigation drawer
     * @param toolbar Toolbar
     * @return View
     */
    private static View getNavButtonInToolBar(Toolbar toolbar) {
        for (int i = 0;i<toolbar.getChildCount();i++) {
            if(toolbar.getChildAt(i) instanceof ImageButton){
                ImageButton button = (ImageButton) toolbar.getChildAt(i);
                if(button.getDrawable().getClass().getSuperclass().equals(DrawerArrowDrawable.class))
                    return toolbar.getChildAt(i);
            }
        }
        return null;
    }
}
