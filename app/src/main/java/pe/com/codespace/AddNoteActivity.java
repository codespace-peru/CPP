package pe.com.codespace;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Created by Carlos on 01/03/14.
 */
public class AddNoteActivity extends ActionBarActivity {
    SQLiteHelper myDBHelper;
    String nota = "";
    String art="";
    String articulo="";
    EditText editText;
    boolean modify = false;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);

        try{
            myDBHelper = SQLiteHelper.getInstance(this);
            Intent intent = getIntent();
            art = intent.getExtras().getString("articulo");
            articulo = intent.getExtras().getString("articuloFull");

            TextView textView = (TextView) findViewById(R.id.tvAddNota);
            textView.setText("Nota para el " + articulo + ":");
            editText = (EditText) findViewById(R.id.edtAddNota);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                    }
            });

            if(myDBHelper.hay_nota(art)){
                TextView textView1 = (TextView) findViewById(R.id.addnote_title);
                textView1.setText("MODIFICAR NOTA");
                nota = myDBHelper.getNota(art)[2];
                editText.setText(nota);
                int end = editText.getText().length();
                editText.setSelection(end); // Colocar el cursor al final
                modify=true;
            }
            else{
                TextView textView1 = (TextView) findViewById(R.id.addnote_title);
                textView1.setText("AGREGAR NOTA");
            }
            // Agregar el adView
            AdView adView = (AdView)this.findViewById(R.id.adView5);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

        }catch (Exception ex){
            Log.e("Debug", "MessageError: " + ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_addnotes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_saveNota:
                nota = editText.getText().toString();
                if(myDBHelper.AddNota(art,nota)){
                    if(modify==true)
                        Toast.makeText(AddNoteActivity.this,"Se modificó la nota de " + articulo,Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(AddNoteActivity.this,"Se agregó la nota a " + articulo,Toast.LENGTH_LONG).show();
                    this.finish();
                }
                break;
            case R.id.action_cancelarNota:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
