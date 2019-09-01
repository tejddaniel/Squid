/*
 * Copyright 2017 James F. Bowring and CIRDLES.org.
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
package org.cirdles.squid.gui.dateInterpretations.plots;

import org.cirdles.squid.gui.dateInterpretations.plots.topsoil.TopsoilPlotWetherill;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import org.cirdles.squid.constants.Squid3Constants;
import org.cirdles.squid.constants.Squid3Constants.SpotTypes;
import org.cirdles.squid.exceptions.SquidException;
import static org.cirdles.squid.gui.SquidUI.PIXEL_OFFSET_FOR_MENU;
import static org.cirdles.squid.gui.SquidUI.primaryStageWindow;
import static org.cirdles.squid.gui.SquidUIController.squidProject;
import org.cirdles.squid.gui.dateInterpretations.plots.squid.WeightedMeanPlot;
import org.cirdles.squid.gui.dateInterpretations.plots.squid.WeightedMeanRefreshInterface;
import org.cirdles.squid.shrimp.ShrimpFractionExpressionInterface;
import org.cirdles.squid.tasks.Task;
import org.cirdles.squid.tasks.expressions.spots.SpotSummaryDetails;
import org.cirdles.topsoil.Variable;
import org.controlsfx.control.CheckTreeView;
import static org.cirdles.squid.gui.SquidUI.SPOT_TREEVIEW_CSS_STYLE_SPECS;
import org.cirdles.squid.gui.dataViews.SampleNode;
import org.cirdles.squid.gui.dataViews.SampleTreeNodeInterface;
import org.cirdles.squid.gui.dateInterpretations.plots.topsoil.TopsoilPlotTeraWasserburg;
import static org.cirdles.squid.gui.topsoil.TopsoilDataFactory.prepareTeraWasserburgDatum;
import static org.cirdles.squid.gui.topsoil.TopsoilDataFactory.prepareWetherillDatum;
import org.cirdles.squid.parameters.parameterModels.ParametersModel;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.PB4CORR;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.PB7CORR;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.PB8CORR;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.CALIB_CONST_206_238_ROOT;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.CALIB_CONST_208_232_ROOT;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.WTDAV_PREFIX;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.REFRAD_AGE_U_PB;

/**
 *
 * @author James F. Bowring, CIRDLES.org, and Earth-Time.org
 */
public class PlotsController implements Initializable, WeightedMeanRefreshInterface {

    private static PlotDisplayInterface plot;
    private static Node topsoilPlotNode;

    @FXML
    private VBox vboxMaster;
    @FXML
    private ToolBar plotToolBar;

    private TreeView<SampleTreeNodeInterface> spotsTreeViewCheckBox = new CheckTreeView<>();
    private TreeView<String> spotsTreeViewString = new TreeView<>();

    private static ObservableList<SampleTreeNodeInterface> fractionNodes;
    private static PlotDisplayInterface rootPlot;
    private static List<Map<String, Object>> rootData;
    private static Map<String, PlotDisplayInterface> mapOfPlotsOfSpotSets;
    private static CheckBoxTreeItem<SampleTreeNodeInterface> chosenSample;
    private static SpotSummaryDetails spotSummaryDetails;

    @FXML
    private VBox plotVBox;
    @FXML
    private RadioButton corr4_RadioButton;
    @FXML
    private ToggleGroup correctionToggleGroup;
    @FXML
    private RadioButton corr8_RadioButton;
    @FXML
    private RadioButton corr7_RadioButton;
    @FXML
    private AnchorPane plotAndConfigAnchorPane;

    @FXML
    private RadioButton plotFlavorOneRadioButton;
    @FXML
    private RadioButton plotFlavorTwoRadioButton;
    @FXML
    private ToggleGroup plotFlavorToggleGroup;
    @FXML
    private VBox vboxTreeHolder;
    @FXML
    private CheckBox autoExcludeSpotsCheckBox;

    public static enum PlotTypes {
        CONCORDIA("CONCORDIA"),
        TERA_WASSERBURG("TERA_WASSERBURG"),
        WEIGHTED_MEAN("WEIGHTED_MEAN");

        private String plotType;

        private PlotTypes(String plotType) {
            this.plotType = plotType;
        }
    }
    public static PlotTypes plotTypeSelected = PlotTypes.CONCORDIA;

    public static SpotTypes fractionTypeSelected = SpotTypes.REFERENCE_MATERIAL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // update 
        squidProject.getTask().setupSquidSessionSpecsAndReduceAndReport(false);

        vboxMaster.prefWidthProperty().bind(primaryStageWindow.getScene().widthProperty());
        vboxMaster.prefHeightProperty().bind(primaryStageWindow.getScene().heightProperty().subtract(PIXEL_OFFSET_FOR_MENU));

        corr4_RadioButton.setUserData(PB4CORR);
        corr7_RadioButton.setUserData(PB7CORR);
        corr8_RadioButton.setUserData(PB8CORR);

        spotsTreeViewCheckBox.setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS);
        spotsTreeViewString.setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS);

        customizePlotChooserToolbarAndInvokePlotter();
    }

    private void showConcordiaPlotsOfUnknownsOrRefMat() {
        spotsTreeViewCheckBox = new CheckTreeView<>();
        spotsTreeViewCheckBox.setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS);
        spotsTreeViewString.setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS);

        final List<ShrimpFractionExpressionInterface> allUnknownOrRefMatShrimpFractions;
        Map<String, List<ShrimpFractionExpressionInterface>> mapOfSpotsBySampleNames;
        if (fractionTypeSelected.compareTo(SpotTypes.UNKNOWN) == 0) {
            allUnknownOrRefMatShrimpFractions = squidProject.getTask().getUnknownSpots();
            mapOfSpotsBySampleNames = squidProject.getTask().getMapOfUnknownsBySampleNames();
            // case of sample names chosen
            if (mapOfSpotsBySampleNames.size() > 1) {
                mapOfSpotsBySampleNames.remove(SpotTypes.UNKNOWN.getPlotType());
            }
        } else {
            allUnknownOrRefMatShrimpFractions = squidProject.getTask().getReferenceMaterialSpots();
            mapOfSpotsBySampleNames = new TreeMap<>();
            mapOfSpotsBySampleNames.put("Reference Mat", squidProject.getTask().getReferenceMaterialSpots());
            mapOfSpotsBySampleNames.put("Concentration Ref Mat", squidProject.getTask().getConcentrationReferenceMaterialSpots());
        }
        // get type of correctionList
        String correction = (String) correctionToggleGroup.getSelectedToggle().getUserData();

        // need current physical contants for plotting of concordia etc.
        ParametersModel physicalConstantsModel = squidProject.getTask().getPhysicalConstantsModel();

        // choose wetherill or tw
        if (plotFlavorOneRadioButton.isSelected()) {
            rootPlot = new TopsoilPlotWetherill(
                    "Wetherill Concordia of " + correction + " for " + fractionTypeSelected.getPlotType(),
                    allUnknownOrRefMatShrimpFractions, physicalConstantsModel);
        } else {
            rootPlot = new TopsoilPlotTeraWasserburg(
                    "Tera-Wasserburg Concordia of " + correction + " for " + fractionTypeSelected.getPlotType(),
                    allUnknownOrRefMatShrimpFractions, physicalConstantsModel);
        }
        rootData = new ArrayList<>();

        List<SampleTreeNodeInterface> fractionNodeDetails = new ArrayList<>();

        // build out set of rootData for samples
        CheckBoxTreeItem<SampleTreeNodeInterface> rootItem
                = new CheckBoxTreeItem<>(new SampleNode(fractionTypeSelected.getPlotType()));
        chosenSample = rootItem;
        rootItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                ((SampleNode) rootItem.getValue()).setSelectedProperty(new SimpleBooleanProperty(newValue));

                ObservableList<TreeItem<SampleTreeNodeInterface>> mySamples = rootItem.getChildren();
                Iterator<TreeItem<SampleTreeNodeInterface>> mySamplesIterator = mySamples.iterator();
                while (mySamplesIterator.hasNext()) {

                    CheckBoxTreeItem<SampleTreeNodeInterface> mySampleItem
                            = (CheckBoxTreeItem<SampleTreeNodeInterface>) mySamplesIterator.next();
                    mySampleItem.setSelected(newValue);
                }
                plot = rootPlot;
                plot.setData(rootData);
            }
        });

        rootItem.setExpanded(true);
        rootItem.setIndependent(true);
        rootItem.setSelected(true);

        spotsTreeViewCheckBox.setRoot(rootItem);
        spotsTreeViewCheckBox.setShowRoot(true);

        mapOfPlotsOfSpotSets = new TreeMap<>();
        for (Map.Entry<String, List<ShrimpFractionExpressionInterface>> entry : mapOfSpotsBySampleNames.entrySet()) {
            CheckBoxTreeItem<SampleTreeNodeInterface> sampleItem
                    = new CheckBoxTreeItem<>(new SampleNode(entry.getKey()));
            sampleItem.setSelected(true);
            rootItem.getChildren().add(sampleItem);

            List<Map<String, Object>> myData = new ArrayList<>();

            // choose wetherill or tw
            PlotDisplayInterface myPlot;
            if (plotFlavorOneRadioButton.isSelected()) {
                myPlot = new TopsoilPlotWetherill(
                        "Wetherill Concordia of " + correction + " for " + entry.getKey(),
                        entry.getValue(), physicalConstantsModel);
            } else {
                myPlot = new TopsoilPlotTeraWasserburg(
                        "Tera-Wasserburg Concordia of " + correction + " for " + entry.getKey(),
                        entry.getValue(), physicalConstantsModel);
            }

            mapOfPlotsOfSpotSets.put(sampleItem.getValue().getNodeName(), myPlot);

            for (ShrimpFractionExpressionInterface spot : entry.getValue()) {
                String flavorOfConcordia = plotFlavorOneRadioButton.isSelected() ? "C" : "TW";
                SampleTreeNodeInterface fractionNode
                        = new ConcordiaFractionNode(flavorOfConcordia, spot, correction);
                fractionNodeDetails.add(fractionNode);

                // handles each spot
                CheckBoxTreeItem<SampleTreeNodeInterface> checkBoxTreeSpotItem
                        = new CheckBoxTreeItem<>(fractionNode);
                sampleItem.getChildren().add(checkBoxTreeSpotItem);

                checkBoxTreeSpotItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        ((ConcordiaFractionNode) checkBoxTreeSpotItem.getValue()).setSelectedProperty(new SimpleBooleanProperty(newValue));
                        myPlot.setData(myData);
                    }
                });
                checkBoxTreeSpotItem.setIndependent(false);
                checkBoxTreeSpotItem.setSelected(fractionNode.getSelectedProperty().getValue());

                myData.add(((ConcordiaFractionNode) fractionNode).getDatum());
                // this contains all samples at the tree top
                rootData.add(((ConcordiaFractionNode) fractionNode).getDatum());
            }

            myPlot.setData(myData);

            // this sample item contains all the spot item checkboxes
            sampleItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    myPlot.setData(myData);
                    rootPlot.setData(rootData);
                }
            });
            sampleItem.setIndependent(false);
        }
        rootPlot.setData(rootData);

        fractionNodes = FXCollections.observableArrayList(fractionNodeDetails);

        ((TreeView<SampleTreeNodeInterface>) spotsTreeViewCheckBox).setCellFactory(cell -> new CheckBoxTreeCell<>(
                (TreeItem<SampleTreeNodeInterface> item) -> ((ConcordiaFractionNode) item.getValue()).getSelectedProperty(),
                new StringConverter<TreeItem<SampleTreeNodeInterface>>() {

            @Override
            public String toString(TreeItem<SampleTreeNodeInterface> object) {
                SampleTreeNodeInterface item = object.getValue();
                return item.getNodeName();
            }

            @Override
            public TreeItem<SampleTreeNodeInterface> fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }));

        vboxTreeHolder.getChildren().clear();
        spotsTreeViewCheckBox.setPrefHeight(vboxMaster.getPrefHeight());
        spotsTreeViewCheckBox.setMinHeight(vboxMaster.getHeight());
        vboxTreeHolder.getChildren().add(spotsTreeViewCheckBox);

        // dec 2018 improvement suggested by Nicole Rayner to use checkboxes to select members
        // thus selecting tree item displays it and checkbox (see above) for a sample will
        // allow toggling of all spots
        spotsTreeViewCheckBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SampleTreeNodeInterface>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<SampleTreeNodeInterface>> observable,
                    TreeItem<SampleTreeNodeInterface> oldValue, TreeItem<SampleTreeNodeInterface> newValue) {
                rootPlot.setData(rootData);
                if (newValue.getValue() instanceof SampleNode) {
                    if (newValue.equals(spotsTreeViewCheckBox.getRoot())) {
                        plot = rootPlot;
                    } else if (chosenSample != newValue) {
                        plot = mapOfPlotsOfSpotSets.get(newValue.getValue().getNodeName());
                    }
                    chosenSample = (CheckBoxTreeItem< SampleTreeNodeInterface>) newValue;
                }
                refreshPlot();
            }
        });

        refreshPlot();
    }

    @Override
    public void refreshPlot() {
        try {
            topsoilPlotNode = plot.displayPlotAsNode();
            plotAndConfigAnchorPane.getChildren().setAll(topsoilPlotNode);
            AnchorPane.setLeftAnchor(topsoilPlotNode, 0.0);
            AnchorPane.setRightAnchor(topsoilPlotNode, 0.0);
            AnchorPane.setTopAnchor(topsoilPlotNode, 0.0);
            AnchorPane.setBottomAnchor(topsoilPlotNode, 0.0);

            VBox.setVgrow(plotAndConfigAnchorPane, Priority.ALWAYS);
            VBox.setVgrow(topsoilPlotNode, Priority.ALWAYS);
            VBox.setVgrow(plotVBox, Priority.NEVER);//ALWAYS);

            plotToolBar.getItems().clear();
            plotToolBar.getItems().addAll(plot.toolbarControlsFactory());
            plotToolBar.setPadding(Insets.EMPTY);
        } catch (Exception e) {
        }
    }

    @Override
    public void toggleSpotExclusionWM(int index) {
        ((CheckBoxTreeItem) spotsTreeViewCheckBox.getRoot().getChildren().get(index))
                .setSelected(spotSummaryDetails.getRejectedIndices()[index]);
    }

    @Override
    public void calculateWeightedMean() {
        try {
            spotSummaryDetails.setValues(spotSummaryDetails.eval(squidProject.getTask()));
        } catch (SquidException squidException) {
        }
    }

    private void showWeightedMeanPlot() {
        // get type of correction
        String correction = (String) correctionToggleGroup.getSelectedToggle().getUserData();
        // flavor of plot
        String calibrConstAgeBaseName = (String) plotFlavorToggleGroup.getSelectedToggle().getUserData();
        // get details
        spotSummaryDetails
                = squidProject.getTask().getTaskExpressionsEvaluationsPerSpotSet().
                        get(WTDAV_PREFIX + correction + calibrConstAgeBaseName + "_CalibConst");
        plot = new WeightedMeanPlot(
                new Rectangle(1000, 600),
                correction + calibrConstAgeBaseName + " calibr.const Weighted Mean of Reference Material "
                + ((Task) squidProject.getTask()).getFilterForRefMatSpotNames(),
                spotSummaryDetails,
                correction + calibrConstAgeBaseName.replace("/", "") + "_Age_RM", // TODO: FIX THIS HACK
                squidProject.getTask().getTaskExpressionsEvaluationsPerSpotSet().get(REFRAD_AGE_U_PB).getValues()[0][0],
                this);//559.1 * 1e6);

        refreshPlot();

        List<ShrimpFractionExpressionInterface> shrimpFractionsDetails
                = spotSummaryDetails.getSelectedSpots();
        List<SampleTreeNodeInterface> fractionNodeDetailsWM = new ArrayList<>();
        rootData = new ArrayList<>();

        for (int i = 0; i < shrimpFractionsDetails.size(); i++) {
            WeightedMeanFractionNode fractionNodeWM
                    = new WeightedMeanFractionNode(shrimpFractionsDetails.get(i), i);
            fractionNodeDetailsWM.add(fractionNodeWM);
        }

        fractionNodes = FXCollections.observableArrayList(fractionNodeDetailsWM);

        try {
            if (spotSummaryDetails.isManualRejectionEnabled()) {
                TreeItem<SampleTreeNodeInterface> rootItemWM
                        = new CheckBoxTreeItem<>(new SampleNode(((Task) squidProject.getTask()).getFilterForRefMatSpotNames()));

                spotsTreeViewCheckBox.setCellFactory(p -> new CheckBoxTreeCell<>(
                        (TreeItem<SampleTreeNodeInterface> item) -> ((WeightedMeanFractionNode) item.getValue()).getSelectedProperty(),
                        new StringConverter<TreeItem<SampleTreeNodeInterface>>() {

                    @Override
                    public String toString(TreeItem<SampleTreeNodeInterface> object) {
                        SampleTreeNodeInterface item = object.getValue();
                        return item.getNodeName();
                    }

                    @Override
                    public TreeItem<SampleTreeNodeInterface> fromString(String string) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }));
                spotsTreeViewCheckBox.setRoot(rootItemWM);
                rootItemWM.setExpanded(true);
                spotsTreeViewCheckBox.setShowRoot(true);

                for (int i = 0; i < fractionNodes.size(); i++) {
                    final CheckBoxTreeItem<SampleTreeNodeInterface> checkBoxTreeItemWM
                            = new CheckBoxTreeItem<>(fractionNodes.get(i));
                    rootItemWM.getChildren().add(checkBoxTreeItemWM);

                    checkBoxTreeItemWM.setSelected(!spotSummaryDetails.getRejectedIndices()[i]);
                    checkBoxTreeItemWM.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        ((WeightedMeanFractionNode) checkBoxTreeItemWM.getValue()).setSelectedProperty(new SimpleBooleanProperty(newValue));
                        spotSummaryDetails.setIndexOfRejectedIndices(((WeightedMeanFractionNode) checkBoxTreeItemWM.getValue())
                                .getIndexOfSpot(), !newValue);
                        try {
                            spotSummaryDetails.setValues(spotSummaryDetails.eval(squidProject.getTask()));
                        } catch (SquidException squidException) {
                        }
                        refreshPlot();
                    });
                }

                vboxTreeHolder.getChildren().clear();
                spotsTreeViewCheckBox.setPrefHeight(vboxMaster.getPrefHeight());
                spotsTreeViewCheckBox.setMinHeight(vboxMaster.getHeight());
                vboxTreeHolder.getChildren().add(spotsTreeViewCheckBox);
            } else {
                TreeItem<String> rootItemWM
                        = new TreeItem<>(squidProject.getFilterForRefMatSpotNames());
                spotsTreeViewString.setRoot(rootItemWM);

                spotsTreeViewString.setCellFactory(tv -> new TreeCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                            setStyle(null);
                        } else {
                            if (item.startsWith("*")) {
                                setText(item.replaceFirst("\\*", ""));
                                setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS + "-fx-text-fill: blue;");
                            } else {
                                setText(item);
                                setStyle(SPOT_TREEVIEW_CSS_STYLE_SPECS + "-fx-text-fill: red;");
                            }
                        }
                    }

                });

                spotsTreeViewString.setRoot(rootItemWM);
                rootItemWM.setExpanded(true);
                spotsTreeViewString.setShowRoot(true);

                for (int i = 0; i < fractionNodes.size(); i++) {
                    boolean rejected = spotSummaryDetails.getRejectedIndices()[i];
                    final TreeItem<String> TreeItemWM
                            = new TreeItem<>((rejected ? "*" : "") + fractionNodes.get(i).getNodeName());

                    rootItemWM.getChildren().add(TreeItemWM);
                }

                vboxTreeHolder.getChildren().clear();
                spotsTreeViewString.setPrefHeight(vboxMaster.getPrefHeight());
                spotsTreeViewString.setMinHeight(vboxMaster.getHeight());
                vboxTreeHolder.getChildren().add(spotsTreeViewString);
            }
        } catch (Exception e) {
        }
    }

    @FXML
    private void plotChooserAction(ActionEvent event) {
        if (((Task) squidProject.getTask()).getReferenceMaterialSpots().size() > 0) {
            customizePlotChooserToolbarAndInvokePlotter();
        }
    }

    private void customizePlotChooserToolbarAndInvokePlotter() {

        switch (plotTypeSelected) {
            case CONCORDIA:
            case TERA_WASSERBURG:
                plotFlavorOneRadioButton.setText("Wetherill Concordia");
                plotFlavorTwoRadioButton.setText("Tera-Wasserburg");
                plotFlavorOneRadioButton.setDisable(false);
                plotFlavorTwoRadioButton.setDisable(fractionTypeSelected.compareTo(SpotTypes.REFERENCE_MATERIAL) == 0);

                corr7_RadioButton.setVisible(false);
                corr8_RadioButton.setVisible(true);

                showConcordiaPlotsOfUnknownsOrRefMat();
                break;
            case WEIGHTED_MEAN:
                plotFlavorOneRadioButton.setText(CALIB_CONST_206_238_ROOT + "_CalibConst");
                plotFlavorTwoRadioButton.setText(CALIB_CONST_208_232_ROOT + "_CalibConst");
                plotFlavorOneRadioButton.setUserData(CALIB_CONST_206_238_ROOT);
                plotFlavorTwoRadioButton.setUserData(CALIB_CONST_208_232_ROOT);

                boolean isDirectAltPD = squidProject.getTask().isDirectAltPD();
                boolean has232 = squidProject.getTask().getParentNuclide().contains("232");

                corr8_RadioButton.setVisible(false);
                if (!isDirectAltPD && !has232) { // perm1
                    plotFlavorOneRadioButton.setSelected(true);
                    plotFlavorTwoRadioButton.setDisable(true);
                    corr8_RadioButton.setVisible(true);
                } else if (!isDirectAltPD && has232) {// perm3
                    plotFlavorOneRadioButton.setDisable(true);
                    plotFlavorTwoRadioButton.setSelected(true);
                } else {
                    plotFlavorOneRadioButton.setDisable(false);
                    plotFlavorTwoRadioButton.setDisable(false);
                }

                corr7_RadioButton.setVisible(true);

                Squid3Constants.IndexIsoptopesEnum selectedIndexIsotope = squidProject.getTask().getSelectedIndexIsotope();
                switch (selectedIndexIsotope) {
                    case PB_204:
                        corr4_RadioButton.setSelected(true);
                        break;
                    case PB_207:
                        corr7_RadioButton.setSelected(true);
                        break;
                    case PB_208:
                        corr8_RadioButton.setSelected(true);
                }

                autoExcludeSpotsCheckBox.setSelected(squidProject.getTask().isSquidAllowsAutoExclusionOfSpots());

                showWeightedMeanPlot();

        }
    }

    @FXML
    private void selectedIsotopeIndexAction(ActionEvent event) {
        String correction = ((String) ((RadioButton) event.getSource()).getUserData()).substring(0, 1);
        switch (correction) {
            case "4":
                squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_204);
                break;
            case "7":
                squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_207);
                break;
            default:
                squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_208);
        }

        squidProject.getTask().setChanged(true);

        switch (plotTypeSelected) {
            case CONCORDIA:
            case TERA_WASSERBURG:
                showConcordiaPlotsOfUnknownsOrRefMat();
                break;
            case WEIGHTED_MEAN:
                showWeightedMeanPlot();
        }
    }

    @FXML
    private void autoExcludeSpotsCheckBoxAction(ActionEvent event) {

        squidProject.getTask().setSquidAllowsAutoExclusionOfSpots(autoExcludeSpotsCheckBox.isSelected());
        // this will cause weighted mean expressions to be changed with boolean flag
        squidProject.getTask().updateRefMatCalibConstWMeanExpressions(autoExcludeSpotsCheckBox.isSelected());

        showWeightedMeanPlot();
    }

    private class ConcordiaFractionNode implements SampleTreeNodeInterface {

        private final ShrimpFractionExpressionInterface shrimpFraction;
        private final Map<String, Object> datum;
        private SimpleBooleanProperty selectedProperty;

        public ConcordiaFractionNode(String flavor, ShrimpFractionExpressionInterface shrimpFraction, String correction) {
            this.shrimpFraction = shrimpFraction;
            this.selectedProperty = new SimpleBooleanProperty(shrimpFraction.isSelected());
            if (flavor.compareToIgnoreCase("C") == 0) {
                this.datum = prepareWetherillDatum(shrimpFraction, correction, !shrimpFraction.isReferenceMaterial());
            } else {
                this.datum = prepareTeraWasserburgDatum(shrimpFraction, correction, !shrimpFraction.isReferenceMaterial());
            }
            this.datum.put(Variable.SELECTED.getTitle(), shrimpFraction.isSelected());
        }

        /**
         * @return the shrimpFraction
         */
        @Override
        public ShrimpFractionExpressionInterface getShrimpFraction() {
            return shrimpFraction;
        }

        /**
         * @return the datum
         */
        public Map<String, Object> getDatum() {
            return datum;
        }

        /**
         * @return the selectedProperty
         */
        @Override
        public SimpleBooleanProperty getSelectedProperty() {
            return selectedProperty;
        }

        /**
         * @param selectedProperty the selectedProperty to set
         */
        @Override
        public void setSelectedProperty(SimpleBooleanProperty selectedProperty) {
            this.selectedProperty = selectedProperty;
            this.shrimpFraction.setSelected(selectedProperty.getValue());
            this.datum.put(Variable.SELECTED.getTitle(), selectedProperty.getValue());
        }

        @Override
        public String getNodeName() {
            return shrimpFraction.getFractionID();// + " AGE = " + datum.get("AGE");
        }
    }

    private class WeightedMeanFractionNode implements SampleTreeNodeInterface {

        private ShrimpFractionExpressionInterface shrimpFraction;
        private SimpleBooleanProperty selectedProperty;
        private int indexOfSpot;

        public WeightedMeanFractionNode(ShrimpFractionExpressionInterface shrimpFraction, int indexOfSpot) {
            this.shrimpFraction = shrimpFraction;
            this.selectedProperty = new SimpleBooleanProperty(shrimpFraction.isSelected());
            this.indexOfSpot = indexOfSpot;
        }

        /**
         * @return the shrimpFraction
         */
        @Override
        public ShrimpFractionExpressionInterface getShrimpFraction() {
            return shrimpFraction;
        }

        /**
         * @return the selectedProperty
         */
        @Override
        public SimpleBooleanProperty getSelectedProperty() {
            return selectedProperty;
        }

        /**
         * @param selectedProperty the selectedProperty to set
         */
        @Override
        public void setSelectedProperty(SimpleBooleanProperty selectedProperty) {
            this.selectedProperty = selectedProperty;
            this.shrimpFraction.setSelected(selectedProperty.getValue());
        }

        @Override
        public String getNodeName() {
            return shrimpFraction.getFractionID() + " " + plot.makeAgeString(indexOfSpot);
        }

        public int getIndexOfSpot() {
            return indexOfSpot;
        }
    }
}