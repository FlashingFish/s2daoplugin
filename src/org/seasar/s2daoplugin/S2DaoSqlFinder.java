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
package org.seasar.s2daoplugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.JavaUtil;

public class S2DaoSqlFinder implements S2DaoConstants {

	public IFolder guessSqlStoredFolder(IMethod method) {
		if (method == null) {
			return null;
		}
		SqlCountingHandler handler = new SqlCountingHandler();
		process(method, handler);
		return handler.getResult();
	}
	
	public IFile[] findSqlFiles(IMethod method) {
		if (method == null) {
			return EMPTY_FILES;
		}
		String sqlBaseName = S2DaoUtil.createBaseSqlFileName(method);
		SqlCollectingHandler handler = new SqlCollectingHandler(sqlBaseName);
		process(method, handler);
		return handler.getResult();
	}
	
	private void process(IMethod method, IFileHandler handler) { 
		String packageName = getPackageName(method);
		IPackageFragmentRoot[] roots = JavaProjectUtil
				.findPackageFragmentRootsSharedOutputLocation(method.getResource());
		for (int i = 0; i < roots.length; i++) {
			IPackageFragment fragment = roots[i].getPackageFragment(packageName);
			if (!fragment.exists()) {
				continue;
			}
			Object[] resources;
			try {
				resources = fragment.getNonJavaResources();
			} catch (JavaModelException e) {
				S2DaoPlugin.log(e);
				continue;
			}
			for (int j = 0; j < resources.length; j++) {
				if (resources[j] instanceof IFile) {
					handler.process((IFile) resources[j]);
				}
			}
			if (handler instanceof ILoopNext) {
				((ILoopNext) handler).nextLoop(fragment);
			}
		}
	}
	
	public IMethod findMethodFromSql(IFile file) {
		if (file == null || !"sql".equals(file.getFileExtension())) {
			return null;
		}
		String[] split = S2DaoUtil.splitSqlFileName(file.getName());
		if (split.length != 2) {
			return null;
		}
		IPath packagePath = JavaUtil.getPackagePath(file);
		if (packagePath == null) {
			return null;
		}
		IJavaProject project = JavaCore.create(file.getProject());
		IPath[] srcPaths = JavaProjectUtil.getSourceFolderPaths(project);
		for (int i = 0; i < srcPaths.length; i++) {
			try {
				IPackageFragment pack =
					project.findPackageFragment(srcPaths[i].append(packagePath));
				if (pack == null) {
					continue;
				}
				IType type = findType(pack, split[0]);
				if (type == null) {
					continue;
				}
				IMethod[] methods = type.getMethods();
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].getElementName().equals(split[1])) {
						return methods[j];
					}
				}
			} catch (JavaModelException e) {
				S2DaoPlugin.log(e);
			}
		}
		return null;
	}

	private String getPackageName(IMethod method) {
		return method.getDeclaringType().getPackageFragment().getElementName().toString();
	}
	
	private IType findType(IPackageFragment fragment, String typeName)
			throws JavaModelException {
		IJavaElement[] elements = fragment.getChildren();
		for (int i = 0; i < elements.length; i++) {
			if (!(elements[i] instanceof ICompilationUnit)) {
				continue;
			}
			ICompilationUnit unit = (ICompilationUnit) elements[i];
			IType[] types = unit.getAllTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].getElementName().equals(typeName)) {
					return types[j];
				}
			}
		}
		return null;
	}
	
	
	private static interface IFileHandler {
		void process(IFile file);
	}
	
	private static interface ILoopNext {
		void nextLoop(IPackageFragment fragment);
	}
	
	private static class SqlCountingHandler implements IFileHandler, ILoopNext {

		private int resultCount;
		private int currentMax;
		private IPackageFragment result;
		
		public void process(IFile file) {
			if ("sql".equalsIgnoreCase(file.getFileExtension())) {
				currentMax++;
			}
		}
		
		public void nextLoop(IPackageFragment fragment) {
			if (result == null) {
				result = fragment;
			}
			if (resultCount < currentMax) {
				resultCount = currentMax;
				result = fragment;
			}
			currentMax = 0;
		}
		
		public IFolder getResult() {
			return result.getResource() instanceof IFolder ?
					(IFolder) result.getResource() : null;
		}
	}
	
	private static class SqlCollectingHandler implements IFileHandler {

		private String sqlBaseName;
		
		private Set result = new HashSet();
		
		public SqlCollectingHandler(String sqlBaseName) {
			this.sqlBaseName = sqlBaseName;
		}
		
		public void process(IFile file) {
			if (S2DaoUtil.isValidSqlFileName(file, sqlBaseName)) {
				result.add(file);
			}
		}
		
		public IFile[] getResult() {
			return (IFile[]) result.toArray(new IFile[result.size()]);
		}
	}

}
