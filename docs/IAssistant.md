# Guide d'IA Locale pour Android

## Applications d'IA locale pour Android

Voici les meilleures options pour utiliser une IA locale sur un appareil Android en mai 2025 :

### 1. Applications directement disponibles sur le Play Store

| Nom de l'application | Description | Points forts | Modèles supportés |
|---------------------|-------------|-------------|-------------------|
| **LM Studio Mobile** | Interface conviviale pour LLMs sur Android | UI intuitive, gestion de la mémoire | Llama 2/3, Mistral, Phi |
| **GPT4All Mobile** | Application mobile de la suite GPT4All | Nombreux modèles préchargés | GPT-J, MPT, Falcon, Llama |
| **LocalLLM** | Client léger pour modèles GGUF | Optimisé pour appareils à faibles ressources | TinyLlama, Phi-2, ggml models |
| **Hugging Face App** | Application officielle de Hugging Face | Accès à la bibliothèque HF | Modèles optimisés pour mobile |

### 2. Applications à installer via APK externe

| Nom de l'application | Lien GitHub | Description |
|---------------------|------------|-------------|
| **LLaMA.cpp Android** | [github.com/ggerganov/llama.cpp](https://github.com/ggerganov/llama.cpp/releases) | Version Android de la populaire implémentation llama.cpp |
| **MLC LLM** | [github.com/mlc-ai/mlc-llm](https://github.com/mlc-ai/mlc-llm/releases) | Anciennement "MLC Chat", optimisé par TVM |

### 3. Modèles recommandés pour téléphones à ressources limitées

| Modèle | Taille | RAM minimale | Performance |
|--------|-------|--------------|-------------|
| **TinyLlama** | 1.1B (≈600MB quantifié) | 2 Go | Basique mais fonctionnel |
| **Phi-2** | 2.7B (≈1.5GB quantifié) | 3 Go | Étonnamment capable |
| **Phi-3-mini** | 3.8B (≈2GB quantifié) | 4 Go | Excellent rapport taille/performance |
| **Mistral 7B-Instruct-v0.2** | 7B (≈4GB en 4-bit) | 6 Go | Très performant, proche de GPT-3.5 |

## Installation de LLaMA.cpp Android (recommandé)

1. Visiter [github.com/ggerganov/llama.cpp/releases](https://github.com/ggerganov/llama.cpp/releases)
2. Télécharger le fichier APK le plus récent (`llamacpp-android-[version].apk`)
3. Activer "Installation depuis sources inconnues" dans les paramètres Android
4. Installer l'APK téléchargé
5. Ouvrir l'application et télécharger un modèle (TinyLlama recommandé pour commencer)

## Configuration et modèles pour Ollama sur PC

Puisque tu possèdes une RTX 2080 Ti, Ollama sur PC est une excellente option :

```bash
# Télécharger Llama 3
ollama pull llama3

# Télécharger Mistral
ollama pull mistral

# Télécharger Phi-3 mini
ollama pull phi3:mini
```

## Projet proposé : création d'une application Android d'assistant vocal

Pour notre projet de création d'une application Android avec assistant vocal local, nous pourrions utiliser :

1. **Interface** : Kotlin + Jetpack Compose
2. **Reconnaissance vocale** : Whisper Tiny (local)
3. **Traitement NLU** : TensorFlow Lite avec modèle personnalisé
4. **Backend IA** : Connexion à LLaMA.cpp Android via API locale

Ce document sera mis à jour au cours de notre projet.