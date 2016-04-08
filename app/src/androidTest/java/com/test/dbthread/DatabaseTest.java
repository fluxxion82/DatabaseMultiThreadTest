package com.test.dbthread;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.test.dbthread.database.DatabaseHelper;
import com.test.dbthread.database.DatabaseManager;
import com.test.dbthread.database.UserDAO;
import com.test.dbthread.model.User;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by salbury on 4/7/16.
 */
public class DatabaseTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        Log.v("DatabaseTest", "setup");
        super.setUp();

        DatabaseManager.initializeInstance(new DatabaseHelper(getContext()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                SQLiteDatabase database =databaseManager.openDatabase();

                new UserDAO(database, getContext()).deleteAll();

                //databaseManager.closeDatabase();
            }
        }).start();

    }

    public void testInsertUserList() {
        Log.v("DatabaseTest", "testInsertUserList");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                SQLiteDatabase database =databaseManager.openDatabase();
                UserDAO dao = new UserDAO(database, getContext());

                try {
                    database.beginTransactionNonExclusive();

                    dao.deleteAll();

                    dao.insert(generateTestUserList(10, this.toString()));

                    List<User> listFromDB = dao.selectAll();

                    Assert.assertTrue("User list is empty", !listFromDB.isEmpty());
                    Assert.assertTrue("User list size is wrong size=" + listFromDB.size(), listFromDB.size() == 10);

                    database.setTransactionSuccessful();
                } catch (SQLException e) {
                } finally {
                    database.endTransaction();
                }

                //databaseManager.closeDatabase();
            }
        }).start();

    }

    public void testInsertUser() {
        Log.v("DatabaseTest", "testInsertUser");

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                SQLiteDatabase database =databaseManager.openDatabase();

                UserDAO dao = new UserDAO(database, getContext());

                try {
                    database.beginTransactionNonExclusive();

                    dao.deleteAll();

                    User user = new User();
                    user.setName("Jon Doe");
                    user.setAge(100);
                    user.setAka("jd");


                    dao.insert(user);

                    List<User> listFromDB = dao.selectAll();

                    Assert.assertTrue("User list is empty", !listFromDB.isEmpty());
                    Assert.assertTrue("User list size is wrong size=" + listFromDB.size(), listFromDB.size() == 1);

                    User userFromDB = listFromDB.get(0);

                    Assert.assertTrue("Incorrect data", String.valueOf(user.getName() + user.getAge()).contentEquals(userFromDB.getName()));
                    Assert.assertTrue("Incorrect data", user.getAge() == userFromDB.getAge());

                    database.setTransactionSuccessful();
                } catch (SQLException e) {
                } finally {
                    database.endTransaction();
                }

               // databaseManager.closeDatabase();
            }
        }).start();
    }

    public void testUpdateUser() {
        Log.v("DatabaseTest", "testUpdateUser");

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                SQLiteDatabase database =databaseManager.openDatabase();

                UserDAO dao = new UserDAO(database, getContext());

                try {
                    database.beginTransactionNonExclusive();

                    dao.deleteAll();

                    User user = new User();
                    user.setAge(18);
                    user.setName("Jon Doe");
                    user.setAka("jj");

                    dao.insert(user);

                    dao.updateNameByAge("Will Smith", 18);

                    List<User> listFromDB = dao.selectByAge(18);
                    Assert.assertTrue("User list is empty", !listFromDB.isEmpty());

                    User userFromDB = listFromDB.get(0);

                    Assert.assertTrue("User is null", userFromDB != null);
                    Assert.assertTrue("User age is wrong", userFromDB.getAge() == 18);
                    Assert.assertTrue("User name is wrong", userFromDB.getName().equals("Will Smith"));

                    database.setTransactionSuccessful();
                } catch (SQLException e) {
                } finally {
                    database.endTransaction();
                }

                //databaseManager.closeDatabase();
            }
        }).start();
    }

    private int totalTasks = 100;
    private AtomicInteger tasksAlive = new AtomicInteger(totalTasks);

    /*
     * Creating a 100 threads that each insert/update 10 entries.
     * Refer to UserDAO to see what transactions are taking place.
     */
    public void testConcurrentAccess() {
        Log.v("DatabaseTest", "testConcurrentAccess");

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        SQLiteDatabase database =databaseManager.openDatabase();
        UserDAO dao = new UserDAO(database, getContext());

        dao.deleteAll();

        for (int i = 0; i < totalTasks; i++) {
            spawnNewThread();
        }

        int totalUsers = 10 * totalTasks;

        List<User> listFromDB = dao.selectAll();

        //databaseManager.closeDatabase();

        Assert.assertTrue("User list is empty", !listFromDB.isEmpty());
        Assert.assertTrue("User list size is wrong total=" + totalUsers + " list=" + listFromDB.size(), listFromDB.size() == totalUsers);
    }

    private void spawnNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                SQLiteDatabase database =databaseManager.openDatabase();
                UserDAO dao = new UserDAO(database, getContext());

                int usersCount = 10;
                dao.insert(generateTestUserList(usersCount, String.valueOf(tasksAlive.get())));

                //databaseManager.closeDatabase();

                Log.v("DatabaseTest", "Task #" + tasksAlive.get() + " is finished");

                tasksAlive.decrementAndGet();
            }
        }).start();
    }

    private List<User> generateTestUserList(int itemsCount, String taskNumber) {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            User user = new User();
            user.setAge(i);
            user.setName("Jon Doe");
            user.setAka("jd" + i);
            userList.add(user);
        }
        return userList;
    }

}
