package com.comonon.mall.pay.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PayNoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String next() {
        String ts = LocalDateTime.now().format(FMT);
        int rand = ThreadLocalRandom.current().nextInt(0, 100000);
        return "P" + ts + String.format("%05d", rand);
    }
}
