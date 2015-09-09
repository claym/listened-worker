package io.listened.worker.util;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

/**
 * Created by Clay on 9/7/2015.
 */
public class TextUtils {

    public static String removeHtml(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }
        return new HtmlToPlainText().getPlainText(Jsoup.parse(text));
    }
}
