package com.modekz.json;

import java.util.ArrayList;
import java.util.List;

public class DbUpdateInfoPlus extends DbUpdateInfo {
    public static final char UPDATED = 'U';
    public static final char INSERTED = 'I';
    public static final char DELETED = 'D';

    public List<Item> items;

    public DbUpdateInfoPlus(String text, int count) {
        String[] lines = text.split("\\r?\\n");

        // First line is header
        items = new ArrayList<>(lines.length - 1);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] parts = line.split(";");
            if (parts.length != count)
                continue;

            items.add(new Item(parts));
        }
    }

    public static class Item {
        public String[] data;
        public char result;

        public Item(String[] data) {
            this.data = data;
        }
    }
}

