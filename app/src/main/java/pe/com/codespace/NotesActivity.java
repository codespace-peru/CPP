package pe.com.codespace;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

/**
 * Creado por Carlos on 01/03/14.
 * Modificado el 10/05/2015
 */
public class NotesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SQLiteHelper myDBHelper;
    ArticulosListAdapter myListAdapter;
    ListView myList;
    String articuloSeleccionado= "";
    SearchView searchView;
    MenuItem menuItem;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setIcon(R.drawable.ic_launcher);
        }

        try{
            myDBHelper = SQLiteHelper.getInstance(this);
            String[][] myListNotes = myDBHelper.getNotes();
            TextView tt = (TextView) findViewById(R.id.txtNoneNotas);
            if(myListNotes.length>0){
                tt.setVisibility(View.GONE);
            }
            myList = (ListView) findViewById(R.id.lvNotes);
            myListAdapter = new ArticulosListAdapter(this,myListNotes);
            myList.setAdapter(myListAdapter);
            registerForContextMenu(myList);
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getBaseContext(),"Mantener presionado para ver opciones",Toast.LENGTH_LONG).show();
                }
            });
            // Agregar el adView
            AdView adView = (AdView)this.findViewById(R.id.adViewNotas);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            //Analytics
            Tracker tracker = ((AnalyticsApplication)  getApplication()).getTracker(AnalyticsApplication.TrackerName.APP_TRACKER);
            String nameActivity = getApplicationContext().getPackageName() + "." + this.getClass().getSimpleName();
            tracker.setScreenName(nameActivity);
            tracker.enableAdvertisingIdCollection(true);
            tracker.send(new HitBuilders.AppViewBuilder().build());

        }catch (Exception ex){
            Log.e("Debug", "MessageError: " + ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MyValues.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0){
                Intent intent = new Intent(this,SearchResultsActivity.class);
                intent.putExtra("searchText",matches.get(0).toString());
                this.startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        articuloSeleccionado = ((TextView) info.targetView.findViewById(R.id.tvTitleItem)).getText().toString();
        menu.setHeaderTitle(articuloSeleccionado);
        inflater.inflate(R.menu.menu_contextual_notas,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String art;
        String artTemp;
        int size = articuloSeleccionado.length();
        if(size >= 12){
            art = articuloSeleccionado.substring(9,size-3);
            if(!Tools.isNumeric(art)){//si no es numero
                art = articuloSeleccionado;
                artTemp = articuloSeleccionado;
            }
            else{
                artTemp = articuloSeleccionado.substring(0,size-3);//Quita los "°:" del texto si los hubiera
            }
        }
        else{//
            art = articuloSeleccionado;
            artTemp = articuloSeleccionado;
        }
        final String a = art;
        final String b = artTemp;
        switch (item.getItemId()) {
            case R.id.CtxEditNote:
                Intent intent = new Intent(this,AddNoteActivity.class);
                intent.putExtra("articulo",art);
                intent.putExtra("articuloFull",artTemp);
                this.startActivity(intent);
                finish();
                return  true;
            case R.id.CtxDelNote:
                AlertDialog.Builder confirmar = new AlertDialog.Builder(this);
                confirmar.setTitle("Eliminar Nota");
                confirmar.setMessage("¿Está seguro que desea eliminar la nota?");
                confirmar.setCancelable(false);
                confirmar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface confirmar, int i) {
                        if(myDBHelper.EliminarNota(a)){
                            Toast.makeText(NotesActivity.this,"Se eliminó la nota de " + b.toLowerCase(),Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });
                confirmar.setNegativeButton("No", new DialogInterface.OnClickListener(){
                   @Override
                    public void onClick(DialogInterface confirmar, int i){
                       /////no hacer nada
                   }
                });
                confirmar.show();
                return  true;
            case R.id.CtxCopyNote:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE) ;
                String nota = myDBHelper.getNota(art)[2];
                ClipData clip = ClipData.newPlainText("text",artTemp.toUpperCase() + ":" + "\n\n" + nota);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(), "La nota ha sido copiada al portapapeles.", Toast.LENGTH_LONG).show();
                return  true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar_main, menu);
        final MenuItem searchItem;
        MenuItem itemHide1 = menu.findItem(R.id.action_notes);
        MenuItem itemHide2 = menu.findItem(R.id.action_goto);
        itemHide1.setVisible(false);
        itemHide2.setVisible(false);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Ingrese su búsqueda...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                searchItem.collapseActionView();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItem = item;
        switch (item.getItemId()){
            case R.id.action_search:
                break;
            case R.id.action_voice:
                SpeechRecognitionHelper.run(this);
                break;
            case R.id.action_favorites:
                Intent intent = new Intent(this,FavoritosActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_share:
                Social.share(this, getResources().getString(R.string.action_share), getResources().getString(R.string.share_description) + " " + Uri.parse("https://play.google.com/store/apps/details?id=pe.com.codespace"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent intent = new Intent(this,SearchResultsActivity.class);
        intent.putExtra("searchText", s);
        this.startActivity(intent);
        menuItem.collapseActionView();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

}
