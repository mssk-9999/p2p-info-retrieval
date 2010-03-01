package p2p.jtella.service;

public interface SearchService {
	
	/**
	 * Propagate a search to the network
	 * @param query
	 */
	public void searchNetwork(String query);
	
	/**
	 * Destroy the service. Must be called when shutting
	 * down the bundle.
	 */
	public void destroy();
}
