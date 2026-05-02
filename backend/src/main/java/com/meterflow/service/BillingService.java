package com.meterflow.service;

import com.meterflow.dto.BillingDtos.BillingResponse;
import com.meterflow.entity.Billing;
import com.meterflow.entity.PaymentStatus;
import com.meterflow.entity.User;
import com.meterflow.repository.ApiKeyRepository;
import com.meterflow.repository.ApiProjectRepository;
import com.meterflow.repository.BillingRepository;
import com.meterflow.repository.UsageLogRepository;
import com.meterflow.repository.UserRepository;
import com.meterflow.security.PrincipalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {
    private static final long FREE_REQUESTS = 1000;
    private static final BigDecimal PRICE_PER_100 = new BigDecimal("0.50");

    private final BillingRepository billingRepository;
    private final UsageLogRepository usageLogRepository;
    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiProjectRepository apiProjectRepository;

    public List<BillingResponse> myBills(PrincipalUser principal) {
        return billingRepository.findByUserOrderByMonthDesc(principal.user()).stream().map(this::toResponse).toList();
    }

    @Scheduled(cron = "0 0 2 1 * *")
    public void generatePreviousMonthInvoices() {
        generateInvoicesFor(YearMonth.now(ZoneOffset.UTC).minusMonths(1));
    }

    public void generateInvoicesFor(YearMonth month) {
        var start = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var end = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        for (User user : userRepository.findAll()) {
            if (billingRepository.findByUserAndMonth(user, month.toString()).isPresent()) {
                continue;
            }
            var apis = apiProjectRepository.findByUser(user);
            var apiKeys = apiKeyRepository.findByApiIn(apis).stream()
                    .map(apiKey -> apiKey.getKeyValue())
                    .toList();
            long total = apiKeys.isEmpty() ? 0 : usageLogRepository.countByApiKeyInAndTimestampBetween(apiKeys, start, end);
            long billable = Math.max(0, total - FREE_REQUESTS);
            BigDecimal units = BigDecimal.valueOf(billable).divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING);
            Billing bill = Billing.builder()
                    .user(user)
                    .month(month.toString())
                    .totalRequests(total)
                    .amount(units.multiply(PRICE_PER_100))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
            billingRepository.save(bill);
        }
    }

    private BillingResponse toResponse(Billing billing) {
        return new BillingResponse(billing.getId(), billing.getMonth(), billing.getTotalRequests(),
                billing.getAmount(), billing.getPaymentStatus());
    }
}
