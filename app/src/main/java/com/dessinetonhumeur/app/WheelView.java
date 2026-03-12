package com.dessinetonhumeur.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class WheelView extends View {

    // Liste de nos humeurs
    private final String[] moods = {
            "Inspiré par la Nature", "Joyeux", "Nostalgique", "Triste",
            "Énergique", "Mystérieux", "Futuriste", "Calme",
            "Colère", "Romantique", "Créatif"
    };

    // Objets Paint utilisés pour définir comment dessiner (couleur, taille, style)
    private Paint segmentPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint trianglePaint;

    private float angle = 0f; // L'angle actuel de rotation de la roue
    private RectF rectF; // La zone rectangulaire dans laquelle la roue sera dessinée

    // Constructeur appelé quand la vue est créée depuis un fichier XML
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Initialisation des outils de dessin (il ne faut jamais instancier des objets dans onDraw pour des raisons de performances)
    private void init() {
        // ANTI_ALIAS_FLAG permet d'avoir des bords lisses (non pixelisés)
        segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        segmentPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(35f);
        textPaint.setTextAlign(Paint.Align.CENTER); // Centre le texte sur ses coordonnées X, Y

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE); // STROKE dessine uniquement les contours
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(10f);

        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setColor(Color.BLACK);
        trianglePaint.setStyle(Paint.Style.FILL);

        rectF = new RectF();
    }

    // Méthode principale où tout le dessin s'effectue
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        // Le rayon est la moitié de la plus petite dimension (largeur ou hauteur)
        // On soustrait 50f pour laisser de l'espace à la flèche indicatrice en haut.
        float radius = Math.min(width, height) / 2f - 50f;
        float centerX = width / 2f;
        float centerY = height / 2f;

        // Définition de la boîte (carré) qui contiendra notre cercle
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Angle de chaque part de "pizza" (360 degrés divisés par le nombre d'humeurs)
        float segmentAngle = 360f / moods.length;

        // Boucle pour dessiner chaque portion de la roue
        for (int i = 0; i < moods.length; i++) {
            segmentPaint.setColor(getColorForMood(i)); // Change la couleur du pinceau

            // Dessine une part de pizza.
            // - startAngle : l'angle de base de la roue + le décalage de ce segment
            // — sweepAngle : la taille du segment
            // — useCenter (true) : relie les bords au centre pour faire une part fermée
            canvas.drawArc(rectF, angle + (i * segmentAngle), segmentAngle, true, segmentPaint);

            // Calcule l'angle au centre exact de ce segment pour y placer le texte
            float textAngle = angle + (i * segmentAngle) + (segmentAngle / 2f);
            drawText(canvas, moods[i], centerX, centerY, radius, textAngle);
        }

        // Dessin du contour noir autour de la roue pour la finition
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Dessin de la flèche de sélection (fixe, tout en haut)
        drawPointerTriangle(canvas, centerX, centerY - radius);
    }

    // Méthode pour dessiner le texte afin qu'il suive la rotation du segment
    private void drawText(Canvas canvas, String text, float centerX, float centerY, float radius, float textAngle) {
        canvas.save(); // Sauvegarde l'état actuel du canvas pour ne pas affecter les autres dessins

        // Mathématiques pour trouver le point (x, y) où placer le texte (à 65% du rayon en partant du centre)
        double radians = Math.toRadians(textAngle);
        float x = (float) (centerX + (radius * 0.65) * Math.cos(radians));
        float y = (float) (centerY + (radius * 0.65) * Math.sin(radians));

        // Tourner le canvas autour du point (x,y) pour que le texte pointe vers l'extérieur
        canvas.rotate(textAngle + 90, x, y);
        canvas.drawText(text, x, y, textPaint);

        canvas.restore(); // Restaure le canvas à son état droit
    }

    // Fournit une couleur spécifique pour chaque humeur
    private int getColorForMood(int index) {
        // Ces couleurs correspondent une à une au tableau 'moods'
        String[] hexColors = {
                "#4CAF50", // Inspiré par la Nature (Vert)
                "#FFEB3B", // Joyeux (Jaune)
                "#9E9E9E", // Nostalgique (Gris)
                "#2196F3", // Triste (Bleu)
                "#FF9800", // Énergique (Orange)
                "#673AB7", // Mystérieux (Violet)
                "#00BCD4", // Futuriste (Cyan)
                "#B2DFDB", // Calme (Vert d'eau)
                "#F44336", // Colère (Rouge)
                "#E91E63", // Romantique (Rose)
                "#FFC107"  // Créatif (Ambre)
        };
        return Color.parseColor(hexColors[index]);
    }

    // Dessine le petit triangle noir en haut qui sert de curseur de sélection
    private void drawPointerTriangle(Canvas canvas, float topX, float topY) {
        Path path = new Path();
        float triangleSize = 40f;

        path.moveTo(topX, topY + 20f); // Pointe basse du triangle (qui touche presque la roue)
        path.lineTo(topX - triangleSize, topY - triangleSize); // Coin haut gauche
        path.lineTo(topX + triangleSize, topY - triangleSize); // Coin haut droit
        path.close(); // Relie le dernier point au premier

        canvas.drawPath(path, trianglePaint);
    }

    // Gère les interactions tactiles de l'utilisateur (quand il touche et bouge le doigt)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Coordonnées du toucher relatives au centre de la roue
        float x = event.getX() - getWidth() / 2f;
        float y = event.getY() - getHeight() / 2f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Math.atan2 calcule l'angle entre le centre (0,0) et le doigt (x, y)
                // On le convertit en degrés. La roue suivra donc le doigt de l'utilisateur.
                angle = (float) Math.toDegrees(Math.atan2(y, x));
                invalidate(); // Dit à Android de rappeler la méthode onDraw() pour redessiner la vue avec le nouvel angle
                return true;
        }
        return super.onTouchEvent(event);
    }

    // Méthode publique que nous appellerons pour savoir sur quelle humeur la flèche pointe
    public String getSelectedMood() {
        float segmentAngle = 360f / moods.length;
        // La flèche est située tout en haut, ce qui correspond mathématiquement à l'angle -90° (ou 270°)
        // On calcule le décalage pour savoir quel segment se trouve sous cet angle de -90°.
        float adjustedAngle = (360 - angle - 90) % 360;
        if (adjustedAngle < 0) adjustedAngle += 360; // Garder l'angle positif

        int index = (int) (adjustedAngle / segmentAngle);
        return moods[index];
    }


}