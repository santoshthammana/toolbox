/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package eu.amidst.flinklink.core.converter;


import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.utils.BayesianNetworkSampler;
import junit.framework.TestCase;
import org.apache.flink.api.java.DataSet;

/**
 * Created by andresmasegosa on 2/9/15.
 */
public class DataStreamToDataSetTest extends TestCase {

    public void test1() throws Exception{
        BayesianNetwork asianet = BayesianNetworkLoader.loadFromFile("networks/asia.bn");
        BayesianNetworkSampler sampler = new BayesianNetworkSampler(asianet);
        sampler.setSeed(0);
        //Load the sampled data
        DataStream<DataInstance> data = sampler.sampleToDataStream(10);

        DataSet<DataInstance> dataSet = DataStreamToDataSet.toDataSet(data);

        dataSet.print();
    }

}