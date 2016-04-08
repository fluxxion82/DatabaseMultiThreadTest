# DatabaseMultiThreadTest
There should be only one instance of SQLiteOpenHelper in your app. To use database with multiple threads we need to make sure we are using one database connection. I made a singleton class DatabaseManager which holds and returns a single SQLiteOpenHelper object. 

Every time you need database you should call openDatabase() method of DatabaseManager class. Inside this method, we have a counter, which indicate how many times database is opened. If it equals to one, it means we need to create new database, if not, database is already created. 
The same happens in closeDatabase() method. Every time we call this method, counter is decreased, whenever it goes to zero, we are closing database.

-Run DatabaseTest under the androidTest folder.
-Some things done in hacky way to test pertinent cases. Need to be careful about adding new cases, although it shouldn't be too bad. I added two classes, DatabaseParams and DatabaseUtils to help with custom sqlite statements. Look in res>values>queries.xml for statements used.

*Feel the tests show SQLiteDatabase is thread-safe. Also all operations, including insert, update, and read, are mutually exclusive. This means, two operations from different threads can not run in parallel. Transaction counts as one operation.
**Another non-documented point is, whether you should SQLiteDatabase.close(), and when exactly.Turns out, Android already implements the reference counting internally. Basically, whenever you do anything with SQLiteDatabase, it increments a counter, and when you’re done, it decrements it. When all operations are done, DB just closes itself.
