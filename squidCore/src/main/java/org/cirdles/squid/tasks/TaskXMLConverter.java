/*
 * Copyright 2016 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.squid.tasks;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.cirdles.squid.constants.Squid3Constants;
import org.cirdles.squid.exceptions.SquidException;
import org.cirdles.squid.tasks.expressions.ExpressionSpec;
import org.cirdles.squid.tasks.expressions.ExpressionSpecInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A <code>TaskXMLConverter</code> is used to marshal and unmarshal data between
 * <code>Task</code> and XML files.
 *
 * @author James F. Bowring, javaDocs by Stan Gasque
 * @imports <a href=http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/converters/Converter.html>
 * com.thoughtworks.xstream.converters.Converter</a>
 * @imports <a href=http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/converters/MarshallingContext.html>
 * com.thoughtworks.xstream.converters.MarhsallingContext</a>
 * @imports <a href=http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/converters/UnmarshallingContext.html>
 * com.thoughtworks.xstream.converters.UnmarshallingContext</a>
 * @imports <a href=http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/io/HierarchicalStreamReader.html>
 * com.thoughtworks.xstream.io.HierachicalSreamReader</a>
 * @imports <a href=http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/io/HierarchicalStreamWriter.html>
 * com.thoughtworks.xstream.io.HierarchicalStreamWriter</a>
 */
public class TaskXMLConverter implements Converter {

    /**
     * checks the argument <code>clazz</code> against <code>Task</code>'s
     * <code>Class</code>. Used to ensure that the object about to be
     * marshalled/unmarshalled is of the correct taskType.
     *
     * @param clazz <code>Class</code> of the <code>Object</code> you wish to
     *              convert to/from XML
     * @return <code>boolean</code> - <code>true</code> if <code>clazz</code>
     * matches <code>Task</code>'s <code>Class</code>; else <code>false</code>.
     * @pre argument <code>clazz</code> is a valid <code>Class</code>
     * @post <code>boolean</code> is returned comparing <code>clazz</code>
     * against <code>Task.class</code>
     */
    @Override
    public boolean canConvert(Class clazz) {
        return Task.class.isAssignableFrom(clazz);
    }

    /**
     * writes the argument <code>value</code> to the XML file specified through
     * <code>writer</code>
     *
     * @param value   <code>Task</code> that you wish to write to a file
     * @param writer  stream to write through
     * @param context <code>MarshallingContext</code> used to store generic data
     * @pre <code>value</code> is a valid <code>Task</code>, <code>
     * writer</code> is a valid <code>HierarchicalStreamWriter</code>, and
     * <code>context</code> is a valid <code>MarshallingContext</code>
     * @post <code>value</code> is written to the XML file specified via
     * <code>writer</code>
     */
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context) {

        TaskInterface task = (Task) value;

        writer.startNode("name");
        writer.setValue(task.getName());
        writer.endNode();

        writer.startNode("taskType");
        writer.setValue(task.getTaskType().name());
        writer.endNode();

        writer.startNode("description");
        writer.setValue(task.getDescription());
        writer.endNode();

        writer.startNode("authorName");
        writer.setValue(task.getAuthorName());
        writer.endNode();

        writer.startNode("labName");
        writer.setValue(task.getLabName());
        writer.endNode();

        writer.startNode("provenance");
        writer.setValue(task.getProvenance());
        writer.endNode();

        writer.startNode("dateRevised");
        writer.setValue(String.valueOf(task.getDateRevised()));
        writer.endNode();

        writer.startNode("parentNuclide");
        writer.setValue(task.getParentNuclide());
        writer.endNode();

        writer.startNode("directAltPD");
        writer.setValue(String.valueOf(task.isDirectAltPD()));
        writer.endNode();

        writer.startNode("nominalMasses");
        context.convertAnother(task.getNominalMasses());
        writer.endNode();

        writer.startNode("indexOfBackgroundSpecies");
        writer.setValue(String.valueOf(task.getIndexOfBackgroundSpecies()));
        writer.endNode();

        writer.startNode("ratioNames");
        context.convertAnother(task.getRatioNames());
        writer.endNode();

        writer.startNode("specialSquidFourExpressionsMap");
        context.convertAnother(task.getSpecialSquidFourExpressionsMap());
        writer.endNode();

        writer.startNode("customExpressions");
        context.convertAnother(((Task) task).getTaskCustomExpressionsOrdered());
        writer.endNode();

    }

    /**
     * reads a <code>Task</code> from the XML file specified through
     * <code>reader</code>
     *
     * @param reader  stream to read through
     * @param context <code>UnmarshallingContext</code> used to store generic
     *                data
     * @return <code>Task</code> - <code>Task</code> read from file specified by
     * <code>reader</code>
     * @pre <code>reader</code> leads to a valid <code>Task</code>
     * @post the <code>Task</code> is read from the XML file and returned
     */
    @Override

    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        TaskInterface task = null;
        try {
            task = new Task();
            reader.moveDown();
            task.setName(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setTaskType(Squid3Constants.TaskTypeEnum.valueOf(reader.getValue()));
            reader.moveUp();

            reader.moveDown();
            task.setDescription(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setAuthorName(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setLabName(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setProvenance(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setDateRevised(Long.parseLong(reader.getValue()));
            reader.moveUp();

            reader.moveDown();
            task.setParentNuclide(reader.getValue());
            reader.moveUp();

            reader.moveDown();
            task.setDirectAltPD(Boolean.parseBoolean(reader.getValue()));
            reader.moveUp();

            List<String> nominalMasses = new ArrayList<>();
            reader.moveDown();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                nominalMasses.add(reader.getValue());
                reader.moveUp();
            }
            reader.moveUp();
            task.setNominalMasses(nominalMasses);

            reader.moveDown();
            task.setIndexOfBackgroundSpecies(Integer.parseInt(reader.getValue()));
            reader.moveUp();

            List<String> ratioNames = new ArrayList<>();
            reader.moveDown();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                ratioNames.add(reader.getValue());
                reader.moveUp();
            }
            reader.moveUp();
            task.setRatioNames(ratioNames);

            Map<String, String> specialSquidFourExpressionsMap = new TreeMap<>();
            reader.moveDown();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                reader.moveDown();
                String key = reader.getValue();
                reader.moveUp();
                reader.moveDown();
                String value = reader.getValue();
                specialSquidFourExpressionsMap.put(key, value);
                reader.moveUp();
                reader.moveUp();
            }
            reader.moveUp();
            task.setSpecialSquidFourExpressionsMap(specialSquidFourExpressionsMap);

            // for custom expressions, we retrieve the expressionspec and build expression
            reader.moveDown();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                ExpressionSpecInterface expressionSpec = new ExpressionSpec();
                expressionSpec = (ExpressionSpecInterface) context.convertAnother(expressionSpec, ExpressionSpec.class);
                ((Task) task).makeCustomExpression(expressionSpec);
                reader.moveUp();
            }
            reader.moveUp();
        } catch (SquidException squidException) {

        }

        return task;
    }

}