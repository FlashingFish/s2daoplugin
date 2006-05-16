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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.util.JavaUtil;

public class S2DaoSqlFinder implements S2DaoConstants {

	public IFolder guessSqlStoredFolder(IMethod method) {
		if (method == null) {
			return null;
		}
		IJavaProject project = method.getJavaProject();
		IType type = method.getCompilationUnit().findPrimaryType();
		String packagePath = packageToPath(type);
		IPath[] sourcePaths = JavaUtil.getSourceFolderPaths(project);
		
		int maxCount = 0;
		IFolder storedFolder = null;
		for (int i = 0; i < sourcePaths.length; i++) {
			IPackageFragment pack = null;
			try {
				pack = project.findPackageFragment(sourcePaths[i].append(packagePath));
			} catch (JavaModelException ignore) {
				S2DaoPlugin.log(ignore);
			}
			if (pack != null) {
				IResource resource = pack.getResource();
				if (resource instanceof IFolder) {
					SqlCountingVisitor visitor = new SqlCountingVisitor();
					try {
						resource.accept(visitor, IResource.DEPTH_ONE, false);
					} catch (CoreException ignore) {
						S2DaoPlugin.log(ignore);
					}
					if (maxCount < visitor.getCount()) {
						storedFolder = (IFolder) resource; 
						maxCount = visitor.getCount();
					}
				}
			}
		}
		return storedFolder;
	}
	
	public IFile[] findSqlFiles(IType type) {
		if (type == null) {
			return EMPTY_FILES;
		}
		try {
			IMethod[] methods = type.getMethods();
			List sqlFileList = new LinkedList();
			for (int i = 0; i < methods.length; i++) {
				sqlFileList.addAll(findSqlFilesFromMethod(methods[i]));
			}
			return (IFile[]) sqlFileList.toArray(new IFile[sqlFileList.size()]);
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
			return EMPTY_FILES;
		}
	}
	
	public IFile[] findSqlFiles(IMethod method) {
		if (method == null) {
			return EMPTY_FILES;
		}
		List sqlFileList = findSqlFilesFromMethod(method);
		return (IFile[]) sqlFileList.toArray(new IFile[sqlFileList.size()]);
	}
	
	public IMethod findMethodFromSql(IFile file) {
		if (file == null || !"sql".equals(file.getFileExtension())) {
			return null;
		}
		String[] split = S2DaoUtil.splitSqlFileName(file.getName());
		if (split.length != 2) {
			return null;
		}
		IPath packagePath = getPackagePath(file);
		if (packagePath == null) {
			return null;
		}
		IJavaProject project = JavaCore.create(file.getProject());
		IPath[] srcPaths = JavaUtil.getSourceFolderPaths(project);
		for (int i = 0; i < srcPaths.length; i++) {
			try {
				IPackageFragment pack =
					project.findPackageFragment(srcPaths[i].append(packagePath));
				if (pack == null) {
					continue;
				}
				ICompilationUnit unit = pack.getCompilationUnit(split[0] + ".java");
				IType type = unit.findPrimaryType();
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
	
	private IPath getPackagePath(IFile file) {
		IJavaProject project = JavaCore.create(file.getProject());
		IPath[] srcPaths = JavaUtil.getSourceFolderPaths(project);
		for (int i = 0; i < srcPaths.length; i++) {
			if (srcPaths[i].matchingFirstSegments(file.getFullPath())
					== srcPaths[i].segmentCount()) {
				return file.getFullPath().removeFirstSegments(
						srcPaths[i].segmentCount()).removeLastSegments(1);
			}
		}
		return null;
	}
	
	private List findSqlFilesFromMethod(IMethod method) {
		IType type = method.getCompilationUnit().findPrimaryType();
		IJavaProject project = type.getJavaProject();
		String packagePath = packageToPath(type);
		String basename = S2DaoUtil.createBaseSqlFileName(method);
		IPath[] sourcePaths = JavaUtil.getSourceFolderPaths(project);
		return findSqlFileFromPaths(project, sourcePaths, packagePath, basename);
	}
	
	private List findSqlFileFromPaths(IJavaProject project, IPath[] paths,
			String packagePath, String basename) {
		List sqlFileList = new LinkedList();
		for (int i = 0; i < paths.length; i++) {
			try {
				sqlFileList.addAll(findSqlFileFromPath(project, paths[i].append(packagePath), basename));
			} catch (CoreException ignore) {
				S2DaoPlugin.log(ignore);
			}
		}
		return sqlFileList;
	}
	
	private List findSqlFileFromPath(IJavaProject project, IPath path,
			String basename) throws CoreException {
		List sqlFileList = new LinkedList();
		IPackageFragment pack = project.findPackageFragment(path);
		if (pack != null) {
			SqlFindingVisitor visitor = new SqlFindingVisitor(basename);
			pack.getResource().accept(visitor, IResource.DEPTH_ONE, false);
			sqlFileList.addAll(visitor.getSqlFileList());
		}
		return sqlFileList;
	}
	
	private String packageToPath(IType type) {
		return type.getPackageFragment().getElementName().replace('.', '/');
	}
	
	
	private static class SqlFindingVisitor implements IResourceVisitor {

		private List sqlFileList = new LinkedList();
		private String basename;
		
		public SqlFindingVisitor(String basename) {
			this.basename = basename;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE) {
				if (S2DaoUtil.isValidSqlFileName((IFile)resource, basename)) {
					sqlFileList.add(resource);
				}
				return false;
			}
			return true;
		}
		
		public List getSqlFileList() {
			return sqlFileList;
		}
	}
	
	private static class SqlCountingVisitor implements IResourceVisitor {

		private int count;
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE &&
					"sql".equals(resource.getFileExtension())) {
				count++;
			}
			return true;
		}
		
		public int getCount() {
			return count;
		}
	}

}
