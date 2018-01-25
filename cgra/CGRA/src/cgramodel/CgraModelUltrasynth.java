package cgramodel;

import static util.SimpleMath.checkedLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

import target.Processor;

/**
 * The model of a Ultrasynth composition extending a CGRA composition.
 */
public class CgraModelUltrasynth extends CgraModel implements util.Version {

	public final static int majorVersion = 2;
	public final static int minorVersion = 0;
	public final static int revisionVersion = 0;

	public int getMajorVersion(){
		return majorVersion;
	}

	public int getMinorVersion(){
		return minorVersion;
	}
	
	public int getRevisionVersion(){
		return revisionVersion;
	}
	
	private static final long serialVersionUID = 1L;
	private static final int UNKNOWN = -1;
	private static final int SYSTEM_ADDR_WIDTH = 32;

	public CgraModelUltrasynth() {
		peComponents = new ArrayList<>(16);
		components = new ArrayList<>(8);
		otherTransactions = new ArrayList<>(16);
		singleRegTransactions = new ArrayList<>(16);
	}

	private ArrayList<AugmentedPE> peComponents;
	private ArrayList<UltrasynthComponent> components;
	private ArrayList<AxiTransactionModel> otherTransactions;
	private ArrayList<AxiTransactionModel> singleRegTransactions;

	private CCUComp ccuComp;
	private CBoxComp cBoxComp;

	private SensorInterface sensorInterface;
	private ActorInterface actorInterface;
	private SyncUnit syncUnit;
	private ComUnit comUnit;

	private ConstBuffer constBuffer;
	private ParameterBuffer parameterBuffer;

	private LogInterface logInterface;
	private OCMInterface ocmInterface;

	private boolean isInitialised = false;
	private boolean isFinalised = false;

	/**
	 * Initialises the attributes of an {@link #CgraModelUltrasynth} object
	 * so that their values may be configured by a information of a loaded
	 * config file.
	 *
	 * Sets {@link #isInitialised} to true.
	 *
	 * It is required that the {@link #CgraModel} represented by this {@link #CgraModelUltrasynth}
	 * is finalisible when calling this method!
	 */
	public void init() {
		if (isInitialised)
			return;

		// Do this before finalising to avoid complaints
		for (PEModel pe : getPEs())
			pe.setLiveout(false);

		// Has to be done first, this method relies on it!
		super.finalizeCgra();

		// create PE components
		for (PEModel pe : getPEs())
			peComponents.add(new AugmentedPE(pe.getID()));

		// create all "other" components
		ccuComp = new CCUComp();
		components.add(ccuComp);
		cBoxComp = new CBoxComp();
		components.add(cBoxComp);
		sensorInterface = new SensorInterface();
		components.add(sensorInterface);
		actorInterface = new ActorInterface();
		components.add(actorInterface);
		syncUnit = new SyncUnit();
		components.add(syncUnit);
		comUnit = new ComUnit();
		components.add(comUnit);
		constBuffer = new ConstBuffer();
		components.add(constBuffer);
		parameterBuffer = new ParameterBuffer();
		components.add(parameterBuffer);
		logInterface = new LogInterface();
		components.add(logInterface);
		ocmInterface = new OCMInterface();
		components.add(ocmInterface);

		isInitialised = true;
	}

	@Override
	public boolean hostSpecificFinalized() {
		return isInitialised && isFinalised;
	}

	/**
	 * Just like the overridden method in {@link #CgraModel},
	 * all attribute values of a {@link #CgraModelUltrasynth}, which are unknown after loading
	 * some basic info from the config file, are computed here.
	 *
	 * It is required that isInitialised is true.
	 */
	@Override
	public void finalizeCgra() {
		if (!isInitialised) {
			System.err.println("Ultrasynth was not initialised, aborting!");
			return;
		}

		if (isFinalised) {
			// Some things have to be cleaned up at this point
			otherTransactions.clear();
			singleRegTransactions.clear();
			isFinalised = false;
		}

		// calculate the PE ID related information for this model
		peIDWidth = checkedLog(getNrOfPEs());
		peLogSelectionBitPos = peIDWidth;
		peLogIDoffset = 1 << peLogSelectionBitPos;

		if (!finaliseComponents())
			return;

		// Collect all other transaction targets
		for (UltrasynthComponent comp : components)
			comp.addOtherAxiTransactionsTo(otherTransactions);

		otherTransactions.sort((AxiTransactionModel m1, AxiTransactionModel m2) -> (m1.id - m2.id));

		// Collect all the single reg targets
		for (UltrasynthComponent comp : components)
			comp.addSingleRegTransactionsTo(singleRegTransactions);

		singleRegTransactions.sort((AxiTransactionModel m1, AxiTransactionModel m2) -> (m1.id - m2.id));

		otherIDWidth = checkedLog(otherTransactions.size());
		singleRegIDWidth = checkedLog(AxiTransactionModel.SingleRegTag.values().length);

		targetIDWidth = Math.max(peIDWidth, otherIDWidth);
		targetIDWidth = Math.max(targetIDWidth, singleRegIDWidth);

		// calculate max context width and min register file size
		minRegFileSize = Integer.MAX_VALUE;
		for (PEModel pe : getPEs()) {
			maxContextWidth = Math.max(maxContextWidth, pe.getContextWidth());
			minRegFileSize = Math.min(minRegFileSize, pe.getRegfilesize());
		}

		maxContextWidth = Math.max(maxContextWidth, getContextmaskccu().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, sensorInterface.getContext().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, actorInterface.getContext().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, logInterface.getGlobalContext().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, ocmInterface.getGatherContext().getContextWidth());
		maxContextWidth = Math.max(maxContextWidth, comUnit.getIDCWidth());
		maxContextWidth = Math.max(maxContextWidth, Processor.Instance.getDataPathWidth()); // ConstBuffer width

		// calculate the max context address width
		offsetAddrWidth = checkedLog(getContextMemorySize());
		offsetAddrWidth = Math.max(offsetAddrWidth, logInterface.getGlobalContextAddrWidth());
		// offsetAddrWidth = Math.max(offsetAddrWidth, constBuffer.getAddrWidth()); // TODO: replace me with something new
		offsetAddrWidth = Math.max(offsetAddrWidth, comUnit.getIDCAddrWidth());

		Consumer<String> err = (String name) ->
				System.err.println("Ultrasynth model was not finalised completely, missing component " + name);

		for (AugmentedPE augmentedPE : peComponents) {
			if (!augmentedPE.isFinal){
				err.accept(augmentedPE.getClass().getName());
				return;
			}
		}

		for (UltrasynthComponent comp : components) {
			if (!comp.isFinal) {
				err.accept(comp.getClass().getName());
				return;
			}
		}

		isFinalised = true;
	}

	/**
	 * All components of this model have to be finalised in this method.
	 *
	 */
	private boolean finaliseComponents() {
		if (peIDWidth == UNKNOWN || peLogIDoffset == UNKNOWN) {
			System.err.println("finaliseComponents() expects that peIDWidth and peLogIDoffset are known values");
			return false;
		}

		// finalise all PE components
		for (AugmentedPE augPE : peComponents)
			augPE.finalise(this, peLogIDoffset);

		// The counter for the next free other ID
		int otherID = 0;

		// finalise the remaining components
		otherID = ccuComp.finalise(getContextmaskccu().getContextWidth(), getContextMemorySize(), otherID);
		otherID = cBoxComp.finalise(getcBoxModel().getContextmaskWrapper().getContextWidth(), getContextMemorySize(),
				getcBoxModel().getNrOfEvaluationBlocks(), getcBoxModel().getContextmaskEvaLuationBlocks().getContextWidth(),
				otherID);

		otherID = comUnit.finalise(getMaxRegfileAddrWidth(), peIDWidth, otherID);
		otherID = sensorInterface.finalise(getContextMemorySize(), otherID);
		otherID = actorInterface.finalise(peIDWidth, getContextMemorySize(), otherID);
		otherID = logInterface.finalise(peComponents, peIDWidth, getCCNTWidth(), SYSTEM_ADDR_WIDTH, otherID);
		otherID = ocmInterface.finalise(peIDWidth, getContextMemorySize(), SYSTEM_ADDR_WIDTH, getCCNTWidth(), otherID);
		otherID = constBuffer.finalise(otherID, Processor.Instance.getDataPathWidth(), getPEs());

		syncUnit.finalise(getCCNTWidth());
		parameterBuffer.finalise(comUnit.getIDCSize());

		return true;
	}

	@Override
	public boolean isFinalized() {
		return super.isFinalized() && isFinalised;
	}


//	@Override
	public HashSet<String> equalsInHostRelatedAttributes(CgraModel o) {
		if (this == o) return new HashSet<String>();
		if (o == null || getClass() != o.getClass()) return new HashSet<String>(){{add("US not same class");}};

//		if (!super.equalsInAttributes(o))
//			return false;
		HashSet<String> diff  = new HashSet<String>();
		CgraModelUltrasynth that = (CgraModelUltrasynth) o;

		if (targetIDWidth != that.targetIDWidth) diff.add("US targetIDWidth");
		if (peIDWidth != that.peIDWidth) diff.add("US peIDWidth");
		if (otherIDWidth != that.otherIDWidth) diff.add("US otherIDWidth");
		if (singleRegIDWidth != that.singleRegIDWidth) diff.add("US singleRegIDWidth");
		if (peLogIDoffset != that.peLogIDoffset) diff.add("US peLogIDoffset");
		if (peLogSelectionBitPos != that.peLogSelectionBitPos) diff.add("US peLogSelectionBitPos");
		if (maxContextWidth != that.maxContextWidth) diff.add("US maxContextWidth");
		if (offsetAddrWidth != that.offsetAddrWidth) diff.add("US offsetAddrWidth");
		if (minRegFileSize != that.minRegFileSize) diff.add("US minRegFileSize");

		if(peComponents.size() != that.peComponents.size())diff.add("US peComponents size");
		if(components.size() != that.components.size()) diff.add("US components size");

		if (!diff.isEmpty()){
			return diff;
		}

		for (int i = 0; i < peComponents.size(); ++i){
			diff.addAll(peComponents.get(i).equalsInAttributes(that.peComponents.get(i)));
		}

		for (int i = 0; i < components.size(); ++i)
			diff.addAll(components.get(i).equalsInAttributes(that.components.get(i)));

		return diff;
	}

	/**
	 * The maximum width of any kind of ID.
	 */
	private int targetIDWidth = UNKNOWN;

	/**
	 * The maximum width of a PE ID.
	 */
	private int peIDWidth = UNKNOWN;

	/**
	 * The maximum width of an "other" ID.
	 */
	private int otherIDWidth = UNKNOWN;

	/**
	 * The maximum width of a single register transaction ID.
	 */
	private int singleRegIDWidth = UNKNOWN;

	/**
	 * The first PE log ID
	 */
	private int peLogIDoffset = UNKNOWN;

	/**
	 * The bit pos (Control Address biased) distinguishing a PE ID from a
	 * PE Log ID.
	 */
	private int peLogSelectionBitPos = UNKNOWN;

	/**
	 * The biggest context width
	 */
	private int maxContextWidth = UNKNOWN;

	/**
	 * The biggest context address width generated from all modules
	 * which use any kind of context memory.
	 */
	private int offsetAddrWidth = UNKNOWN;

	/**
	 * The minimal size out of all register files of this composition.
	 */
	private int minRegFileSize = UNKNOWN;

	public int getOffsetAddrWidth() {
		return offsetAddrWidth;
	}

	public int getTargetIDWidth() {
		return targetIDWidth;
	}

	public int getMaxContextWidth() {
		return maxContextWidth;
	}

	public int getPeLogIDoffset() {
		return peLogIDoffset;
	}

	public int getPeIDWidth() {
		return peIDWidth;
	}

	public int getOtherIDWidth() {
		return otherIDWidth;
	}

	public int getSingleRegIDWidth() {
		return singleRegIDWidth;
	}

	public int getPeLogSelectionBitPos() {
		return peLogSelectionBitPos;
	}

	public int getAxiOtherTargetCount() { return otherTransactions.size(); }

	public int getMinRegFileSize() {
		return minRegFileSize;
	}

	public ArrayList<AugmentedPE> getPeComponents() {
		return peComponents;
	}

	public ArrayList<UltrasynthComponent> getComponents() {
		return components;
	}

	public ArrayList<AxiTransactionModel> getOtherTransactions() {
		return otherTransactions;
	}

	public ArrayList<AxiTransactionModel> getSingleRegTransactions() {
		return singleRegTransactions;
	}

	public CCUComp getCcuComp() {
		return ccuComp;
	}

	public CBoxComp getcBoxComp() {
		return cBoxComp;
	}

	public SensorInterface getSensorInterface() {
		return sensorInterface;
	}

	public ActorInterface getActorInterface() {
		return actorInterface;
	}

	public SyncUnit getSyncUnit() {
		return syncUnit;
	}

	public ComUnit getComUnit() {
		return comUnit;
	}

	public ConstBuffer getConstBuffer() {
		return constBuffer;
	}

	public ParameterBuffer getParameterBuffer() {
		return parameterBuffer;
	}

	public LogInterface getLogInterface() {
		return logInterface;
	}

	public OCMInterface getOcmInterface() {
		return ocmInterface;
	}
}
