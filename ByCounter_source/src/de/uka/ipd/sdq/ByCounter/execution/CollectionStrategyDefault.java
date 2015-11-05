package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion.StopPointType;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.results.RequestResult;

/**
 * This class is used in {@link CountingResultCollector} in 
 * order to process results during result collection when no other strategy 
 * is specified. 
 * @author Martin Krogmann
 *
 */
public class CollectionStrategyDefault extends AbstractCollectionStrategy {
	
	/** Indexing infrastructure for counting results. */
	private CountingResultIndexing countingResultIndexing;

	/** Indexing infrastructure for section update results. */
	private CountingResultUpdateIndexing countingResultUpdateIndexing;

	/** Indexing infrastructure for counting regions. */
	private CountingResultRegionIndexing countingResultRegionIndexing;
	
	/** Indexing infrastructure for counting thread structures. */
	private CountingResultThreadIndexing countingResultThreadIndexing;
	
	/** For each method: Last basic block execution sequence. For updates. */
	private Map<UUID,List<Integer>> lastBasicBlockExecutionSequenceByMethod;
	
	/** For each method: Last label block execution sequence. For updates. */
	private Map<UUID,List<Integer>> lastLabelBlockExecutionSequenceByMethod;

	/** Regions that are currently counted. Is empty when no region is 
	 * active. */
	private Vector<InstrumentedRegion> currentRegions;
	
	/**
	 * Thread ids for current regions. 
	 * The thread id is assigned when the region starts.
	 */
	private Map<InstrumentedRegion, Long> threadByRegion;

	/**
	 * When a instrumentation region ends, the last block needs 
	 * to be added still.
	 */
	private List<InstrumentedRegion> regionsThatEnd;

	/**
	 * Map that maps requestIds to a {@link RequestResult} if there was such 
	 * a result before. Otherwise the entry will be null.
	 */
	private Map<UUID, RequestResult> requestMap;
	
	/**
	 * Construct the strategy object.
	 * @param parent {@link CountingResultCollector} using this strategy.
	 */
	public CollectionStrategyDefault(CountingResultCollector parent) {
		super(parent);
		this.countingResultIndexing = new CountingResultIndexing();
		this.countingResultUpdateIndexing = new CountingResultUpdateIndexing(parent);
		this.countingResultRegionIndexing = new CountingResultRegionIndexing();
		this.countingResultThreadIndexing = new CountingResultThreadIndexing();
		this.lastBasicBlockExecutionSequenceByMethod = new HashMap<UUID, List<Integer>>();
		this.lastLabelBlockExecutionSequenceByMethod = new HashMap<UUID, List<Integer>>();
		this.currentRegions = new Vector<InstrumentedRegion>();
		this.threadByRegion = new HashMap<InstrumentedRegion, Long>();
		this.regionsThatEnd = new LinkedList<InstrumentedRegion>();
		this.requestMap = new HashMap<UUID, RequestResult>();
	}

	/** {@inheritDoc} */
	@Override
	public void clearResults() {
		this.countingResultIndexing.clearResults();
		this.countingResultUpdateIndexing.clearResults();
		this.countingResultRegionIndexing.clearResults();
		this.countingResultThreadIndexing.clearResults();
		this.lastBasicBlockExecutionSequenceByMethod.clear();
		this.lastLabelBlockExecutionSequenceByMethod.clear();
		this.currentRegions.clear();
		this.threadByRegion.clear();
		this.regionsThatEnd.clear();
		this.requestMap.clear();
	}

	/** 
	 * @return Indexing infrastructure for counting results.
	 */
	public CountingResultIndexing getCountingResultIndexing() {
		return countingResultIndexing;
	}

	/** {@inheritDoc} */
	@Override
	public void prepareCountingResults() {
		MethodExecutionRecord lastMethodExecutionDetails = parentResultCollector.getLastMethodExecutionDetails();
		if(lastMethodExecutionDetails == null) {
			log.warning("No method execution details are available. Please make certain that instrumented code has been executed.");
		} else  if(lastMethodExecutionDetails.executionSettings.getAddUpResultsRecursively()) {
			currentResultCollection.getCountingResults().addAll(this.countingResultIndexing.retrieveRecursiveSum(lastMethodExecutionDetails));
		}
	}
	
	/**
	 * Allocate a result.
	 */
	@Override
	public void protocolFutureCount(final ProtocolFutureCountStructure futureCount) {
		CountingResult cr = new CountingResult();
		cr.setQualifiedMethodName(futureCount.canonicalMethodName);
		cr.setMethodExecutionID(futureCount.ownID);
		cr.setObservedElement(parentResultCollector.getInstrumentationContext().getEntitiesToInstrument().get(futureCount.observedEntityID));
		cr.setThreadId(Thread.currentThread().getId());
		cr.setFinal(false);
		cr = (CountingResult) this.countingResultThreadIndexing.apply(cr, futureCount.spawnedThreads);
	}

	/** Add to counting results. */
	@Override
	public boolean protocolCount(ProtocolCountStructure result) {
		final long threadID = Thread.currentThread().getId();
		// Is this an update?
		if(!(result instanceof ProtocolCountUpdateStructure)) {
			// This is not an update so all updates are done.
			this.countingResultUpdateIndexing.setMethodDone(threadID, result.ownID);
			List<InstrumentedRegion> regionsToRemove = new LinkedList<InstrumentedRegion>();
			for(InstrumentedRegion region : this.regionsThatEnd) {
				if(region.getStopMethod().getCanonicalMethodName().equals(result.qualifyingMethodName)) {
					this.countingResultUpdateIndexing.setRegionDone(threadID, region.getId());
					regionsToRemove.add(region);
				}
			}
			this.regionsThatEnd.removeAll(regionsToRemove);
		} else if(result.basicBlockExecutionSequence != null) {
			// This is an update. Replace the block execution sequence with 
			// the part of the sequence that is new since the last update.
			result.basicBlockExecutionSequence = getNewPartOfExecutionSequence(result, lastBasicBlockExecutionSequenceByMethod, result.basicBlockExecutionSequence);
			if(result.blockCountingMode == BlockCountingMode.LabelBlocks) {
				result.labelBlockExecutionSequence = getNewPartOfExecutionSequence(result, lastLabelBlockExecutionSequenceByMethod, result.labelBlockExecutionSequence);
			}
			if(result.blockCountingMode == BlockCountingMode.RangeBlocks
					&& result.rangeBlockExecutionSequence.size() > 0) {
				// consider only the last entered range block?
				int rangeBlockIndex = result.rangeBlockExecutionSequence.get(result.rangeBlockExecutionSequence.size()-1);
				result.rangeBlockExecutionSequence = new ArrayList<Integer>();
				result.rangeBlockExecutionSequence.add(rangeBlockIndex);
			}
		}

		MethodExecutionRecord lastMethodExecutionDetails = parentResultCollector.getLastMethodExecutionDetails();
		if(lastMethodExecutionDetails == null) {
			log.warning("No method execution details are available. Please make certain that instrumented code has been executed.");
		}
		
		CalculatedCounts[] ccounts = calculateResultCounts(result);

		for(int ccountsNum = 0; ccountsNum < ccounts.length; ccountsNum++) {
			Map<ArrayCreation, Long> arrayCreationCounts = null;
			if(result.newArrayCounts != null && 
					result.newArrayCounts.length != 0) {
				// create the map for array creation counts
				arrayCreationCounts = new HashMap<ArrayCreation, Long>();
				List<ArrayCreation> creations = this.parentResultCollector.getInstrumentationContext().getArrayCreations().get(result.qualifyingMethodName);
				for(int i = 0; i < result.newArrayCounts.length; i++) {
					long count = result.newArrayCounts[i];
					arrayCreationCounts.put(creations.get(i), count);
				}
			}
			CountingResult res = new CountingResult();
			res.setRequestID(result.requestID);
			res.setMethodExecutionID(result.ownID);
			res.setCallerID(result.callerID);
			res.setQualifiedMethodName(result.qualifyingMethodName);
			res.setMethodInvocationBeginning(result.executionStart+ccountsNum);
			res.setReportingTime(result.reportingStart);
			res.setOpcodeCounts(ccounts[ccountsNum].opcodeCounts);
			res.overwriteMethodCallCounts(ccounts[ccountsNum].methodCounts);
			res.setArrayCreationCounts(arrayCreationCounts);
			res.setThreadId(threadID);
			res.setObservedElement(this.parentResultCollector.getInstrumentationContext().getEntitiesToInstrument().get(result.observedEntityID));
			res.setFinal(true);
			if(result.blockCountingMode == BlockCountingMode.RangeBlocks) {
				// set the index of the range block, i.e. the number of the section as
				// defined by the user in the instrumentation settings. This 
				// enables the user to find the counts for specific sections.
				final int indexOfRangeBlock = ccounts[ccountsNum].indexOfRangeBlock;
				res.setIndexOfRangeBlock(indexOfRangeBlock);
				
				InstrumentedCodeArea observedRange = 
						this.parentResultCollector.getInstrumentationContext().getRangesByMethod().get(
								result.qualifyingMethodName)[indexOfRangeBlock];
				res.setObservedElement(observedRange);
			} else if(result.blockCountingMode == BlockCountingMode.LabelBlocks) {
				final int labelBlockIndex = result.labelBlockExecutionSequence.get(result.labelBlockExecutionSequence.size()-1);
				for(InstrumentedRegion ir : parentResultCollector.getInstrumentationContext().getInstrumentationRegions()) {
					if(ir != null) {
						if(ir.getStartLabelIds().contains(labelBlockIndex)
								&& result.qualifyingMethodName.equals(ir.getStartMethod().getQualifyingMethodName())) {
							// region started
							if(!currentRegions.contains(ir)) {
								this.parentResultCollector.protocolActiveEntity(ir.getId().toString());
								startRegion(ir);
								res.setObservedElement(this.parentResultCollector.getInstrumentationContext().getEntitiesToInstrument().get(ir.getId()));
								addResultToCollection(res);
								if(regionsThatEnd.contains(ir)) {
									regionsThatEnd.remove(ir);
								}
							}
						}
						// this is not the else case if the region is a single line
						if(this.currentRegions.contains(ir) 
								&& ir.getStopLabelIds().contains(labelBlockIndex)
								&& result.qualifyingMethodName.equals(ir.getStopMethod().getQualifyingMethodName())) {
							// region ended
							if(ir.getStopPointType() == StopPointType.BEFORE_SPECIFIED_LABEL) {
								// make sure observers are updated
								if(!this.regionsThatEnd.contains(ir)) {
									endRegion(ir);
								}
							} else if(ir.getStopPointType() == StopPointType.AFTER_SPECIFIED_LABEL) {
								res.setObservedElement(this.parentResultCollector.getInstrumentationContext().getEntitiesToInstrument().get(ir.getId()));
								if(!this.regionsThatEnd.contains(ir)) {
									endRegion(ir);
								}
							}
						}
					}
				}
			}
			if(!this.regionsThatEnd.isEmpty()) {
				// we left a region, report the currently active region
				if(currentRegions.size() > 0) {
					this.parentResultCollector.protocolActiveEntity(currentRegions.lastElement().getId().toString());
				} else {
					this.parentResultCollector.protocolActiveEntity(null);
				}
			}
			res.setResultCollection(this.currentResultCollection);
			// When this result is not an update, add it to the permanent results
			if(!(result instanceof ProtocolCountUpdateStructure)) {
				if(result.blockCountingMode != BlockCountingMode.LabelBlocks) {
					if(this.parentResultCollector.getInstrumentationContext().getCountingMode() == CountingMode.Regions) {
						if(this.currentRegions != null && !this.currentRegions.isEmpty()) {
							res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
							this.countingResultRegionIndexing.add(res, this.currentRegions);
						} else {
							// no region active, skip result
						}
					} else {
						res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
						this.countingResultIndexing.add(res, result.reportingStart);

						if(lastMethodExecutionDetails != null && lastMethodExecutionDetails.executionSettings.getAddUpResultsRecursively()) {
							// adding will be done on retrieveResults()
							return true;
						} else {
							addResultToCollection(res);
						}
					}
				} else {
					return false;
				}
			} else {
				// result is an instance of ProtocolCountUpdateStructure

				if(this.parentResultCollector.getInstrumentationContext().getCountingMode() == CountingMode.Regions) {
					boolean onlyAddToMostRecentRegion = this.parentResultCollector.getLastMethodExecutionDetails().executionSettings.getOnlyAddCountsForMostRecentRegion();
					// add up for the counting region if necessary
					if(this.currentRegions != null && !this.currentRegions.isEmpty()) {
						if(onlyAddToMostRecentRegion) {
							Iterator<InstrumentedRegion> crIter = this.currentRegions.iterator();
							InstrumentedRegion mostRecentRegion = crIter.next();
							while(crIter.hasNext()) {
								// find the most current region for the thread
								final InstrumentedRegion currentRegion = crIter.next();
								if(threadByRegion.get(currentRegion) == threadID) {
									mostRecentRegion = currentRegion;
								}
							}
							this.countingResultRegionIndexing.add(res, Arrays.asList(mostRecentRegion));
							this.countingResultUpdateIndexing.add(res, mostRecentRegion.getId());
						} else {
							this.countingResultRegionIndexing.add(res, this.currentRegions);
							for(InstrumentedRegion currentRegion : this.currentRegions) {
								this.countingResultUpdateIndexing.add(res, currentRegion.getId());
							}	
						}
					}
					if(this.regionsThatEnd != null && !this.regionsThatEnd.isEmpty()) {
						final int labelBlockIndex = result.labelBlockExecutionSequence.get(result.labelBlockExecutionSequence.size()-1);
						List<InstrumentedRegion> regionsToRemove = new LinkedList<InstrumentedRegion>();
						// first handle all regions that ended before this label to maintain correct sequence
						for(InstrumentedRegion r : regionsThatEnd) {
							if(r.getStopPointType() == StopPointType.BEFORE_SPECIFIED_LABEL) {
								// make sure observers are updated
								this.countingResultUpdateIndexing.setRegionDone(threadID, r.getId());
	
								// the current label is not in the region; the region is done
								if(!regionsToRemove.contains(r)) {
									regionsToRemove.add(r);
								}
							}
						}
						// then handle all regions that end after this label to maintain correct sequence
						for(InstrumentedRegion r : regionsThatEnd) {
							if(r.getStopPointType() == StopPointType.AFTER_SPECIFIED_LABEL) {
								if(r.getStopLabelIds().contains(labelBlockIndex)
										&& result.qualifyingMethodName.equals(r.getStopMethod().getQualifyingMethodName())) {
									// the current label is a stop label, so we are still in the region
									if(onlyAddToMostRecentRegion) {
										InstrumentedRegion mostRecentRelevantRegion = null;
										for(InstrumentedRegion innerR : regionsThatEnd) {
											// regions that ended before this label cannot be most recent
											if(innerR.getStopPointType() != StopPointType.BEFORE_SPECIFIED_LABEL) {
												mostRecentRelevantRegion = innerR;
												break;
											}
										}
										if(mostRecentRelevantRegion != null) {
											this.countingResultRegionIndexing.add(res, Arrays.asList(mostRecentRelevantRegion));
											this.countingResultUpdateIndexing.add(res, mostRecentRelevantRegion.getId());
										}
									} else {
										this.countingResultRegionIndexing.add(res, Arrays.asList(r));
										this.countingResultUpdateIndexing.add(res, r.getId());
									}
	
									// make sure observers are updated
									this.countingResultUpdateIndexing.setRegionDone(threadID, r.getId());
								} else {
									// the current label is not in the region; the region is done
									if(!regionsToRemove.contains(r)) {
										regionsToRemove.add(r);
									}
								}
							}
						}
						this.regionsThatEnd.removeAll(regionsToRemove);
					}
				} else {
					res = this.countingResultThreadIndexing.apply(res, result.spawnedThreads);
					this.countingResultUpdateIndexing.add(res, lastBasicBlockExecutionSequenceByMethod.get(result.ownID));
				}
			}

		}
		return true;
	}

	/**
	 * The given region has been entered. Update data relevant structures.
	 * @param ir Region that started.
	 */
	private void startRegion(InstrumentedRegion ir) {
		this.currentRegions.add(ir);
		log.info("Region started: " + ir);
		this.threadByRegion.put(ir, Thread.currentThread().getId());
	}

	/**
	 * The given region has been left. Update data relevant structures.
	 * @param ir Region that ended.
	 */
	private void endRegion(InstrumentedRegion ir) {
		this.regionsThatEnd.add(ir);
		this.currentRegions.remove(ir);
		log.info("Region ended: " + ir);
		this.threadByRegion.remove(ir);
	}

	/** 
	 * @param result {@link CountingResult}.
	 * @param lastExecutionSequence Previous execution sequence.
	 * @param newExecutionSequence Current execution sequence.
	 * @return The new part of the execution sequence.
	 */
	private ArrayList<Integer> getNewPartOfExecutionSequence(
			final ProtocolCountStructure result, 
			final Map<UUID, List<Integer>> lastExecutionSequence, 
			final ArrayList<Integer> newExecutionSequence) {
		List<Integer> lastExSeq = lastExecutionSequence.get(result.ownID);
		if(lastExSeq != null) {
			Integer lastExSeqLength = lastExSeq.size();
			Integer newExSeqLength = newExecutionSequence.size();
			if(lastExSeqLength != null) {
				ArrayList<Integer> newSequence = new ArrayList<Integer>();
				for(int i = lastExSeqLength; i < newExSeqLength; i++) {
					newSequence.add(newExecutionSequence.get(i));
				}
				// update execution sequence
				lastExecutionSequence.put(result.ownID, new ArrayList<Integer>(newExecutionSequence));
				return newSequence;
			}
		} else {
			// update execution sequence
			lastExecutionSequence.put(result.ownID, new ArrayList<Integer>(newExecutionSequence));
		}
		return newExecutionSequence;
	}

	/**
	 * Adds the result to {@link #currentResultCollection}.
	 * @param res CountingResult.
	 */
	private void addResultToCollection(CountingResult res) {
		final UUID requestID = res.getRequestID();
		if(requestID != null) {
			RequestResult requestResult = requestMap.get(requestID);
			if(requestResult == null) {
				// add a new RequestResult
				requestResult = new RequestResult();
				requestResult.setRequestId(requestID);
				requestMap.put(requestID, requestResult);
				this.currentResultCollection.getRequestResults().add(requestResult);
			}
			// add the result to the RequestResult
			requestResult.getCountingResults().add(res);
			res.setRequestResult(requestResult);
		} else {
			if(res.getSpawnedThreadedCountingResults().isEmpty()) {
				if(res.getThreadedCountingResultSource() == null) {
					this.currentResultCollection.getCountingResults().add(res);
				} else {
					// the parent result is already added, do nothing.
				}
			} else {
				// not a threaded result
				this.currentResultCollection.getCountingResults().add(res);
			}
		}
	}

	/**
	 * Based on the kind of information available in the 
	 * {@link ProtocolCountStructure}, calculate result counts.
	 * @param result The recorded data.´
	 * @return The result counts.
	 * @see CalculatedCounts
	 */
	private CalculatedCounts[] calculateResultCounts(
			ProtocolCountStructure result) {
		// {@link BlockResultCalculation} helper
		BlockResultCalculation blockCalculation = new BlockResultCalculation(parentResultCollector.getInstrumentationContext());
		// the result (calculated counts)
		CalculatedCounts[] ccounts;
		SortedMap<String, Long> methodCounts = new TreeMap<String, Long>();
		if(result.blockCountingMode == BlockCountingMode.BasicBlocks) {
			if(result.basicBlockExecutionSequence != null) {
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(result);
			} else {
				ccounts = new CalculatedCounts[] {
						blockCalculation.calculateCountsFromBBCounts(
						result.qualifyingMethodName, 
						result.opcodeCounts,
						new long[CountingResultBase.MAX_OPCODE], // opcode counts
						methodCounts)
				};
			}
		} else if (result.blockCountingMode == BlockCountingMode.LabelBlocks) {//Label blocks!
			if(result.labelBlockExecutionSequence != null) {
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(result);
			} else {
				throw new RuntimeException("Label blocks currently only support calculation from block execution sequences.");
			}
		} else if (result.blockCountingMode == BlockCountingMode.RangeBlocks) {//Ranges!
			if(result.basicBlockExecutionSequence != null) {
				result.rangeBlockExecutionSequence = removeDuplicateSequencesFromList(result.rangeBlockExecutionSequence);
				// use of basic block execution sequence for range blocks is correct
				ccounts = blockCalculation.calculateCountsFromBlockExecutionSequence(result);
			} else {
				ccounts = blockCalculation.calculateCountsFromRBCounts(
						result.qualifyingMethodName, 
						result.opcodeCounts,
						new long[CountingResultBase.MAX_OPCODE], // opcode counts
						methodCounts);
			}
		} else {
			// check proper length
			if(result.methodCallCounts.length != result.calledMethods.length) {
				throw new IllegalArgumentException("Reported method call count structures must match in length.");
			}
			// create a HashMap for the method signatures and their counts
			for(int i = 0; i < result.methodCallCounts.length; i++) {
				methodCounts.put(result.calledMethods[i], result.methodCallCounts[i]);//TODO too much effort...
			}
			ccounts = new CalculatedCounts[1];//again, too many conversions...
			ccounts[0] = new CalculatedCounts();
			ccounts[0].opcodeCounts = result.opcodeCounts;
			ccounts[0].methodCounts = methodCounts;
		}
		return ccounts;
	}

	/**
	 * Remove duplicate sequences from the list and return the filtered result.
	 * Duplicate sequences are identical values that appear multiple times in 
	 * the list with no other values in between. I.e. in the list [1,2,3,3,3,4],
	 * [3,3,3] are duplicate sequences and will be replaced by [3].
	 * @param list List to remove the duplicate sequences from.
	 * @return Filtered list.
	 */
	private <T> ArrayList<T> removeDuplicateSequencesFromList(
			ArrayList<T> list) {
		ArrayList<T> result = new ArrayList<T>();
		T lastEntry = null;
		for(T entry : list) {
			if(lastEntry == entry) {
				continue;
			}
			result.add(entry);
			lastEntry = entry;
		}
		return result;
	}
}
