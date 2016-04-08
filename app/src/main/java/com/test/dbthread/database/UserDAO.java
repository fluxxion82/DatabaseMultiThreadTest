package com.test.dbthread.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.dbthread.R;
import com.test.dbthread.model.User;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by salbury on 4/7/16.
 */
public class UserDAO {

    interface Table {

        String COLUMN_ID = "id";
        String COLUMN_NAME = "name";
        String COLUMN_AGE = "age";
        String COLUMN_ALIAS = "aka";
    }

    private SQLiteDatabase mDatabase;
    private Context mContext;

    public UserDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable(Context context) {
        return context.getString(R.string.create_table_user);
    }

    public static String getDropTable(Context context) {
        return context.getString(R.string.drop_table_users);
    }

    public void deleteAll() {
        mDatabase.execSQL(mContext.getString(R.string.delete_all_users));
    }

    public void insert(List<User> userList) {
        try {
            mDatabase.beginTransactionNonExclusive();

            for (User user : userList) {
                String[] bindArgs = {
                        user.getName(),
                        String.valueOf(user.getAge()),
                        user.getAka()
                };
                mDatabase.execSQL(mContext.getString(R.string.insert_user), bindArgs);

                updateNameByAge(user.getName() + user.getAge(), user.getAge());
            }

            mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void insert(User user) {
        try {
            mDatabase.beginTransactionNonExclusive();

            String[] bindArgs = {
                    user.getName(),
                    String.valueOf(user.getAge()),
                    user.getAka()
            };

            mDatabase.execSQL(mContext.getString(R.string.insert_user), bindArgs);

            updateNameByAge(user.getName() + user.getAge(), user.getAge());

            mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void updateNameByAge(String name, int age) {
        try {
            mDatabase.beginTransactionNonExclusive();

            String[] bindArgs = {
                    name,
                    String.valueOf(age),
            };
            mDatabase.execSQL(mContext.getString(R.string.update_user_name_by_age), bindArgs);

            mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            mDatabase.endTransaction();
        }
    }

    public List<User> selectByAge(int age) {
        String[] selectionArgs = {
                String.valueOf(age)
        };
        String query = mContext.getString(R.string.select_users_by_age);
        Cursor cursor = mDatabase.rawQuery(query, selectionArgs);

        List<User> dataList = manageCursor(cursor);

        closeCursor(cursor);

        return dataList;
    }

    public List<User> selectAll() {
        Cursor cursor = mDatabase.rawQuery(mContext.getString(R.string.select_all_users), null);

        List<User> dataList = manageCursor(cursor);

        closeCursor(cursor);

        return dataList;
    }

    protected User cursorToData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(Table.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(Table.COLUMN_NAME);
        int ageIndex = cursor.getColumnIndex(Table.COLUMN_AGE);
        int akaIndex = cursor.getColumnIndex(Table.COLUMN_ALIAS);

        User user = new User();
        user.setId(cursor.getLong(idIndex));
        user.setAge(cursor.getInt(ageIndex));
        user.setName(cursor.getString(nameIndex));
        user.setAka(cursor.getString(akaIndex));

        return user;
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    protected List<User> manageCursor(Cursor cursor) {
        List<User> dataList = new ArrayList<User>();

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                User user = cursorToData(cursor);
                dataList.add(user);
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
