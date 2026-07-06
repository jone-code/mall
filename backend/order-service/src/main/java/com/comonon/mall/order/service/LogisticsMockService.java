package com.comonon.mall.order.service;

import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.vo.LogisticsEventVO;
import com.comonon.mall.order.vo.LogisticsVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Mock 物流轨迹（快递100 接入前占位）。 */
@Component
public class LogisticsMockService {

    public LogisticsVO build(OrderEntity order) {
        if (order.getTrackingNo() == null || order.getTrackingNo().isBlank()) {
            return null;
        }
        LogisticsVO vo = new LogisticsVO();
        vo.setTrackingNo(order.getTrackingNo());
        vo.setTrackingCompany(order.getTrackingCompany() != null ? order.getTrackingCompany() : "快递");
        vo.setStatus(orderStatusLabel(order.getStatus()));

        List<LogisticsEventVO> events = new ArrayList<>();
        LocalDateTime shipAt = order.getShipAt() != null ? order.getShipAt() : LocalDateTime.now().minusDays(1);
        events.add(event(shipAt, "已发货", "商家已发货，等待揽收"));
        events.add(event(shipAt.plusHours(4), "运输中", "快件已到达【" + vo.getTrackingCompany() + "转运中心】"));
        events.add(event(shipAt.plusDays(1), "派送中", "快件正在派送中，请保持电话畅通"));
        if ("COMPLETED".equals(order.getStatus())) {
            LocalDateTime done = order.getCompleteAt() != null ? order.getCompleteAt() : shipAt.plusDays(2);
            events.add(0, event(done, "已签收", "您的快件已签收，感谢使用"));
        }
        vo.setEvents(events);
        return vo;
    }

    private static LogisticsEventVO event(LocalDateTime time, String status, String desc) {
        LogisticsEventVO e = new LogisticsEventVO();
        e.setTime(time);
        e.setStatus(status);
        e.setDesc(desc);
        return e;
    }

    private static String orderStatusLabel(String status) {
        return switch (status) {
            case "SHIPPED" -> "运输中";
            case "COMPLETED" -> "已签收";
            default -> "已发货";
        };
    }
}
