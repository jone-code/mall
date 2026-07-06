package com.comonon.mall.order.web;

import com.comonon.mall.common.web.PageResult;
import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.dto.AdminRemarkRequest;
import com.comonon.mall.order.dto.BatchShipRequest;
import com.comonon.mall.order.dto.ShipOrderRequest;
import com.comonon.mall.order.service.OrderService;
import com.comonon.mall.order.vo.BatchShipResultVO;
import com.comonon.mall.order.vo.LogisticsVO;
import com.comonon.mall.order.vo.OrderStatsVO;
import com.comonon.mall.order.vo.OrderTrendVO;
import com.comonon.mall.order.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> list(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String productType,
                                            @RequestParam(required = false) LocalDate from,
                                            @RequestParam(required = false) LocalDate to,
                                            @RequestParam(value = "page", defaultValue = "1") int page,
                                            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(orderService.adminList(status, userId, orderNo, phone, productType, from, to, page, size));
    }

    @GetMapping("/stats")
    public Result<OrderStatsVO> stats() {
        return Result.ok(orderService.stats());
    }

    @GetMapping("/trends")
    public Result<OrderTrendVO> trends(@RequestParam(value = "days", defaultValue = "7") int days) {
        return Result.ok(orderService.trend(days));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String orderNo,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String productType,
                                         @RequestParam(required = false) LocalDate from,
                                         @RequestParam(required = false) LocalDate to) {
        String csv = orderService.adminExportCsv(status, userId, orderNo, phone, productType, from, to);
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping("/batch-ship")
    public Result<BatchShipResultVO> batchShip(@Valid @RequestBody BatchShipRequest req,
                                               HttpServletRequest request) {
        return Result.ok(orderService.batchShipByAdmin(req, request.getHeader("X-Admin-User-Id")));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable("orderNo") String orderNo) {
        return Result.ok(orderService.adminDetail(orderNo));
    }

    @PostMapping("/{orderNo}/close")
    public Result<Void> close(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        orderService.closeByAdmin(orderNo, request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @PostMapping("/{orderNo}/ship")
    public Result<Void> ship(@PathVariable("orderNo") String orderNo,
                             @Valid @RequestBody ShipOrderRequest req,
                             HttpServletRequest request) {
        orderService.shipByAdmin(orderNo, req.getTrackingNo(), req.getTrackingCompany(),
                request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @PutMapping("/{orderNo}/remark")
    public Result<Void> remark(@PathVariable("orderNo") String orderNo,
                               @Valid @RequestBody AdminRemarkRequest req) {
        orderService.updateAdminRemark(orderNo, req.getRemark());
        return Result.ok();
    }

    @PostMapping("/{orderNo}/refund/approve")
    public Result<Void> approveRefund(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        orderService.approveRefundByAdmin(orderNo, request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @PostMapping("/{orderNo}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        orderService.rejectRefundByAdmin(orderNo, request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @GetMapping("/{orderNo}/logistics")
    public Result<LogisticsVO> logistics(@PathVariable("orderNo") String orderNo) {
        return Result.ok(orderService.adminLogistics(orderNo));
    }
}
