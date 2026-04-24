package com.dessinetonhumeur.app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private List<GalleryItem> items;

    public GalleryAdapter(Context context, List<GalleryItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size(); // Le nombre d'images à afficher
    }

    @Override
    public Object getItem(int position) {
        return items.get(position); // Récupère l'élément à une position précise
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    // C'est ICI que la magie opère : on crée la vue pour CHAQUE case de la grille
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // On "gonfle" (transforme le XML en objet Java) notre item_image.xml
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        }

        // On récupère les éléments de l'interface
        ImageView imageView = convertView.findViewById(R.id.item_image_view);
        TextView titleText = convertView.findViewById(R.id.item_text_title);

        // On récupère l'objet correspondant à cette case
        GalleryItem currentItem = items.get(position);

        titleText.setText(currentItem.title);

        // On affiche l'image depuis notre stockage interne
        File imgFile = new File(currentItem.imagePath);
        if (imgFile.exists()) {
            imageView.setImageURI(Uri.fromFile(imgFile));
        }

        return convertView;
    }
}