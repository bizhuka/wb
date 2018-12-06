package com.modekz.json;

public class DbUpdateInfo {
    public int inserted;
    public int updated;
    public int deleted;
    public int dbcnt;

    public static int countModified(int[] results) {
        int ok = 0;
        for (int res : results)
            ok += res;
        return ok;
    }
}
