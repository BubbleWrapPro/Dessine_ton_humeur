package com.dessinetonhumeur.app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private DatabaseHelper dbHelper;
    private GalleryAdapter adapter;
    private Button btnDeletePhoto;

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

    private String currentPhotoPath;

    // Le lanceur spécifique pour l'appareil photo
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    // La photo a été prise et sauvegardée avec succès dans notre fichier !
                    askForTitleForCameraPhoto(currentPhotoPath);
                } else {
                    // L'utilisateur a annulé ou l'appareil photo a planté : on nettoie le fichier vide
                    FileManager.deleteImageFile(currentPhotoPath);
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
        btnDeletePhoto = findViewById(R.id.btn_delete_photo);
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // Ferme l'activité actuelle pour revenir à la précédente

        // On charge la galerie une première fois
        loadGallery();

        // Clic sur le bouton "Depuis le téléphone"
        btnChoosePhoto.setOnClickListener(v -> {
            // Création d'une "Intention" pour ouvrir le sélecteur d'images d'Android
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoPickerLauncher.launch(Intent.createChooser(intent, getString(R.string.prompt_select_image)));
        });

        Button btnTakePhoto = findViewById(R.id.btn_take_photo);

        btnTakePhoto.setOnClickListener(v -> {
            // 1. On crée le fichier vide
            File photoFile = FileManager.createImageFile(this);

            // 2. On mémorise son chemin pour plus tard
            currentPhotoPath = photoFile.getAbsolutePath();

            // 3. On génère un URI sécurisé avec le FileProvider
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            // 4. On lance l'appareil photo en lui donnant l'URI cible
            takePictureLauncher.launch(photoURI);
        });

        // Bouton supprimer
        btnDeletePhoto.setOnClickListener(v -> {
            if (adapter == null || adapter.getCount() == 0) {
                Toast.makeText(GalleryActivity.this, R.string.gallery_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!adapter.isSelectionMode()) {
                // On entre en mode sélection
                adapter.setSelectionMode(true);
                btnDeletePhoto.setText(R.string.gallery_confirm_deletion);
                Toast.makeText(GalleryActivity.this, R.string.prompt_select_images_to_delete, Toast.LENGTH_SHORT).show();
            } else {
                // On supprime les éléments sélectionnés
                Set<Integer> selectedPositions = adapter.getSelectedPositions();
                if (selectedPositions.isEmpty()) {
                    // Si rien n'est sélectionné, on quitte juste le mode
                    adapter.setSelectionMode(false);
                    btnDeletePhoto.setText(R.string.action_delete);
                } else {
                    // Demander confirmation avant de supprimer plusieurs
                    new AlertDialog.Builder(GalleryActivity.this)
                            .setTitle(R.string.dialog_deletion_title)
                            .setMessage("Voulez-vous supprimer les " + selectedPositions.size() + " image(s) sélectionnée(s) ?")
                            .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                                for (int pos : selectedPositions) {
                                    GalleryItem item = (GalleryItem) adapter.getItem(pos);
                                    if (item != null) {
                                        // 1. Suppression physique du fichier
                                        FileManager.deleteImageFile(item.imagePath);
                                        // 2. Suppression en base de données
                                        dbHelper.deleteGalleryItem(item.id);
                                    }
                                }
                                adapter.setSelectionMode(false);
                                btnDeletePhoto.setText(R.string.action_delete);
                                loadGallery();
                                Toast.makeText(GalleryActivity.this, R.string.gallery_images_deleted_toast, Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(R.string.dialog_no, null)
                            .show();
                }
            }
        });

        // Clic sur un item de la grille
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            GalleryItem selectedItem = (GalleryItem) parent.getItemAtPosition(position);
            if (selectedItem != null) {
                if (adapter != null && adapter.isSelectionMode()) {
                    adapter.toggleSelection(position);
                } else {
                    showFullImageDialog(selectedItem);
                }
            } else if (adapter == null || !adapter.isSelectionMode()) {
                Toast.makeText(GalleryActivity.this, R.string.prompt_add_image_slot, Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Méthode pour afficher un Popup demandant le titre du dessin
    private void askForTitleAndSave(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.drawing_artwork_title_prompt);

        // On crée un champ de texte (EditText) dans le code pour le mettre dans le popup
        final EditText input = new EditText(this);
        builder.setView(input);

        // Bouton Valider
        builder.setPositiveButton(R.string.action_save, (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                title = String.valueOf(R.string.drawing_untitled);
            }

            // 1. On copie physiquement l'image
            String savedPath = FileManager.copyImageToInternalStorage(GalleryActivity.this, imageUri);

            if (savedPath != null) {
                // 2. On sauvegarde en base de données
                dbHelper.addImageToGallery(title, savedPath);
                Toast.makeText(GalleryActivity.this, R.string.action_save, Toast.LENGTH_SHORT).show();
                // 3. On rafraîchit la grille pour voir la nouvelle image
                loadGallery();
            } else {
                Toast.makeText(GalleryActivity.this, R.string.error_image_copy, Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton Annuler
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void showFullImageDialog(GalleryItem item) {
        // On crée une boîte de dialogue personnalisée
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        View dialogRoot = dialog.findViewById(R.id.dialog_root);
        dialogRoot.setOnClickListener(v -> dialog.dismiss());

        ImageView fullImage = dialog.findViewById(R.id.full_image_view);
        TextView fullTitle = dialog.findViewById(R.id.full_title_text);
        Button btnShare = dialog.findViewById(R.id.btn_share_image);
        Button btnDelete = dialog.findViewById(R.id.btn_delete_image);

        // Affichage des données
        fullTitle.setText(item.title);
        File imgFile = new File(item.imagePath);
        if (imgFile.exists()) {
            fullImage.setImageURI(Uri.fromFile(imgFile));
        }

        // Logique du bouton Partager l'image
        btnShare.setOnClickListener(v -> shareImage(imgFile));

        btnDelete.setOnClickListener(v -> {
            // 1. Confirmation par l'utilisateur
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_delete_drawing_title)
                    .setMessage(R.string.dialog_irreversible_warning)
                    .setPositiveButton(R.string.action_delete, (d, which) -> {
                        // 2. Suppression physique du fichier
                        FileManager.deleteImageFile(item.imagePath);

                        // 3. Suppression en base de données
                        dbHelper.deleteGalleryItem(item.id);

                        // 4. Fermer le popup
                        dialog.dismiss();

                        // 5. IMPORTANT : Recharger la galerie pour mettre à jour la grille
                        loadGallery();

                        Toast.makeText(this, R.string.drawing_deleted_toast, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });

        dialog.show();
    }

    private void shareImage(File imageFile) {
        // On utilise le FileProvider pour obtenir un URI sécurisé
        Uri contentUri = FileProvider.getUriForFile(this,
                "com.dessinetonhumeur.app.fileprovider", imageFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

        // On donne la permission de lecture temporaire à l'app qui recevra l'image
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.sharing_tip)));
    }

    private void loadGallery() {
        List<GalleryItem> galleryItems = dbHelper.getAllGalleryItems();
        adapter = new GalleryAdapter(this, galleryItems);
        gridView.setAdapter(adapter);
        
        // S'assurer que le bouton revient à son état initial si on recharge
        if (btnDeletePhoto != null) {
            btnDeletePhoto.setText(R.string.action_delete);
        }
    }

    // Demande le titre spécifiquement pour une image prise avec l'appareil photo
    private void askForTitleForCameraPhoto(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.drawing_artwork_title_prompt);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_save, (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) title = getString(R.string.drawing_untitled);

            // L'image est DÉJÀ copiée, on l'insère directement dans la base de données !
            dbHelper.addImageToGallery(title, imagePath);
            Toast.makeText(this, R.string.action_save, Toast.LENGTH_SHORT).show();
            loadGallery();
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> {
            // Si on annule, on supprime la photo du téléphone pour ne pas gâcher d'espace
            FileManager.deleteImageFile(imagePath);
            dialog.cancel();
        });

        builder.show();
    }
}