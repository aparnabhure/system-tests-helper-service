package com.example.systemtestshelper;

import com.example.systemtestshelper.domains.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        try{
            convertCustomers("/Users/ab732698/github_ripo/develop/DataStructure/app_isolation_v1.csv");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void convertCustomers(final String filePath) throws JsonProcessingException {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                Customer customer = new Customer(columns[1], columns[2]);
                customers.add(customer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(new ObjectMapper().writeValueAsString(customers));
    }
}
