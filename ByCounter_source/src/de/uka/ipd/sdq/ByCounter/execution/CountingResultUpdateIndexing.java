package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * When online updates are enabled, this class holds updates to individual 
 * sections, until a new section is reported or the method ends.
 * @see InstrumentationParameters#getProvideOnlineSectionExecutionUpdates()
 * @author Martin Krogmann
 *
 */
public class CountingResultUpdateIndexing {
	/**
	 * This class groups all indexing information relevant to a single thread.
	 * @author Martin Krogmann
	 */
	private class ThreadIndex {
		/**
		 * {@link MethodIndex} by method id.
		 */
		public Map<UUID, MethodIndex> methodIndexById;
		/** The method for which the last update was provided. */
		public UUID lastUpdatedMethod;
		/** Id of the thread that this index is for. */
		public long threadId;
		
		/**
		 * @param threadId Id of the thread that this index is for.
		 */
		public ThreadIndex(final long threadId) {
			this.threadId = threadId;
			lastUpdatedMethod = null;
			this.methodIndexById = new HashMap<UUID, CountingResultUpdateIndexing.MethodIndex>();
		}

		@Override
		public String toString() {
			return "ThreadIndex [methodIndexById=" + this.methodIndexById
					+ ", lastUpdatedMethod=" + this.lastUpdatedMethod
					+ ", threadId=" + this.threadId + "]";
		}
	}
	/**
	 * This class groups all indexing information relevant to a single method.
	 * @author Martin Krogmann
	 */
	private class MethodIndex {
		/**
		 * The index of the section that was last added for the method.
		 */
		public int lastUpdatedSectionIndex;
		/**
		 * Queue that holds counting results.
		 */
		public Queue<CountingResult> sectionUpdates;

		/** The last basic block execution sequence of a result. */
		public List<Integer> lastBlockExecutionSequence;
		
		/** Id of the method that this index is for. */
		public UUID methodId;
		
		/** Marker for when the method was left. */
		public boolean isDone;

		/**
		 * @param methodId Id of the method that this index is for.
		 */
		public MethodIndex(final UUID methodId) {
			this.methodId = methodId;
			this.sectionUpdates = new LinkedList<CountingResult>(); 
			this.lastBlockExecutionSequence = null;
			this.lastUpdatedSectionIndex = -1;
			this.isDone = false;
		}

		@Override
		public String toString() {
			return "MethodIndex [lastUpdatedSectionIndex="
					+ this.lastUpdatedSectionIndex + ", sectionUpdates="
					+ this.sectionUpdates + ", lastBlockExecutionSequence="
					+ this.lastBlockExecutionSequence + ", methodId="
					+ this.methodId + "]";
		}
	}

	/**
	 * This class groups all indexing information relevant to a single thread 
	 * for regions.
	 * @author Martin Krogmann
	 */
	private class ThreadRegionIndex {
		/**
		 * {@link CountingResult} by region id.
		 */
		public Map<UUID, Queue<CountingResult>> regionResultsById;
		/** Id of the thread that this index is for. */
		public long threadId;
		
		/**
		 * @param threadId Id of the thread that this index is for.
		 */
		public ThreadRegionIndex(final long threadId) {
			this.threadId = threadId;
			this.regionResultsById = new HashMap<UUID, Queue<CountingResult>>();
		}

		@Override
		public String toString() {
			return "ThreadRegionIndex [methodIndexById=" + this.regionResultsById
					+ ", threadId=" + this.threadId + "]";
		}
	}

	/**
	 * Map that holds a {@link ThreadIndex} for each thread by thread id.
	 */
	private Map<Long, ThreadIndex> indexForThread;
	/**
	 * Map that holds a {@link ThreadRegionIndex} for each thread by thread id.
	 */
	private Map<Long, ThreadRegionIndex> indexForThreadRegion;
	/**
	 * Reference to the result collector used to access the instrumentation context.
	 */
	private CountingResultCollector parentResultCollector;
	
	/**
	 * Construct the indexing structure.
	 */
	public CountingResultUpdateIndexing(final CountingResultCollector crc) {
		this.indexForThread = new HashMap<Long, CountingResultUpdateIndexing.ThreadIndex>();
		this.indexForThreadRegion = new HashMap<Long, CountingResultUpdateIndexing.ThreadRegionIndex>();
		this.parentResultCollector = crc;
	}

	/**
	 * This handles updates reported for individual section when online 
	 * updates are enabled.
	 * @param result The calculated counting result for the update.
	 * @param blockExecutionSequence Basic block execution sequence used to 
	 * see if consecutive results contain new information.
	 */
	public void add(CountingResult result, final List<Integer> blockExecutionSequence) {
		long currentThreadId = result.getThreadId();
		ThreadIndex currentThreadIndex = this.indexForThread.get(currentThreadId);
		if(currentThreadIndex == null) {
			currentThreadIndex = new ThreadIndex(currentThreadId);
			this.indexForThread.put(currentThreadId, currentThreadIndex);
		}
		final UUID methodID = result.getMethodExecutionID();
		if(currentThreadIndex.lastUpdatedMethod != null && !methodID.equals(currentThreadIndex.lastUpdatedMethod)) {
			// we entered a new method
			// provide an update for the previous method
			setMethodDone(currentThreadId, currentThreadIndex.lastUpdatedMethod);
		}

		MethodIndex currentMethodIndex = currentThreadIndex.methodIndexById.get(methodID);
		if(currentMethodIndex == null) {
			// no entry for this method yet
			currentMethodIndex = new MethodIndex(methodID);
			currentThreadIndex.methodIndexById.put(methodID, currentMethodIndex);
		}
		Queue<CountingResult> resultQueue = currentMethodIndex.sectionUpdates;
		Integer luSectionIndex = currentMethodIndex.lastUpdatedSectionIndex;
		
		if(!currentMethodIndex.isDone 
				&& luSectionIndex >= 0 
				&& luSectionIndex != result.getIndexOfRangeBlock()) {
			// a new section is being executed
			updateObserversWithSection(resultQueue);
		}
		
		if(currentMethodIndex.lastBlockExecutionSequence == null
				|| blockExecutionSequence != null
				&& !currentMethodIndex.lastBlockExecutionSequence.equals(blockExecutionSequence)) {
			// This is new information, so add it to the queue
			currentMethodIndex.isDone = false;
			resultQueue.add(result);
		}

		// update last section index for the method
		currentThreadIndex.lastUpdatedMethod = methodID;
		currentMethodIndex.lastUpdatedSectionIndex = result.getIndexOfRangeBlock();
		currentMethodIndex.lastBlockExecutionSequence = new ArrayList<Integer>(blockExecutionSequence);
	}
	

	/**
	 * This handles updates reported for a region when online 
	 * updates are enabled.
	 * @param result The calculated counting result for the update.
	 * @param regionId Id of the region for which a new result is added.
	 */
	public void add(CountingResult result, final UUID regionId) {
		long currentThreadId = result.getThreadId();
		ThreadRegionIndex currentThreadIndex = this.indexForThreadRegion.get(currentThreadId);
		if(currentThreadIndex == null) {
			currentThreadIndex = new ThreadRegionIndex(currentThreadId);
			this.indexForThreadRegion.put(currentThreadId, currentThreadIndex);
		}

		Queue<CountingResult> resultQueue = currentThreadIndex.regionResultsById.get(regionId);
		if(resultQueue == null) {
			// no entry for this method yet
			resultQueue = new LinkedList<CountingResult>();
			currentThreadIndex.regionResultsById.put(regionId, resultQueue);
		}
		resultQueue.add(result.clone());
	}


	/**
	 * Add up all results for the finished section and send an update.
	 * The results for the section are removed by this method.
	 * @param resultQueue Result queue with the partial results for the section. This queue will be cleared with the update.
	 */
	private static void updateObserversWithSection(Queue<CountingResult> resultQueue) {
		if(resultQueue == null || resultQueue.isEmpty()) {
			throw new RuntimeException("Cannot update observers when result queue is empty.");
		}
		CountingResult resultSumForSection = resultQueue.remove();
		for(CountingResult r : resultQueue) {
			resultSumForSection.add(r);
		}
		resultQueue.clear();
		MethodExecutionRecord lastMethodExecutionDetails = CountingResultCollector.getInstance().getLastMethodExecutionDetails();
		CountingResultIndexing.removeInternalCalls(lastMethodExecutionDetails, resultSumForSection);
		CountingResultSectionExecutionUpdate update = 
				new CountingResultSectionExecutionUpdate(resultSumForSection);
		CountingResultCollector.getInstance().setChanged();
		CountingResultCollector.getInstance().notifyObservers(update);
	}

	/**
	 * Clears all results in the structure.
	 */
	public void clearResults() {
		this.indexForThread.clear();
		this.indexForThreadRegion.clear();
	}

	/**
	 * Signal that no further updates for the method are to be expected.
	 * @param threadID {@link Thread#getId()} of the thread the method executed in.
	 * @param methodID {@link UUID} of the method in question.
	 */
	public void setMethodDone(final long threadID, final UUID methodID) {
		ThreadIndex threadIndex = this.indexForThread.get(threadID);
		if(threadIndex == null) {
			return;
		}
		MethodIndex methodIndex = threadIndex.methodIndexById.get(methodID);
		if(methodIndex == null) {
			return;
		}
		if(methodIndex.isDone) {
			return;
		}
		Queue<CountingResult> resultQueue = methodIndex.sectionUpdates;
		if(resultQueue == null) {
			return;
		}
		updateObserversWithSection(resultQueue);
		// since the method was left, mark it as done
		methodIndex.isDone = true;
	}


	/**
	 * Signal that no further updates for the region are to be expected.
	 * @param threadID {@link Thread#getId()} of the thread the method executed in.
	 * @param regionID {@link UUID} of the region in question.
	 */
	public void setRegionDone(final long threadID, final UUID regionID) {
		ThreadRegionIndex threadIndex = this.indexForThreadRegion.get(threadID);
		if(threadIndex == null) {
			return;
		}
		Queue<CountingResult> resultQueue = threadIndex.regionResultsById.get(regionID);
		if(resultQueue == null) {
			return;
		}
		if(resultQueue.isEmpty()) {
			throw new IllegalStateException("Region is set to done, yet the result queue is empty.");
		}
		CountingResult top = resultQueue.peek();
		if(top.getObservedElement() == null) {
			top.setObservedElement(this.parentResultCollector.getInstrumentationContext().getEntitiesToInstrument().get(regionID));
		}
		updateObserversWithSection(resultQueue);
		threadIndex.regionResultsById.remove(regionID);
	}
}
