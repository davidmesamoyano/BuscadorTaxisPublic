package es.upv.etsit.atelem.buscadortaxis.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import es.upv.etsit.atelem.buscadortaxis.R;

public class MiToolbar {
    public static void show(AppCompatActivity activity, String titulo, boolean volverButton){

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(titulo);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(volverButton);

    }
}
