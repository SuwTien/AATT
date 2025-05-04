PS J:\DEV\Android\AATT> git add . 
warning: in the working copy of 'app/proguard-rules.pro', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'aatt_debug.log', LF will be replaced by CRLF the next time Git touches it
PS J:\DEV\Android\AATT> git commit -m "RELEASE 1.1.4: Implémentation SAF complète et correction des problèmes de sauvegarde en version release" 
[main c8602a9] RELEASE 1.1.4: Implémentation SAF complète et correction des problèmes de sauvegarde en version release
 10 files changed, 613 insertions(+), 271 deletions(-)
 create mode 100644 aatt_debug.log
 create mode 100644 app/src/main/java/fr/bdst/aatt/data/util/FileLogger.kt
 create mode 100644 docs/release_notes_1.1.4.md
PS J:\DEV\Android\AATT> git config --global core.autocrlf true                            
PS J:\DEV\Android\AATT> 
PS J:\DEV\Android\AATT> 
PS J:\DEV\Android\AATT> git push
Enumerating objects: 40, done.
Counting objects: 100% (40/40), done.
Delta compression using up to 8 threads
Compressing objects: 100% (19/19), done.
Writing objects: 100% (22/22), 12.40 KiB | 4.13 MiB/s, done.
Total 22 (delta 8), reused 0 (delta 0), pack-reused 0 (from 0)
remote: Resolving deltas: 100% (8/8), completed with 8 local objects.
To https://github.com/SuwTien/AATT.git
   951a0cb..c8602a9  main -> main
PS J:\DEV\Android\AATT>