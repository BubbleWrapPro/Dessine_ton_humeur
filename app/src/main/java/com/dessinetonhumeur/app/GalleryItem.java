package com.dessinetonhumeur.app;

public class GalleryItem {
    public int id;
    public String title;
    public String imagePath; // Le chemin physique de l'image sur le téléphone

    public GalleryItem(int id, String title, String imagePath) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
    }
}