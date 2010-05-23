package org.codefaces.core.models;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ObjectUtils;
import org.codefaces.core.events.WorkspaceChangeEvent;
import org.codefaces.core.events.WorkspaceChangeEventListener;
import org.eclipse.rwt.SessionSingletonBase;

public class Workspace {
	private RepoBranch workingBranch;

	private final List<WorkspaceChangeEventListener> changeListeners;

	private final ReentrantLock lock = new ReentrantLock();

	
	/**
	 * This constructor is only used for testing purpose.
	 */
	public Workspace() {
		changeListeners = new CopyOnWriteArrayList<WorkspaceChangeEventListener>();
	}

	public RepoBranch getWorkingBranch() {
		return workingBranch;
	}

	/**
	 * Change the current working repository branch without changing the working
	 * repository.
	 * 
	 * @param branch
	 *            a new repository branch
	 */
	public void update(RepoBranch branch) {
		lock.lock();
		try {
			if (ObjectUtils.equals(workingBranch, branch)) {
				return;
			}

			workingBranch = branch;
			WorkspaceChangeEvent evt = new WorkspaceChangeEvent(this,
					workingBranch);
			notifyChange(evt);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Attach a listener to the listener list
	 * 
	 * @param listener
	 *            a listener
	 */
	public void addWorkSpaceChangeEventListener(
			WorkspaceChangeEventListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Remove the given listener from the listener list
	 * 
	 * @param listener
	 *            the listener being removed
	 */
	public void removeWorkSpaceChangeEventListener(
			WorkspaceChangeEventListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Notify the listeners that the workspace is changed. NOTE: if any listener
	 * is null, it will cause a runtime error.
	 */
	private void notifyChange(WorkspaceChangeEvent event) {
		for (WorkspaceChangeEventListener listener : changeListeners) {
			listener.workspaceChanged(event);
		}
	}

	public static Workspace getCurrent() {
		return (Workspace) SessionSingletonBase.getInstance(Workspace.class);
	}
}