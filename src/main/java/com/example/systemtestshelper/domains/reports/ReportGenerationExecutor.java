package com.example.systemtestshelper.domains.reports;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class ReportGenerationExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerationExecutor.class);
    private static final String REPORTS_LOCATION = "%s/logs1";
    private static final String SERVICE_LOCATION = "%s/%s";
    private final String jarLocation;
    private final String reportsLocation;
    Deque<String> executors = new ArrayDeque<>();
    private final Deque<String> serviceEntities;
    List<String> failedReports = new ArrayList<>();
    private String reportType;
    private final String appHome;

        public ReportGenerationExecutor(String appHome, String coverageLocation, List<String> services, String reportType) {
            this.appHome = appHome;
            this.jarLocation = coverageLocation+"/jars/jacococli.jar";
            this.serviceEntities = new ArrayDeque<>();
            this.serviceEntities.addAll(services);
            this.reportType = reportType;
            this.reportsLocation = String.format(REPORTS_LOCATION, coverageLocation);
        }

        public void generateReport() throws InterruptedException, ExecutionException {
            while (true) {
                fillExecutorForServices();
                if (serviceEntities.isEmpty() && executors.isEmpty()) {
                    break;
                }
                execute(executors);
            }
            LOGGER.info("Failed Reports {}", failedReports.size());

        }

        private void fillExecutorForServices() {
            while (!serviceEntities.isEmpty()) {
                executors.push(serviceEntities.pop());
                if (executors.size() == 5) {
                    break;
                }
            }
        }

        private void execute(final Deque<String> executors) throws InterruptedException, ExecutionException {
            ExecutorService executorService = null;

            try {
                executorService = Executors.newFixedThreadPool(executors.size());

                Collection<Callable<String>> callables = new ArrayList<>();
                IntStream.rangeClosed(1, executors.size()).forEach(i ->
                    callables.add(createCallable(executors.pop()))
                );

                /* invoke all supplied Callables */
                List<Future<String>> taskFutureList = executorService.invokeAll(callables);

                // call get on Futures to retrieve result.
                for (Future<String> future : taskFutureList) {
                    String entity = future.get();
                    LOGGER.info("EntityInfo {}: resolve task completed {}", entity, future.isDone());
                }
            } finally {
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
        }

        private Callable<String> createCallable(final String entity) {
            return () -> {
                LOGGER.info("Entity{} to  resolve", entity);
                    try {
                        String servicePath = String.format(SERVICE_LOCATION, reportsLocation, entity);
                        String generatedReport;
                        if (StringUtils.equalsIgnoreCase(reportType, "csv")) {
                            reportType="--csv";
                            generatedReport=servicePath+"/report.csv";
                        } else if (StringUtils.equalsIgnoreCase(reportType, "html")) {
                            reportType="--html";
                            generatedReport=servicePath+"/report";
                        } else {
                            reportType="--xml";
                            generatedReport=servicePath+"/report.xml";
                        }

                        ProcessBuilder builder = new ProcessBuilder();
                        builder.command("java","-jar",jarLocation, "report", servicePath+"/jacoco.exec", "--classfiles", servicePath+"/target", reportType, generatedReport);
                        builder.directory(new File(appHome));
                        builder.start();

                    }catch (Exception e){
                        LOGGER.error("Failed to generate report for {}", entity, e);
                        failedReports.add(entity);
                    }

                return entity;
            };
        }

}
