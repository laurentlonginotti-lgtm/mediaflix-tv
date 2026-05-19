# Configuration des secrets GitHub pour les builds release

Pour que le workflow `Build Mediaflix APK` produise un APK release **signé**, il faut
ajouter 4 secrets dans le repository GitHub.

## 1. Aller dans la page Secrets

https://github.com/laurentlonginotti-lgtm/mediaflix-tv/settings/secrets/actions

Clique sur **New repository secret** pour chaque secret ci-dessous.

## 2. Les 4 secrets à créer

| Nom du secret | Valeur |
|---|---|
| `KEYSTORE_BASE64` | Contenu du fichier `release-keystore/mediaflix-release.jks.base64` (tout copier-coller, une seule ligne) |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore (voir `PASSWORDS.txt`) |
| `KEY_ALIAS` | `mediaflix` |
| `KEY_PASSWORD` | **Même valeur que `KEYSTORE_PASSWORD`** (PKCS12 partage les deux) |

## 3. Vérifier

- Pousse un commit (ou relance le workflow via **Actions → Run workflow**)
- Dans Actions, le job doit afficher deux étapes : *Build Debug APK* ET *Build Release APK*
- À la fin, deux artifacts sont disponibles : `Mediaflix-TV-debug` et `Mediaflix-TV-release`

## 4. Vérifier la signature de l'APK release

```bash
# Sur Linux/Mac
unzip -p app-release.apk META-INF/MEDIAFLI.RSA | keytool -printcert
# ou simplement
apksigner verify --verbose app-release.apk
```

L'output doit indiquer : `CN=Mediaflix TV, OU=Dev, O=Mediaflix, ...` et "Signature verifies".

## Sécurité

- Le fichier `mediaflix-release.jks` ne doit **JAMAIS** être commité dans le repo (`.gitignore` le protège).
- Le `PASSWORDS.txt` non plus. Stocke-le dans un gestionnaire (KeePass / Bitwarden / 1Password).
- Si tu perds le keystore, tu ne peux plus publier de mises à jour de cette app
  (Google Play et Android refusent toute update non signée par la même clé).
- Si le keystore fuite, révoque-le et reconstruis-en un nouveau (les utilisateurs devront
  désinstaller/réinstaller l'app).
