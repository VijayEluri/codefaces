package org.codefaces.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodeFacesUIActivator extends AbstractUIPlugin {
	private static final String CODE = "#{code}";

	private static final String LANG = "#{lang}";

	private static final String EDITOR_TEMPLATE_PATH = "web/code_editor_template.html";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.codefaces.ui";

	// The shared instance
	private static CodeFacesUIActivator plugin;

	private String codeEditorTemplate;

	/**
	 * The constructor
	 */
	public CodeFacesUIActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CodeFacesUIActivator getDefault() {
		return plugin;
	}

	public String getCodeEditorHTML(String lang, String code) {
		try {
			if (codeEditorTemplate == null) {
				codeEditorTemplate = getCodeEditorTemplate();
			}

			StringBuilder builder = new StringBuilder(codeEditorTemplate);

			int langInsertIndex = builder.indexOf(LANG);
			builder.replace(langInsertIndex, langInsertIndex + LANG.length(),
					lang);

			int codeInsertIndex = builder.indexOf(CODE);
			builder.replace(codeInsertIndex, codeInsertIndex + CODE.length(),
					StringEscapeUtils.escapeHtml(code));

			return builder.toString();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	private String getCodeEditorTemplate() throws IOException {
		InputStream inputStream = FileLocator.openStream(getBundle(), new Path(
				EDITOR_TEMPLATE_PATH), false);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		StringBuilder builder = new StringBuilder();
		String inputLine;
		while ((inputLine = reader.readLine()) != null) {
			builder.append(inputLine);
		}
		
		return builder.toString();
	}

	/**
	 * Initialize the image registry
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		putImageInRegistry(registry, Images.IMG_BRANCHES, "icons/branches.gif");
		putImageInRegistry(registry, Images.IMG_ERRORS, "icons/errors.gif");
	}

	/**
	 * Put an image into the image registry
	 * 
	 * @param registry
	 *            the image registry
	 * @param imgID
	 *            a image ID
	 * @param path
	 *            the path of the image. e.g. icons/abc.gif
	 */
	private void putImageInRegistry(ImageRegistry registry, final String imgID,
			final String path) {
		registry.put(imgID, AbstractUIPlugin.imageDescriptorFromPlugin(
				PLUGIN_ID, path));
	}

}
