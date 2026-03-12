package com.dessinetonhumeur.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Informations de base de la base de données
    private static final String DATABASE_NAME = "DrawYourMood.db";
    private static final int DATABASE_VERSION = 1;

    // --- TABLE DES IDÉES ---
    private static final String TABLE_IDEAS = "ideas";
    private static final String COLUMN_IDEA_ID = "id";
    private static final String COLUMN_IDEA_MOOD = "mood";
    private static final String COLUMN_IDEA_TEXT = "text";
    private static final String COLUMN_IDEA_LAST_USED = "last_used"; // Stockera le temps en millisecondes

    // --- TABLE DE LA GALERIE ---
    private static final String TABLE_GALLERY = "gallery";
    private static final String COLUMN_GALLERY_ID = "id";
    private static final String COLUMN_GALLERY_TITLE = "title";
    private static final String COLUMN_GALLERY_IMAGE_PATH = "image_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Appelée la TOUTE PREMIÈRE FOIS que l'application a besoin de la base de données
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table des idées
        String createIdeasTable = "CREATE TABLE " + TABLE_IDEAS + " (" +
                COLUMN_IDEA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_IDEA_MOOD + " TEXT, " +
                COLUMN_IDEA_TEXT + " TEXT, " +
                COLUMN_IDEA_LAST_USED + " INTEGER DEFAULT 0)";
        db.execSQL(createIdeasTable);

        // Création de la table de la galerie
        String createGalleryTable = "CREATE TABLE " + TABLE_GALLERY + " (" +
                COLUMN_GALLERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GALLERY_TITLE + " TEXT, " +
                COLUMN_GALLERY_IMAGE_PATH + " TEXT)";
        db.execSQL(createGalleryTable);

        // On insère nos idées par défaut directement à la création
        insertInitialIdeas(db);
    }

    // Appelée si on change le DATABASE_VERSION (ex : mise à jour de l'app sur le Play Store avec une nouvelle table)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IDEAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GALLERY);
        onCreate(db);
    }

    // --- MÉTHODES POUR LES IDÉES ---

    // Récupère une idée au hasard pour une humeur donnée, en ignorant celles utilisées il y a moins d'un mois
    public Idea getRandomIdea(String mood) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Calcul du timestamp d'il y a un mois (30 jours en millisecondes)
        long oneMonthAgoMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        // Requête SQL : "Sélectionne tout de 'ideas' OÙ mood = ? ET last_used < (il y a un mois) ORDER BY RANDOM() LIMIT 1"
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_IDEAS + " WHERE " + COLUMN_IDEA_MOOD + " = ? AND " + COLUMN_IDEA_LAST_USED + " < ? ORDER BY RANDOM() LIMIT 1",
                new String[]{mood, String.valueOf(oneMonthAgoMillis)}
        );

        Idea idea = null;
        if (cursor.moveToFirst()) { // Si on a trouvé un résultat
            int idIndex = cursor.getColumnIndex(COLUMN_IDEA_ID);
            int textIndex = cursor.getColumnIndex(COLUMN_IDEA_TEXT);
            idea = new Idea(cursor.getInt(idIndex), cursor.getString(textIndex));
        }
        cursor.close();
        return idea;
    }

    // Met à jour la date d'utilisation d'une idée pour déclencher le blocage d'un mois
    public void markIdeaAsUsed(int ideaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDEA_LAST_USED, System.currentTimeMillis()); // Heure actuelle

        db.update(TABLE_IDEAS, values, COLUMN_IDEA_ID + " = ?", new String[]{String.valueOf(ideaId)});
    }

    // Insertion des idées par défaut (codées en dur comme dans l'ancien projet)
    private void insertInitialIdeas(SQLiteDatabase db) {
        // --- INSPIRÉ PAR LA NATURE ---
        addIdeaToDb(db, "Inspiré par la Nature", "Dessiner une forêt dense à l'aquarelle");
        addIdeaToDb(db, "Inspiré par la Nature", "Peindre une montagne enneigée dans un style réaliste");
        addIdeaToDb(db, "Inspiré par la Nature", "Esquisser une rivière sauvage au fusain");
        addIdeaToDb(db, "Inspiré par la Nature", "Illustrer un vieux chêne dans un style manga");
        addIdeaToDb(db, "Inspiré par la Nature", "Croquer un oiseau en plein vol au stylo bille");
        addIdeaToDb(db, "Inspiré par la Nature", "Dessiner une clairière lumineuse avec des couleurs pastel");
        addIdeaToDb(db, "Inspiré par la Nature", "Peindre un loup hurlant à la lune en peinture acrylique");
        addIdeaToDb(db, "Inspiré par la Nature", "Esquisser une fleur sauvage en line-art");
        addIdeaToDb(db, "Inspiré par la Nature", "Illustrer un coucher de soleil sur la mer en pixel art");
        addIdeaToDb(db, "Inspiré par la Nature", "Croquer un champ de blé au crayon de papier");

        // --- JOYEUX ---
        addIdeaToDb(db, "Joyeux", "Dessiner un enfant qui rit dans un style cartoon");
        addIdeaToDb(db, "Joyeux", "Peindre un chien qui court après une balle à l'aquarelle");
        addIdeaToDb(db, "Joyeux", "Esquisser une fête d'anniversaire au fusain");
        addIdeaToDb(db, "Joyeux", "Illustrer un énorme gâteau coloré dans un style manga");
        addIdeaToDb(db, "Joyeux", "Croquer un soleil avec des lunettes au stylo bille");
        addIdeaToDb(db, "Joyeux", "Dessiner un groupe d'amis dans un parc avec des couleurs pastel");
        addIdeaToDb(db, "Joyeux", "Peindre une scène de danse joyeuse dans un style abstrait");
        addIdeaToDb(db, "Joyeux", "Esquisser un lâcher de ballons en line-art");
        addIdeaToDb(db, "Joyeux", "Illustrer une journée à la plage en pixel art");
        addIdeaToDb(db, "Joyeux", "Croquer une pile de cadeaux au crayon de papier");

        // --- NOSTALGIQUE ---
        addIdeaToDb(db, "Nostalgique", "Dessiner un vieux vélo rouillé à l'aquarelle");
        addIdeaToDb(db, "Nostalgique", "Peindre la façade de ton école d'enfance dans un style réaliste");
        addIdeaToDb(db, "Nostalgique", "Esquisser un appareil photo argentique au fusain");
        addIdeaToDb(db, "Nostalgique", "Illustrer une montre à gousset dans un style manga");
        addIdeaToDb(db, "Nostalgique", "Croquer un vieux carnet de souvenirs au stylo bille");
        addIdeaToDb(db, "Nostalgique", "Dessiner une balançoire vide avec des couleurs pastel");
        addIdeaToDb(db, "Nostalgique", "Peindre un gramophone ancien à la peinture à l'huile");
        addIdeaToDb(db, "Nostalgique", "Esquisser une lettre manuscrite ouverte en line-art");
        addIdeaToDb(db, "Nostalgique", "Illustrer un jouet en bois ancien en pixel art");
        addIdeaToDb(db, "Nostalgique", "Croquer un banc public sous un réverbère au crayon de papier");

        // --- TRISTE ---
        addIdeaToDb(db, "Triste", "Dessiner un parapluie abandonné à l'aquarelle");
        addIdeaToDb(db, "Triste", "Peindre un visage en larmes dans un style réaliste");
        addIdeaToDb(db, "Triste", "Esquisser une fenêtre battue par la pluie au fusain");
        addIdeaToDb(db, "Triste", "Illustrer une fleur fanée dans un style manga");
        addIdeaToDb(db, "Triste", "Croquer un quai de gare vide au stylo bille");
        addIdeaToDb(db, "Triste", "Dessiner un regard perdu dans le vide avec des couleurs pastel");
        addIdeaToDb(db, "Triste", "Peindre un oiseau blessé dans un style sombre");
        addIdeaToDb(db, "Triste", "Esquisser un bâtiment en ruines en line-art");
        addIdeaToDb(db, "Triste", "Illustrer un paysage gris et brumeux en pixel art");
        addIdeaToDb(db, "Triste", "Croquer une silhouette solitaire au crayon de papier");

        // --- ÉNERGIQUE ---
        addIdeaToDb(db, "Énergique", "Dessiner un coureur en plein sprint à l'aquarelle");
        addIdeaToDb(db, "Énergique", "Peindre un guépard bondissant dans un style réaliste");
        addIdeaToDb(db, "Énergique", "Esquisser une explosion de roches au fusain");
        addIdeaToDb(db, "Énergique", "Illustrer un éclair déchirant le ciel dans un style manga");
        addIdeaToDb(db, "Énergique", "Croquer un danseur de hip-hop au stylo bille");
        addIdeaToDb(db, "Énergique", "Dessiner une voiture de course à pleine vitesse avec des couleurs pastel");
        addIdeaToDb(db, "Énergique", "Peindre un boxeur sur le ring en peinture acrylique");
        addIdeaToDb(db, "Énergique", "Esquisser une énorme vague déferlante en line-art");
        addIdeaToDb(db, "Énergique", "Illustrer un feu de joie crépitant en pixel art");
        addIdeaToDb(db, "Énergique", "Croquer un saut en parachute au crayon de papier");

        // --- MYSTÉRIEUX ---
        addIdeaToDb(db, "Mystérieux", "Dessiner une porte entrebâillée brillante à l'aquarelle");
        addIdeaToDb(db, "Mystérieux", "Peindre un chat noir aux yeux jaunes dans un style réaliste");
        addIdeaToDb(db, "Mystérieux", "Esquisser une forêt couverte de brouillard au fusain");
        addIdeaToDb(db, "Mystérieux", "Illustrer un vieux grimoire magique dans un style manga");
        addIdeaToDb(db, "Mystérieux", "Croquer une lanterne allumée dans la nuit au stylo bille");
        addIdeaToDb(db, "Mystérieux", "Dessiner une silhouette encapuchonnée avec des couleurs pastel");
        addIdeaToDb(db, "Mystérieux", "Peindre la pleine lune à travers des branches en peinture acrylique");
        addIdeaToDb(db, "Mystérieux", "Esquisser un hibou perché en line-art");
        addIdeaToDb(db, "Mystérieux", "Illustrer un labyrinthe de haies en pixel art");
        addIdeaToDb(db, "Mystérieux", "Croquer une clé ornée ancienne au crayon de papier");

        // --- FUTURISTE ---
        addIdeaToDb(db, "Futuriste", "Dessiner un robot humanoïde à l'aquarelle");
        addIdeaToDb(db, "Futuriste", "Peindre une ville aux voitures volantes dans un style réaliste");
        addIdeaToDb(db, "Futuriste", "Esquisser un vaisseau spatial en orbite au fusain");
        addIdeaToDb(db, "Futuriste", "Illustrer un cyborg avec un bras mécanique dans un style manga");
        addIdeaToDb(db, "Futuriste", "Croquer un astronaute sur une planète inconnue au stylo bille");
        addIdeaToDb(db, "Futuriste", "Dessiner une ruelle éclairée aux néons avec des couleurs pastel");
        addIdeaToDb(db, "Futuriste", "Peindre un drone de livraison dans un style industriel");
        addIdeaToDb(db, "Futuriste", "Esquisser un portail de téléportation en line-art");
        addIdeaToDb(db, "Futuriste", "Illustrer une ville sous dôme de verre en pixel art");
        addIdeaToDb(db, "Futuriste", "Croquer une interface holographique au crayon de papier");

        // --- CALME ---
        addIdeaToDb(db, "Calme", "Dessiner une tasse de thé fumante à l'aquarelle");
        addIdeaToDb(db, "Calme", "Peindre un chat endormi sur un coussin dans un style réaliste");
        addIdeaToDb(db, "Calme", "Esquisser un lac paisible sans vagues au fusain");
        addIdeaToDb(db, "Calme", "Illustrer un hamac accroché entre deux palmiers dans un style manga");
        addIdeaToDb(db, "Calme", "Croquer un livre ouvert sur un bureau au stylo bille");
        addIdeaToDb(db, "Calme", "Dessiner une bougie allumée avec des couleurs pastel");
        addIdeaToDb(db, "Calme", "Peindre un petit bonsaï à la peinture à l'huile");
        addIdeaToDb(db, "Calme", "Esquisser un nuage cotonneux en line-art");
        addIdeaToDb(db, "Calme", "Illustrer une personne en méditation en pixel art");
        addIdeaToDb(db, "Calme", "Croquer une pile de galets zen au crayon de papier");

        // --- COLÈRE ---
        addIdeaToDb(db, "Colère", "Dessiner un poing serré frappant une table à l'aquarelle");
        addIdeaToDb(db, "Colère", "Peindre un volcan en éruption dans un style réaliste");
        addIdeaToDb(db, "Colère", "Esquisser une tempête de sable au fusain");
        addIdeaToDb(db, "Colère", "Illustrer un taureau prêt à charger dans un style manga");
        addIdeaToDb(db, "Colère", "Croquer un monstre rugissant au stylo bille");
        addIdeaToDb(db, "Colère", "Dessiner un miroir brisé avec des couleurs pastel");
        addIdeaToDb(db, "Colère", "Peindre de grandes flammes dévorantes en peinture acrylique");
        addIdeaToDb(db, "Colère", "Esquisser un visage hurlant en line-art");
        addIdeaToDb(db, "Colère", "Illustrer un ciel rouge et orageux en pixel art");
        addIdeaToDb(db, "Colère", "Croquer un loup montrant les crocs au crayon de papier");

        // --- ROMANTIQUE ---
        addIdeaToDb(db, "Romantique", "Dessiner un couple sous un parapluie à l'aquarelle");
        addIdeaToDb(db, "Romantique", "Peindre une rose rouge parfaite dans un style réaliste");
        addIdeaToDb(db, "Romantique", "Esquisser un cadenas accroché à un pont au fusain");
        addIdeaToDb(db, "Romantique", "Illustrer deux cygnes formant un cœur dans un style manga");
        addIdeaToDb(db, "Romantique", "Croquer une lettre d'amour avec un sceau au stylo bille");
        addIdeaToDb(db, "Romantique", "Dessiner un baiser sur le front avec des couleurs pastel");
        addIdeaToDb(db, "Romantique", "Peindre une bague de fiançailles brillante à la peinture à l'huile");
        addIdeaToDb(db, "Romantique", "Esquisser un dîner aux chandelles en line-art");
        addIdeaToDb(db, "Romantique", "Illustrer une danse enlacée en pixel art");
        addIdeaToDb(db, "Romantique", "Croquer deux mains qui se tiennent au crayon de papier");

        // --- CRÉATIF ---
        addIdeaToDb(db, "Créatif", "Dessiner une chimère mi-oiseau mi-poisson à l'aquarelle");
        addIdeaToDb(db, "Créatif", "Peindre une machine volante à vapeur dans un style réaliste");
        addIdeaToDb(db, "Créatif", "Esquisser un paysage poussant à l'envers au fusain");
        addIdeaToDb(db, "Créatif", "Illustrer une ville construite sur des champignons géants dans un style manga");
        addIdeaToDb(db, "Créatif", "Croquer une horloge fondante au stylo bille");
        addIdeaToDb(db, "Créatif", "Dessiner un arbre dont les feuilles sont des étoiles avec des couleurs pastel");
        addIdeaToDb(db, "Créatif", "Peindre un grand œil observant l'univers en peinture acrylique");
        addIdeaToDb(db, "Créatif", "Esquisser une nouvelle planète inventée en line-art");
        addIdeaToDb(db, "Créatif", "Illustrer un escalier qui tourne à l'infini en pixel art");
        addIdeaToDb(db, "Créatif", "Croquer un visage composé de formes géométriques au crayon de papier");
    }

    private void addIdeaToDb(SQLiteDatabase db, String mood, String text) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDEA_MOOD, mood);
        values.put(COLUMN_IDEA_TEXT, text);
        values.put(COLUMN_IDEA_LAST_USED, 0); // 0 = jamais utilisé
        db.insert(TABLE_IDEAS, null, values);
    }
}
