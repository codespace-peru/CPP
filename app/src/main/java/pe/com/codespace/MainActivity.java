package pe.com.codespace;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    private SearchView searchView;
    SQLiteHelper myDBHelper;
    ExpandableListAdapter listAdapter;
    ExpandableListView myExpandList;
    List<RowGroupCpp> listDataHeader;
    HashMap<RowGroupCpp, List<RowItemCpp>> listDataChild;
    MenuItem menuItem;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDBHelper = SQLiteHelper.getInstance(this);

        myExpandList = (ExpandableListView) findViewById(R.id.lvCppExpand);
        prepararMenu();
        listAdapter = new CppExpandableListAdapter(this,listDataHeader, listDataChild);
        myExpandList.setGroupIndicator(null);
        myExpandList.setAdapter(listAdapter);


        myExpandList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition)
                    myExpandList.collapseGroup(lastExpandedPosition);
                lastExpandedPosition = groupPosition;
            }
        });

        myExpandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPos, int childPos, long id) {
                Intent intent = new Intent(view.getContext(),CppTextActivity.class);
                intent.putExtra("titulo", groupPos);
                intent.putExtra("capitulo",childPos + 1); //Estos titulos empiezan en el capitulo 1
                view.getContext().startActivity(intent);
                return false;
            }
        });

        myExpandList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
                Intent intent = new Intent(view.getContext(),CppTextActivity.class);
                intent.putExtra("titulo",groupPosition);
                intent.putExtra("capitulo",0); // Los titulos 0, 5,6,7 y 8 no tienen capitulos
                int i = (int)id;
                switch (i){
                    case 0: case 5: case 6: case 7: case 8:
                        view.getContext().startActivity(intent);
                        break;
                }
                return false;
            }
        });

        // Agregar el adView
        AdView adView = (AdView)this.findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Búsqueda...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
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
                                Intent intent = new Intent(MainActivity.this,CppTextActivity.class);
                                intent.putExtra("titulo", Integer.parseInt(articulo[0]));
                                intent.putExtra("capitulo",Integer.parseInt(articulo[1]));
                                intent.putExtra("gotoArticulo",Integer.parseInt(value));
                                intent.putExtra("ir",true);
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
        menuItem.collapseActionView();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    private void prepararMenu(){
        listDataHeader = new ArrayList<RowGroupCpp>();
        listDataChild = new HashMap<RowGroupCpp, List<RowItemCpp>>();
        int size=0;

        String[][] Titulos = myDBHelper.getTitulos();
        for(int i=0;i<9;i++) {
            RowGroupCpp temp = new RowGroupCpp(Integer.parseInt(Titulos[0][i]),Titulos[1][i],Titulos[2][i],Titulos[3][i]);
            listDataHeader.add(temp);
        }

        List<RowItemCpp> titulo1 = new ArrayList<RowItemCpp>();
        String[][] CapitulosxTitulo1 = myDBHelper.getCapitulosxTitulo(1) ;
        size = CapitulosxTitulo1[0].length;
        for(int i=0;i<size;i++)
            titulo1.add(new RowItemCpp(CapitulosxTitulo1[0][i], CapitulosxTitulo1[1][i], CapitulosxTitulo1[2][i]));

        List<RowItemCpp> titulo2 = new ArrayList<RowItemCpp>();
        String[][] CapitulosxTitulo2 = myDBHelper.getCapitulosxTitulo(2) ;
        size = CapitulosxTitulo2[0].length;
        for(int i=0;i<size;i++)
            titulo2.add(new RowItemCpp(CapitulosxTitulo2[0][i], CapitulosxTitulo2[1][i], CapitulosxTitulo2[2][i]));

        List<RowItemCpp> titulo3 = new ArrayList<RowItemCpp>();
        String[][] CapitulosxTitulo3 = myDBHelper.getCapitulosxTitulo(3) ;
        size = CapitulosxTitulo3[0].length;
        for(int i=0;i<size;i++){
            titulo3.add(new RowItemCpp(CapitulosxTitulo3[0][i],CapitulosxTitulo3[1][i],CapitulosxTitulo3[2][i]));
        }

        List<RowItemCpp> titulo4 = new ArrayList<RowItemCpp>();
        String[][] CapitulosxTitulo4 = myDBHelper.getCapitulosxTitulo(4) ;
        size = CapitulosxTitulo4[0].length;
        for(int i=0;i<size;i++){
            titulo4.add(new RowItemCpp(CapitulosxTitulo4[0][i],CapitulosxTitulo4[1][i],CapitulosxTitulo4[2][i]));
        }

        //Hay 9 headers: el 0 es el preambulo y no tiene hijos
        listDataChild.put(listDataHeader.get(0),new ArrayList<RowItemCpp>());
        listDataChild.put(listDataHeader.get(1), titulo1);
        listDataChild.put(listDataHeader.get(2), titulo2);
        listDataChild.put(listDataHeader.get(3), titulo3);
        listDataChild.put(listDataHeader.get(4), titulo4);
        listDataChild.put(listDataHeader.get(5),new ArrayList<RowItemCpp>());
        listDataChild.put(listDataHeader.get(6),new ArrayList<RowItemCpp>());
        listDataChild.put(listDataHeader.get(7),new ArrayList<RowItemCpp>());
        listDataChild.put(listDataHeader.get(8),new ArrayList<RowItemCpp>());

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
