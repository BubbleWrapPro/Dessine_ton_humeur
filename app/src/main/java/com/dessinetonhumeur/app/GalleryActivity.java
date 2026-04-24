package com.dessinetonhumeur.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private DatabaseHelper dbHelper;
    private GalleryAdapter adapter;

    // Cet outil moderne d'Android remplace l'ancien "onActivityResult".
    // Il gère l'action "aller chercher un document" et nous renvoie un Uri quand l'utilisateur a choisi une image.
    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // L'utilisateur a choisi une image ! On lui demande un titre.
                        askForTitleAndSave(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        dbHelper = new DatabaseHelper(this);
        gridView = findViewById(R.id.grid_view);
        Button btnChoosePhoto = findViewById(R.id.btn_choose_photo);
        Button btnDeletePhoto = findViewById(R.id.btn_delete_photo);

        // On charge la galerie une première fois
        loadGallery();

        // Clic sur le bouton "Depuis le téléphone"
        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Création d'une "Intention" pour ouvrir le sélecteur d'images d'Android
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                photoPickerLauncher.launch(Intent.createChooser(intent, "Sélectionnez une image"));
            }
        });

        // Bouton Supprimer
        btnDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null || adapter.getCount() == 0) {
                    Toast.makeText(GalleryActivity.this, "Galerie vide", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!adapter.isSelectionMode()) {
                    // On entre en mode sélection
                    adapter.setSelectionMode(true);
                    btnDeletePhoto.setText("Confirmer la suppression");
                    Toast.makeText(GalleryActivity.this, "Sélectionnez les images à supprimer", Toast.LENGTH_SHORT).show();
                } else {
                    // On supprime les éléments sélectionnés
                    Set<Integer> selectedPositions = adapter.getSelectedPositions();
                    if (selectedPositions.isEmpty()) {
                        // Si rien n'est sélectionné, on quitte juste le mode
                        adapter.setSelectionMode(false);
                        btnDeletePhoto.setText(R.string.supprime_photo);
                    } else {
                        // Demander confirmation avant de supprimer plusieurs
                        new AlertDialog.Builder(GalleryActivity.this)
                                .setTitle("Suppression")
                                .setMessage("Voulez-vous supprimer les images sélectionnées ?")
                                .setPositiveButton("Oui", (dialog, which) -> {
                                    List<GalleryItem> items = dbHelper.getAllGalleryItems();
                                    for (int pos : selectedPositions) {
                                        dbHelper.deleteGalleryItem(items.get(pos).id);
                                    }
                                    adapter.setSelectionMode(false);
                                    btnDeletePhoto.setText(R.string.supprime_photo);
                                    loadGallery();
                                    Toast.makeText(GalleryActivity.this, "Images supprimées", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Non", null)
                                .show();
                    }
                }
            }
        });

        // Clic sur un item de la grille
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter != null && adapter.isSelectionMode()) {
                adapter.toggleSelection(position);
            }
        });

    }

    // Méthode pour afficher un Popup demandant le titre du dessin
    private void askForTitleAndSave(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Titre de votre œuvre");

        // On crée un champ de texte (EditText) dans le code pour le mettre dans le popup
        final EditText input = new EditText(this);
        builder.setView(input);

        // Bouton Valider
        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString().trim();
                if (title.isEmpty()) {
                    title = "Sans titre";
                }

                // 1. On copie physiquement l'image
                String savedPath = FileManager.copyImageToInternalStorage(GalleryActivity.this, imageUri);

                if (savedPath != null) {
                    // 2. On sauvegarde en base de données
                    dbHelper.addImageToGallery(title, savedPath);
                    Toast.makeText(GalleryActivity.this, "Dessin sauvegardé !", Toast.LENGTH_SHORT).show();
                    // 3. On rafraîchit la grille pour voir la nouvelle image
                    loadGallery();
                } else {
                    Toast.makeText(GalleryActivity.this, "Erreur lors de la copie de l'image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bouton Annuler
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Charge (ou recharge) les images depuis la BDD vers l'écran
    private void loadGallery() {
        List<GalleryItem> galleryItems = dbHelper.getAllGalleryItems();
        adapter = new GalleryAdapter(this, galleryItems);
        gridView.setAdapter(adapter);
    }
}