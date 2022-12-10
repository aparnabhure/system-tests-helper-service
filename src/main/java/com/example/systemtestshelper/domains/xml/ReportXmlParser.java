package com.example.systemtestshelper.domains.xml;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportXmlParser extends DefaultHandler {
    private static final String REPORT = "report";
    private static final String PACKAGE = "package";
    private static final String CLASS = "class";
    private static final String METHOD = "method";
    private static final String COUNTER = "counter";
    private static final String NAME = "name";
    private static final String SOURCE_FILE_NAME = "sourcefilename";
    private static final String DESC = "desc";
    private static final String LINE = "line";
    private static final String TYPE = "type";
    private static final String MISSED = "missed";
    private static final String COVERED = "covered";
    public static final String INSTRUCTION = "INSTRUCTION";
    public static final String BRANCH = "BRANCH";
    public static final String LINE1 = "LINE";
    public static final String COMPLEXITY = "COMPLEXITY";
    public static final String METHOD1 = "METHOD";
    public static final String CLASS1 = "CLASS";

    Map<String, Package> packages = new HashMap<>();
    Map<String, Class> classes;
    Map<String, Method> methods;
    Map<String, Coverage> coverageMap;
    Report aReport;
    Package aPackage;
    Class aClass;
    Method aMethod;

    public ReportXmlParser(String reportName){
        aReport = new Report();
        aReport.setName(reportName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName){
            case PACKAGE:
                classes = new HashMap<>();
                aPackage = new Package();
                String name = attributes.getValue(NAME);
                name = name.replace("/", ".");
                aPackage.setName(name);
                break;
            case CLASS:
                methods = new HashMap<>();
                aClass = new Class();
                String className = attributes.getValue(NAME);
                int index = className.lastIndexOf("/");
                if(index != -1) {
                    className = className.substring(index+1);
                }
                aClass.setName(className);
                aClass.setSourceFileName(attributes.getValue(SOURCE_FILE_NAME));
                break;
            case METHOD:
                aMethod = new Method();
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
                    methodName = aClass.getName();
                    methodName+=params.toString();
                }else if(methodName.equals("<clinit>")){
                    methodName = "static{...}";
                }else{
                    methodName+=params.toString();
                }
                aMethod.setName(methodName);

                aMethod.setDesc(attributes.getValue(DESC));
                if(StringUtils.isNotEmpty(attributes.getValue(LINE))) {
                    aMethod.setLine(Integer.parseInt(attributes.getValue(LINE)));
                }
                break;
            case COUNTER:
                if(coverageMap == null){
                    coverageMap = new HashMap<>();
                }
                Coverage coverage = new Coverage();
                coverage.setMissed(Integer.parseInt(attributes.getValue(MISSED)));
                coverage.setCovered(Integer.parseInt(attributes.getValue(COVERED)));
                coverage.setTotal(coverage.getMissed()+coverage.getCovered());
                coverageMap.put(attributes.getValue(TYPE), coverage);
                break;
            default:
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName){
            case METHOD:
                if(coverageMap != null) {
                    aMethod.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new Coverage()));
                    aMethod.setBranches(coverageMap.getOrDefault(BRANCH, new Coverage()));
                    aMethod.setLines(coverageMap.getOrDefault(LINE1, new Coverage()));
                    aMethod.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new Coverage()));
                    aMethod.setMethods(coverageMap.getOrDefault(METHOD1, new Coverage()));
                    aMethod.setClasses(coverageMap.getOrDefault(CLASS1, new Coverage()));
                }
                methods.put(aMethod.getName(), aMethod);
                coverageMap = null;
                break;
            case CLASS:
                if(MapUtils.isNotEmpty(methods)) {
                    aClass.setUri(aReport.getName()+"/"+aPackage.getName()+"/"+aClass.getName());
                    aClass.setList(methods);
                    if (coverageMap != null) {
                        aClass.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new Coverage()));
                        aClass.setBranches(coverageMap.getOrDefault(BRANCH, new Coverage()));
                        aClass.setLines(coverageMap.getOrDefault(LINE1, new Coverage()));
                        aClass.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new Coverage()));
                        aClass.setMethods(coverageMap.getOrDefault(METHOD1, new Coverage()));
                        aClass.setClasses(coverageMap.getOrDefault(CLASS1, new Coverage()));
                    }
                    classes.put(aClass.getName(), aClass);
                }
                coverageMap = null;
                break;
            case PACKAGE:
                aPackage.setUri(aReport.getName()+"/"+aPackage.getName());
                aPackage.setList(classes);
                if(coverageMap != null) {
                    aPackage.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new Coverage()));
                    aPackage.setBranches(coverageMap.getOrDefault(BRANCH, new Coverage()));
                    aPackage.setLines(coverageMap.getOrDefault(LINE1, new Coverage()));
                    aPackage.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new Coverage()));
                    aPackage.setMethods(coverageMap.getOrDefault(METHOD1, new Coverage()));
                    aPackage.setClasses(coverageMap.getOrDefault(CLASS1, new Coverage()));
                }
                packages.put(aPackage.getName(), aPackage);
                coverageMap = null;
                break;
            case REPORT:
                aReport.setList(packages);
                if(coverageMap != null) {
                    aReport.setInstructions(coverageMap.getOrDefault(INSTRUCTION, new Coverage()));
                    aReport.setBranches(coverageMap.getOrDefault(BRANCH, new Coverage()));
                    aReport.setLines(coverageMap.getOrDefault(LINE1, new Coverage()));
                    aReport.setComplexities(coverageMap.getOrDefault(COMPLEXITY, new Coverage()));
                    aReport.setMethods(coverageMap.getOrDefault(METHOD1, new Coverage()));
                    aReport.setClasses(coverageMap.getOrDefault(CLASS1, new Coverage()));
                }
                coverageMap = null;
                break;
            default:
        }
    }

    public Report getReport(){
        return aReport;
    }

}
