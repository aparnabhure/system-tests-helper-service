package com.example.systemtestshelper;

import com.example.systemtestshelper.domains.reports.ReportXmlParser;
import com.example.systemtestshelper.domains.reports.XReport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileInputStream;

@EnableScheduling
@SpringBootApplication
public class SystemTestsHelperServiceApplication {

    //@Value("${SYSTEM_TESTS_COVERAGE_LOCATION:}")
    private String coverageLocation ="/Users/ab732698/Downloads/systemtests/test";
    private String logsFolder = "logs";

    private static final String REPORTS_LOCATION = "%s/%s";

    public static void main(String[] args) {
        SpringApplication.run(SystemTestsHelperServiceApplication.class, args);
    }

    //@Scheduled(initialDelay = 2000, fixedDelay = 30000)
    public void coverageFolderWatcher(){
        System.out.println("In coverageFolderWatcher");
        String reportsLocation = String.format(REPORTS_LOCATION, coverageLocation, logsFolder);
        File directoryPath = new File(reportsLocation);
        if(directoryPath.exists()) {
            File[] files = directoryPath.listFiles();
            if(files == null){
                System.out.println("No service folder present yet");
                return;
            }
            //For each directory/service folder
            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println(f.getName());
                    File[] filesInDirectory = f.listFiles();
                    if(filesInDirectory != null) {
                        for(File df:filesInDirectory){
                            if(df.isFile() && df.getName().equals("jacoco.exec")){
                                long bytes = df.length();
                                long kilobytes = (bytes / 1024);
                                System.out.println(f.getName()+"/"+df.getName()+" ::"+kilobytes);
                            }
                        }
                        //TODO:: Check if this folder has jacoco.exec file, if has then validate the size of the file, if it is greater than 0 then upload to the bucket against the folder
                    }
                }
            }
        }else {
            throw new IllegalArgumentException("Invalid coverage Path "+reportsLocation);
        }
    }

}
