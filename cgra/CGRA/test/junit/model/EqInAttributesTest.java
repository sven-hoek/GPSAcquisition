package junit.model;

import cgramodel.CgraModel;

import static org.junit.Assert.*;

import generator.DriverUltrasynth;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static target.Processor.Instance;

public class EqInAttributesTest {

    @Test
    public void modelsEqualInAttributes() {
        List<CgraModel> list = equalUltrasynthModels();
        runEqualListTest(list);
    }

    @Test
    public void modelsNotEqualInAttributes() {
        List<CgraModel> list = unequalUltrasynthModels();
        runUnequalListTest(list);
    }

    private void runEqualListTest(List<CgraModel> eqModelList) {
        for (CgraModel modelOuter : eqModelList) {
            for (CgraModel modelInner : eqModelList) {
                assertTrue(modelOuter.equalsInAttributes(modelInner).isEmpty());
                assertTrue(modelInner.equalsInAttributes(modelOuter).isEmpty());
            }
        }
    }

    private void runUnequalListTest(List<CgraModel> eqModelList) {
        for (CgraModel modelOuter : eqModelList) {
            for (CgraModel modelInner : eqModelList) {
                if (modelOuter == modelInner)
                    continue;

                assertFalse(!modelOuter.equalsInAttributes(modelInner).isEmpty());
                assertFalse(!modelInner.equalsInAttributes(modelOuter).isEmpty());
            }
        }
    }

    private List<CgraModel> equalUltrasynthModels() {
        Instance = target.UltraSynth.Instance;
        List<CgraModel> list = new LinkedList<>();
        CgraModel model;

        model = DriverUltrasynth.createModel("ultrasynth4");
        list.add(model);

        model = DriverUltrasynth.createModel("ultrasynth4");
        list.add(model);

        return list;
    }

    private List<CgraModel> unequalUltrasynthModels() {
        Instance = target.UltraSynth.Instance;
        List<CgraModel> list = new LinkedList<>();
        CgraModel model;

        model = DriverUltrasynth.createModel("ultrasynth4");
        list.add(model);

        model = DriverUltrasynth.createModel("ultrasynth16");
        list.add(model);

        return list;
    }

}
