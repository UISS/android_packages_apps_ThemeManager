/*
 * Copyright (C) 2013 The ChameleonOS Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.thememanager.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.IThemeManagerService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.thememanager.Globals;
import com.android.thememanager.PreviewHelper;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;

import com.android.thememanager.provider.FileProvider;

import java.io.*;

public class ThemeElementDetailActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private Gallery mPreviews = null;
    private String[] mPreviewList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private int mElementType = 0;
    private Theme mTheme = null;
    private boolean mFontReboot = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);


        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);

        if (mTheme == null)
            finish();

        mElementType = getIntent().getIntExtra("type", 0);
        String themeName = mTheme.getFileName();
        switch (mElementType) {
            case Theme.THEME_ELEMENT_TYPE_ICONS:
                mPreviewList = PreviewHelper.getIconPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                if (!(new File(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName) + "/default_wallpaper.jpg")).exists()) {
                    ThemeUtils.extractThemeWallpaper(ThemeUtils.stripExtension(themeName), mTheme.getThemePath());
                }
                mPreviewList = PreviewHelper.getWallpaperPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                mPreviewList = PreviewHelper.getStatusbarPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                mPreviewList = PreviewHelper.getLauncherPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                mPreviewList = PreviewHelper.getContactsPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                mPreviewList = PreviewHelper.getContactsPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                mPreviewList = PreviewHelper.getBootanimationPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_MMS:
                mPreviewList = PreviewHelper.getMmsPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_FONT:
                mPreviewList = PreviewHelper.getFontsPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(themeName));
                break;
        }

        ((TextView)findViewById(R.id.theme_name)).setText(mTheme.getTitle());

        setTitle(Theme.sElementLabels[mElementType]);

        mAdapter = new ImageAdapter(this);
        mPreviews = (Gallery) findViewById(R.id.previews);
        mPreviews.setAdapter(mAdapter);
        mPreviews.setSpacing(20);
        mPreviews.setAnimationDuration(1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_details:
                Theme.showThemeDetails(this, mTheme);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeElementDetailActivity.this);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        mAdapter.destroyImages();
        mAdapter = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
        } catch (Exception e) {}

        mAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.ACTION_THEME_APPLIED);
        filter.addAction(Globals.ACTION_THEME_NOT_APPLIED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void applyTheme(View view) {
        if (mElementType == Theme.THEME_ELEMENT_TYPE_FONT) {
            SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_apply_font_and_reboot),
                    getString(R.string.dlg_apply_font_without_reboot),
                    getString(R.string.dlg_apply_font_title),
                    getString(R.string.dlg_apply_font_body),
                    this,
                    new SimpleDialogs.OnYesNoResponse() {
                        @Override
                        public void onYesNoResponse(boolean isYes) {
                            mFontReboot = isYes;
                            applyTheme(mTheme.getThemePath());
                        }
                    });
        } else {
            applyTheme(mTheme.getThemePath());
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        private ImageView[] mImages;

        public ImageAdapter(Context c) {
            mContext = c;
            if (mImages == null && mPreviewList != null) {
                mImages = new ImageView[mPreviewList.length];
            }
        }

        @Override
        public int getCount() {
            return mPreviewList != null ? mPreviewList.length : 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (mPreviewList == null) {
                ImageView iv = new ImageView(mContext);
                iv.setImageResource(R.drawable.no_preview);
                return iv;
            }

            if (mImages[position] == null) {
                int h = mPreviews.getHeight();
                mImages[position] = new ImageView(mContext);
                mImages[position].setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                FileInputStream is = null;
                try {
                    is = new FileInputStream(THEMES_PATH + "/.cache/" +
                        mTheme.getFileName() + "/" + mPreviewList[position]);
                } catch (FileNotFoundException e) {
                }
                if (is != null) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    opts.inDensity = DisplayMetrics.DENSITY_HIGH;
                    opts.inTargetDensity = mContext.getResources().getDisplayMetrics().densityDpi;
                    opts.inScaled = true;
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    Drawable drawable = new BitmapDrawable(bmp);
                    mImages[position].setImageDrawable(drawable);
                } else
                    mImages[position].setImageResource(R.drawable.no_preview);
                mImages[position].setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            return mImages[position];
        }

        /** Returns the size (0.0f to 1.0f) of the views
         * depending on the 'offset' to the center. */
        public float getScale(boolean focused, int offset) {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
        }

        public void destroyImages() {
            for (int i = 0; mImages != null && i < mImages.length; i++) {
                if (mImages[i] != null) {
                    if (mImages[i].getDrawable() != null)
                        mImages[i].getDrawable().setCallback(null);
                    mImages[i].setImageDrawable(null);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.applying_theme));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false );
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private void applyTheme(String theme) {
        String themeFileName = ThemeUtils.stripPath(theme);
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            switch (mElementType) {
                case Theme.THEME_ELEMENT_TYPE_ICONS:
                    ts.applyThemeIcons(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                    ts.applyThemeWallpaper(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                    ts.applyThemeSystemUI(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                    ts.applyThemeFramework(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                    ts.applyThemeContacts(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    ts.applyThemeRingtone(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    ts.applyThemeBootanimation(FileProvider.CONTENT + themeFileName, false);
                    break;
                case Theme.THEME_ELEMENT_TYPE_MMS:
                    ts.applyThemeMms(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_FONT:
                    if (mFontReboot)
                        ts.applyThemeFontReboot(FileProvider.CONTENT + themeFileName);
                    else
                        ts.applyThemeFont(FileProvider.CONTENT + themeFileName);
                    break;
            }
            showDialog(DIALOG_PROGRESS);
        } catch (Exception e) {
            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                    ThemeElementDetailActivity.this);
        }
    }
}
