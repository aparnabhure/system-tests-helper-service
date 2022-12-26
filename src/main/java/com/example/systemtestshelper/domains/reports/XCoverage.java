package com.example.systemtestshelper.domains.reports;

import lombok.Data;

@Data
public class XCoverage {
    int missed;
    int covered;
    int total;

    public void updateCoverage(XCoverage newCoverage){
        this.missed += newCoverage.getMissed();
        this.covered += newCoverage.getCovered();
        this.total += newCoverage.getTotal();
    }
}
