package util;

import java.time.YearMonth;

/** Validazioni dei form. Self-check: java util/Validators.java */
public final class Validators {
    private Validators() {}

    public static boolean email(String s) {
        return s != null && s.trim().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /** Numero carta 13-19 cifre con controllo di Luhn. */
    public static boolean cardNumber(String s) {
        if (s == null) return false;
        String n = s.replaceAll("\\s+", "");
        if (!n.matches("\\d{13,19}")) return false;
        int sum = 0;
        boolean alt = false;
        for (int i = n.length() - 1; i >= 0; i--) {
            int d = n.charAt(i) - '0';
            if (alt && (d *= 2) > 9) d -= 9;
            sum += d;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    /** MM/AA o MM/AAAA, non scaduta. */
    public static boolean expiry(String s, YearMonth now) {
        if (s == null || !s.trim().matches("(0[1-9]|1[0-2])/(\\d{2}|\\d{4})")) return false;
        String[] p = s.trim().split("/");
        int year = Integer.parseInt(p[1]);
        if (year < 100) year += 2000;
        return !YearMonth.of(year, Integer.parseInt(p[0])).isBefore(now);
    }

    public static boolean cvv(String s) { return s != null && s.trim().matches("\\d{3,4}"); }

    public static boolean cardHolder(String s) { return s != null && s.trim().matches("[A-Za-zÀ-ÖØ-öø-ÿ' ]{3,}"); }

    public static void main(String[] args) {
        YearMonth now = YearMonth.of(2026, 11);
        assert email("mario.rossi@mail.it") && !email("mario@") && !email("");
        assert cardNumber("4111 1111 1111 1111") && !cardNumber("4111 1111 1111 1112") && !cardNumber("123");
        assert expiry("11/26", now) && expiry("01/2030", now) && !expiry("10/26", now) && !expiry("13/30", now);
        assert cvv("123") && cvv("1234") && !cvv("12a");
        assert cardHolder("Mario Rossi") && !cardHolder("M1");
        System.out.println("Validators OK");
    }
}
