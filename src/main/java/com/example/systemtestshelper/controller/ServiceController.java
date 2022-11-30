package com.example.systemtestshelper.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.lang3.StringUtils;
import com.example.systemtestshelper.domains.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping(value = "/systemtests")
public class ServiceController {
    @Value("${SERVICE_PATH:}")
    private String servicePath;
    @Value("${PVC_PATH:}")
    private String pvcPath;

    AwsConfig awsConfig = new AwsConfig();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

    @GetMapping
    @ResponseBody
    public ResponseEntity<String> home(){
        return ResponseEntity.ok("Home :PVC path: "+ pvcPath + " : service path :"+servicePath);
    }

    @PostMapping(value = "/generate")
    @ResponseBody
    public ResponseEntity<String> generateReport(){
        try {
            ProcessBuilder builder = new ProcessBuilder();
            //builder.command("sh", "-c", "ls");
            builder.command("sh", "generate_reports.sh");
            builder.directory(new File("/Users/ab732698/github_ripo/develop/system-tests-helper-service"));
            Process process = builder.start();
//            StreamGobbler streamGobbler =
//                new StreamGobbler(process.getInputStream(), System.out::println);
//            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            //future.get(10, TimeUnit.SECONDS);
        }catch (Exception e){
            System.out.println("Exception "+e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok("done");
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
