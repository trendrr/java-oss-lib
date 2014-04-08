package com.trendrr.oss;

/**
 * Simple xml cleaner
 */
public class SimpleXmlFormatter implements XMLFormatter {
    public String cleanKey(String str) {
        return str.replaceAll(" ", "_");
    }
    public String cleanValue(String str) {
        return str;
    }
}
