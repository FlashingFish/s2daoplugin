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

public interface ISqlMarker {

	String ATTRIBUTE_SQL_PATHS = "sqlpaths";
	
	String SQL_PATHS_SEPARATOR = ",";
	
	int getAvailableSqlFileSize();
	
	IMarker getMarker();
	
	void setStart(int start);
	
	int getStart();
	
	void setEnd(int end);
	
	int getEnd();

	IFile[] getSqlFiles();
	
	void setSqlFiles(IFile[] sqlFiles);
	
	void addSqlFile(IFile sqlFile);
	
	void removeSqlFile(IFile sqlFile);
}
