package destroyer;

import org.apache.log4j.Logger;

public class Destroyer {
	private static Logger logger = Logger.getLogger(Destroyer.class);
	
	public void Destroy(String instanceId) {
		logger.info(instanceId);
	}
}
