# Dessine ton humeur 🎨

**Dessine ton humeur** est une application Android conçue pour stimuler la créativité artistique en suggérant des idées de dessin adaptées à l'humeur du moment de l'utilisateur.

---

## Fonctionnalités principales

- **Roue des humeurs interactive** (`WheelView`) : Une roue visuelle permettant de sélectionner son humeur actuelle parmi de nombreuses émotions (Rêveur, Joyeux, Nostalgique, Triste, Énergique, Mystérieux, Futuriste, Calme, Colère, Romantique, Créatif).
- **Génération d'idées dynamiques** (Base de données SQLite) : 
  - Plus de 220 idées de dessin structurées selon un format précis (`verbe + sujet + style`).
  - Diversité de difficulté (idées simples pour débuter ou techniques plus complexes).
- **Système anti-répétition intelligent** : 
  - Chaque idée générée est bloquée pendant *un mois** pour éviter les doublons trop fréquents.
- **Système anti-spam** : 
  - Un cooldown d'**une heure** entre chaque génération d'idée pour encourager l'inspiration et la réflexion.
- **Galerie artistique** (`GalleryActivity`) : 
  - Un espace pour enregistrer, consulter et gérer ses propres réalisations artistiques.
- **Partage social** : 
  - Possibilité de partager facilement ses défis et idées de dessin avec ses proches via les applications installées sur l'appareil.
- **Multi-langues** :
  - L'application est disponible en français et en anglais. Toutes les chaines de caractères ont leur traduction correspondante.

---

## 🛠Stack technique

- **Langage** : Java (Android SDK)
- **Base de données** : SQLite (`SQLiteOpenHelper`)
- **Stockage local** : `SharedPreferences` (gestion des cooldowns)
- **UI / Vues** : Composants Android natifs, Vues personnalisées (`WheelView`), Material Design.

---

## Installation et Développement

1. Clonez ce dépôt sur votre machine :
   ```bash
   git clone https://github.com/votre-nom/Dessine_ton_humeur.git
   ```
2. Ouvrez le projet dans **Android Studio**.
3. Synchronisez le projet avec Gradle (`Sync Project with Gradle Files`).
4. Lancez l'application sur un émulateur ou un appareil Android physique (Android Studio / ADB).
5. Contribuez ;)

---

## Licence

Ce projet est sous licence. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
