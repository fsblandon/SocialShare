package com.example.socialshare;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialshare.Models.DrawView;
import com.example.socialshare.Models.Persona;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

import static android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    //boolean flag to know if main FAB is in open or closed state.
    private boolean fabExpanded = false;
    private FloatingActionButton fab;

    //Linear layout holding the Save submenu
    private FloatingActionButton fabWhatsapp;

    //Linear layout holding the Edit submenu
    private FloatingActionButton fabGmail;

    private EditText name;
    private EditText lastName;

    private Persona persona = new Persona();

    private DrawView draw;

    private Bitmap bitmap = Bitmap.createBitmap(1000/*width*/, 1000/*height*/, Bitmap.Config.ARGB_8888);

    private Canvas canvas = new Canvas(bitmap);

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    private FirebaseApp firebaseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseApp.initializeApp(this);

        fab = findViewById(R.id.fab);
        fabWhatsapp = findViewById(R.id.fabWhatsapp);
        fabGmail = findViewById(R.id.fabGmail);
        name = findViewById(R.id.editText);
        lastName = findViewById(R.id.editText2);

        draw = findViewById(R.id.simpleDrawingView1);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });

        fabWhatsapp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                persona.setName(name.getText().toString());
                persona.setLastName(lastName.getText().toString());

                draw.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);

                persona.setSignatue(encode);

                openWhatsApp(view, persona);

                FirebaseDatabase db = FirebaseDatabase.getInstance();

                DatabaseReference dbRef = db.getReference("socialshare");
                dbRef.child("whatsapp").push().setValue(persona);
            }
        });

        fabGmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                persona.setName(name.getText().toString());
                persona.setLastName(lastName.getText().toString());

                draw.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);

                persona.setSignatue(encode);

                openMail(view, persona);

                FirebaseDatabase db = FirebaseDatabase.getInstance();

                DatabaseReference dbRef = db.getReference("socialshare");
                dbRef.child("email").push().setValue(persona);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //closes FAB submenus
    private void closeSubMenusFab(){
        fabWhatsapp.animate().translationY(0);
        fabGmail.animate().translationY(0);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab() {
        fabWhatsapp.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabGmail.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fabExpanded = true;
    }

    public void openWhatsApp(View view, Persona persona){
        PackageManager pm = getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Nombre: "+ persona.getName() +"\n"+
                    "Apellido: "+ persona.getLastName() + "\n"+
                    "Firma: " + persona.getSignature();

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Compartir con"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void openMail(View view, Persona persona) {

        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);

            String subject = "Test movity";
            String body = "Nombre: "+ persona.getName() +"\n"+
                    "Apellido: "+ persona.getLastName() + "\n"+
                    "Firma: " + persona.getSignature();
            String mailto = "ingesisblandon@outlook.com";

            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailto});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            emailIntent.setType("message/rfc822");

            //emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Enviar con"));

        } catch (Exception e){
            Toast.makeText(this, "Correo no está instalado", Toast.LENGTH_SHORT).show();
        }
    }
}
