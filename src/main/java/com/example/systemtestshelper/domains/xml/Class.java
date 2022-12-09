package com.example.systemtestshelper.domains.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Class {
    String name;
    String sourceFileName;
    List<Method> methodList;
    Coverage instructions = new Coverage();
    Coverage branches = new Coverage();
    Coverage lines = new Coverage();
    Coverage complexities = new Coverage();
    Coverage methods = new Coverage();
    Coverage classes = new Coverage();
}
