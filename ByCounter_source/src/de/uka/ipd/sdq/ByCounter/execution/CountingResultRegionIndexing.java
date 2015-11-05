package de.uka.ipd.sdq.ByCounter.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * Indexing for counting regions.
 * @author Martin Krogmann
 */
public class CountingResultRegionIndexing {
	
	/**
	 * Index of results by {@link InstrumentedRegion}.
	 */
	Map<InstrumentedRegion, CountingResult> results;
	
	/**
	 * Construct a new index.
	 */
	public CountingResultRegionIndexing() {
		this.results = new HashMap<InstrumentedRegion, CountingResult>();
	}

	/**
	 * @param res Partial counting result.
	 * @param currentRegions Region to which the result belongs.
	 */
	public void add(CountingResult res, List<InstrumentedRegion> currentRegions) {
		for(InstrumentedRegion region : currentRegions) {
			CountingResult rs = this.results.get(region);
			if(rs == null) {
				// no entry for this region id yet
				res.setObservedElement(region);
				this.results.put(region, res);
			} else {
				// add up with the existing results
				rs.add(res);
			}
		}
	}

	/**
	 * Clear the internal map.
	 */
	public void clearResults() {
		this.results.clear();
	}
}
