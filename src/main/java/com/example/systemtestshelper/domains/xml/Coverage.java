package com.example.systemtestshelper.domains.xml;

import lombok.Data;

@Data
public class Coverage {
    int missed;
    int covered;
    int total;
}
