package com.example.systemtestshelper.domains.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class XReport {
    String name;
    Map<String, XPackage> list;
    XCoverage instructions = new XCoverage();
    XCoverage branches = new XCoverage();
    XCoverage lines = new XCoverage();
    XCoverage complexities = new XCoverage();
    XCoverage methods = new XCoverage();
    XCoverage classes = new XCoverage();
}
