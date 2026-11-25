# EV Control (Android)

WebView qui ouvre https://control.effettoventuri.eu/ et se connecte automatiquement.

## Définir / changer l'utilisateur et le mot de passe
- Au premier lancement (ou si la connexion échoue), un dialogue s'ouvre automatiquement.
- À tout moment : appuyer sur le bouton **⚙** (semi-transparent, en bas à droite), saisir user et mot de passe,
  **Enregistrer** (la session est réinitialisée et l'app se reconnecte). **Oublier** efface les identifiants.
- Les identifiants sont stockés chiffrés sur le téléphone (EncryptedSharedPreferences), jamais dans le dépôt.

## Obtenir l'APK (Github Actions, sans Android Studio)
1. Onglet **Actions** > workflow **Build APK** (se lance au push, ou **Run workflow**) > dernier run > artifact **EVControl-debug-apk**.
2. Copier `app-debug.apk` sur le téléphone et l'installer (autoriser les sources inconnues).
