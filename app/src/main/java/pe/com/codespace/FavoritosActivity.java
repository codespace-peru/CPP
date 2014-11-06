package pe.com.codespace;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
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
import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;

/**
 * Created by Carlos on 16/02/14.
 */
public class FavoritosActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    ArticulosListAdapter myListAdapter;
    SQLiteHelper myDBHelper;
    ListView myList;
    String articuloSeleccionado= "";
    private SearchView searchView;
    MenuItem menuItem;
    String art="";
    String artTemp="";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        try{
            myDBHelper = SQLiteHelper.getInstance(this);
            String[][] myListaFav = myDBHelper.getFavoritos();
            TextView tt = (TextView) findViewById(R.id.txtNone);
            if(myListaFav.length>0){
                tt.setVisibility(View.GONE);
            }
            myList = (ListView) findViewById(R.id.lvFavoritos);
            myListAdapter = new ArticulosListAdapter(this,myListaFav);
            myList.setAdapter(myListAdapter);
            registerForContextMenu(myList);
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getBaseContext(),"Mantener presionado para ver opciones",Toast.LENGTH_LONG).show();
                }
            });
            // Agregar el adView
            AdView adView = (AdView)this.findViewById(R.id.adView3);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }catch (Exception ex){
           Log.e("Debug","MessageError: " + ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
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
        super.onCreateContextMenu(menu,view,menuInfo);
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        articuloSeleccionado = ((TextView) info.targetView.findViewById(R.id.tvTitleItem)).getText().toString();
        menu.setHeaderTitle(articuloSeleccionado);
        inflater.inflate(R.menu.menu_contextual_lista,menu);
        Tools tools = new Tools();
        int size = articuloSeleccionado.length();
        if(size >= 12){
            art = articuloSeleccionado.substring(9,size-3);
            if(!tools.isNumeric(art)){//si no es numero
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
        if(myDBHelper.hay_nota(art)){
            MenuItem itemHide1 = menu.findItem(R.id.CtxAddNote);
            itemHide1.setVisible(false);
        }
        else{
            MenuItem itemHide1 = menu.findItem(R.id.CtxEditNote);
            itemHide1.setVisible(false);
            MenuItem itemHide2 = menu.findItem(R.id.CtxShowNote);
            itemHide2.setVisible(false);
        }
        MenuItem itemHide3 = menu.findItem(R.id.CtxAddFavorito);
        itemHide3.setVisible(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String a = art;
        final String b = artTemp;
        switch (item.getItemId()) {
            case R.id.CtxAddFavorito:
                return false; // Esta opción no se mostrará al usuario en esta activiy
            case R.id.CtxDelFavorito:
                AlertDialog.Builder confirmar = new AlertDialog.Builder(this);
                confirmar.setTitle("Eliminar de Favoritos");
                confirmar.setMessage("¿Está seguro que desea quitar el " + artTemp + " de Mis Favoritos?");
                confirmar.setCancelable(false);
                confirmar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface confirmar, int i) {
                        if(myDBHelper.eliminarFavorito(a)){
                            Toast.makeText(FavoritosActivity.this,"Se eliminó " + b.toLowerCase() + " de Favoritos",Toast.LENGTH_LONG).show();
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
                return true;
            case R.id.CtxShowNote:
                String nota = myDBHelper.getNota(art)[2];
                AlertDialog.Builder dialogoNota = new AlertDialog.Builder(this);
                dialogoNota.setTitle("Notas del " + artTemp);
                dialogoNota.setMessage(nota);
                dialogoNota.setCancelable(true);
                dialogoNota.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int i) {
                        dialog1.cancel();
                    }
                });
                dialogoNota.show();
                return true;
            case R.id.CtxCopyArticulo:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE) ;
                String articulo = myDBHelper.getArticulo(art)[2];
                ClipData clip = ClipData.newPlainText("text",artTemp.toUpperCase() + ":" + "\n\n" + articulo);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(), "El " + artTemp + " ha sido copiado al portapapeles.", Toast.LENGTH_LONG).show();
                return  true;
            case R.id.CtxAddNote: case R.id.CtxEditNote:
                Intent intent1 = new Intent(this,AddNoteActivity.class);
                intent1.putExtra("articulo",art);
                intent1.putExtra("articuloFull",artTemp);
                this.startActivity(intent1);
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
        MenuItem itemHide1 = menu.findItem(R.id.action_favorites);
        MenuItem itemHide2 = menu.findItem(R.id.action_goto);
        itemHide1.setVisible(false);
        itemHide2.setVisible(false);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Ingrese su búsqueda...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b){
                menuItem.collapseActionView();
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
                SpeechRecognitionHelper speech = new SpeechRecognitionHelper();
                speech.run(this);
                break;
            case R.id.action_notes:
                Intent intent1 = new Intent(this,NotesActivity.class);
                this.startActivity(intent1);
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

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
