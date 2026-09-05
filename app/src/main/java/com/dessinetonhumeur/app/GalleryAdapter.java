package com.dessinetonhumeur.app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private List<GalleryItem> items;
    private boolean selectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public GalleryAdapter(Context context, List<GalleryItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void toggleSelection(int position) {
        if (position >= items.size()) {
            return;
        }
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    @Override
    public int getCount() {
        return Math.max(items.size(), 6); // Le nombre d'images à afficher (6 minimum)
    }

    @Override
    public Object getItem(int position) {
        if (position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < items.size()) {
            return items.get(position).id;
        }
        return position;
    }

    // C'est ICI que la magie opère : on crée la vue pour CHAQUE case de la grille
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // On "gonfle" (transforme le XML en objet Java) notre item_image.xml
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        }

        if (position < items.size()) {

            // On récupère les éléments de l'interface
            ImageView imageView = convertView.findViewById(R.id.item_image_view);
            TextView titleText = convertView.findViewById(R.id.item_text_title);
            CheckBox checkBox = convertView.findViewById(R.id.item_checkbox);

            // On récupère l'objet correspondant à cette case
            GalleryItem currentItem = items.get(position);

            titleText.setText(currentItem.title);

            // On affiche l'image depuis notre stockage interne
            File imgFile = new File(currentItem.imagePath);
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
            } else {
                // Si le fichier image est introuvable, on réinitialise l'ImageView
                // pour afficher le fond gris de substitution (placeholder) défini en XML.
                imageView.setImageURI(null);
            }

            // Afficher/Cacher la CheckBox selon le mode
            if (selectionMode) {
                checkBox.setVisibility(View.VISIBLE);
                boolean isSelected = selectedPositions.contains(position);
                checkBox.setChecked(isSelected);

                // Petit retour visuel : on assombrit un peu si sélectionné
                if (isSelected) {
                    convertView.setAlpha(0.5f);
                } else {
                    convertView.setAlpha(1.0f);
                }
            } else {
                checkBox.setVisibility(View.GONE);
                convertView.setAlpha(1.0f);
            }

            return convertView;
        }
        else{
            // On récupère les éléments de l'interface et on met des valeurs de substitution
            ImageView imageView = convertView.findViewById(R.id.item_image_view);
            imageView.setImageURI(null);

            TextView titleText = convertView.findViewById(R.id.item_text_title);
            String emptyTitle = "Dessin n°" + (position + 1);
            titleText.setText(emptyTitle);

            CheckBox checkBox = convertView.findViewById(R.id.item_checkbox);
            // On disable la checkbox car elle n'est pas utilisée dans ce contexte
            checkBox.setVisibility(View.GONE);
            convertView.setAlpha(1.0f);

            return convertView;
        }
    }
}