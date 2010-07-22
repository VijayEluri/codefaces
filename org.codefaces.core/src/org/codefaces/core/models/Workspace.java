package org.codefaces.core.models;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ObjectUtils;
import org.codefaces.core.events.WorkspaceChangeEvent;
import org.codefaces.core.events.WorkspaceChangeListener;
import org.eclipse.rwt.SessionSingletonBase;

public class Workspace {
	private RepoFolder workingBaseDirectory;

	private final List<WorkspaceChangeListener> changeListeners;

	private final ReentrantLock lock = new ReentrantLock();

	
	/**
	 * This constructor is only used for testing purpose.
	 */
	protected Workspace() {
		changeListeners = new CopyOnWriteArrayList<WorkspaceChangeListener>();
	}

	public RepoFolder getWorkingBaseDirectory() {
		return workingBaseDirectory;
	}

	/**
	 * Change the current working repository base directory without changing the working
	 * repository.
	 * 
	 * @param newBaseDirectory
	 *            a new repository folder 
	 */
	public void update(RepoFolder newBaseDirectory) {
		lock.lock();
		try {
			if (ObjectUtils.equals(workingBaseDirectory, newBaseDirectory)) {
				return;
			}

			workingBaseDirectory = newBaseDirectory;
			WorkspaceChangeEvent evt = new WorkspaceChangeEvent(this,
					workingBaseDirectory);
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
	public void addWorkspaceChangeListener(
			WorkspaceChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Remove the given listener from the listener list
	 * 
	 * @param listener
	 *            the listener being removed
	 */
	public void removeWorkspaceChangeListener(
			WorkspaceChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Notify the listeners that the workspace is changed. NOTE: if any listener
	 * is null, it will cause a runtime error.
	 */
	private void notifyChange(WorkspaceChangeEvent event) {
		for (WorkspaceChangeListener listener : changeListeners) {
			listener.workspaceChanged(event);
		}
	}

	public static Workspace getCurrent() {
		return (Workspace) SessionSingletonBase.getInstance(Workspace.class);
	}
}
