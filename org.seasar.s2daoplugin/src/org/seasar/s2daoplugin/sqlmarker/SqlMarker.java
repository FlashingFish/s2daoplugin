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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.util.ArrayUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class SqlMarker implements ISqlMarker {

	private IMarker marker;
	
	public SqlMarker(IMarker marker) {
		if (marker == null) {
			throw new IllegalArgumentException();
		}
		this.marker = marker;
	}
	
	public int getAvailableSqlFileSize() {
		int count = 0;
		IFile[] sqlFiles = getSqlFiles();
		for (int i = 0; i < sqlFiles.length; i++) {
			if (sqlFiles[i].exists()) {
				count++;
			}
		}
		return count;
	}
	
	public IMarker getMarker() {
		return marker;
	}
	
	public void setStart(int start) {
		try {
			marker.setAttribute(IMarker.CHAR_START, new Integer(start));
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	public int getStart() {
		return marker.getAttribute(IMarker.CHAR_START, 0);
	}
	
	public void setEnd(int end) {
		try {
			marker.setAttribute(IMarker.CHAR_END, new Integer(end));
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	public int getEnd() {
		return marker.getAttribute(IMarker.CHAR_END, 0);
	}
	
	public IFile[] getSqlFiles() {
		String[] sqlpaths = getSqlPathsAttribute();
		IFile[] sqlFiles = new IFile[sqlpaths.length];
		for (int i = 0; i < sqlpaths.length; i++) {
			sqlFiles[i] = getProject().getFile(sqlpaths[i]);
		}
		return sqlFiles;
	}
	
	public void setSqlFiles(IFile[] sqlFiles) {
		String[] sqlPaths = new String[sqlFiles.length];
		for (int i = 0; i < sqlFiles.length; i++) {
			sqlPaths[i] = toSqlPathString(sqlFiles[i]);
		}
		setSqlPathsAttribute(sqlPaths);
	}
	
	public void addSqlFile(IFile sqlFile) {
		String sqlFilePath = toSqlPathString(sqlFile);
		String[] sqlPaths = getSqlPathsAttribute();
		setSqlPathsAttribute((String[]) ArrayUtil.add(sqlPaths, sqlFilePath));
	}
	
	public void removeSqlFile(IFile sqlFile) {
		String sqlFilePath = toSqlPathString(sqlFile);
		String[] sqlPaths = getSqlPathsAttribute();
		setSqlPathsAttribute((String[]) ArrayUtil.remove(sqlPaths, sqlFilePath));
	}
	
	private String toSqlPathString(IFile file) {
		return file.getProjectRelativePath().toOSString();
	}
	
	private String[] getSqlPathsAttribute() {
		try {
			return StringUtil.split((String) marker.getAttribute(
					ATTRIBUTE_SQL_PATHS), SQL_PATHS_SEPARATOR);
		} catch (CoreException e) {
			return StringUtil.EMPTY_STRINGS;
		}
	}
	
	private void setSqlPathsAttribute(String[] sqlPaths) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < sqlPaths.length; i++) {
			buf.append(sqlPaths[i]).append(SQL_PATHS_SEPARATOR);
		}
		try {
			marker.setAttribute(ATTRIBUTE_SQL_PATHS, buf.toString());
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	private IProject getProject() {
		return marker.getResource().getProject();
	}

}
