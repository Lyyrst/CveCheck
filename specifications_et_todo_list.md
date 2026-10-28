# 📋 Récapitulatif Technique & TODO List : CVE Watcher & Discord Bot

Ce document récapitule l'architecture, le cycle de vie des données, les contrats d'API et la feuille de route opérationnelle pour le système de veille CVE et de création automatisée de fils de discussion Discord.

---

## 1. Rappel de l'Architecture & Principes Clés

```text
┌────────────────────────────────────────────────────────┐
│               API OFFICIELLE CVE.ORG (v5)              │
└───────────────────────────┬────────────────────────────┘
                            │ (1 seul appel / delta temporel)
                            ▼
┌────────────────────────────────────────────────────────┐
│                     BACKEND SPRING                     │
│                                                        │
│  [ Cron Ingestion Global ] ──► Filtre local en mémoire │
│                                (technos actives)       │
│                                      │                 │
│                                      ▼                 │
│  [ DB Locale ] ◄────────────── Persistance             │
│  (Postgres/SQLite)                                     │
│                                      ▲                 │
│                                      │                 │
│  [ Worker Articles ] ───────── Surveillance CVEs       │
│                                ciblées (ex: 14 jours)  │
│                                      ▲                 │
│                                      │                 │
│  [ Spring Security ] ◄──────── JWT (client_id fixe)    │
│  [ REST Controllers ]                                  │
└──────────────────────────────▲─────────────────────────┘
                               │
                Appels REST authentifiés (JWT)
                               │
┌──────────────────────────────┴─────────────────────────┐
│                   CLIENT BOT DISCORD                   │
│                                                        │
│  • Consomme les nouvelles CVE depuis son dernier poll  │
│  • Déclenche la surveillance d'articles sur une CVE    │
│  • Crée et alimente les threads dans les salons Forum  │
└────────────────────────────────────────────────────────┘
```

### Principes retenus
1. **Source officielle unique :** Utilisation de l'API CVE v5 (`cveawg.mitre.org`).
2. **Polling par delta :** Le backend interroge les modifications globales via un timestamp (`time_modified.gte`), sans multiplier les appels par techno.
3. **Filtre local dynamique :** Le matching s'effectue en mémoire sur le champ `containers.cna.affected[].product` contre la liste des technos actives en base.
4. **Gestion de session client :** Le `client_id` est extrait du token JWT. Le backend retient la date de dernière interrogation (`last_cve_poll_at`) pour renvoyer uniquement les deltas non lus.
5. **Surveillance d'articles ciblée :** Déclenchée explicitement par le client sur un numéro de CVE précis avec une durée paramétrable ($X$ jours).

---

## 2. Spécification des Contrats d'API (Backend)

| Verbe | Endpoint | Rôle | Paramètres / Body | Comportement clé |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/cves/new` | Récupérer les nouvelles CVEs | `None` *(auth via JWT)* | Lit `last_cve_poll_at`, renvoie les CVEs matchant les abonnements du client, et met à jour le curseur à `now()`. |
| `POST` | `/api/cves/{cveId}/watch` | Demander la veille sur une CVE | `durationDays` (query param, ex: 14) | Enregistre une entrée avec `watch_until = now() + durationDays`. |
| `GET` | `/api/cves/{cveId}/articles` | Lister les articles découverts | `since` & `until` (timestamps ISO) | Renvoie la liste ordonnée des articles indexés sur la période. |
| `POST` | `/api/subscriptions` | Ajouter une techno à surveiller | `{"technology": "spring"}` | Ajoute l'association `client_id` $\leftrightarrow$ `technology`. |
| `DELETE`| `/api/subscriptions/{tech}` | Retirer une veille techno | Path variable `tech` | Supprime l'abonnement du client. |

---

## 4. TODO List Opérationnelle

### Phase 1 : Socle Backend & Persistance
- [x] Initialiser le projet Spring Boot (`Web`, `Security`, `Data JPA`, `Validation`).
- [x] Configurer la base de données (PostgreSQL ou SQLite) et implémenter les entités JPA.
- [ ] Mettre en place la configuration Spring Security :
  - [ ] Décodeur JWT et filtre d'authentification.
  - [ ] Extraction automatique du `client_id` depuis le claim du token dans le `SecurityContext`.

### Phase 2 : Ingestion CVE.org (Moteur global)
- [ ] Configurer le `WebClient`.
- [ ] Développer le service de mapping du format CVE v5 (`containers.cna.affected`).
- [ ] Créer le cron d'ingestion planifié (ex: toutes les 5 ou 10 minutes) :
  - [ ] Lecture du `last_modified_timestamp` depuis `ingestion_cursor`.
  - [ ] Appel de `/cve?time_modified.gte=...`.
  - [ ] Extraction en mémoire des technos distinctes abonnées (`SELECT DISTINCT technology`).
  - [ ] Filtrage local et insertion des CVEs correspondantes en base.
  - [ ] Mise à jour du curseur d'ingestion.

### Phase 3 : Endpoints REST Backend
- [ ] Endpoint `GET /api/cves/new` :
  - [ ] Lecture du curseur du client (`client_sync_state`).
  - [ ] Requête jointure CVE $\leftrightarrow$ Abonnements.
  - [ ] Mise à jour du curseur à `Instant.now()`.
- [ ] Endpoint `POST /api/cves/{cveId}/watch` :
  - [ ] Validation de l'existence de la CVE.
  - [ ] Calcul et sauvegarde de la date de fin de surveillance.
- [ ] Endpoint `GET /api/cves/{cveId}/articles` :
  - [ ] Filtrage par date de publication entre `since` et `until`.
- [ ] Endpoints de gestion d'abonnements (`POST` / `DELETE` / `GET /api/subscriptions`).

### Phase 4 : Module d'Enrichissement (Articles & Analyses)
- [ ] Sélectionner les sources de collecte d'analyses (APIs moteurs de recherche, flux RSS spécialisés en sécu, API GitHub Search pour les PoC).
- [ ] Créer un worker planifié :
  - [ ] Sélection des tâches actives (`watch_until > now()`).
  - [ ] Requêtage par identifiant CVE.
  - [ ] Déduplication par URL et sauvegarde dans `cve_articles`.

### Phase 5 : Client Bot Discord
- [ ] Initialiser le bot Discord (`discord.js` ou `discord.py`) avec un token applicatif et le salon Forum cible.
- [ ] Mettre en place un cron côté bot qui appelle `GET /api/cves/new` :
  - [ ] Pour chaque CVE retournée : créer un thread dans le Forum Discord (`name = "[CVE-ID] Techno"`).
  - [ ] Appeler immédiatement `POST /api/cves/{id}/watch?durationDays=14` sur le backend.
- [ ] Mettre en place une tâche périodique de mise à jour des threads :
  - [ ] Appel de `GET /api/cves/{id}/articles`.
  - [ ] Publication des nouveaux liens d'articles directement dans le thread de discussion existant.
- [ ] *(Optionnel)* Commandes Slash Discord `/watch <techno>` et `/unwatch <techno>` reliées aux endpoints d'abonnements du backend.