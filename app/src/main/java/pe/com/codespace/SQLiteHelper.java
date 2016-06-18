package pe.com.codespace;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

/**
 * Creado por Carlos on 7/01/14.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private final Context myContext;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "BD_CPP.db";
    private static final String DATABASE_PATH = "databases/";
    private static File DATABASE_FILE = null;
    private boolean mInvalidDatabaseFile = false;
    private boolean mIsUpgraded  = false;
    private int mOpenConnections=0;
    private static SQLiteHelper mInstance;

    public synchronized static SQLiteHelper getInstance (Context context){
        if(mInstance == null){
            mInstance = new SQLiteHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private SQLiteHelper(Context context)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;

        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            DATABASE_FILE = context.getDatabasePath(DATABASE_NAME);
            if(mInvalidDatabaseFile){
                copyDatabase();
            }
            if(mIsUpgraded){
                doUpgrade();
            }
        }
        catch(SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mInvalidDatabaseFile = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mInvalidDatabaseFile = true;
        mIsUpgraded = true;
    }

    @Override
    public synchronized void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        mOpenConnections++;
        if(!db.isReadOnly()){
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public synchronized void close(){
        mOpenConnections--;
        if(mOpenConnections == 0){
            super.close();
        }
    }

    public void copyDatabase()  {
        AssetManager assetManager = myContext.getResources().getAssets();
        InputStream myInput = null;
        OutputStream myOutput = null;
        try{
            myInput = assetManager.open(DATABASE_PATH +DATABASE_NAME);
            myOutput = new FileOutputStream(DATABASE_FILE);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = myInput.read(buffer)) != -1) {
                myOutput.write(buffer, 0, read);
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {
            if(myInput != null){
                try{ myInput.close(); }
                catch(IOException ex){ex.printStackTrace(); }
            }
            if(myOutput!=null){
                try{ myOutput.close(); }
                catch (IOException ex){ ex.printStackTrace(); }
            }
            setDataBaseVersion();
            mInvalidDatabaseFile = false;
        }
    }

    private void setDataBaseVersion(){
        SQLiteDatabase db = null;
        try{
            db = SQLiteDatabase.openDatabase(DATABASE_FILE.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.execSQL("PRAGMA user_version=" + DATABASE_VERSION);
        }
        catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    private void doUpgrade(){
        try{
            myContext.deleteDatabase(DATABASE_NAME);
            copyDatabase();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String[][] getTitulos(){
        SQLiteDatabase db = null;
        try{
            db = getWritableDatabase();
            Cursor cursor = db.rawQuery("select NUMTIT, TIT, TITULO, DESCRIPCION from TITULOS",null);
            String[][] arrayOfString = (String[][])Array.newInstance(String.class, new int[] { 4, cursor.getCount() });
            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0][i] = cursor.getString(0);
                    arrayOfString[1][i] = cursor.getString(1);
                    arrayOfString[2][i] = cursor.getString(2);
                    arrayOfString[3][i] = cursor.getString(3);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[][] getCapitulosxTitulo(int titulo) {
        SQLiteDatabase db = null;
        try{
            db = getWritableDatabase();
            Cursor cursor = db.rawQuery("select NUMCAP, CAPITULO, DESCRIPCION from CAPITULOS WHERE NUMTIT = ?", new String[] {String.valueOf(titulo)});
            String[][] arrayOfString = (String[][])Array.newInstance(String.class, new int[] { 3, cursor.getCount() });
            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0][i] = cursor.getString(0);
                    arrayOfString[1][i] = cursor.getString(1);
                    arrayOfString[2][i] = cursor.getString(2);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[] getTitulo(int tit){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("select TIT, TITULO from TITULOS WHERE NUMTIT = ?", new String[] {String.valueOf(tit)});
            String[] arrayOfString = (String[]) Array.newInstance(String.class, new int[]{2});
            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0] = cursor.getString(0);
                    arrayOfString[1] = cursor.getString(1);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
           throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[] getCapitulo(int tit, int cap){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            String[] array = new String[2];
            array[0] = String.valueOf(tit);
            array[1] = String.valueOf(cap);
            Cursor cursor = db.rawQuery("select NUMCAP, CAPITULO from CAPITULOS WHERE NUMTIT = ? and NUMCAP = ?", array);
            String[] arrayOfString = (String[]) Array.newInstance(String.class, new int[]{2});
            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0] = cursor.getString(0);
                    arrayOfString[1] = cursor.getString(1);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }


    public String[] getArticulo(String art){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT NUMTIT, NUMCAP, ARTICULO FROM ARTICULOS WHERE NUMART = ?", new String[] {String.valueOf(art)});
            String[] arrayOfString = (String[]) Array.newInstance(String.class, new int[]{3});
            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0] = cursor.getString(0);
                    arrayOfString[1] = cursor.getString(1);
                    arrayOfString[2] = cursor.getString(2);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[][] getListaArticulosxTitxCap(int tit, int cap) {
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            String[] array = new String[2];
            array[0] = String.valueOf(tit);
            array[1] = String.valueOf(cap);
            Cursor cursor = db.rawQuery("select A.NUMART, A.ARTICULO from ARTICULOS AS A WHERE A.NUMTIT = ? and A.NUMCAP = ?", array);
            String[][] arrayOfString = (String[][]) Array.newInstance(String.class, new int[]{cursor.getCount(),3});

            int i = 0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[i][0] = cursor.getString(0);
                    if(tit!=0 && tit!=7 && tit!=8)
                        arrayOfString[i][1] = "Artículo " + cursor.getString(0) + "º: ";
                    else{
                        arrayOfString[i][1] = cursor.getString(0);
                    }
                    arrayOfString[i][2] = cursor.getString(1);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean es_favorito(String art) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = getReadableDatabase();
            cursor = db.rawQuery("select ES_FAVORITO from ARTICULOS where NUMART = ? ", new String[]{art});
            cursor.moveToFirst();
            return Integer.parseInt(cursor.getString(0))  == 1;
        }catch (SQLiteException ex){
            throw ex;
        }
        finally {
            cursor.close();
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[][] getFavoritos(){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT NUMTIT, NUMART, ARTICULO FROM ARTICULOS WHERE ES_FAVORITO = ? ", new String[]{"1"});
            String[][] arrayOfString = (String[][])Array.newInstance(String.class, new int[] {cursor.getCount(),3});
            int i = 0;
            int tit = -1;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    tit = Integer.parseInt(cursor.getString(0));
                    arrayOfString[i][0] = cursor.getString(1);
                    if(tit!=0 && tit!=7 && tit!=8)
                        arrayOfString[i][1] = "Artículo " + cursor.getString(1) + "º: ";
                    else {
                        arrayOfString[i][1] = cursor.getString(1);
                    }
                    arrayOfString[i][2] = cursor.getString(2);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }catch(SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean setFavorito(String art){
        SQLiteDatabase db=null;
        try{
            boolean flag = false;
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ES_FAVORITO",1);
            String[] whereArgs={art};
            int x = db.update("ARTICULOS",values,"NUMART = ? ",whereArgs);
            if (x > 0){
                flag=true;
            }
            return flag;
        } catch (SQLiteException ex){
			ex.printStackTrace();
          throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean eliminarFavorito(String art){
        SQLiteDatabase db = null;
        try{
            boolean flag = false;
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ES_FAVORITO",0);
            String[] whereArgs={art};
            int x = db.update("ARTICULOS",values,"NUMART LIKE ? ",whereArgs);
            if (x > 0){
                flag=true;
            }
            return flag;
        } catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[][] getNotes(){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT NUMTIT, NUMART, NOTAS FROM ARTICULOS WHERE HAY_NOTAS = ? ", new String[] {"1"});
            String[][] arrayOfString = (String[][])Array.newInstance(String.class, new int[] {cursor.getCount(),3});
            int i = 0;
            int tit = -1;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    tit = Integer.parseInt(cursor.getString(0));
                    arrayOfString[i][0] = cursor.getString(1);
                    if(tit!=0 && tit!=7 && tit!=8)
                        arrayOfString[i][1] = "Artículo " + cursor.getString(1) + "º: ";
                    else {
                        arrayOfString[i][1] = cursor.getString(1);
                    }
                    arrayOfString[i][2] = cursor.getString(2);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }catch(SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean hay_nota(String art) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = getReadableDatabase();
            cursor = db.rawQuery("select HAY_NOTAS from ARTICULOS where NUMART = ? ", new String[]{art});
            if(cursor.moveToFirst()){
                return cursor.getInt(0) == 1;
            }
            else return false;
        }catch (SQLiteException ex){
            ex.printStackTrace();
            throw ex;
        }
        finally {
            cursor.close();
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[] getNota(String art){
        SQLiteDatabase db = null;
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT NUMTIT, NUMCAP, NOTAS FROM ARTICULOS WHERE NUMART = ? AND HAY_NOTAS = 1", new String[] {String.valueOf(art)});
            String[] arrayOfString = (String[]) Array.newInstance(String.class, new int[]{3});
            int i=0;
            if (cursor.moveToFirst()) {
                while ( !cursor.isAfterLast() ) {
                    arrayOfString[0] = cursor.getString(0);
                    arrayOfString[1] = cursor.getString(1);
                    arrayOfString[2] = cursor.getString(2);
                    i++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean AddNota(String art, String nota){
        SQLiteDatabase db = null;
        try{
            boolean flag = false;
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("HAY_NOTAS",1);
            values.put("NOTAS",nota);
            String[] whereArgs={art};
            int x = db.update("ARTICULOS",values,"NUMART = ? ",whereArgs);
            if (x > 0){
                flag=true;
            }
            return flag;
        } catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public boolean EliminarNota(String art){
        SQLiteDatabase db = null;
        try{
            boolean flag = false;
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("HAY_NOTAS",0);
            values.put("NOTAS","");
            String[] whereArgs={art};
            int x = db.update("ARTICULOS",values,"NUMART = ? ",whereArgs);
            if (x > 0){
                flag=true;
            }
            return flag;
        } catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }

    public String[][] searchArticulo(String cadena) {
        SQLiteDatabase db = null;
        //String[] cad = cadena.split(" ");
        String sqlLike = "SELECT NUMTIT, NUMART, ARTICULO from ARTICULOS where ARTICULO LIKE " + "\'%" + cadena + "%\' COLLATE NOCASE";
   /*     for(int i=0;i<cad.length;i++){
            if(i<cad.length-1){
                sqlLike = sqlLike + "ARTICULO LIKE " + "\'%" + cad[i]  + "%\' AND ";
            }
            else{
                sqlLike = sqlLike + "ARTICULO LIKE " + "\'%" + cad[i]  + "%\' COLLATE NOCASE";
            }
        }*/
        try{
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlLike, null);
            int j = 0;
            String[][] arrayOfString = (String[][])Array.newInstance(String.class, new int[] { cursor.getCount(),2 });
            int tit;
            if(cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    tit = Integer.parseInt(cursor.getString(0));
                    if(tit!=0 && tit!=7 && tit!=8)
                        arrayOfString[j][0] = "Artículo " + cursor.getString(1) + "º: ";
                    else
                        arrayOfString[j][0] = cursor.getString(1);

                    arrayOfString[j][1] = cursor.getString(2);
                    j++;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return arrayOfString;
        }
        catch (SQLiteException ex){
			ex.printStackTrace();
            throw ex;
        }
        finally {
            if(db != null && db.isOpen()){
                db.close();
            }
        }
    }
}
