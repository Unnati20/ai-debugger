package com.example.aidebugger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.sample-data")
public class SampleDataProperties {

    private boolean enabled = false;
    private boolean skipIfDataExists = true;
    private List<String> paymentFailureLogs = new ArrayList<>();
    private List<String> nullPointerLogs = new ArrayList<>();
    private List<String> runbookSteps = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSkipIfDataExists() {
        return skipIfDataExists;
    }

    public void setSkipIfDataExists(boolean skipIfDataExists) {
        this.skipIfDataExists = skipIfDataExists;
    }

    public List<String> getPaymentFailureLogs() {
        return paymentFailureLogs;
    }

    public void setPaymentFailureLogs(List<String> paymentFailureLogs) {
        this.paymentFailureLogs = paymentFailureLogs;
    }

    public List<String> getNullPointerLogs() {
        return nullPointerLogs;
    }

    public void setNullPointerLogs(List<String> nullPointerLogs) {
        this.nullPointerLogs = nullPointerLogs;
    }

    public List<String> getRunbookSteps() {
        return runbookSteps;
    }

    public void setRunbookSteps(List<String> runbookSteps) {
        this.runbookSteps = runbookSteps;
    }
}
