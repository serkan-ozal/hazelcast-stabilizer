/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.stabilizer.tests;

import com.hazelcast.stabilizer.TestCase;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.hazelcast.stabilizer.Utils.loadProperties;
import static java.lang.String.format;

public class TestSuite implements Serializable {

    private static final long serialVersionUID = 1;

    public static TestSuite loadTestSuite(File file) throws Exception {
        Properties properties = loadProperties(file);

        Map<String, TestCase> testcases = new HashMap<String, TestCase>();
        for (String property : properties.stringPropertyNames()) {
            String value = (String) properties.get(property);
            int indexOfDot = property.indexOf(".");

            String testCaseId = "";
            String field = property;
            if (indexOfDot > -1) {
                testCaseId = property.substring(0, indexOfDot);
                field = property.substring(indexOfDot + 1);
            }

            TestCase testCase = testcases.get(testCaseId);
            if (testCase == null) {
                testCase = new TestCase();
                testCase.id = testCaseId;
                testcases.put(testCaseId, testCase);
            }

            testCase.setProperty(field, value);
        }

        List<String> testcaseIds = new LinkedList<String>(testcases.keySet());
        Collections.sort(testcaseIds);

        TestSuite testSuite = new TestSuite();
        for (String testcaseId : testcaseIds) {
            TestCase testcase = testcases.get(testcaseId);
            if (testcase.getClassname() == null) {
                if ("".equals(testcaseId)) {
                    throw new RuntimeException(format("There is no class set for the in property file [%s]." +
                            "Add class=YourTestClass",
                            file.getAbsolutePath()
                    ));
                } else {
                    throw new RuntimeException(format("There is no class set for test [%s] in property file [%s]." +
                            "Add %s.class=YourTestClass",
                            testcaseId, file.getAbsolutePath(), testcaseId
                    ));
                }
            }
            testSuite.addTest(testcase);
        }
        return testSuite;
    }

    public final String id = "" + System.currentTimeMillis();
    public List<TestCase> testCaseList = new LinkedList<TestCase>();
    public int duration;
    public boolean failFast;
    public Set<Failure.Type> tolerableFailures = Collections.EMPTY_SET;

    public TestCase getTestCase(String testCaseId){
        if(testCaseId == null){
            return null;
        }

        for(TestCase testCase: testCaseList){
            if(testCaseId.equals(testCase.getId())){
                return testCase;
            }
        }

        return null;
    }

    public void addTest(TestCase testCase) {
        testCaseList.add(testCase);
    }

    public int size() {
        return testCaseList.size();
    }

    @Override
    public String toString() {
        return "TestSuite{" +
                "duration=" + duration +
                ", id='" + id + '\'' +
                ", testRecipeList=" + testCaseList +
                ", failFast=" + failFast +
                '}';
    }
}
