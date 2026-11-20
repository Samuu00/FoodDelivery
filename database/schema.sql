-- Food Delivery: schema + dati demo.
-- Uso: mysql -u root -p < database/schema.sql
CREATE DATABASE IF NOT EXISTS fooddelivery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fooddelivery;

CREATE TABLE IF NOT EXISTS Utenti (
    Username VARCHAR(50)  PRIMARY KEY,
    Email    VARCHAR(120) NOT NULL UNIQUE,
    Password CHAR(64)     NOT NULL          -- SHA-256 esadecimale
);

CREATE TABLE IF NOT EXISTS Indirizzi (
    IndirizzoID INT AUTO_INCREMENT PRIMARY KEY,
    Username    VARCHAR(50)  NOT NULL,
    Indirizzo   VARCHAR(255) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Utenti(Username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Ristoranti (
    RistoranteID INT AUTO_INCREMENT PRIMARY KEY,
    Nome         VARCHAR(100) NOT NULL,
    Descrizione  TEXT,
    Indirizzo    VARCHAR(255),
    ImmagineURL  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Menu (
    MenuID       INT AUTO_INCREMENT PRIMARY KEY,
    RistoranteID INT NOT NULL,
    NomePiatto   VARCHAR(100) NOT NULL,
    Prezzo       DECIMAL(6, 2) NOT NULL,
    ImmagineURL  VARCHAR(255),
    Categoria    VARCHAR(50) NOT NULL,     -- Pizza | Sushi | Panini | Dolci
    FOREIGN KEY (RistoranteID) REFERENCES Ristoranti(RistoranteID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS coupon (
    Codice   VARCHAR(30) PRIMARY KEY,
    Sconto   DECIMAL(5, 2) NOT NULL,       -- percentuale
    Username VARCHAR(50) NULL,             -- NULL = valido per tutti
    FOREIGN KEY (Username) REFERENCES Utenti(Username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Ordini (
    OrdineID       INT AUTO_INCREMENT PRIMARY KEY,
    Username       VARCHAR(50) NOT NULL,
    NumeroOrdine   INT NOT NULL,           -- progressivo per utente
    DataOrdine     DATETIME NOT NULL,
    Stato          VARCHAR(30) NOT NULL,
    PrezzoTotale   DECIMAL(8, 2) NOT NULL,
    PrezzoScontato DECIMAL(8, 2) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Utenti(Username)
);

CREATE TABLE IF NOT EXISTS DettagliOrdine (
    OrdineID       INT NOT NULL,
    MenuID         INT NOT NULL,
    Quantita       INT NOT NULL,
    PrezzoUnitario DECIMAL(6, 2) NOT NULL,
    PRIMARY KEY (OrdineID, MenuID),
    FOREIGN KEY (OrdineID) REFERENCES Ordini(OrdineID) ON DELETE CASCADE,
    FOREIGN KEY (MenuID) REFERENCES Menu(MenuID)
);

-- ---------- Dati demo ----------
INSERT IGNORE INTO Ristoranti (RistoranteID, Nome, Descrizione, Indirizzo, ImmagineURL) VALUES
 (1, 'La Terra',      'Pizzeria napoletana con forno a legna e impasti a lunga lievitazione.', 'Via Roma 12, Cosenza',     'LaTerra.png'),
 (2, 'Sushi House',   'Sushi fresco preparato al momento, roll creativi e nigiri classici.',  'Corso Mazzini 88, Cosenza', 'SushiHouse.png'),
 (3, 'Burger Co.',    'Burger di manzo e opzioni veggie, pane artigianale.',                  'Via Popilia 5, Rende',      'BurgerCo.png'),
 (4, 'Dolci Delizie', 'Pasticceria siciliana e dessert della casa.',                          'Piazza Kennedy 3, Rende',   'DolciDelizie.png');

INSERT IGNORE INTO Menu (MenuID, RistoranteID, NomePiatto, Prezzo, ImmagineURL, Categoria) VALUES
 (1,  1, 'Margherita',        6.50, 'margherita.png',       'Pizza'),
 (2,  1, 'Diavola',           8.00, 'Diavola.png',          'Pizza'),
 (3,  1, 'Quattro Formaggi',  9.00, 'quattro_formaggi.png', 'Pizza'),
 (4,  1, 'Tiramisù',          4.50, 'tiramisu.png',         'Dolci'),
 (5,  2, 'California Roll',  10.00, 'california_roll.png',  'Sushi'),
 (6,  2, 'Nigiri Salmone',    7.50, 'nigiri_sal.png',       'Sushi'),
 (7,  2, 'Sushi Misto',      18.00, 'sushi_misto.png',      'Sushi'),
 (8,  3, 'Burger Classico',   9.50, 'burger_classico.png',  'Panini'),
 (9,  3, 'Veggie Burger',     9.00, 'veggie_burger.png',    'Panini'),
 (10, 3, 'Chicken Wrap',      8.00, 'chicken_wrap.png',     'Panini'),
 (11, 4, 'Cannoli',           3.50, 'cannoli.png',          'Dolci'),
 (12, 4, 'Cheesecake',        5.00, 'cheesecake.png',       'Dolci'),
 (13, 4, 'Tiramisù',          4.50, 'tiramisu.png',         'Dolci');

INSERT IGNORE INTO coupon (Codice, Sconto, Username) VALUES ('BENVENUTO10', 10, NULL);
