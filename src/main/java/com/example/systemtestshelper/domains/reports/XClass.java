package com.example.systemtestshelper.domains.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class XClass {
    String name;
    Map<String, XMethod> list;
    XCoverage instructions = new XCoverage();
    XCoverage branches = new XCoverage();
    XCoverage lines = new XCoverage();
    XCoverage complexities = new XCoverage();
    XCoverage methods = new XCoverage();
    XCoverage classes = new XCoverage();
    String uri;
}
