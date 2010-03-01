package p2p.web.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.directwebremoting.ScriptSession;
import org.directwebremoting.proxy.ScriptProxy;

public class ReverseAjaxThread extends Thread {
	private static ReverseAjaxThread INSTANCE;
	private Set<ScriptSession> scriptSessions = new HashSet<ScriptSession>();
	private boolean canExecute = false;

	public static synchronized ReverseAjaxThread getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ReverseAjaxThread();
			INSTANCE.start();
		}
		return INSTANCE;
	}

	public void run() {
		while(true) {
			synchronized(INSTANCE){
				while(!canExecute) {
					try { wait(); } catch (InterruptedException e) {}
				}
				canExecute = false;
			}
			for (ScriptSession scriptSession : scriptSessions) {
				if (!scriptSession.isInvalidated()) {
					String callback = (String) scriptSession.getAttribute("callback");
					String storeId = (String) scriptSession.getAttribute("storeId");
					List<Result> data = (List<Result>) scriptSession.getAttribute("results");
					new ScriptProxy(scriptSession).addFunctionCall(callback, storeId, data);
				} else {
					synchronized (INSTANCE) {
						scriptSessions.remove(scriptSession);
					}
				}
			}
		}
	}

	public synchronized void addScriptSession(ScriptSession scriptSession) {
		// use a copy so that code reading from scriptSessions does not need to be synchronized
		Set<ScriptSession> scriptSessionsCopy = new HashSet<ScriptSession>(scriptSessions);
		scriptSessionsCopy.remove(scriptSession);
		scriptSessionsCopy.add(scriptSession);
		scriptSessions = scriptSessionsCopy;
		synchronized(INSTANCE){
			canExecute = true;
			notify();
		}
	}

}
