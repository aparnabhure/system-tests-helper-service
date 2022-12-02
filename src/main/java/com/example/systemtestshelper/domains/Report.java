package com.example.systemtestshelper.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Report {
    String packageName;
    String className;
    int instructionTotal;
    int instructionCovered;
    int branchTotal;
    int branchCovered;
    int lineTotal;
    int lineCovered;
    int complexityTotal;
    int complexityCovered;
    int methodTotal;
    int methodCovered;
}
