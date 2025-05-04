# Plan d'implémentation SAF complète pour AATT

## Phase 1 : Audit du code existant
- [ ] Identifier tous les fichiers qui utilisent l'ancien système (ExternalStorageHelper)
- [ ] Vérifier les références dans EditViewModel
- [ ] Analyser le workflow complet des sauvegardes/restaurations
- [ ] Rechercher les références mixtes entre ancien et nouveau système
- [ ] Documenter tous les points d'intégration nécessitant des modifications

## Phase 2 : Migration vers SAF exclusif
- [ ] Supprimer toutes les références à l'ancien système de sauvegarde
- [ ] Assurer la cohérence dans EditViewModel pour n'utiliser que SAFBackupHelper
- [ ] Adapter l'UI pour guider clairement l'utilisateur dans la sélection du dossier SAF
- [ ] Implémenter la gestion des erreurs robuste pour tous les cas d'utilisation SAF

## Phase 3 : Migration des données
- [ ] Créer un système de détection des anciennes sauvegardes
- [ ] Implémenter un mécanisme de migration des sauvegardes existantes vers SAF
- [ ] Proposer à l'utilisateur de migrer ses anciennes sauvegardes automatiquement

## Phase 4 : Tests et validation
- [ ] Tester la sauvegarde/restauration en mode debug
- [ ] Vérifier le comportement après réinstallation
- [ ] Tester sur plusieurs appareils avec différentes versions d'Android
- [ ] Générer une version release locale et tester avant publication

## Phase 5 : Publication et surveillance
- [ ] Mettre à jour le CHANGELOG.md
- [ ] Publier la version avec correctif sur le Play Store
- [ ] Documenter les changements pour les utilisateurs
- [ ] Mettre en place un système de reporting pour détecter d'éventuels problèmes

## Fichiers à examiner en priorité
- `app/src/main/java/fr/bdst/aatt/data/util/SAFBackupHelper.kt`
- `app/src/main/java/fr/bdst/aatt/data/util/ExternalStorageHelper.kt`
- `app/src/main/java/fr/bdst/aatt/data/util/DatabaseBackupHelper.kt`
- `app/src/main/java/fr/bdst/aatt/viewmodel/EditViewModel.kt`
- `app/src/main/java/fr/bdst/aatt/ui/screens/EditScreen.kt`

## Notes importantes
- Attention aux permissions Android 13+ (API 33+) versus versions antérieures
- Gérer correctement les URI persistants via takePersistableUriPermission
- Assurer la compatibilité avec les différents fournisseurs de documents (stockage local, Google Drive, etc.)