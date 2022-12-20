package com.example.systemtestshelper.domains.reports;

import lombok.Data;

@Data
public class XCoverage {
    int missed;
    int covered;
    int total;
}
