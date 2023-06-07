package com.example.systemtestshelper;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

@Component
public class CoverageWatcherCommandRunner implements CommandLineRunner {

    //@Value("${SYSTEM_TESTS_COVERAGE_LOCATION:}")
    private String coverageLocation ="/Users/ab732698/Downloads/systemtests/test";
    private String logsFolder = "spring-reactive-demo";

    private static final String REPORTS_LOCATION = "%s/%s";

    @Value("${gcs.bucket.name:}")
    private String reportsUploadBucketName;
    @Value("${gcs.access.key:}")
    private String bucketAccessKey;

    @Override
    public void run(String... args) throws Exception {
        //TODO::Upload everything available in the PVC on service restart just to have data in sync

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(String.format(REPORTS_LOCATION, coverageLocation, logsFolder));
        String basePath = path.toUri().getPath();
        System.out.println(basePath);
        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
           // StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                String folder = basePath+event.context();
                System.out.println(event.kind()+"***** "+ folder);
                File file = new File(folder);
                if(file.isDirectory()){
                    String jfS = file.getAbsolutePath()+"/jacoco.exec";
                    File jf = new File(jfS);
                    if(jf.exists()){
                        System.out.println("****"+jf.getParent());
                        System.out.println("File found "+jf.getName() + jf.getPath());
                        upload(jf, event.context().toString());
                    }
                }
            }
            key.reset();
        }
    }


    private void upload(File file, String folderName){
        try{
            GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(bucketAccessKey.getBytes()));

            Storage storage = StorageOptions.newBuilder().setCredentials(googleCredentials).build().getService();
            BlobId blobId = BlobId.of(reportsUploadBucketName, "jacoco/ci/"+folderName+ "/jacoco.exec");
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.createFrom(blobInfo, new FileInputStream(file));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
