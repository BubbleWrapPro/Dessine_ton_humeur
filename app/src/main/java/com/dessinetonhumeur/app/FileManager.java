package com.dessinetonhumeur.app;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FileManager {

    // Cette méthode prend l'Uri de l'image choisie par l'utilisateur et la copie chez nous
    public static String copyImageToInternalStorage(Context context, Uri imageUri) {
        try {
            // 1. Ouvrir le fichier original fourni par Android
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            // 2. Créer un nom de fichier unique basé sur la date et l'heure
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "MOOD_DRAWING_" + timeStamp + ".jpg";

            // 3. Préparer le fichier de destination dans le dossier privé de l'app (getFilesDir())
            File destinationFile = new File(context.getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(destinationFile);

            // 4. Copier les données octet par octet (buffer)
            byte[] buffer = new byte[1024];
            int length;
            while ((length = Objects.requireNonNull(inputStream).read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // 5. Fermer les flux pour libérer la mémoire
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // On retourne le chemin absolu du nouveau fichier pour le sauvegarder dans la base de données
            return destinationFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null; // En cas d'erreur
        }
    }
}