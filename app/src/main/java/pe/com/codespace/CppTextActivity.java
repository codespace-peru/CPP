package pe.com.codespace;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;

public class CppTextActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    private SearchView searchView;
    ArticulosListAdapter myListAdapter;
    ListView myList;
    SQLiteHelper myDBHelper;
    String articuloSeleccionado= "";
    String art="";
    String artTemp="";
    MenuItem menuItem;
    int tit, cap;
    boolean ir = false;
    int primerArticulo, gotoArticulo;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpp_text);
        myDBHelper = SQLiteHelper.getInstance(this);
        Intent intent = getIntent();
        tit = intent.getExtras().getInt("titulo");
        cap = intent.getExtras().getInt("capitulo");
        ir = intent.getExtras().getBoolean("ir");

        TextView myText;
        TextView myText1;
        try {
            myDBHelper = SQLiteHelper.getInstance(this);
            String[] titTitle = myDBHelper.getTitulo(tit);
            String[] capTitle = myDBHelper.getCapitulo(tit,cap);
            String[][] LstArticulos = myDBHelper.getListaArticulosxTitxCap(tit,cap);

            myText = (TextView) findViewById(R.id.tvTitleTitulo);
            myText1 = (TextView) findViewById(R.id.tvTitleCapitulo);

            if(tit!=0 && tit!=8){
                myText.setText("Título " + titTitle[0] + ": " + titTitle[1]);
            }
            else if(tit==0){//No muestra el Preambulo
                myText.setText("");
            }
            else{
                myText.setText(titTitle[1]);
            }

            if(tit!=0 && tit!=5 && tit!=6 && tit!=7 && tit!=8){
                myText1.setText("Capítulo " + capTitle[0] + ": " + capTitle[1]);
            }
            else{
                myText1.setText(capTitle[1]);
            }

            myList = (ListView) findViewById(R.id.lvTextCpp);
            myListAdapter = new ArticulosListAdapter(this,LstArticulos);
            myList.setAdapter(myListAdapter);
            if(ir==true){
                String tt = LstArticulos[0][0];
                primerArticulo = Integer.parseInt(tt);
                gotoArticulo = intent.getExtras().getInt("gotoArticulo");
                myList.setSelection(gotoArticulo-primerArticulo);
            }
            registerForContextMenu(myList);
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getBaseContext(),"Mantener presionado para ver opciones",Toast.LENGTH_LONG).show();
                }
            });

            // Agregar el adView
            AdView adView = (AdView)this.findViewById(R.id.adView2);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

        } catch (Exception ex) {
            ex.printStackTrace();
        };
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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,view,menuInfo);
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        articuloSeleccionado = ((TextView) info.targetView.findViewById(R.id.tvTitleItem)).getText().toString();
        menu.setHeaderTitle(articuloSeleccionado);
        inflater.inflate(R.menu.menu_contextual_lista,menu);
        Tools tools = new Tools();
        int size = articuloSeleccionado.length();
        if(size>=12){
            art = articuloSeleccionado.substring(9,size-3);
            if(!tools.isNumeric(art)){
                art = articuloSeleccionado;
                artTemp = articuloSeleccionado;
            }
            else{
                artTemp = articuloSeleccionado.substring(0,size-3);//Quita los ":" del texto si los hubiera
            }
        }
        else{
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

        if(myDBHelper.es_favorito(art)){
            MenuItem itemHide3 = menu.findItem(R.id.CtxAddFavorito);
            itemHide3.setVisible(false);
        }
        else{
            MenuItem itemHide3 = menu.findItem(R.id.CtxDelFavorito);
            itemHide3.setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String a = art;
        final String b = artTemp;
        try{
            switch (item.getItemId()) {
                case R.id.CtxAddFavorito:
                    myDBHelper.setFavorito(art);
                    Toast.makeText(CppTextActivity.this,"Se agregó " + artTemp.toLowerCase() + " a Favoritos",Toast.LENGTH_LONG).show();
                    return true;
                case R.id.CtxDelFavorito:
                    AlertDialog.Builder confirmar = new AlertDialog.Builder(this);
                    confirmar.setTitle("Eliminar de Favoritos");
                    confirmar.setMessage("¿Está seguro que desea quitar el " + artTemp + " de Mis Favoritos?");
                    confirmar.setCancelable(false);
                    confirmar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface confirmar, int i) {
                            if(myDBHelper.eliminarFavorito(a)){
                                Toast.makeText(CppTextActivity.this,"Se eliminó " + b.toLowerCase() + " de Favoritos",Toast.LENGTH_LONG).show();
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
                case R.id.CtxAddNote: case R.id.CtxEditNote:
                    Intent intent1 = new Intent(this,AddNoteActivity.class);
                    intent1.putExtra("articulo",art);
                    intent1.putExtra("articuloFull",artTemp);
                    this.startActivity(intent1);
                    return true;
                case R.id.CtxCopyArticulo:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE) ;
                    String articulo = myDBHelper.getArticulo(art)[2];
                    ClipData clip = ClipData.newPlainText("text",artTemp.toUpperCase() + ":" + "\n\n" + articulo);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getBaseContext(), "El " + artTemp + " ha sido copiado al portapapeles.", Toast.LENGTH_LONG).show();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        catch (Exception ex){
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar_main, menu);
        final MenuItem searchItem;
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Ingrese su búsqueda...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                MenuItemCompat.collapseActionView(searchItem);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        menuItem = item;
        switch (item.getItemId()){
            case R.id.action_search:
                break;
            case R.id.action_voice:
                SpeechRecognitionHelper speech = new SpeechRecognitionHelper();
                speech.run(this);
                break;
            case R.id.action_goto:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Ir al Artículo");
                final EditText input = new EditText(this);
                //Para que acepte maximo 3 caracteres
                input.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(3),
                });
                //Para que use el softkeyborad con solo numeros
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setPositiveButton("Mostrar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if(value!=null && !value.isEmpty()){
                            String[] articulo;
                            articulo = myDBHelper.getArticulo(value);
                            int art = Integer.parseInt(value);
                            if(art>0 && art<=206){
                                Intent intent = new Intent(CppTextActivity.this,CppTextActivity.class);
                                intent.putExtra("titulo", Integer.parseInt(articulo[0]));
                                intent.putExtra("capitulo",Integer.parseInt(articulo[1]));
                                intent.putExtra("gotoArticulo",Integer.parseInt(value));
                                intent.putExtra("ir",true);
                                finish();
                                startActivity(intent);
                            }
                            else
                                Toast.makeText(getApplicationContext(),"Numero de artículo no válido", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(),"Numero de artículo no válido", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) { }
                });
                alert.show();
                break;
            case R.id.action_favorites:
                Intent intent = new Intent(this,FavoritosActivity.class);
                this.startActivity(intent);
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
        MenuItemCompat.collapseActionView(menuItem);
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
