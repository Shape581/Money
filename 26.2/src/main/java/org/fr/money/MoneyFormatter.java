package org.fr.money;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private MoneyFormatter() {

    }

    public static String format(double amount) {
        int mode = Money.getInstance().getMoneyFormatMode();
        return switch (mode) {
            case 1 -> formatWithSpaces(amount);
            case 2 -> formatCompact(amount);
            default -> String.valueOf(amount);
        };
    }

    private static String formatWithSpaces(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' ');

        DecimalFormat format = new DecimalFormat("#,###", symbols);

        return format.format(amount);
    }

    private static String formatCompact(double amount) {
        if (amount >= 1_000_000_000) {
            return removeDecimal(amount / 1_000_000_000D) + "B";
        }

        if (amount >= 1_000_000) {
            return removeDecimal(amount / 1_000_000D) + "M";
        }

        if (amount >= 1_000) {
            return removeDecimal(amount / 1_000D) + "K";
        }

        return removeDecimal(amount);
    }

    private static String removeDecimal(double value) {
        if (value % 1 == 0) {
            return String.valueOf((long) value);
        }

        return String.format(Locale.US, "%.1f", value);
    }
}