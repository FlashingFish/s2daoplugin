/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2daoplugin.sqlmarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.s2daoplugin.Messages;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.sqlopener.wizard.SqlCreationWizardUtil;
import org.seasar.s2daoplugin.util.ArrayUtil;
import org.seasar.s2daoplugin.util.IDEUtil;
import org.seasar.s2daoplugin.util.IOUtil;

public class SqlMarkerResolutionGenerator implements
		IMarkerResolutionGenerator2 {

	private static final IMarkerResolution OPEN_ALL_RESOLUTION =
		new OpenAllSqlsResolution();
	private static final IMarkerResolution CREATE_RESOLUTION =
		new CreateSqlResolution();
	private static final Image quickfixImage;
	
	static {
		ImageDescriptor desc = S2DaoPlugin.imageDescriptorFromPlugin(
				S2DaoConstants.ID_PLUGIN, "icons/sql_marker_quickfix.gif");
		quickfixImage = desc.createImage();
	}
	
	public boolean hasResolutions(IMarker marker) {
		try {
			return S2DaoConstants.ID_SQL_MARKER.equals(marker.getType());
		} catch (CoreException e) {
			return false;
		}
	}
	
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution[] resolutions = createOpenResolutionEachSqlFile(marker);
		if (resolutions.length > 1) {
			resolutions = add(resolutions, OPEN_ALL_RESOLUTION);
		}
		return add(resolutions, CREATE_RESOLUTION);
	}
	
	private IMarkerResolution[] createOpenResolutionEachSqlFile(IMarker marker) {
		IFile[] sqls = getSqlMarker(marker).getSqlFiles();
		IMarkerResolution[] resolutions = new IMarkerResolution[sqls.length];
		for (int i = 0; i < sqls.length; i++) {
			resolutions[i] = new OpenSqlResolution(sqls[i]);
		}
		return resolutions;
	}
	
	private IMarkerResolution[] add(IMarkerResolution[] resolutions,
			IMarkerResolution resolution) {
		return (IMarkerResolution[]) ArrayUtil.add(resolutions, resolution);
	}
	
	private static ISqlMarker getSqlMarker(IMarker marker) {
		return new SqlMarker(marker);
	}
	
	private static Image getQuickFixImage() {
		return quickfixImage;
	}
	
	
	private static class OpenSqlResolution implements IMarkerResolution2 {
		
		private IFile sqlFile;
		
		public OpenSqlResolution(IFile sqlFile) {
			this.sqlFile = sqlFile;
		}
		
		public String getDescription() {
			if (!sqlFile.exists()) {
				return "";
			}
			InputStream in = null;
			BufferedReader reader = null;
			StringBuffer ret = new StringBuffer();
			try {
				in = sqlFile.getContents();
				reader = new BufferedReader(new InputStreamReader(in,
						sqlFile.getCharset()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					// ‹ó”’‚Í&nbsp;‚É‚µ‚Ä‚àƒ_ƒ‚¾‚Á‚½c
					ret.append(line).append("<br>");
				}
			} catch (CoreException ignore) {
			} catch (IOException ignore) {
			} finally {
				IOUtil.close(in);
				IOUtil.close(reader);
			}
			return ret.toString();
		}
		
		public Image getImage() {
			return getQuickFixImage();
		}
		
		public String getLabel() {
			return Messages.getMessage("SQLMarker.open", sqlFile.getName());
		}

		public void run(IMarker marker) {
			IDEUtil.openEditor(sqlFile);
		}
	}
	
	private static class OpenAllSqlsResolution implements IMarkerResolution2 {

		public String getDescription() {
			return "";
		}
		
		public Image getImage() {
			return getQuickFixImage();
		}
		
		public String getLabel() {
			return Messages.getMessage("SQLMarker.openAll");
		}

		public void run(IMarker marker) {
			IDEUtil.openEditors(getSqlMarker(marker).getSqlFiles());
		}
	}

	private static class CreateSqlResolution implements IMarkerResolution2 {

		public String getDescription() {
			return "";
		}
		
		public Image getImage() {
			return getQuickFixImage();
		}
		
		public String getLabel() {
			return Messages.getMessage("SQLMarker.create");
		}

		public void run(IMarker marker) {
			if (marker.getResource().getType() != IResource.FILE) {
				return;
			}
			ICompilationUnit unit = JavaCore.createCompilationUnitFrom(
					(IFile) marker.getResource());
			int offset = getSqlMarker(marker).getStart();
			try {
				IJavaElement element = unit.getElementAt(offset);
				if (element instanceof IMethod) {
					SqlCreationWizardUtil.open((IMethod) element);
				}
			} catch (JavaModelException e) {
				S2DaoPlugin.log(e);
			}
		}
	}

}
