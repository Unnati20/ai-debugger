package com.example.aidebugger.config;

import com.example.aidebugger.repository.DocumentRepository;
import com.example.aidebugger.repository.IncidentRecordRepository;
import com.example.aidebugger.service.IngestionService;
import com.example.aidebugger.service.IncidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final SampleDataProperties properties;
    private final IncidentService incidentService;
    private final IngestionService ingestionService;
    private final IncidentRecordRepository incidentRecordRepository;
    private final DocumentRepository documentRepository;

    public SampleDataLoader(
            SampleDataProperties properties,
            IncidentService incidentService,
            IngestionService ingestionService,
            IncidentRecordRepository incidentRecordRepository,
            DocumentRepository documentRepository
    ) {
        this.properties = properties;
        this.incidentService = incidentService;
        this.ingestionService = ingestionService;
        this.incidentRecordRepository = incidentRecordRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        if (properties.isSkipIfDataExists()
                && (incidentRecordRepository.count() > 0 || documentRepository.count() > 0)) {
            log.info("Sample data loading skipped because data already exists.");
            return;
        }

        List<String> paymentLogs = withDefaults(
                properties.getPaymentFailureLogs(),
                List.of(
                        "PaymentServiceError: gateway timeout while charging customer orderId=ORD-10231",
                        "Payment authorization failed for orderId=ORD-10988 with code=INSUFFICIENT_FUNDS",
                        "Stripe API failure: 502 Bad Gateway during capture for transactionId=TXN-55321"
                )
        );

        List<String> npeLogs = withDefaults(
                properties.getNullPointerLogs(),
                List.of(
                        "NullPointerException at com.example.payment.CheckoutService.process(CheckoutService.java:142)",
                        "NullPointerException at com.example.user.ProfileService.getPreferences(ProfileService.java:67)",
                        "NullPointerException at com.example.order.InvoiceService.generate(InvoiceService.java:219)"
                )
        );

        List<String> runbooks = withDefaults(
                properties.getRunbookSteps(),
                List.of(
                        "Runbook: Payment service failure\n1) Check gateway status page.\n2) Validate API keys and webhook secrets.\n3) Replay failed payment events.",
                        "Runbook: NullPointerException triage\n1) Capture full stack trace.\n2) Identify null object source.\n3) Add null guard and regression test.",
                        "Runbook: Incident response\n1) Create incident ticket.\n2) Notify on-call engineer.\n3) Track mitigation and postmortem."
                )
        );

        paymentLogs.forEach(logLine -> incidentService.createIncident("payment-log", logLine));
        npeLogs.forEach(logLine -> incidentService.createIncident("npe-log", logLine));

        for (String runbook : runbooks) {
            try {
                ingestionService.ingest(runbook);
            } catch (Exception ex) {
                log.warn("Runbook ingestion skipped for one entry: {}", ex.getMessage());
            }
        }

        log.info("Sample data loaded: paymentLogs={}, npeLogs={}, runbooks={}",
                paymentLogs.size(), npeLogs.size(), runbooks.size());
    }

    private List<String> withDefaults(List<String> configuredValues, List<String> defaults) {
        if (configuredValues == null || configuredValues.isEmpty()) {
            return defaults;
        }
        return configuredValues;
    }
}
