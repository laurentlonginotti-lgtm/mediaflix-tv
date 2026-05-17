# Mediaflix TV — APK Android TV

## Compiler l'APK via GitHub Actions (sans Android Studio)

1. Créer un repo GitHub (ex: `mediaflix-tv`)
2. Uploader ce dossier `mediaflix-apk/` dedans
3. GitHub Actions compile automatiquement à chaque push
4. Télécharger l'APK : onglet **Actions** → dernier build → **Artifacts → Mediaflix-TV**

## Installer l'APK sur la TV Android

### Via clé USB
1. Copier `app-debug.apk` sur une clé USB
2. Brancher la clé sur la TV
3. Ouvrir le gestionnaire de fichiers de la TV
4. Naviguer jusqu'à la clé → taper sur l'APK → Installer

### Via ADB (si débogage USB activé sur la TV)
```
adb connect <ip-tv>:5555
adb install app-debug.apk
```

## Utilisation
1. Ouvrir l'app **Mediaflix** sur la TV
2. Entrer le code PIN à 4 chiffres avec la télécommande
3. Accès direct à VOD + Cinéma

## Modifier l'URL du serveur
Changer `SERVER_URL` dans :
- `app/src/main/java/com/mediaflix/tv/PinActivity.kt` ligne 17
- `app/src/main/java/com/mediaflix/tv/MainActivity.kt` ligne 12
