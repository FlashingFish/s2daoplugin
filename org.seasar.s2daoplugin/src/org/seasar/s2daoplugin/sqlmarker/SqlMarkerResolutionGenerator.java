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
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.PlatformUI;
import org.seasar.s2daoplugin.Messages;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.sqlopener.wizard.SqlCreationWizardUtil;
import org.seasar.s2daoplugin.util.ArrayUtil;
import org.seasar.s2daoplugin.util.IDEUtil;
import org.seasar.s2daoplugin.util.IOUtil;

public class SqlMarkerResolutionGenerator implements
		IMarkerResolutionGenerator2 {

	private static final String KEY_SQL_MARKER_QUICKFIX = "sql_marker_quickfix";
	private static final String PATH_SQL_MARKER_QUICKFIX =
		"icons/sql_marker_quickfix.gif";
	private static final IMarkerResolution OPEN_ALL_RESOLUTION =
		new OpenAllSqlsResolution();
	private static final IMarkerResolution CREATE_RESOLUTION =
		new CreateSqlResolution();
	
	static {
		S2DaoPlugin plugin = S2DaoPlugin.getDefault();
		InputStream in = null;
		try {
			in = plugin.openStream(new Path(PATH_SQL_MARKER_QUICKFIX));
			ImageRegistry registry = plugin.getImageRegistry();
			registry.put(KEY_SQL_MARKER_QUICKFIX, new Image(PlatformUI
					.getWorkbench().getDisplay(), in));
		} catch (IOException e) {
		} finally {
			IOUtil.close(in);
		}
	}
	
	public boolean hasResolutions(IMarker marker) {
		try {
			return S2DaoConstants.ID_SQL_MARKER.equals(marker.getType());
		} catch (CoreException e) {
			return false;
		}
	}
	
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution[] resolutions = createResolutionEachSqlFile(marker);
		if (resolutions.length > 1) {
			resolutions = add(resolutions, OPEN_ALL_RESOLUTION);
		}
		return add(resolutions, CREATE_RESOLUTION);
	}
	
	private IMarkerResolution[] createResolutionEachSqlFile(IMarker marker) {
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
		ImageRegistry registry = S2DaoPlugin.getDefault().getImageRegistry();
		return registry.get(KEY_SQL_MARKER_QUICKFIX);
	}
	
	
	private static class OpenSqlResolution implements IMarkerResolution2 {
		
		private IFile sql;
		
		public OpenSqlResolution(IFile sql) {
			this.sql = sql;
		}
		
		public String getDescription() {
			InputStream in = null;
			Reader reader = null;
			StringBuffer ret = new StringBuffer();
			try {
				in = sql.getContents();
				reader = new BufferedReader(new InputStreamReader(in));
				int ch = -1;
				while ((ch = reader.read()) != -1) {
					ret.append((char) ch);
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
			return Messages.getMessage("SQLMarker.open", sql.getName());
		}

		public void run(IMarker marker) {
			IDEUtil.openEditor(sql);
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
