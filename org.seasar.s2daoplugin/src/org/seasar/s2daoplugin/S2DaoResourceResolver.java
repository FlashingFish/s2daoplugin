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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.util.JavaProjectUtil;

public class S2DaoResourceResolver implements S2DaoConstants {

	private static final IFile[] EMPTY_FILES = new IFile[0];
	
	public IFolder resolveSqlStoredFolder(IMethod method) {
		if (method == null) {
			return null;
		}
		SqlStoredFolderFindingHandler handler = new SqlStoredFolderFindingHandler();
		process(method, handler);
		return handler.getResult();
	}
	
	public IFile[] findSqlFiles(IMethod method) {
		if (method == null) {
			return EMPTY_FILES;
		}
		String sqlBaseName = S2DaoNamingConventions.createBaseSqlFileName(method);
		SqlCollectingHandler handler = new SqlCollectingHandler(sqlBaseName);
		process(method, handler);
		return handler.getResult();
	}
	
	public IMethod findMethodFromSql(IFile file) {
		if (file == null) {
			return null;
		}
		String[][] daoNames = S2DaoNamingConventions.resovleDao(file);
		IPackageFragmentRoot[] roots = JavaProjectUtil
				.findPackageFragmentRootsSharedOutputLocation(file);
		for (int i = 0; i < daoNames.length; i++) {
			for (int j = 0; j < roots.length; j++) {
				IPackageFragment fragment = roots[j].getPackageFragment(daoNames[i][0]);
				if (!fragment.exists()) {
					continue;
				}
				IMethod method = findMethod(fragment, daoNames[i][1], daoNames[i][2]);
				if (method != null) {
					return method;
				}
			}
		}
		return null;
	}
	
	private IMethod findMethod(IPackageFragment fragment, String typeName,
			String methodName) {
		IType type = findType(fragment, typeName);
		if (type == null) {
			return null;
		}
		try {
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getElementName().equals(methodName)) {
					return methods[i];
				}
			}
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
		}
		return null;
	}

	private IType findType(IPackageFragment fragment, String typeName) {
		IJavaElement[] elements;
		try {
			elements = fragment.getChildren();
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
			return null;
		}
		for (int i = 0; i < elements.length; i++) {
			if (!(elements[i] instanceof ICompilationUnit)) {
				continue;
			}
			ICompilationUnit unit = (ICompilationUnit) elements[i];
			try {
				IType[] types = unit.getAllTypes();
				for (int j = 0; j < types.length; j++) {
					if (types[j].getElementName().equals(typeName)) {
						return types[j];
					}
				}
			} catch (JavaModelException e) {
				S2DaoPlugin.log(e);
			}
		}
		return null;
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

	private String getPackageName(IMethod method) {
		return method.getDeclaringType().getPackageFragment().getElementName().toString();
	}
	
	
	private static interface IFileHandler {
		void process(IFile file);
	}
	
	private static interface ILoopNext {
		void nextLoop(IPackageFragment fragment);
	}
	
	private static class SqlStoredFolderFindingHandler implements IFileHandler, ILoopNext {

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
			if (S2DaoNamingConventions.isValidSqlFileName(file, sqlBaseName)) {
				result.add(file);
			}
		}
		
		public IFile[] getResult() {
			return (IFile[]) result.toArray(new IFile[result.size()]);
		}
	}

}
