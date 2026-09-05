# Analyse du projet "Dessine ton humeur" & Recommandations d'Amélioration

Ce rapport présente une analyse complète du projet **Dessine ton humeur** (application Android native en Java avec XML Layouts et vues personnalisées) et propose des pistes d'amélioration concrètes réparties en quatre axes : **Responsive**, **Accessibilité**, **Style** et **Fonctionnalités (Features)**.

---

## 1. Responsive & Adaptabilité (Responsive Design)

### Constats actuels
- **WheelView à hauteur fixe :** Dans [activity_main.xml](file:///C:/Users/Thomas/Documents/GitHub/Dessine_ton_humeur/app/src/main/res/layout/activity_main.xml#L20-L24), la roue a une hauteur fixe de `439dp`, ce qui peut provoquer des dépassements (*overflow*) sur les petits écrans ou paraître disproportionné sur les tablettes et grands écrans pliables.
- **Boutons à largeurs fixes :** Certains boutons de la galerie (ex: `116dp` pour le bouton "depuis le téléphone") risquent de tronquer le texte selon la langue ou la taille de police système.
- **Orientation Paysage & Tablettes :** Pas de mise en page alternative (`layout-land` ou `sw600dp`) pour exploiter l'espace horizontal sur tablette (ex: afficher la roue à gauche et les idées/contrôles à droite).
- **Edge-to-Edge (API 36) :** L'application cible l'API 36, mais la gestion des insets système (barres de navigation et statut) n'est pas explicitement configurée, ce qui peut créer des superpositions avec les boutons ou la grille.

### Pistes d'amélioration
1. **Roue adaptative :** Remplacer la hauteur fixe de `WheelView` par `0dp` avec un `layout_weight` ou utiliser un `ConstraintLayout` avec un ratio carré (`app:layout_constraintDimensionRatio="1:1"`) pour que la roue s'adapte dynamiquement à la largeur disponible.
2. **Layouts Multi-écrans (`sw600dp`) :** Créer un layout horizontal pour les tablettes et pliables (Mode double panneau : Roue d'un côté, Générateur & Historique de l'autre).
3. **Gestion Edge-to-Edge :** Activer et gérer correctement les fenêtres Edge-to-Edge avec `WindowCompat.setDecorFitsSystemWindows` et appliquer les insets (`WindowInsetsCompat`) sur les conteneurs principaux.

---

## 2. Accessibilité (Accessibility)

### Constats actuels
- **WheelView non accessible (TalkBack) :** Étant une vue personnalisée dessinée sur un `Canvas` avec gestion tactile directe, `WheelView` ne possède pas de fournisseur de nœuds d'accessibilité (`AccessibilityNodeProvider`). Les utilisateurs malvoyants utilisant TalkBack ne peuvent ni savoir quelle humeur est sélectionnée ni faire tourner la roue.
- **Descriptions de contenu manquantes :** Les `ImageView` dans la grille de la galerie (`GalleryAdapter`) et les boutons d'actions manquent potentiellement de descriptions textuelles explicites (`contentDescription`).
- **Contrastes et Tailles de texte :** Bien que les textes utilisent des polices artistiques (`Cabin Sketch`), certains contrastes de couleurs (notamment sur les pastilles de couleur de la roue ou les boutons) nécessitent une vérification rigoureuse selon les normes WCAG AA.

### Pistes d'amélioration
1. **Accessibilité de la Roue (`WheelView`) :** Implémenter un `AccessibilityNodeProvider` ou surcharger les méthodes d'accessibilité (`onInitializeAccessibilityNodeInfo`, `performAccessibilityAction`) pour exposer chaque humeur comme un élément cliquable et annoncer l'humeur active lors du changement.
2. **Content Descriptions :** Ajouter des attributs `android:contentDescription` dynamiques sur les vignettes de la galerie et les boutons d'icônes.
3. **Aides textuelles :** Permettre de sélectionner l'humeur via une liste déroulante ou des boutons accessibles en alternative à la roue tactile pour les utilisateurs ne pouvant pas effectuer de mouvements rotatifs.

---

## 3. Style & Design System (Style)

### Constats actuels
- **Cohérence des Composants :** Mélange de composants Material 2 et Material 3 (`Widget.MaterialComponents.Button...` au lieu de `com.google.android.material.button.MaterialButton` avec les thèmes M3 unifiés).
- **Typographie :** L'utilisation exclusive de la police `Cabin Sketch` donne un cachet artistique et ludique fort, mais peut nuire à la lisibilité sur les longs textes (comme les descriptions d'idées ou les dialogues).
- **Mode Sombre (`Values-night`) :** Le fichier de thèmes de nuit existe mais mérite d'être enrichi pour garantir une harmonie visuelle complète (couleurs des cartes, fonds de dialogues, contrastes des textes).

### Pistes d'amélioration
1. **Migration Material 3 Complète :** Utiliser les thèmes et composants Material 3 (`com.google.android.material.card.MaterialCardView`, boutons M3, formes dynamiques `shapeAppearance`).
2. **Hiérarchie Typographique :** Conserver `Cabin Sketch` pour les titres et en-têtes (ambiance artistique/carnet de croquis), et basculer sur une police sans-serif moderne (ex: Roboto ou Inter) pour le corps de texte et les boutons afin d'améliorer le confort de lecture.
3. **Animations et Feedback Visuel :**
   - Ajouter une animation d'inertie (effet de décélération / *fling*) lorsque l'utilisateur lance la roue, au lieu d'un arrêt brusque.
   - Ajouter un retour haptique léger (`HapticFeedbackConstants`) à chaque passage de segment sur la roue.

---

## 4. Fonctionnalités & Nouvelles Idées (Features)

### Idées d'enrichissement de l'application
1. **Mini-Studio de Dessin Intégré (Canvas de Dessin) :**
   - Plutôt que d'importer uniquement des photos externes ou de prendre des photos, intégrer un écran de dessin minimaliste directement dans l'application (choix de pinceaux, couleurs, gomme) pour dessiner l'idée générée sur le moment !
2. **Journal d'Humeur & Statistiques (Mood Tracker Dashboard) :**
   - Suivre l'évolution des humeurs au fil du temps (graphiques hebdomadaires/mensuels : *"Cette semaine, vous avez été 4 fois 'Créatif' et 2 fois 'Joyeux'"*).
   - Associer chaque dessin de la galerie à l'humeur précise qui l'a inspiré.
3. **Rappels Quotidiens & Notifications :**
   - Proposer une notification quotidienne personnalisable (*"Comment te sens-tu aujourd'hui ? Viens tourner la roue !"*) pour stimuler l'engagement.
4. **Favoris & Partage Avancé :**
   - Permettre de marquer certaines idées de dessin en "Favoris" pour les retrouver rapidement.
   - Exporter le journal d'humeur ou la galerie sous forme de carnet/PDF récapitulatif.
5. **Mode "Défi / Challenge du mois" :**
   - Proposer des thèmes thématiques saisonniers (Halloween, Printemps, Été) ou des défis de 7 jours de dessin consécutifs.

---

> [!TIP]
> **Prochaine étape suggérée :** Si vous souhaitez approfondir l'un de ces axes (par exemple, rendre la roue accessible avec TalkBack, ajouter l'inertie de rotation, ou concevoir l'écran de dessin intégré), nous pouvons créer un plan d'implémentation détaillé.
