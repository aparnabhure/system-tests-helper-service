package com.example.systemtestshelper.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.example.systemtestshelper.domains.CoverageReport;
import com.example.systemtestshelper.domains.Report;
import com.example.systemtestshelper.domains.xml.Class;
import com.example.systemtestshelper.domains.xml.Package;
import com.example.systemtestshelper.domains.xml.ReportXmlParser;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import com.example.systemtestshelper.domains.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

@Controller
@RequestMapping(value = "/systemtests")
public class ServiceController {
    @Value("${SERVICE_PATH:}")
    private String servicePath;
    //@Value("${PVC_PATH:}")
    private String pvcPath="/Users/ab732698/Downloads/systemtests/UAT_Phase1/log1";
    private static final String CSV_REPORT_PATH = "%s/report.csv";
    @Value(("${spring.web.resources.static-locations:}"))
    String path;

    private String coverageLocation = "/Users/ab732698/Downloads/systemtests/UAT_Phase1";
    private static final String REPORTS_LOCATION = "%s/log1";
    private static final String REPORT_LOCATION = REPORTS_LOCATION+"/%s/report.xml";
    private static final String SERVICE_LOCATION = "%s/%s";

    AwsConfig awsConfig = new AwsConfig();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

    SAXParser saxParser;

    Map<String, com.example.systemtestshelper.domains.xml.Report> cachedReports = new TreeMap<>();

    @Value("${gcs.bucket.name:}")
    private String reportsUploadBucketName;
    @Value("${gcs.access.key:}")
    private String bucketAccessKey;

    @GetMapping(value = "/download")
    @ResponseBody
    public ResponseEntity<String> download(){
        downloadFromGCS();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private void downloadFromGCS(){
        try{
            GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(bucketAccessKey.getBytes()));

            Storage storage = StorageOptions.newBuilder().setCredentials(googleCredentials).build().getService();

            Page<Blob> blobs = storage.list(reportsUploadBucketName, Storage.BlobListOption.prefix("jacoco/ci/"));

            for (Blob blob : blobs.iterateAll()) {
                String name = blob.getName();
                //System.out.println("**** " + name);
                if(name.endsWith("/jacoco.exec")) {
                    String destFilePath = coverageLocation+"/"+name;
                    System.out.println("**** DestFile " + destFilePath);
                    blob.downloadTo(Paths.get(destFilePath));
                }
            }

            System.out.println("**** ");
            blobs = storage.list(reportsUploadBucketName, Storage.BlobListOption.prefix("jacoco/dev/"));

            for (Blob blob : blobs.iterateAll()) {
                String name = blob.getName();
               // System.out.println("**** " + name);
                if(name.endsWith("/jacoco.exec")) {
                    String destFilePath = coverageLocation+"/"+name;
                    System.out.println("**** DestFile " + destFilePath);
                    blob.downloadTo(Paths.get(destFilePath));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<String> home(){
        cachedReports.clear();
        return ResponseEntity.ok("Home :PVC path: "+ pvcPath + " : service path :"+servicePath +" : resource path "+path);
    }

    @GetMapping(value = "/reports/view")
    @ResponseStatus(value = HttpStatus.OK)
    public String viewReports(@RequestHeader final HttpHeaders headers, Model model){
        fillReports();
        List<com.example.systemtestshelper.domains.xml.Report> reports = new ArrayList<>(cachedReports.values());
        model.addAttribute("reports", reports);
        return "reports_2";
    }

    @GetMapping(value = "/reports/{reportId}")
    public ModelAndView report(@RequestHeader final HttpHeaders headers, @PathVariable String reportId){
        return new ModelAndView(String.format("redirect:/%s/report/index.html", reportId));
    }

    @GetMapping(value = "reports/{reportId}/{packageId}/{classId}")
    public String viewReportInXML(@RequestHeader final HttpHeaders headers, @PathVariable String reportId,
                                  @PathVariable String packageId, @PathVariable String classId, Model model) {
        model.addAttribute("report_id", packageId);
        model.addAttribute("report_name", classId);
        com.example.systemtestshelper.domains.xml.Report report = getReportsData(reportId);
        if(report == null){
            return "no_reports";
        }

        if(report.getList().containsKey(packageId)){
            Package pk = report.getList().get(packageId);
            if(pk.getList().containsKey(classId)) {
                model.addAttribute("report", pk.getList().get(classId));
                return "report_details_2";
            }
        }

        return "no_reports";
    }

    @GetMapping(value = "reports/{reportId}/{packageId}")
    public String viewReportInXML(@RequestHeader final HttpHeaders headers, @PathVariable String reportId,
                                  @PathVariable String packageId, Model model) {
        model.addAttribute("report_id", reportId);
        model.addAttribute("report_name", packageId);
        com.example.systemtestshelper.domains.xml.Report report = getReportsData(reportId);
        if(report == null){
            return "no_reports";
        }

        if(report.getList().containsKey(packageId)){
            model.addAttribute("report", report.getList().get(packageId));
            return "report_details_2";
        }

        return "no_reports";
    }

    @GetMapping(value = "reports/{reportId}/xml")
    public String viewReportInXML(@RequestHeader final HttpHeaders headers, @PathVariable String reportId, Model model) {
        model.addAttribute("report_id", reportId);
        model.addAttribute("report_name", reportId);
        com.example.systemtestshelper.domains.xml.Report report = getReportsData(reportId);
        if(report == null){
            return "no_reports";
        }

        model.addAttribute("report", report);
        return "report_details_2";
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

    private com.example.systemtestshelper.domains.xml.Report getReportsData(String reportId){
        if(cachedReports.containsKey(reportId)){
            return cachedReports.get(reportId);
        }

        String reportLocation = String.format(REPORT_LOCATION, coverageLocation, reportId);
        try(FileInputStream fileInputStream = new FileInputStream(reportLocation)) {
            initSaxParser();
            ReportXmlParser parser = new ReportXmlParser(reportId);
            saxParser.parse(fileInputStream, parser);
            com.example.systemtestshelper.domains.xml.Report report = parser.getReport();
            if(report != null){
                cachedReports.put(reportId, report);
                return report;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

//
//    private com.example.systemtestshelper.domains.xml.Report getReportsData(String reportId){
//        if(cachedReports.containsKey(reportId)){
//            return cachedReports.get(reportId);
//        }
//
//        try(FileInputStream fileInputStream = new FileInputStream(pvcPath+"/"+reportId+"/report.xml")) {
//            initSaxParser();
//            ReportXmlParser parser = new ReportXmlParser(reportId);
//            saxParser.parse(fileInputStream, parser);
//            com.example.systemtestshelper.domains.xml.Report report = parser.getReport();
//            if(report != null){
//                cachedReports.put(reportId, report);
//                return report;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

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

    @GetMapping(value = "/reports/{reportId}/view")
    public String viewReportById(@RequestHeader final HttpHeaders headers, @PathVariable String reportId, Model model) {
        System.out.println(reportId);
        List<Report> reports = new ArrayList<>();
        String filePath = pvcPath+"/"+reportId+"/report.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                String[] columns = line.split(",");
                if(columns[0].equals("GROUP")){
                    continue;
                }

                Report report = new Report();
                report.setPackageName(columns[1]);
                report.setClassName(columns[2]);
                int missed = Integer.parseInt(columns[3]);
                int covered = Integer.parseInt(columns[4]);
                report.setInstructionCovered(covered);
                report.setInstructionTotal(missed+covered);

                missed = Integer.parseInt(columns[5]);
                covered = Integer.parseInt(columns[6]);
                report.setBranchCovered(covered);
                report.setBranchTotal(missed+covered);

                missed = Integer.parseInt(columns[7]);
                covered = Integer.parseInt(columns[8]);
                report.setLineCovered(covered);
                report.setLineTotal(missed+covered);

                missed = Integer.parseInt(columns[9]);
                covered = Integer.parseInt(columns[10]);
                report.setComplexityCovered(covered);
                report.setComplexityTotal(missed+covered);

                missed = Integer.parseInt(columns[11]);
                covered = Integer.parseInt(columns[12]);
                report.setMethodCovered(covered);
                report.setMethodTotal(missed+covered);

                reports.add(report);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("report", new CoverageReport(reportId, filePath));

        model.addAttribute("reports", reports);
        return "report_details";
    }

    @PostMapping("/reports/view/details")
    public String submitForm(@ModelAttribute CoverageReport coverageReport, Model model) {
        model.addAttribute("coverageReport", coverageReport);
        return "report_details";
    }

    @GetMapping("/view")
    public String welcome() {
        return "1a-read-csv";
    }

    @PostMapping(value = "/generate")
    @ResponseBody
    public ResponseEntity<List<String>> generateReport(){

                //Execute command for each service
        List<String> failedReports = new ArrayList<>();
        List<String> reportIds = new ArrayList<>();
        String jarLocation = coverageLocation+"/jars/jacococli.jar";
        String reportsLocation = String.format(REPORTS_LOCATION, coverageLocation);
        File directoryPath = new File(reportsLocation);
        if(directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isDirectory()) {
                    executeCommand("xml", jarLocation, reportsLocation, f.getName(), reportIds, failedReports);
                }
            }
        }else {
            throw new IllegalArgumentException("Invalid coverage Path "+reportsLocation);
        }

        for(String reportId:reportIds){
            getReportsData(reportId);
        }

        return ResponseEntity.ok(failedReports);





//        try {
//            ProcessBuilder builder = new ProcessBuilder();
//            //builder.command("sh", "-c", "ls");
//            //builder.command("sh", "generate_reports.sh");
//            builder.command("java","-jar", "/Users/ab732698/Downloads/jacoco-0.8.8/lib/jacococli.jar", "report")
//            builder.directory(new File("/Users/ab732698/github_ripo/develop/system-tests-helper-service"));
//            Process process = builder.start();
////            StreamGobbler streamGobbler =
////                new StreamGobbler(process.getInputStream(), System.out::println);
////            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
//            int exitCode = process.waitFor();
//            assert exitCode == 0;
//            //future.get(10, TimeUnit.SECONDS);
//        }catch (Exception e){
//            System.out.println("Exception "+e.getMessage());
//            e.printStackTrace();
//        }

//        return new ResponseEntity<>(failedReports, HttpStatus.OK);
    }

    private void executeCommand(String type, String jarLocation, String reportsLocation, String name, List<String> reportIds, List<String> failedReports) {
        try {
            String servicePath = String.format(SERVICE_LOCATION, reportsLocation, name);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("java","-jar",jarLocation, "report", servicePath+"/jacoco.exec", "--classfiles", servicePath+"/target", "--xml", servicePath+"/report_new.xml");
            builder.directory(new File("/Users/ab732698/github_ripo/develop/system-tests-helper-service"));
            builder.start();
            reportIds.add(name);
        }catch (Exception e){
           e.printStackTrace();
            failedReports.add(name);
        }
    }

    @GetMapping(value = "/folders")
    @ResponseBody
    public ResponseEntity<List<String>> getFolderNames(){
        List<String> folders = new ArrayList<>();
        File directoryPath = new File("/Users/ab732698/Downloads/jacoco-0.8.8");
        if(directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isDirectory()) {
                    folders.add(f.getAbsolutePath());
                }
            }
        }else throw new NoSuchElementException();

        return ResponseEntity.ok(folders);
    }

    @PostMapping(value = "/upload")
    @ResponseBody
    public ResponseEntity<String> execute(){
        return ResponseEntity.ok(uploadToAWS());
    }

    private String uploadToAWS(){
        String filePath = pvcPath+"/logs";
        //String filePath = "/Users/ab732698/Downloads/systemtests";
        System.out.println("JaCoCo logs folder path "+filePath);
        return printFileSizeAndUpload(filePath);
    }

    String printFileSizeAndUpload(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return upload(file);
        }
        return "Path does not exists";
    }

    String upload(File file){
        try {
            if(StringUtils.isEmpty(awsConfig.getBucketName())){
                System.out.println("Invalid details to upload");
                return "Invalid details to upload";
            }

            AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());

            AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_1)
                .build();

            TransferManagerBuilder.standard().withS3Client(s3client).build().uploadDirectory(awsConfig.getBucketName(),
                "st_"+dateFormat.format(new Date()), file, true);
            return "Folder uploaded ";
        }catch (Exception e){
            System.out.println("Failed to upload "+e.getMessage());
        }

        return "Failed to upload";
    }

}
