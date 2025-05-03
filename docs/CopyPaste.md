Votre appli utilise les autorisations non déclarées liées aux photos et vidéos suivantes

android.permission.READ_MEDIA_IMAGES

android.permission.READ_MEDIA_VIDEO

Si votre appli nécessite un accès exceptionnel ou ponctuel aux photos et vidéos, passez au sélecteur de photos d'Android ou à celui de votre choix.

Si votre appli doit accéder fréquemment aux photos et vidéos, indiquez-nous pourquoi vous pensez que vous remplissez les conditions requises pour utiliser ces autorisations.

## Réponse courte pour Google (max 250 caractères)

"Ces autorisations sont nécessaires pour la fonctionnalité de sauvegarde/restauration de l'app qui stocke les données dans Documents/AATT. Sur Android 13+, READ_MEDIA_* remplace READ/WRITE_EXTERNAL_STORAGE pour accéder au stockage externe."

## Réponse à Google Play concernant les autorisations médias

"L'application AATT nécessite ces autorisations pour sa fonctionnalité de sauvegarde/restauration des données. L'application crée et gère des fichiers de sauvegarde au format JSON dans le dossier Documents/AATT sur le stockage externe de l'appareil.

Sur Android 13 et versions ultérieures (API 33+), les autorisations READ_MEDIA_* remplacent les anciennes autorisations READ/WRITE_EXTERNAL_STORAGE pour accéder au stockage externe partagé. Ces autorisations sont essentielles pour permettre aux utilisateurs de sauvegarder leurs données d'activité et de les restaurer en cas de besoin, notamment lors d'un changement d'appareil ou après une réinitialisation.

La fonctionnalité de sauvegarde/restauration est une fonctionnalité principale de l'application, car elle permet aux utilisateurs de conserver leur historique de suivi de temps sans perte de données. Sans ces autorisations, les utilisateurs risqueraient de perdre toutes leurs données de suivi de temps en cas de problème avec leur appareil."

## Alternatives possibles

1. **Utiliser le Storage Access Framework (SAF)** - Nous pourrions remplacer notre méthode actuelle d'accès au stockage par des intents ACTION_CREATE_DOCUMENT et ACTION_OPEN_DOCUMENT, ce qui éviterait de demander ces permissions. Cependant, cela rendrait l'expérience utilisateur moins fluide car l'utilisateur devrait interagir avec le sélecteur de fichiers à chaque sauvegarde.

2. **Utiliser le stockage interne uniquement** - Nous pourrions stocker les sauvegardes uniquement dans le stockage interne de l'application (qui sera supprimé si l'application est désinstallée). Cela ne nécessiterait pas d'autorisations spéciales, mais limiterait considérablement l'utilité de la fonction de sauvegarde.