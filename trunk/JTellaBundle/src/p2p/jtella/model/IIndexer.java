package p2p.jtella.model;

public interface IIndexer {
	
	/**
	 * Search the index for the specified query. Maintains the 
	 * integrity of the original query, but replaces the search
	 * terms with the search results.
	 * @param query
	 * @return The search results in JSON string format.
	 */
	public String search(String line);

	/**
	 * Destroy the service. Must be called when shutting
	 * down the bundle.
	 */
	public void destroy();
}
