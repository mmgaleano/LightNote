package com.asiamvl.lightnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Represents a Database that extends SQLiteOpenHelper.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

public class Database extends SQLiteOpenHelper {

    private static final String POINTS_TABLE = "POINTS";
    private static final String COL_ID = "ID";
    private static final String COL_X = "X";
    private static final String COL_Y = "Y";

    /** Database constructor.
     * @param context A Context reference.
     */

    public Database(Context context) {
        super(context, "LightNote.db", null, 1);
    }

    /** Overrides the onCreate method for creating the DB table.
     * @param sqLiteDatabase  SQLiteDataBase reference .
     */

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql = String.format(Locale.ENGLISH, "create table %s(%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL," +
                " %s INTEGER NOT NULL)", POINTS_TABLE, COL_ID, COL_X, COL_Y);

        sqLiteDatabase.execSQL(sql);

    }


    /** Overrides the onUpgrade, currently it does nothing.
     * @param sqLiteDatabase  SQLiteDataBase reference .
     */

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    /** Method for storing points into the database.
     * @param points An ArrayList<Point> to be stored into the Database.
     */

    public void storePoints(List<Point> points){

        SQLiteDatabase db = getWritableDatabase();
        db.delete(POINTS_TABLE,null,null);

        int i = 0;
        for(Point point: points){
            ContentValues values = new ContentValues();

            values.put(COL_ID, i);
            values.put(COL_X, point.x);
            values.put(COL_Y, point.y);

            db.insert(POINTS_TABLE, null, values);

            i++;
        }

        db.close();

    }


    /** Method for getting the points sstoring points into the database.
     * @return ArrayList<Point> object of the points currently stored in Database.
     */

    public List<Point> getPoints(){
        List<Point> points = new ArrayList<Point>();
        SQLiteDatabase db = getWritableDatabase();

        String sql = String.format(Locale.ENGLISH, "SELECT %s, %s FROM %s ORDER BY %s",COL_X, COL_Y, POINTS_TABLE,COL_ID);
        Cursor cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext()){
            int x = cursor.getInt(0);
            int y = cursor.getInt(1);

            points.add(new Point(x,y));
        }

        db.close();
        return points;
    }
}
