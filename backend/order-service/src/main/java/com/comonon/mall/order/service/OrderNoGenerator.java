package com.comonon.mall.order.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** O + yyyyMMddHHmmss + userTail(2) + random(3)，长度 20。 */
    public String next(Long userId) {
        String ts = LocalDateTime.now().format(FMT);
        long tail = userId == null ? 0 : Math.floorMod(userId, 100);
        int rand = ThreadLocalRandom.current().nextInt(0, 1000);
        return "O" + ts + String.format("%02d%03d", tail, rand);
    }
}
