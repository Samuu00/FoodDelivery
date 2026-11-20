package util;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formattazione coerente in tutta l'app (formato italiano). */
public final class Fmt {
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ITALY);

    private Fmt() {}

    public static String money(double v) { return EURO.format(v); }

    public static String date(LocalDateTime d) { return d == null ? "" : d.format(DATE); }

    /** Es. "20 nov, 21:14". */
    public static String shortDate(LocalDateTime d) { return d == null ? "" : d.format(SHORT); }
}
