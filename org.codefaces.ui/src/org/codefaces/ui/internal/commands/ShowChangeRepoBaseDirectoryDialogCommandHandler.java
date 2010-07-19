package org.codefaces.ui.internal.commands;

import org.codefaces.ui.internal.dialogs.ChangeRepoBaseDirectoryDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowChangeRepoBaseDirectoryDialogCommandHandler extends AbstractHandler{
	public static final String ID = 
		"org.codefaces.ui.internal.commands.showChangeRepoBaseDirectoryDialogCommand";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		new ChangeRepoBaseDirectoryDialog(shell).open();
		return null;
	}

}