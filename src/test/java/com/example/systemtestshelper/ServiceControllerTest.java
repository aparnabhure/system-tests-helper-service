package com.example.systemtestshelper;

import com.example.systemtestshelper.domains.xml.Report;
import com.example.systemtestshelper.domains.xml.ReportXmlParser;
import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;

public class ServiceControllerTest {

    @Test
    public void test(){
        try(FileInputStream fileInputStream = new FileInputStream("/Users/ab732698/Downloads/systemtests/view/logs/app-control-service/report.xml")) {

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            //Adding these properties will fix security issue with the Parser
            saxParserFactory.setXIncludeAware(false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            //saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            ReportXmlParser parser = new ReportXmlParser();
            saxParser.parse(fileInputStream, parser);

            Report report = parser.getReport();
            if(report != null){
                System.out.println(report.getName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
