<div align="center">
  
# 🍔 FoodDelivery App
**Applicazione Desktop JavaFX per la gestione di ordini a domicilio con integrazione di Intelligenza Artificiale.**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)]()
[![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)]()
[![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white)]()

---

Un'applicazione gestionale per ordini di ristorazione a domicilio, progettata utilizzando architetture software robuste (MVC, DAO, Service Layer) e dotata di un "Assistente AI" integrato in grado di convertire le richieste naturali in query SQL dinamiche.

</div>

<br />

## 🚀 Caratteristiche Principali

- **Architettura Enterprise (MVC Esteso)**: Struttura a strati con netta separazione delle responsabilità tra Viste (JavaFX/FXML), Controllers, Modelli, Service Layer e DAOs.
- **Integrazione AI Intelligente**: Motore di ricerca alimentato dall'API di OpenAI in grado di tradurre l'intento dell'utente (es. *"Voglio una pizza piccante sotto i 10 euro"*) in vere e proprie query sul database dei menu.
- **Transazioni Sicure (ACID)**: Gestione completa degli ordini a carrello tramite il *Service Layer*, garantendo integrità delle tabelle Relazionali mediante Transazioni Protette (nessun ordine orfano).
- **Crittografia Integrata**: Cifratura delle password utente (`SHA-256`) fin dal momento della registrazione per una totale compliance agli standard di sicurezza.
- **Sistema Carrello Avanzato**: Implementato con il design pattern `Bill Pugh Singleton` (*Initialization-on-Demand Holder*), che garantisce sicurezza multi-thread e massima efficienza in memoria.
- **Gestore di Configurazione Centralizzata**: Connessioni DB e chiavi API salvaguardate in file offuscati, distaccati dal codice sorgente esposto.

<br />

## 🛠️ Stack Tecnologico

- **Linguaggio**: Java
- **Interfaccia Grafica**: JavaFX
- **Database**: MySQL
- **Logica Architetturale**: MVC, Facade (Orders), Singleton (Cart), DAO (Data Access Object)
- **API Esterna**: OpenAI GPT API
- **Tool di Esportazione**: iTextPDF (per la generazione della cronologia ordini in PDF documentale)

<br />

## 📦 Struttura del Progetto

```plaintext
📦 FoodDelivery
 ┣ 📂 src
 ┃ ┣ 📂 AI             # Agent core per l'interfacciamento con OpenAI (AIAgent.java)
 ┃ ┣ 📂 Controller     # Controllers UI (SceneManager, Home, Checkout, Login)
 ┃ ┣ 📂 DAO            # Data Access Objects (OrderDAO, MenuDAO, CouponDAO...)
 ┃ ┣ 📂 DatabaseConn   # Singleton Connection al DB e pooling potenziale
 ┃ ┣ 📂 Model          # Strutture dati base (User, Restaurant, MenuItem, Cart)
 ┃ ┣ 📂 Scene          # Entry point (Main Applicativo e pre-render JavaFX)
 ┃ ┣ 📂 Service        # Core Business Logic (es: OrderService transazionale)
 ┃ ┣ 📂 Utils          # Multi-tools crittografaci (Hash) e Gestori di Configurazione
 ┃ ┗ 📜 config.properties # FIle di properties per credenziali Database e API
 ┣ 📂 resources
 ┃ ┣ 📂 FXML           # Markup grafico Viste Applicazione
 ┃ ┗ 📂 CSS            # Fogli di Stile
```
