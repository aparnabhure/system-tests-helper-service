package com.example.systemtestshelper.domains.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Report {
    String name;
    Map<String, Package> list;
    Coverage instructions = new Coverage();
    Coverage branches = new Coverage();
    Coverage lines = new Coverage();
    Coverage complexities = new Coverage();
    Coverage methods = new Coverage();
    Coverage classes = new Coverage();
}
