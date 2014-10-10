package org.concretejungle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database/* extends SQLiteOpenHelper */{
    private static final String DB_NAME = "arena.sqlite";
    private static final int DB_VERSION = 1;

    public static final String TABLE_TREE = "Gig";
    public static final String TABLE_TYPE = "User";

    public static final String COLUMN_TREE_ID = "tree_id";
    public static final String COLUMN_TREE_LAST_UPDATED = "gig_last_updated";
    public static final String COLUMN_TREE_TITLE = "gig_title";
    public static final String COLUMN_TREE_DESCRIPTION = "gig_description";

    public static final String COLUMN_TREE_TYPE_ID = "tt_id";

/*
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTreeTable(db);
        createTreeTypeTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
        }
    }

    private void createTreeTable(SQLiteDatabase db) {
        StringBuilder gigCreate = new StringBuilder();
        gigCreate
                .append("CREATE TABLE " + TABLE_TREE + " ( ")
                .append(COLUMN_TREE_ID + " INTEGER ")
                .append(", " + COLUMN_TREE_LAST_UPDATED + " TIMESTAMP ")
                .append(");");

        db.execSQL(gigCreate.toString());
    }

    public List<Tree> loadTrees() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TREE_ID
                + ", " + COLUMN_TREE_DESCRIPTION

                + " FROM " + TABLE_TREE
                + " JOIN " + TABLE_TREE_TYPE, null);
        cursor.moveToFirst();

        ArrayList<Tree> trees = new ArrayList<Tree>();
        while (!cursor.isAfterLast()) {
            Tree nextTree = new Tree(
                    cursor.getString(cursor.getColumnIndex(COLUMN_TREE_ID)),

            );

            cursor.moveToNext();
        }
        cursor.close();

        return trees;
    }

    public long storeTree(SQLiteDatabase db, Tree tree) {

        cursor = db.query(TABLE_TYPE, new String[] {COLUMN_USER_ID}, COLUMN_USER_EMAIL + "='" + tree + "'", null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            recipientId = cursor.getInt(0);
        } else {
            ContentValues values = new ContentValues();

            recipientId = db.insert(TABLE_TREE_TYPE, null, values);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TREE_ID, tree.getId());

        return db.insert(TABLE_TREE, null, values);
    }
    */
}