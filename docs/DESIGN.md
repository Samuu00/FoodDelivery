# Design — Food Delivery v2

Il redesign è nato su una tela di design (l'equivalente di un file Figma), poi è stato implementato in JavaFX 1:1.
Tela con design system, componente App bar e prototipo cliccabile: https://claude.ai/artifact/8G1fySJZttQmR3xiThmFMs (privata finché non la condividi).

Identità invariata rispetto al progetto originale: arancio `#EA641E`, ambra `#FFB83F`, crema `#FFF2B2`, bruciato `#CC5500`,
pattern a pizze, illustrazioni e loghi. Cambiano gerarchia, layout, flussi e stati.

## 1. Token (`src/css/app.css`, blocco `.root`)

| Token | Valore | Uso |
|---|---|---|
| `-fd-orange` | `#EA641E` | app bar, focus |
| `-fd-amber` | `#FFB83F` | bottone secondario, carrello nell'app bar |
| `-fd-cream` | `#FFF2B2` | sfondo loghi, chip, stepper |
| `-fd-cta` | `#CC5500` | azione primaria (una per schermata), prezzi |
| `-fd-canvas` | `#FFF8EE` | sfondo pagine |
| `-fd-surface` | `#FFFFFF` | card e pannelli |
| `-fd-border` / `-fd-border-strong` | `#EFE3D0` / `#DCCDB8` | divisori / bordi dei campi |
| `-fd-ink` / `-fd-muted` | `#2B2118` / `#6B5B4B` | testo (15:1) / testo secondario (6,4:1) |
| `-fd-success` / `-fd-danger` | `#1E7E34` / `#C62828` | sconti e conferme / errori |

Il tema scuro (`src/css/dark.css`) ridefinisce solo i token di superfici e testo.
Raggi: 8 campi · 12 menu · 16 schede scelta · 20 card · 24 pannelli · pill per bottoni e chip. Spaziatura su griglia da 4.

**Tipografia** (font inclusi in `src/fonts`, licenza OFL, caricati all'avvio):
Fredoka per titoli e prezzi (display 38–44, title 28, heading 21), Nunito Sans per il testo (15 regular, 14 bold per le etichette, ExtraBold per i bottoni).
Sostituisce "Super Adorable", che non era incluso nel progetto: sui PC senza quel font i titoli finivano in un font di sistema.

**Icone**: tratto 2.2 su griglia 24, disegnate con `SVGPath` (`ui/Icons.java`) e colorate da CSS. Niente emoji (JavaFX le mostra a quadratini o in monocromo).

## 2. Componenti (`src/ui/`, `src/view/shell.fxml`)

| Componente | Implementazione | Note |
|---|---|---|
| App bar | `shell.fxml` + `ShellController` | logo, ricerca (nascosta in home e checkout), indirizzo attivo, carrello con quantità e totale, avatar con menu |
| Bottoni | classi `button` / `btn-secondary` / `btn-ghost` / `btn-outline`, `btn-lg`, `btn-sm` | 44px (52 per la CTA); focus visibile solo da tastiera |
| Campo password | `PasswordInput` | occhio mostra/nascondi, stato errore |
| Stepper quantità | `Ui.stepper` | "Aggiungi" → `− n +`, larghezza riservata, si aggiorna da solo |
| Card ristorante | `HomeController.card` | logo su crema, categorie come chip, indirizzo |
| Schede di scelta | classe `choice-card` | indirizzo e metodo di pagamento (radio a scheda) |
| Stato vuoto, toast, conferma | `Ui.emptyState`, `ShellController.toast`, `Ui.confirm` | feedback senza finestre modali quando non servono |

## 3. Flusso

```
Accesso (login | crea account) ──► Home ──► Ristorante ──► Checkout ──► Conferma
                                    ▲  categorie / ricerca     │ carrello sempre visibile
                                    │                          ▼
                     App bar: indirizzo · carrello · avatar (Ordini, Indirizzi, Tema scuro, Esci)
```
Da 6 a 4 passaggi: la pagina risultati è confluita nella home, pagamento e checkout sono una sola pagina.

## 4. Decisioni UX (prima → dopo)

| Prima | Dopo |
|---|---|
| Header, footer, menu e carrello copiati in 8 FXML | Una shell; le pagine sono solo contenuto |
| Carrello in un cassetto nascosto | Colonna fissa nel ristorante + quantità e totale sempre nell'app bar |
| Si potevano mescolare piatti di ristoranti diversi | Un carrello = un ristorante; se cambi ristorante l'app chiede conferma |
| Ricerca AI senza feedback; senza chiave non funzionava | Spinner e messaggio; senza chiave ricerca testuale su nomi, piatti, categorie |
| Pagina risultati separata | Risultati nella griglia della home, con "Mostra tutti" |
| Checkout e pagamento separati, alert per ogni errore | Una pagina a sezioni numerate, riepilogo fisso, errori sotto il form, "Paga" disabilitato con motivo |
| Indirizzo scelto solo al checkout | Indirizzo attivo nell'app bar, cambiabile ovunque, sincronizzato col checkout |
| Login e registrazione separati, conferma password | Una schermata con switch, indicatore di robustezza, accesso diretto dopo la registrazione |
| Ordini e profilo in pagine scollegate | Area account con navigazione laterale; ogni ordine mostra ristorante e piatti; "Riordina" riporta al ristorante |
| Testo bianco su giallo (contrasto 1,6:1) | Giallo solo come secondario con testo scuro (10:1); primario bruciato con testo bianco |
| Tema scuro nei file ma mai attivabile | Dal menu dell'avatar |

## 5. Accessibilità
- Tutto usabile da tastiera: card e schede sono bottoni o toggle, `Invio` invia i form, anello di focus visibile solo da tastiera.
- `accessibleText` su bottoni con sola icona e sugli stepper ("Aggiungi una Margherita").
- Contrasti AA su testo e componenti; i colori di stato hanno anche icona o testo, non solo colore.
