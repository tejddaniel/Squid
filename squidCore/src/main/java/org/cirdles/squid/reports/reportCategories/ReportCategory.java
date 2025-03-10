/*
 * ReportCategory.java
 *
 * Created on September 9, 2008, 3:05 PM
 *
 *
 * Copyright 2006-2018 James F. Bowring, CIRDLES.org, and Earth-Time.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cirdles.squid.reports.reportCategories;

import org.cirdles.squid.exceptions.SquidException;
import org.cirdles.squid.reports.reportColumns.ReportColumn;
import org.cirdles.squid.reports.reportColumns.ReportColumnInterface;
import org.cirdles.squid.shrimp.ShrimpFraction;
import org.cirdles.squid.shrimp.SquidRatiosModel;
import org.cirdles.squid.tasks.TaskInterface;
import org.cirdles.squid.tasks.evaluationEngines.TaskExpressionEvaluatedPerSpotPerScanModelInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.*;

/**
 * @author James F. Bowring
 */
public class ReportCategory implements org.cirdles.squid.reports.reportCategories.ReportCategoryInterface {

    // private static final long serialVersionUID = 5227409808812622714L;
    // Fields
    private String displayName;
    private int positionIndex;
    private TaskInterface task;
    private ReportColumnInterface[] categoryColumns;
    private boolean visible;
    private boolean legacyData;

    /**
     * Creates a new instance of ReportCategory
     */
    public ReportCategory() {
    }

    /**
     * Creates a new instance of ReportCategory
     *
     * @param displayName
     * @param reportCategorySpecs
     * @param isVisible
     * @param task                the value of task
     */
    public ReportCategory(
            String displayName, String[][] reportCategorySpecs, boolean isVisible, TaskInterface task) throws SquidException {

        this.displayName = displayName;
        this.positionIndex = 0;

        if (reportCategorySpecs[0][6].compareToIgnoreCase("<SPECIES_ARRAY>") == 0) {
            // special case of generation
            String[] isotopeLabels = new String[task.getSquidSpeciesModelList().size()];
            task.getMapOfIndexToMassStationDetails().get(1).getIsotopeLabel();
            for (int i = 0; i < isotopeLabels.length; i++) {
                isotopeLabels[i] = task.getMapOfIndexToMassStationDetails().get(i).getIsotopeLabel();
            }

            String[][] generatedReportCategorySpecs = new String[isotopeLabels.length][];
            for (int i = 0; i < isotopeLabels.length; i++) {
                // Report column order =
                //  displayName1, displayName2, displayName3, displayName4, units, retrieveMethodName, retrieveParameterName, uncertaintyType,
                //     footnoteSpec, visible, useArbitrary? for value, digitcount value, unct visible (if required), description where needed,
                //     needsLead, needsUranium
                generatedReportCategorySpecs[i]
                        = new String[]{
                        "total",
                        isotopeLabels[i],
                        "cts",
                        "/sec",
                        "",
                        reportCategorySpecs[0][5],
                        "<INDEX>" + i,
                        reportCategorySpecs[0][7],
                        reportCategorySpecs[0][8],
                        reportCategorySpecs[0][9],
                        reportCategorySpecs[0][10],
                        reportCategorySpecs[0][11],
                        reportCategorySpecs[0][12], "", "false", "false"
                };
            }
            categoryColumns = new ReportColumn[generatedReportCategorySpecs.length];
            for (int i = 0; i < categoryColumns.length; i++) {
                categoryColumns[i] = setupReportColumn(i, generatedReportCategorySpecs[i]);
            }
        } else if (reportCategorySpecs[0][6].compareToIgnoreCase("<RATIOS_ARRAY>") == 0) {
            // special case of generation
            Iterator<SquidRatiosModel> squidRatiosIterator = ((ShrimpFraction) task.getUnknownSpots().get(0)).getIsotopicRatiosII().iterator();
            List<String[]> generatedReportCategorySpecsList = new ArrayList<>();
            while (squidRatiosIterator.hasNext()) {
                SquidRatiosModel entry = squidRatiosIterator.next();
                if (entry.isActive()) {
                    // Report column order =
                    //  displayName1, displayName2, displayName3, displayName4, units, retrieveMethodName, retrieveParameterName, uncertaintyType,
                    //     footnoteSpec, visible, useArbitrary? for value, digitcount value, unct visible (if required), description where needed,
                    //     needsLead, needsUranium
                    String displayNameNoSpaces = entry.getDisplayNameNoSpaces().substring(0, StrictMath.min(20, entry.getDisplayNameNoSpaces().length()));
                    String[] columnSpec = new String[]{
                            "",
                            "",
                            displayNameNoSpaces.split("/")[0],
                            "/" + displayNameNoSpaces.split("/")[1],
                            "",
                            reportCategorySpecs[0][5],
                            displayNameNoSpaces,
                            reportCategorySpecs[0][7],
                            reportCategorySpecs[0][8],
                            reportCategorySpecs[0][9],
                            reportCategorySpecs[0][10],
                            reportCategorySpecs[0][11],
                            reportCategorySpecs[0][12], "", "false", "false"
                    };

                    generatedReportCategorySpecsList.add(columnSpec);
                }
            }
            categoryColumns = new ReportColumn[generatedReportCategorySpecsList.size()];
            for (int i = 0; i < categoryColumns.length; i++) {
                categoryColumns[i] = setupReportColumn(i, generatedReportCategorySpecsList.get(i));
            }
        } else if (reportCategorySpecs[0][6].compareToIgnoreCase("<RM_EXPRESSIONS_ARRAY>") == 0) {
            // special case of generation
            List<TaskExpressionEvaluatedPerSpotPerScanModelInterface> taskExpressionsEvaluated
                    = task.getUnknownSpots().get(0).getTaskExpressionsForScansEvaluated();
            List<String[]> generatedReportCategorySpecsList = new ArrayList<>();
            for (TaskExpressionEvaluatedPerSpotPerScanModelInterface taskExpressionEval : taskExpressionsEvaluated) {

                if ((!taskExpressionEval.getExpression().isSquidSpecialUPbThExpression())
                        && (taskExpressionEval.getExpression().isSquidSwitchSTReferenceMaterialCalculation())) {
                    // Report column order =
                    //  displayName1, displayName2, displayName3, displayName4, units, retrieveMethodName, retrieveParameterName, uncertaintyType,
                    //     footnoteSpec, visible, useArbitrary? for value, digitcount value, unct visible (if required), description where needed,
                    //     needsLead, needsUranium
                    String expressionName = taskExpressionEval.getExpression().getName().substring(0, StrictMath.min(20, taskExpressionEval.getExpression().getName().length()));
                    String[] columnSpec = new String[]{
                            "",
                            "",
                            "",
                            expressionName,
                            "",
                            reportCategorySpecs[0][5],
                            expressionName,
                            reportCategorySpecs[0][7],
                            reportCategorySpecs[0][8],
                            reportCategorySpecs[0][9],
                            reportCategorySpecs[0][10],
                            reportCategorySpecs[0][11],
                            reportCategorySpecs[0][12], "", "false", "false"
                    };

                    generatedReportCategorySpecsList.add(columnSpec);
                }

            }
            categoryColumns = new ReportColumn[generatedReportCategorySpecsList.size()];
            for (int i = 0; i < categoryColumns.length; i++) {
                categoryColumns[i] = setupReportColumn(i, generatedReportCategorySpecsList.get(i));
            }
        } else {

            List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
            int colIndex = 0;
            boolean has232 = task.getParentNuclide().contains("232");
            boolean isDirect = task.isDirectAltPD();

            for (int specIndex = 0; specIndex < reportCategorySpecs.length; specIndex++) {

                if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<SQUID_TH_U_EQN_NAME>") == 0) {
                    List<ReportColumnInterface> categoryColumnListSQUID_TH_U_EQN_NAME
                            = produceColumnSQUID_TH_U_EQN_NAME(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListSQUID_TH_U_EQN_NAME);
                    colIndex = colIndex + categoryColumnListSQUID_TH_U_EQN_NAME.size();
                } else if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<SQUID_PPM_PARENT_EQN_NAME_U>") == 0) {
                    List<ReportColumnInterface> categoryColumnListSQUID_PPM_PARENT_EQN_NAME_U
                            = produceColumnSQUID_PPM_PARENT_EQN_NAME_U(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListSQUID_PPM_PARENT_EQN_NAME_U);
                    colIndex = colIndex + categoryColumnListSQUID_PPM_PARENT_EQN_NAME_U.size();
                } else if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<SQUID_PPM_PARENT_EQN_NAME_TH>") == 0) {
                    List<ReportColumnInterface> categoryColumnListSQUID_PPM_PARENT_EQN_NAME_TH
                            = produceColumnSQUID_PPM_PARENT_EQN_NAME_TH(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListSQUID_PPM_PARENT_EQN_NAME_TH);
                    colIndex = colIndex + categoryColumnListSQUID_PPM_PARENT_EQN_NAME_TH.size();
                } else if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<OVER_COUNT_4_6_8>") == 0) {
                    List<ReportColumnInterface> categoryColumnListOVER_COUNT_4_6_8
                            = produceColumnOVER_COUNT_4_6_8(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListOVER_COUNT_4_6_8);
                    colIndex = colIndex + categoryColumnListOVER_COUNT_4_6_8.size();
                } else if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<OVER_COUNTS_PERSEC_4_8>") == 0) {
                    List<ReportColumnInterface> categoryColumnListOVER_COUNTS_PERSEC_4_8
                            = produceColumnOVER_COUNTS_PERSEC_4_8(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListOVER_COUNTS_PERSEC_4_8);
                    colIndex = colIndex + categoryColumnListOVER_COUNTS_PERSEC_4_8.size();
                } else if (reportCategorySpecs[specIndex][6].compareToIgnoreCase("<CORR_8_PRIMARY_CALIB_CONST_PCT_DELTA>") == 0) {
                    List<ReportColumnInterface> categoryColumnListCORR_8_PRIMARY_CALIB_CONST_PCT_DELTA
                            = produceColumnCORR_8_PRIMARY_CALIB_CONST_PCT_DELTA(reportCategorySpecs[specIndex], colIndex, isDirect, has232);
                    categoryColumnList.addAll(categoryColumnListCORR_8_PRIMARY_CALIB_CONST_PCT_DELTA);
                    colIndex = colIndex + categoryColumnListCORR_8_PRIMARY_CALIB_CONST_PCT_DELTA.size();
                } else {
                    categoryColumnList.add(setupReportColumn(colIndex, reportCategorySpecs[specIndex]));
                    colIndex++;
                }
            }
            categoryColumns = new ReportColumn[categoryColumnList.size()];
            for (int i = 0; i < categoryColumnList.size(); i++) {
                categoryColumns[i] = categoryColumnList.get(i);
            }

        }

        this.visible = isVisible;
        this.legacyData = false;

    }

    private List<ReportColumnInterface> produceColumnSQUID_TH_U_EQN_NAME(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "",
                "204corr",
                "232Th",
                "/238U",
                "",
                specs[5],
                TH_U_EXP_RM,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[1] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[1] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[1] = "204corr";
            columnSpec[6] = PB4CORR + TH_U_EXP_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            columnSpec[6] = PB7CORR + TH_U_EXP_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private List<ReportColumnInterface> produceColumnSQUID_PPM_PARENT_EQN_NAME_U(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "",
                "204corr",
                "U",
                "(ppm)",
                "",
                specs[5],
                U_CONCEN_PPM_RM,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[1] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[1] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[1] = "204corr";
            columnSpec[6] = PB4CORR + U_CONCEN_PPM_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            columnSpec[6] = PB7CORR + U_CONCEN_PPM_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private List<ReportColumnInterface> produceColumnSQUID_PPM_PARENT_EQN_NAME_TH(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "",
                "204corr",
                "Th",
                "(ppm)",
                "",
                specs[5],
                TH_CONCEN_PPM_RM,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[1] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[1] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[1] = "204corr";
            columnSpec[6] = PB4CORR + TH_CONCEN_PPM_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[1] = "207corr";
            columnSpec[6] = PB7CORR + TH_CONCEN_PPM_RM;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private List<ReportColumnInterface> produceColumnOVER_COUNT_4_6_8(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "204corr", "204", "/206", "(fr. 208)",
                "",
                specs[5],
                OVER_COUNT_4_6_8,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[0] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[0] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[0] = "204corr";
            columnSpec[6] = PB4CORR + OVER_COUNT_4_6_8;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            columnSpec[6] = PB7CORR + OVER_COUNT_4_6_8;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private List<ReportColumnInterface> produceColumnOVER_COUNTS_PERSEC_4_8(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "204corr", "204", "overcts/sec", "(fr. 208)",
                "",
                specs[5],
                OVER_COUNTS_PERSEC_4_8,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[0] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[0] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[0] = "204corr";
            columnSpec[6] = PB4CORR + OVER_COUNTS_PERSEC_4_8;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            columnSpec[6] = PB7CORR + OVER_COUNTS_PERSEC_4_8;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private List<ReportColumnInterface> produceColumnCORR_8_PRIMARY_CALIB_CONST_PCT_DELTA(
            String[] specs, int myColIndex, boolean isDirect, boolean has232) {
        List<ReportColumnInterface> categoryColumnList = new ArrayList<>();
        int colIndex = myColIndex;

        String[] columnSpec = new String[]{
                "204corr", "8-corr", "206Pb/238U", "const delta%",
                "",
                specs[5],
                CORR_8_PRIMARY_CALIB_CONST_DELTA_PCT,
                specs[7],
                specs[8],
                specs[9],
                specs[10],
                specs[11],
                specs[12],
                specs[13], "false", "false"};
        // perm1 and 3
        if (!isDirect) {
            columnSpec[0] = "204corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            // perm 1 only
            if (!has232) {
                columnSpec[0] = "208corr";
                categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
                colIndex++;
            }
        } else {
            // perm2 and 4
            columnSpec[0] = "204corr";
            columnSpec[6] = PB4CORR + CORR_8_PRIMARY_CALIB_CONST_DELTA_PCT;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;

            columnSpec[0] = "207corr";
            columnSpec[6] = PB7CORR + CORR_8_PRIMARY_CALIB_CONST_DELTA_PCT;
            categoryColumnList.add(setupReportColumn(colIndex, columnSpec));
            colIndex++;
        }
        return categoryColumnList;
    }

    private ReportColumnInterface setupReportColumn(int index, String[] specs) {

        ReportColumnInterface retVal = new ReportColumn(//
                specs[0], // displayname1
                specs[1], // displayname2
                specs[2], // displayname3
                specs[3],//.contains("delta") ? specs[3].replace("delta", "\u1E9F") : specs[3], // displayname4
                index, // positionIndex
                specs[4], // units
                specs[5], // retrieveMethodName
                specs[6], // retrieveMethodParameterName
                specs[7], // uncertaintyType
                specs[8], // footnoteSpec
                Boolean.valueOf(specs[9]), // visible
                false); // amUncertainty

        retVal.setDisplayedWithArbitraryDigitCount(Boolean.valueOf(specs[10]));
        retVal.setCountOfSignificantDigits(Integer.parseInt(specs[11]));
        retVal.setAlternateDisplayName(specs[13]);
        retVal.setNeedsPb(Boolean.valueOf(specs[14]));
        retVal.setNeedsU(Boolean.valueOf(specs[15]));
        retVal.setLegacyData(isLegacyData());

        // check for need to create uncertainty column
        ReportColumnInterface uncertaintyCol = null;

        if (!specs[7].equals("")) {
            uncertaintyCol = new ReportColumn(//
                    "",
                    "",
                    specs[7].equalsIgnoreCase("PCT") ? "" : "\u00B11\u03C3",
                    specs[7].equalsIgnoreCase("PCT") ? "\u00B11\u03C3 %" : "abs",
                    //"third",
                    index,
                    specs[4],
                    specs[5],
                    specs[6],
                    specs[7],
                    "",
                    Boolean.valueOf(specs[12]),// show uncertainty
                    true); // amUncertainty

            uncertaintyCol.setAlternateDisplayName("");
        }

        retVal.setUncertaintyColumn(uncertaintyCol);

        return retVal;
    }

    /**
     * @return
     */
    @Override
    public ReportColumnInterface[] getCategoryColumns() {
        return categoryColumns;
    }

    /**
     * @param categoryColumns
     */
    @Override
    public void setCategoryColumns(ReportColumnInterface[] categoryColumns) {
        this.categoryColumns = categoryColumns;
    }

    /**
     * @return
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     */
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return
     */
    @Override
    public int getPositionIndex() {
        return positionIndex;
    }

    /**
     * @param positionIndex
     */
    @Override
    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    /**
     * @return
     */
    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     *
     */
    @Override
    public void ToggleIsVisible() {
        this.setVisible(!isVisible());
    }

    /**
     * @return the legacyData
     */
    @Override
    public boolean isLegacyData() {
        return legacyData;
    }

    /**
     * @param legacyData the legacyData to set
     */
    @Override
    public void setLegacyData(boolean legacyData) {
        this.legacyData = legacyData;
        for (ReportColumnInterface categoryColumn : categoryColumns) {
            categoryColumn.setLegacyData(legacyData);
        }
    }
}