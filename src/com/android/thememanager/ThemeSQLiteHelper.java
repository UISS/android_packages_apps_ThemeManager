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

package com.android.thememanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ThemeSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_THEMES = "themes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_THEME_FILE_NAME = "file_name";
    public static final String COLUMN_THEME_TITLE = "title";
    public static final String COLUMN_THEME_AUTHOR = "author";
    public static final String COLUMN_THEME_DESIGNER = "designer";
    public static final String COLUMN_THEME_VERSION = "version";
    public static final String COLUMN_THEME_UI_VERSION = "ui_version";
    public static final String COLUMN_THEME_PATH = "theme_path";
    public static final String COLUMN_IS_COS_THEME = "cos_theme";
    public static final String COLUMN_IS_DEFAULT_THEME = "default_theme";
    public static final String COLUMN_HAS_WALLPAPER = "has_wallpaper";
    public static final String COLUMN_HAS_ICONS = "has_icons";
    public static final String COLUMN_HAS_LOCKSCREEN = "has_lockscreen";
    public static final String COLUMN_HAS_CONTACTS = "has_contacts";
    public static final String COLUMN_HAS_SYSTEMUI = "has_systemui";
    public static final String COLUMN_HAS_FRAMEWORK = "has_framework";
    public static final String COLUMN_HAS_RINGTONE = "has_ringtone";
    public static final String COLUMN_HAS_NOTIFICATION = "has_notification";
    public static final String COLUMN_HAS_BOOTANIMATION = "has_bootanimation";
    public static final String COLUMN_HAS_MMS = "has_mms";
    public static final String COLUMN_HAS_FONT = "has_font";
    public static final String COLUMN_LAST_MODIFIED = "last_modified";

    private static final String DATABASE_NAME = "themesdb";
    private static final int DATABASE_VERSION = 9;

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_THEMES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_THEME_FILE_NAME + " text not null, "
            + COLUMN_LAST_MODIFIED + " text, "
            + COLUMN_THEME_TITLE + " text, "
            + COLUMN_THEME_AUTHOR + " text, "
            + COLUMN_THEME_DESIGNER + " text, "
            + COLUMN_THEME_VERSION + " text, "
            + COLUMN_THEME_UI_VERSION + " text, "
            + COLUMN_THEME_PATH + " text not null, "
            + COLUMN_IS_COS_THEME + " integer, "
            + COLUMN_IS_DEFAULT_THEME + " integer, "
            + COLUMN_HAS_WALLPAPER + " integer, "
            + COLUMN_HAS_ICONS + " integer, "
            + COLUMN_HAS_LOCKSCREEN + " integer, "
            + COLUMN_HAS_CONTACTS + " integer, "
            + COLUMN_HAS_SYSTEMUI + " integer, "
            + COLUMN_HAS_FRAMEWORK + " integer, "
            + COLUMN_HAS_RINGTONE + " integer, "
            + COLUMN_HAS_NOTIFICATION + " integer, "
            + COLUMN_HAS_BOOTANIMATION + " integer, "
            + COLUMN_HAS_MMS + " integer, "
            + COLUMN_HAS_FONT + " integer);";


    public ThemeSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
/*
        if (version < 4) {
            db.execSQL("ALTER TABLE themes ADD COLUMN has_mms INTEGER NOT NULL DEFAULT 0;");
            db.setTransactionSuccessful();
            version = 4;
        }
*/
        //if (version != DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEMES);
            onCreate(db);
        //}
    }
}
