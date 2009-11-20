package p2p.info.retrieval.web.model;

import java.util.HashSet;
import java.util.Set;

import org.directwebremoting.ScriptSession;
import org.directwebremoting.proxy.ScriptProxy;

public class ReverseAjaxThread extends Thread {
	private static ReverseAjaxThread INSTANCE;
	private Set<ScriptSession> scriptSessions = new HashSet<ScriptSession>();

	public static synchronized ReverseAjaxThread getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ReverseAjaxThread();
			INSTANCE.start();
		}
		return INSTANCE;
	}

	public void run() {
		while (true) {
			for (ScriptSession scriptSession : scriptSessions) {
				if (!scriptSession.isInvalidated()) {
					// alert('hello')
					new ScriptProxy(scriptSession).addFunctionCall("alert", "hello");
				} else {
					synchronized (this) {
						scriptSessions.remove(scriptSession);
					}
				}
			}
			try {
				// sleep for 10 seconds
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	public synchronized void addScriptSession(ScriptSession scriptSession) {
		// use a copy so that code reading from scriptSessions does not need to be synchronized
		Set<ScriptSession> scriptSessionsCopy = new HashSet<ScriptSession>(scriptSessions);
		scriptSessionsCopy.add(scriptSession);
		scriptSessions = scriptSessionsCopy;
	}

}
