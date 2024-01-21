package org.ISMP;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES.ordinal(), 10);
        String signature = "example_signature";
        crptApi.createDocument(crptApi.getDefaultDocument(), signature);
    }
}