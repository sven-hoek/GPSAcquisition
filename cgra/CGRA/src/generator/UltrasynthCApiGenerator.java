package generator;

import cgramodel.*;
import io.SourceFile;
import io.SourceFileLib;
import io.TemplateSourceFile;
import org.stringtemplate.v4.ST;
import scheduler.RCListSched;
import target.Processor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UltrasynthCApiGenerator {
    public static void generate(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
                                int sourceIndex, DriverUltrasynth.Config config) 
    {
        generateCgraApiSource(model, sourceFileLib, sourceIndex);
        generatePrivateStaticDataHeader(model, sourceFileLib, sourceIndex);
        generatePrivateStaticDataSource(sourceFileLib, sourceIndex);
        generateStaticDataHeader(sourceFileLib, sourceIndex);
        generateStaticDataSource(sourceFileLib, sourceIndex);
    }

    private static void generateCgraApiSource(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
                                              int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgraapi.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        // The main file to fill out
        ST template = templateSourceFile.getTargetTemplate();
        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtC);

        BiConsumer<ST, AxiTransactionModel> generateCall = (st, axiModel) -> {
            st.add("id", axiModel.id);
            st.add("context_array_name", axiModel.name);
            st.add("size", axiModel.maxTransferCount());

            // As the model assumes 0 to be the indicator for one transfer, we have to add 1
            st.add("transfers_per_entry", axiModel.valueTransferCount + 1);
        };

        for (AugmentedPE augPe : model.getPeComponents()) {
            // Template for a PE context init call
            ST initCallPe = templateSourceFile.getStGroupFile().getInstanceOf("context_init_call_pe");
            generateCall.accept(initCallPe, augPe.getAxiModel());
            template.add("send_context_calls_pe", initCallPe.render());

            // Template for a PE Log context init call
            ST initCallPELog = templateSourceFile.getStGroupFile().getInstanceOf("context_init_call_pe_log");
            generateCall.accept(initCallPELog, augPe.getLogAxiModel());
            template.add("send_context_calls_pe_log", initCallPELog.render());
        }

        for (AxiTransactionModel axiModel : model.getOtherTransactions()) {
            // if (axiModel.id == model.getConstBuffer().getAxiModel().id)
                // continue;

            ST initCallOther = templateSourceFile.getStGroupFile().getInstanceOf("context_init_call_other");
            generateCall.accept(initCallOther, axiModel);
            template.add("send_context_calls_other", initCallOther.render());
        }
    }

    private static void generatePrivateStaticDataHeader(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
                                                        int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgraprivatestaticdata_h.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        // The main file to fill out
        ST template = templateSourceFile.getTargetTemplate();
        templateSourceFile.setTargetFileName("cgraprivatestaticdata");
        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtH);

        // We do not set all the parameters of this template, as we will need scheduling info to
        // do so. This means another generator method has to run afterwards.

        template.add("ccnt_width", model.getCCNTWidth());
        template.add("lut_data_size", (Processor.Instance.getDataPathWidth() - 1) / 32 + 1);
        template.add("pe_log_id_offset", model.getPeLogIDoffset());
        template.add("addr_offset_width", model.getOffsetAddrWidth());
        template.add("addr_id_width", model.getTargetIDWidth());
        template.add("param_size", (Processor.Instance.getDataPathWidth() - 1) / 32 + 1);
    }

    private static void generatePrivateStaticDataSource(SourceFileLib sourceFileLib, int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgraprivatestaticdata.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtC);
    }

    private static void generateStaticDataHeader(SourceFileLib sourceFileLib, int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgrastaticdata_h.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        templateSourceFile.setTargetFileName("cgrastaticdata");
        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtH);
    }

    private static void generateStaticDataSource(SourceFileLib sourceFileLib, int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgrastaticdata.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtC);
    }

    public static void applySchedulingInfo(RCListSched sched, SourceFileLib sourceFileLib,
                                           int sourceIndex, DriverUltrasynth.Config config)
    {
        SchedResults schedResults = SchedResults.from(sched);
        applySchedInfoPrivateStaticDataHeader(schedResults, sourceFileLib, sourceIndex, config);
        applySchedInfoPrivateStaticDataSource(schedResults, sourceFileLib, sourceIndex, config);
        applySchedInfoStaticDataHeader(sched, sourceFileLib, sourceIndex, config);
        applySchedInfoStaticDataSource(sched, sourceFileLib, sourceIndex, config);
    }

    private static void applySchedInfoPrivateStaticDataHeader(SchedResults results, SourceFileLib sourceFileLib,
                                                              int sourceIndex, DriverUltrasynth.Config config)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgraprivatestaticdata_h.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        ST template = templateSourceFile.getTargetTemplate();
        template.add("static_param_count", results.staticParamCount);
        template.add("runtime_param_count", results.runTimeParamCount);
        template.add("max_indexed_count", results.maxIndexedParamCount);
    }

    private static void applySchedInfoPrivateStaticDataSource(SchedResults results, SourceFileLib sourceFileLib,
                                                              int sourceIndex, DriverUltrasynth.Config config)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgraprivatestaticdata.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        ST template = templateSourceFile.getTargetTemplate();
        template.add("static_param_id_offset", results.staticParamOffset);
        template.add("runtime_param_id_offset", results.runTimeParamOffset);
        template.add("host_result_id_offset", results.hostResultOffset);

        template.add("static_parameter_count", results.staticParamCount);
        template.add("runtime_parameter_count", results.runTimeParamCount);
        template.add("host_result_count", results.hostResultCount);

        template.add("integration_step_size_id_offset", results.integrationStepSizeOffset);
        template.add("integration_step_size_id_length", results.integrationStepSizeCount);
        template.add("requires_host_result_ranges", results.hostResultCount != 0);

        for (int data : results.staticParamData)
            template.add("static_params", data + ",\n");

        for (int data : results.initialRunTimeParamData)
            template.add("runtime_params", data + ",\n");

        BiConsumer<String, ArrayList<IDCrange>> rangePrinter = (target, ranges) -> {
            for (IDCrange range : ranges) {
                ST rangeTemplate = templateSourceFile.getST("range");
                rangeTemplate.add("lower_id", range.getLower());
                rangeTemplate.add("upper_id", range.getUpper());
                template.add(target, rangeTemplate.render());
            }
        };

        rangePrinter.accept("runtime_id_ranges", results.runTimeParamRanges);
        rangePrinter.accept("host_result_id_ranges", results.hostResultRanges);
    }

    private static void applySchedInfoStaticDataHeader(RCListSched sched, SourceFileLib sourceFileLib,
                                                       int sourceIndex, DriverUltrasynth.Config config)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgrastaticdata_h.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        ST template = templateSourceFile.getTargetTemplate();
        template.add("log_res_count", sched.getLogOffsets().length);
        template.add("ocm_res_count", sched.getOcmOffsets().length);
    }

    private static void applySchedInfoStaticDataSource(RCListSched sched, SourceFileLib sourceFileLib,
                                                       int sourceIndex, DriverUltrasynth.Config config)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgrastaticdata.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        ST template = templateSourceFile.getTargetTemplate();

        for (int offset : sched.getLogOffsets())
            template.add("log_result_offsets", offset + ",\n");

        for (int offset : sched.getOcmOffsets())
            template.add("ocm_result_offsets", offset + ",\n");
    }

    public static void generateContexts(RCListSched sched, CgraModelUltrasynth model, SourceFileLib sourceFileLib,
                                        int sourceIndex, DriverUltrasynth.Config config)
    {
        generateContextHeader(model, sourceFileLib, sourceIndex, config);

        List<ContextArray> contextArrays = buildContextArrays(sched, model);
        generateContextSource(contextArrays, model, sourceFileLib, sourceIndex);
    }

    private static List<ContextArray> buildContextArrays(RCListSched sched, CgraModelUltrasynth model) {

        final List<ContextArray> contextArrays = new ArrayList<>(
                model.getNrOfPEs() * 2 + model.getOtherTransactions().size()
        );

        AxiTransactionModel axiModel;
        ContextArray contextArray;
        long[] context;

        {
            // All PEs and their related Logs

            final long[][] peScheds = sched.getContextsPE();
            final long[][] peLogScheds = sched.getContextsLocalLog();

            for (AugmentedPE augmentedPE : model.getPeComponents()) {
                int id;

                axiModel = augmentedPE.getAxiModel();
                id = axiModel.id; // We will use the PE transfer ID to access both schedule arrays
                contextArray = new ContextArray(peScheds[id], axiModel);
                contextArrays.add(contextArray);

                axiModel = augmentedPE.getLogAxiModel();
                contextArray = new ContextArray(peLogScheds[id], axiModel);
                contextArrays.add(contextArray);
            }
        }

        {
            // CBox eval blocks

            final long[][] cboxScheds = sched.getContextsCBox();
            List<AxiTransactionModel> transactionModels = model.getcBoxComp().getEvalBlockContextTransactions();

            if (cboxScheds.length != transactionModels.size()) {
                throw new RuntimeException("CBox schedule amount does not match transfer model amount");
            }

            int i = 0;
            for (AxiTransactionModel transactionModel : transactionModels) {
                context = cboxScheds[i++];
                contextArray = new ContextArray(context, transactionModel);
                contextArrays.add(contextArray);
            }
        }

        axiModel = model.getCcuComp().getAxiModel();
        context = sched.getContextsControlUnit();
        contextArrays.add(new ContextArray(context, axiModel));

        // TODO: Is there no top level CBox wrapper context?
        axiModel = model.getcBoxComp().getAxiModel();
        context = new long[1];
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getLogInterface().getAxiModel();
        context = sched.getContextsGlobalLog();
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getOcmInterface().getOutputContextAxiModel();
        context = sched.getContextsOCMAddr();
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getOcmInterface().getAxiModel();
        context = sched.getContextsOCM();
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getActorInterface().getAxiModel();
        context = sched.getContextsActuator();
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getSensorInterface().getAxiModel();
        context = sched.getContextsSensor();
        contextArrays.add(new ContextArray(context, axiModel));

        axiModel = model.getComUnit().getAxiModel();
        context = sched.getIdcEntries();
        contextArrays.add(new ContextArray(context, axiModel));

        return contextArrays;
    }

    private static void generateContextHeader(CgraModelUltrasynth model, SourceFileLib sourceFileLib,
                                              int sourceIndex, DriverUltrasynth.Config config)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgracontext_h.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        // The main file to fill out
        ST template = templateSourceFile.getTargetTemplate();
        templateSourceFile.setTargetFileName("cgracontext");
        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtH);

        BiConsumer<ST, AxiTransactionModel> generateArrayDecl = (st, axiModel) -> {
            ST arrayDeclTemplate = templateSourceFile.getStGroupFile().getInstanceOf("context_array_decl");
            arrayDeclTemplate.add("name", axiModel.name);
            arrayDeclTemplate.add("size", axiModel.maxTransferCount());
            st.add("context_array_decl", arrayDeclTemplate.render());
        };

        for (AugmentedPE augPe : model.getPeComponents()) {
            generateArrayDecl.accept(template, augPe.getAxiModel());
            generateArrayDecl.accept(template, augPe.getLogAxiModel());
        }

        for (AxiTransactionModel axiModel : model.getOtherTransactions())
            generateArrayDecl.accept(template, axiModel);
    }

    private static void generateContextSource(List<ContextArray> contextArrays, CgraModelUltrasynth model,
                                              SourceFileLib sourceFileLib, int sourceIndex)
    {
        TemplateSourceFile templateSourceFile = sourceFileLib.getSourceFile(
                sourceIndex, "cgracontext.stg", TemplateSourceFile.class
        );

        if (templateSourceFile == null)
            return;

        // The main file to fill out
        ST template = templateSourceFile.getTargetTemplate();
        templateSourceFile.setTargetExtension(SourceFile.Extension.ExtC);

        for (ContextArray contextArray : contextArrays) {
            ST arrayDefTemplate = templateSourceFile.getStGroupFile().getInstanceOf("context_array_def");
            arrayDefTemplate.add("name", contextArray.axiTransaction.name);
            arrayDefTemplate.add("size", contextArray.axiTransaction.maxTransferCount());

            final int transferCount = contextArray.axiTransaction.valueTransferCount;
            for (long context : contextArray.data) {
                for (int i = 0; i < transferCount + 1; ++i) {
                    final long truncatedContext = (context << ((transferCount - i) * 32)) >>> (transferCount * 32);
                    arrayDefTemplate.add("context", truncatedContext + ",\n");
                }
            }

            template.add("context_array_def", arrayDefTemplate.render());
        }
    }

    static private class ContextArray {
        final public long[] data;
        final AxiTransactionModel axiTransaction;

        private ContextArray(long[] data, AxiTransactionModel axiTransaction) {
            this.data = data;
            this.axiTransaction = axiTransaction;
        }
    }

    static private class SchedResults {
        int staticParamCount;
        int runTimeParamCount;
        int hostResultCount;

        int staticParamOffset;
        int runTimeParamOffset;
        int hostResultOffset;

        int maxIndexedParamCount;

        int integrationStepSizeCount;
        int integrationStepSizeOffset;

        int[] staticParamData;
        int[] initialRunTimeParamData;

        ArrayList<IDCrange> runTimeParamRanges;
        ArrayList<IDCrange> hostResultRanges;

        private SchedResults() {}

        static SchedResults from(RCListSched sched) {
            SchedResults res = new SchedResults();

            res.staticParamCount = sched.getRangeConst().getUpper() - sched.getRangeConst().getLower();
            res.runTimeParamCount = sched.getRangeParam().getUpper() - sched.getRangeParam().getLower();
            res.hostResultCount = sched.getRangeHost().getUpper() - sched.getRangeHost().getLower();

            res.staticParamOffset = sched.getRangeConst().getLower();
            res.runTimeParamOffset = sched.getRangeParam().getLower();
            res.hostResultOffset = sched.getRangeHost().getLower();

            res.maxIndexedParamCount = 0;
            res.maxIndexedParamCount = Math.max(res.maxIndexedParamCount, res.runTimeParamCount);
            res.maxIndexedParamCount = Math.max(res.maxIndexedParamCount, res.hostResultCount);

            res.integrationStepSizeCount = sched.getRangeStepSize().getUpper() - sched.getRangeStepSize().getLower();
            res.integrationStepSizeOffset = sched.getRangeStepSize().getLower();

            res.staticParamData = sched.getInitConsts();
            res.initialRunTimeParamData = sched.getInitParams();

            BiConsumer<ArrayList<IDCrange>, Map<Integer, IDCrange>> rangeArrayBuilder = (array, map) -> {
                for (int index = 0; index < array.size(); ++index) {
                    if (map.containsKey(index))
                        array.set(index, map.get(index));
                    else
                        array.set(index, new IDCrange(0, 0));
                }
            };

            BiConsumer<Integer, List<?>> resizer = (size, array) -> {
                while (array.size() < size)
                    array.add(null);
            };

            res.runTimeParamRanges = new ArrayList<>(res.runTimeParamCount);
            resizer.accept(res.runTimeParamCount, res.runTimeParamRanges);
            rangeArrayBuilder.accept(res.runTimeParamRanges, sched.getParamRanges());

            res.hostResultRanges = new ArrayList<>(res.hostResultCount);
            resizer.accept(res.hostResultCount, res.hostResultRanges);
            rangeArrayBuilder.accept(res.hostResultRanges, sched.getHostRanges());

            return res;
        }
    }
}
