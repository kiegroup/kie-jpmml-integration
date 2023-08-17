/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.jpmml;

import java.math.BigDecimal;
import java.util.Map;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.impl.CompositeTypeImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.SimpleTypeImpl;
import org.kie.dmn.core.pmml.DMNImportPMMLInfo;
import org.kie.dmn.core.pmml.DMNPMMLModelInfo;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class DMNInvokingjPMMLTest {

    public DMNInvokingjPMMLTest() {
        super();
    }

    public static final Logger LOG = LoggerFactory.getLogger(DMNInvokingjPMMLTest.class);

    @Test
    public void testInvokeIris() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat( dmnModel).isNotNull();
        assertThat( dmnModel.hasErrors())
                .as( DMNRuntimeUtil.formatMessages( dmnModel.getMessages() ))
                .isFalse();

        final DMNContext emptyContext = DMNFactory.newContext();

        checkInvokeIris(runtime, dmnModel, emptyContext);
    }

    private void checkInvokeIris(final DMNRuntime runtime, final DMNModel dmnModel, final DMNContext emptyContext) {
        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(dmnResult.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()))
                .isFalse();

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision")).isEqualTo("Iris-versicolor");
    }

    @Test
    public void testInvokeIris_in1_wrong() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris_in1.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat(dmnModel).isNotNull();
        assertThat(dmnModel.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()))
                .isFalse();

        final DMNContext context = DMNFactory.newContext();
        context.set("in1", 99);

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        LOG.debug("{}", dmnResult);
        assertThat(dmnResult.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()))
                .isTrue();
        assertThat(dmnResult.getMessages()).anyMatch(m -> m.getSourceId().equals("in1")); // ... 'in1': the dependency value '99' is not allowed by the declared type (DMNType{ iris : sepal_length })
    }

    @Test
    public void testInvokeIris_in1_ok() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris_in1.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat(dmnModel).isNotNull();
        assertThat(dmnModel.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()))
                .isFalse();

        final DMNContext context = DMNFactory.newContext();
        context.set("in1", 4.3);

        checkInvokeIris(runtime, dmnModel, context);
    }

    @Test
    public void testDummyInteger() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("dummy_integer.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "dummy_integer.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_d9065b95-bc37-41dc-8566-8191af7b7867", "Drawing 1");
        assertThat(dmnModel).isNotNull();
        assertThat(dmnModel.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()))
                .isFalse();

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(dmnResult.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()))
                .isFalse();

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("hardcoded")).isEqualTo(new BigDecimal(3));
    }

    @Test
    public void testMultipleOutputs() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("invoke_iris_KNN.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris_KNN.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_a76cdc83-83b1-4f9c-8cf8-5a0179e776d5", "Drawing 1");
        assertThat(dmnModel).isNotNull();
        assertThat(dmnModel.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()))
                .isFalse();

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(dmnResult.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()))
                .isFalse();

        // > iris[150,]
        //     Sepal.Length Sepal.Width Petal.Length Petal.Width   Species  ID
        // 150          5.9           3          5.1         1.8 virginica 150
        final DMNContext result = dmnResult.getContext();
        @SuppressWarnings("unchecked")
        Map<String, Object> resultOfHardcoded = (Map<String, Object>) result.get("hardcoded");
        assertThat(resultOfHardcoded).hasSizeGreaterThan(1);
        assertThat(resultOfHardcoded).containsEntry("Predicted_Species", "virginica");
        assertThat(resultOfHardcoded).containsKey("Predicted_Petal.Width");
        assertThat((BigDecimal) resultOfHardcoded.get("Predicted_Petal.Width"))
                .isCloseTo(new BigDecimal("1.9333333333333336"), Offset.offset(new BigDecimal("0.1")));
        // no special interest to check the other output fields as the above are the user-facing most interesting ones.

        // additional import info.
        Map<String, DMNImportPMMLInfo> pmmlImportInfo = ((DMNModelImpl) dmnModel).getPmmlImportInfo();
        assertThat(pmmlImportInfo).hasSize(1);
        DMNImportPMMLInfo p0 = pmmlImportInfo.values().iterator().next();
        assertThat(p0.getImportName()).isEqualTo("test20190907");
        assertThat(p0.getModels()).hasSize(1);
        DMNPMMLModelInfo m0 = p0.getModels().iterator().next();
        assertThat(m0.getName()).isEqualTo("kNN_model");

        Map<String, DMNType> outputFields = m0.getOutputFields();
        CompositeTypeImpl output = (CompositeTypeImpl)outputFields.get("kNN_model");
        assertThat("test20190907").isEqualTo(output.getNamespace());

        Map<String, DMNType> fields = output.getFields();
        SimpleTypeImpl out1 = (SimpleTypeImpl)fields.get("Predicted_Species");
        assertThat("test20190907").isEqualTo(out1.getNamespace());
        assertThat(BuiltInType.STRING).isEqualTo(out1.getFeelType());

        SimpleTypeImpl out2 = (SimpleTypeImpl)fields.get("Predicted_Petal.Width");
        assertThat("test20190907").isEqualTo(out2.getNamespace());
        assertThat(BuiltInType.NUMBER).isEqualTo(out2.getFeelType());

        SimpleTypeImpl out3 = (SimpleTypeImpl)fields.get("neighbor1");
        assertThat("test20190907").isEqualTo(out3.getNamespace());
        assertThat(BuiltInType.STRING).isEqualTo(out3.getFeelType());

        SimpleTypeImpl out4 = (SimpleTypeImpl)fields.get("neighbor2");
        assertThat("test20190907").isEqualTo(out4.getNamespace());
        assertThat(BuiltInType.STRING).isEqualTo(out4.getFeelType());

        SimpleTypeImpl out5 = (SimpleTypeImpl)fields.get("neighbor3");
        assertThat("test20190907").isEqualTo(out5.getNamespace());
        assertThat(BuiltInType.STRING).isEqualTo(out5.getFeelType());
    }

    @Test
    public void testMultipleOutputsNoModelName() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("invoke_iris_KNN_noModelName.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris_KNN_noModelName.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_a76cdc83-83b1-4f9c-8cf8-5a0179e776d5", "Drawing 1");
        assertThat(dmnModel).isNotNull();
        assertThat(dmnModel.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()))
                .isFalse();

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(dmnResult.hasErrors())
                .as(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()))
                .isFalse();

        // > iris[150,]
        //     Sepal.Length Sepal.Width Petal.Length Petal.Width   Species  ID
        // 150          5.9           3          5.1         1.8 virginica 150
        final DMNContext result = dmnResult.getContext();
        @SuppressWarnings("unchecked")
        Map<String, Object> resultOfHardcoded = (Map<String, Object>) result.get("hardcoded");
        assertThat(resultOfHardcoded.size()).isGreaterThan(1);
        assertThat(resultOfHardcoded).containsEntry("Predicted_Species", "virginica");
        assertThat(resultOfHardcoded).containsKey("Predicted_Petal.Width");
        assertThat((BigDecimal) resultOfHardcoded.get("Predicted_Petal.Width"))
                .isCloseTo(new BigDecimal("1.9333333333333336"), Offset.offset(new BigDecimal("0.1")));
        // no special interest to check the other output fields as the above are the user-facing most interesting ones.

        // additional import info.
        Map<String, DMNImportPMMLInfo> pmmlImportInfo = ((DMNModelImpl) dmnModel).getPmmlImportInfo();
        assertThat(pmmlImportInfo.keySet()).hasSize(1);
        DMNImportPMMLInfo p0 = pmmlImportInfo.values().iterator().next();
        assertThat(p0.getImportName()).isEqualTo("test20190907");
        assertThat(p0.getModels()).hasSize(1);
        DMNPMMLModelInfo m0 = p0.getModels().iterator().next();
        assertThat(m0.getName()).isNull();

        Map<String, DMNType> outputFields = m0.getOutputFields();

        SimpleTypeImpl out1 = (SimpleTypeImpl)outputFields.get("Predicted_Species");
        assertThat(BuiltInType.UNKNOWN).isEqualTo(out1.getFeelType());

        SimpleTypeImpl out2 = (SimpleTypeImpl)outputFields.get("Predicted_Petal.Width");
        assertThat(BuiltInType.UNKNOWN).isEqualTo(out2.getFeelType());

        SimpleTypeImpl out3 = (SimpleTypeImpl)outputFields.get("neighbor1");
        assertThat(BuiltInType.UNKNOWN).isEqualTo(out3.getFeelType());

        SimpleTypeImpl out4 = (SimpleTypeImpl)outputFields.get("neighbor2");
        assertThat(BuiltInType.UNKNOWN).isEqualTo(out4.getFeelType());

        SimpleTypeImpl out5 = (SimpleTypeImpl)outputFields.get("neighbor3");
        assertThat(BuiltInType.UNKNOWN).isEqualTo(out5.getFeelType());
    }
}
