package com.example.systemtestshelper.domains.reports;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class ReportXmlParser extends DefaultHandler {
    private static final String REPORT = "report";
    private static final String PACKAGE = "package";
    private static final String CLASS = "class";
    private static final String METHOD = "method";
    private static final String COUNTER = "counter";
    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final String TYPE = "type";
    private static final String MISSED = "missed";
    private static final String COVERED = "covered";
    public static final String INSTRUCTION = "INSTRUCTION";
    public static final String BRANCH = "BRANCH";
    public static final String LINE1 = "LINE";
    public static final String COMPLEXITY = "COMPLEXITY";
    public static final String METHOD1 = "METHOD";
    public static final String CLASS1 = "CLASS";

    Map<String, XPackage> packages = new HashMap<>();
    Map<String, XClass> classes;
    Map<String, XMethod> methods;
    Map<String, XCoverage> coverageMap;
    XReport aXReport;
    XPackage aXPackage;
    XClass aXClass;
    XMethod aXMethod;

    public ReportXmlParser(String reportName){
        aXReport = new XReport();
        aXReport.setName(reportName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName){
            case PACKAGE:
                classes = new HashMap<>();
                aXPackage = new XPackage();
                String name = attributes.getValue(NAME);
                name = name.replace("/", ".");
                aXPackage.setName(name);
                break;
            case CLASS:
                methods = new HashMap<>();
                aXClass = new XClass();
                String className = attributes.getValue(NAME);
                int index = className.lastIndexOf("/");
                if(index != -1) {
                    className = className.substring(index+1);
                }
                aXClass.setName(className);
                break;
            case METHOD:
                aXMethod = new XMethod();
                aXMethod.setName(getMethodName(attributes));
                break;
            case COUNTER:
                if(coverageMap == null){
                    coverageMap = new HashMap<>();
                }
                XCoverage coverage = new XCoverage();
                coverage.setMissed(Integer.parseInt(attributes.getValue(MISSED)));
                coverage.setCovered(Integer.parseInt(attributes.getValue(COVERED)));
                coverage.setTotal(coverage.getMissed()+coverage.getCovered());
                coverageMap.put(attributes.getValue(TYPE), coverage);
                break;
            default:
        }
    }

    private String getMethodName(Attributes attributes) {
        StringBuilder params = new StringBuilder("(");
        String desc = attributes.getValue(DESC);
        desc = StringUtils.substringBetween(desc, "(", ")");
        if(StringUtils.isNotEmpty(desc)){
            String[] methodParams = desc.split(";");
            int n = methodParams.length;
            for(int i=0; i< n; i++){
                String p = methodParams[i];
                params.append(p.substring(p.lastIndexOf("/")+1));
                if(i<n-1){
                    params.append(",");
                }
            }
        }
        params.append(")");

        String methodName = attributes.getValue(NAME);
        if(methodName.equals("<init>")){
            methodName = aXClass.getName();
            methodName+=params.toString();
        }else if(methodName.equals("<clinit>")){
            methodName = "static{...}";
        }else{
            methodName+=params.toString();
        }
        return methodName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName){
            case METHOD:
                if(coverageMap != null) {
                    aXMethod.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new XCoverage()));
                    aXMethod.setBranches(coverageMap.getOrDefault(BRANCH, new XCoverage()));
                    aXMethod.setLines(coverageMap.getOrDefault(LINE1, new XCoverage()));
                    aXMethod.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new XCoverage()));
                    aXMethod.setMethods(coverageMap.getOrDefault(METHOD1, new XCoverage()));
                    aXMethod.setClasses(coverageMap.getOrDefault(CLASS1, new XCoverage()));
                }
                methods.put(aXMethod.getName(), aXMethod);
                coverageMap = null;
                break;
            case CLASS:
                if(MapUtils.isNotEmpty(methods)) {
                    aXClass.setUri(aXReport.getName()+"/"+ aXPackage.getName()+"/"+ aXClass.getName());
                    aXClass.setList(methods);
                    if (coverageMap != null) {
                        aXClass.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new XCoverage()));
                        aXClass.setBranches(coverageMap.getOrDefault(BRANCH, new XCoverage()));
                        aXClass.setLines(coverageMap.getOrDefault(LINE1, new XCoverage()));
                        aXClass.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new XCoverage()));
                        aXClass.setMethods(coverageMap.getOrDefault(METHOD1, new XCoverage()));
                        aXClass.setClasses(coverageMap.getOrDefault(CLASS1, new XCoverage()));
                    }
                    classes.put(aXClass.getName(), aXClass);
                }
                coverageMap = null;
                break;
            case PACKAGE:
                aXPackage.setUri(aXReport.getName()+"/"+ aXPackage.getName());
                aXPackage.setList(classes);
                if(coverageMap != null) {
                    aXPackage.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new XCoverage()));
                    aXPackage.setBranches(coverageMap.getOrDefault(BRANCH, new XCoverage()));
                    aXPackage.setLines(coverageMap.getOrDefault(LINE1, new XCoverage()));
                    aXPackage.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new XCoverage()));
                    aXPackage.setMethods(coverageMap.getOrDefault(METHOD1, new XCoverage()));
                    aXPackage.setClasses(coverageMap.getOrDefault(CLASS1, new XCoverage()));
                }
                packages.put(aXPackage.getName(), aXPackage);
                coverageMap = null;
                break;
            case REPORT:
                aXReport.setList(packages);
                if(coverageMap != null) {
                    aXReport.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new XCoverage()));
                    aXReport.setBranches(coverageMap.getOrDefault(BRANCH, new XCoverage()));
                    aXReport.setLines(coverageMap.getOrDefault(LINE1, new XCoverage()));
                    aXReport.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new XCoverage()));
                    aXReport.setMethods(coverageMap.getOrDefault(METHOD1, new XCoverage()));
                    aXReport.setClasses(coverageMap.getOrDefault(CLASS1, new XCoverage()));
                }
                coverageMap = null;
                break;
            default:
        }
    }

    public XReport getReport(){
        return aXReport;
    }

}
