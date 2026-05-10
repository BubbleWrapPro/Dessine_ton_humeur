package com.dessinetonhumeur.app;

// Default imports
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Import for database and time limitation
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private WheelView wheel;
    private TextView ideaText;
    private DatabaseHelper dbHelper;

    // Constantes pour les SharedPreferences (notre sauvegarde locale légère)
    private static final String PREFS_NAME = "DrawYourMoodPrefs";
    private static final String KEY_LAST_SPIN_TIME = "last_spin_time";

    // 1 heure en millisecondes : 60 minutes * 60 secondes * 1000
    private static final long ONE_HOUR_MILLIS = 300_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assure-toi que ton fichier XML s'appelle bien activity_main
        setContentView(R.layout.activity_main);

        // Initialisation de la base de données
        dbHelper = new DatabaseHelper(this);

        // Liaison avec les éléments de l'interface XML
        wheel = findViewById(R.id.wheel);
        Button generateButton = findViewById(R.id.generate_button);
        ideaText = findViewById(R.id.idea_text);

        Button galleryButton = findViewById(R.id.gallery_button);

        Button shareButton = findViewById(R.id.share_button);


        generateButton.setOnClickListener(v -> handleGenerateClick());

        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentIdea = ideaText.getText().toString();

                // On vérifie que l'utilisateur a bien généré une idée avant de partager
                if (!currentIdea.equals("Tournez la roue et appuyez sur générer !") && !currentIdea.isEmpty()) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain"); // On précise qu'on envoie du texte simple

                    // Le texte qui sera envoyé
                    String messageToShare = "Aujourd'hui, mon humeur m'inspire cette idée de dessin : " + currentIdea + " 🎨✨ (Généré via DrawYourMood)";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, messageToShare);

                    // Ouvre le menu de choix d'application
                    startActivity(Intent.createChooser(shareIntent, "Partager mon idée avec..."));
                } else {
                    Toast.makeText(MainActivity.this, "Générez d'abord une idée pour la partager !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleGenerateClick() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastSpinTime = prefs.getLong(KEY_LAST_SPIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        // 1. VÉRIFICATION DU TEMPS (1 heure)
        if (currentTime - lastSpinTime < ONE_HOUR_MILLIS) {
            // Le temps n'est pas écoulé. On calcule combien de minutes, il reste.
            long timeLeftMillis = ONE_HOUR_MILLIS - (currentTime - lastSpinTime);
            int minutesLeft = (int) (timeLeftMillis / (1000 * 60));

            Toast.makeText(MainActivity.this,
                    "Vous devez attendre encore " + minutesLeft + " minutes avant de changer d'idée !",
                    Toast.LENGTH_LONG).show();
            return; // On arrête la fonction ici, on ne génère rien.
        }

        // 2. GÉNÉRATION DE L'IDÉE
        // On demande à notre WheelView sur quelle humeur la flèche pointe
        String selectedMood = wheel.getSelectedMood();

        // On cherche une idée au hasard dans la base de données qui n'a pas été utilisée depuis 1 mois
        Idea randomIdea = dbHelper.getRandomIdea(selectedMood);

        if (randomIdea != null) {
            // On affiche le texte à l'écran
            ideaText.setText(randomIdea.text);

            // On met à jour la base de données pour dire que cette idée vient d'être utilisée (bloquée UN mois).
            dbHelper.markIdeaAsUsed(randomIdea.id);

            // On sauvegarde l'heure actuelle pour bloquer le bouton pendant 1 heure
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_LAST_SPIN_TIME, currentTime);
            editor.apply();

        } else {
            // Cas très rare : l'utilisateur a épuisé toutes les idées de cette humeur ce mois-ci
            Toast.makeText(this, "Toutes les idées pour cette humeur ont déjà été utilisées ce mois-ci !", Toast.LENGTH_LONG).show();
        }
    }
}