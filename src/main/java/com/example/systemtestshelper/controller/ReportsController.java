package com.example.systemtestshelper.controller;

import com.example.systemtestshelper.domains.reports.ReportGenerationExecutor;
import com.example.systemtestshelper.domains.reports.ReportXmlParser;
import com.example.systemtestshelper.domains.reports.XPackage;
import com.example.systemtestshelper.domains.reports.XReport;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/v1/systemtests")
public class ReportsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsController.class);
    private static final String REPORTS_LOCATION = "%s/logs1";
    private static final String REPORT_LOCATION = REPORTS_LOCATION+"/%s/report.xml";
    private static final String REPORT_ID = "report_id";
    private static final String NO_REPORTS = "no_reports_d";
    private static final String REPORT = "report";
    private static final String DETAILS_XML = "report_details_d";
    //@Value("${APP_HOME:}")
    private String appHome = "/Users/ab732698/github_ripo/develop/system-tests-helper-service";
    //@Value("${SYSTEM_TESTS_COVERAGE_LOCATION:}")
    private String coverageLocation ="/Users/ab732698/Downloads/systemtests/UAT_Phase1";

    private SAXParser saxParser;
    Map<String, XReport> cachedReports = new HashMap<>();

    @PostMapping(value = "/reports/generate")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public ResponseEntity<String> generateReport(@RequestHeader final HttpHeaders headers,
                                                     @RequestParam(value = "type", defaultValue = "xml", required = false) final String type) {
        //Clear cached reports before generating new
        cachedReports.clear();

        //Execute command for each service
        List<String> reportIds = extractReportIds();

        //Generate reports in executor
        try {
            ReportGenerationExecutor executor = new ReportGenerationExecutor(appHome, coverageLocation, reportIds, type);
            executor.generateReport();
        }catch (final InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for tasks to complete.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("Exception while generating reports in batches {}", e.getMessage(), e);
        }

        return new ResponseEntity<>("Report generation is in progress", HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/reports/prepareview")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> prepareReportView(@RequestHeader final HttpHeaders headers) {
        //Clear cached reports before generating new
        cachedReports.clear();
        //Parse and fill cache
        fillReports();

        return new ResponseEntity<>("Prepared", HttpStatus.OK);
    }

    @GetMapping(value = "/reports")
    @ResponseStatus(value = HttpStatus.OK)
    public String viewReports(@RequestHeader final HttpHeaders headers, Model model){
        fillReports();
        List<XReport> reports = new ArrayList<>(cachedReports.values());
        model.addAttribute("reports", reports);
        return "reports_d";
    }

    @GetMapping(value = "reports/{reportId}")
    public String viewReport(@RequestHeader final HttpHeaders headers, @PathVariable String reportId,
                             Model model) {
        model.addAttribute(REPORT_ID, reportId);
        XReport report = getReportsData(reportId);
        if(report == null){
            return NO_REPORTS;
        }

        model.addAttribute(REPORT, report);
        return DETAILS_XML;
    }

    @GetMapping(value = "reports/{reportId}/{packageId}")
    public String viewReport(@RequestHeader final HttpHeaders headers, @PathVariable String reportId,
                                  @PathVariable String packageId, Model model) {
        model.addAttribute(REPORT_ID, packageId);
        XReport report = getReportsData(reportId);
        if(report != null && report.getList().containsKey(packageId)){
            model.addAttribute(REPORT, report.getList().get(packageId));
            return DETAILS_XML;
        }

        return NO_REPORTS;
    }

    @GetMapping(value = "reports/{reportId}/{packageId}/{classId}")
    public String viewReport(@RequestHeader final HttpHeaders headers, @PathVariable String reportId,
                                  @PathVariable String packageId, @PathVariable String classId, Model model) {
        model.addAttribute(REPORT_ID, classId);
        XReport report = getReportsData(reportId);
        if(report != null && report.getList().containsKey(packageId)){
            XPackage pk = report.getList().get(packageId);
            if(pk.getList().containsKey(classId)) {
                model.addAttribute(REPORT, pk.getList().get(classId));
                return DETAILS_XML;
            }
        }

        return NO_REPORTS;
    }

    private XReport getReportsData(String reportId){
        LOGGER.info("Report ID {}", reportId);
        if(cachedReports.containsKey(reportId)){
            return cachedReports.get(reportId);
        }

        String reportLocation = String.format(REPORT_LOCATION, coverageLocation, reportId);
        try(FileInputStream fileInputStream = new FileInputStream(reportLocation)) {
            initSaxParser();
            ReportXmlParser parser = new ReportXmlParser(reportId);
            saxParser.parse(fileInputStream, parser);
            XReport report = parser.getReport();
            if(report != null){
                cachedReports.put(reportId, report);
                return report;
            }
        }catch (Exception e){
            LOGGER.error("Exception while parsing report reports {}", e.getMessage(), e);
        }
        return null;
    }

    private void initSaxParser() throws SAXException, ParserConfigurationException {
        if(saxParser == null){
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            //Adding these properties will fix security issue with the Parser
            saxParserFactory.setXIncludeAware(false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser = saxParserFactory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        }
    }

    private void fillReports(){
        if(MapUtils.isEmpty(cachedReports)){
            String reportsLocation = String.format(REPORTS_LOCATION, coverageLocation);
            File directoryPath = new File(reportsLocation);
            if(directoryPath.exists()) {
                File[] files = directoryPath.listFiles();
                assert files != null;
                for (File f : files) {
                    if (f.isDirectory()) {
                        getReportsData(f.getName());
                    }
                }
            }else {
                throw new IllegalArgumentException("Invalid coverage Path "+reportsLocation);
            }
        }
    }

    private List<String> extractReportIds() {
        List<String> reportIds = new ArrayList<>();
        String reportsLocation = String.format(REPORTS_LOCATION, coverageLocation);
        File directoryPath = new File(reportsLocation);
        if(directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isDirectory()) {
                    reportIds.add(f.getName());
                }
            }
        }else {
            throw new IllegalArgumentException("Invalid coverage Path "+reportsLocation);
        }
        return reportIds;
    }
}
