package schedule;

import entity.Discount;
import enums.DiscountStatusEnum;
import service.DiscountService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscountStatusSchedule {

    private final DiscountService discountService = new DiscountService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {

        long initialDelay = getInitialDelayToMidnight();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateDiscountStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private long getInitialDelayToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();

        return Duration.between(now, nextMidnight).getSeconds();
    }

    public void updateDiscountStatus() {
        List<Discount> discounts = discountService.findAll(null);
        LocalDateTime now = LocalDateTime.now();

        for (Discount d : discounts) {

            DiscountStatusEnum newStatus = calculateStatus(d.getStartedAt(), d.getEndedAt());

            if (d.getStatus() != newStatus) {
                discountService.updateStatus(d.getId(), newStatus.getValue());
            }
        }

        System.out.println("Updated discount status at: " + now);
    }

    public DiscountStatusEnum calculateStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (end != null) {
            end = end.withHour(23).withMinute(59).withSecond(59);
        }

        if (start != null && now.isBefore(start)) {
            return DiscountStatusEnum.UPCOMING;
        } else if (end != null && now.isAfter(end)) {
            return DiscountStatusEnum.EXPIRED;
        } else {
            return DiscountStatusEnum.ACTIVE;
        }
    }
}