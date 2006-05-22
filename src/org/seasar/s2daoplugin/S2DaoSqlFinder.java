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
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.JavaUtil;

public class S2DaoSqlFinder implements S2DaoConstants {

	public IFolder guessSqlStoredFolder(IMethod method) {
		if (method == null) {
			return null;
		}
		IJavaProject project = method.getJavaProject();
		IType type = method.getCompilationUnit().findPrimaryType();
		String packagePath = packageToPath(type);
		IPath[] srcPaths = JavaProjectUtil.getSourceFolderPaths(project);
		
		int maxCount = 0;
		IFolder storedFolder = null;
		for (int i = 0; i < srcPaths.length; i++) {
			SqlCountingVisitor visitor = new SqlCountingVisitor();
			visit(project, srcPaths[i].append(packagePath), visitor);
			if (maxCount < visitor.getCount()) {
				IPackageFragment pack = visitor.getPathPackage();
				if (pack != null && pack.getResource() instanceof IFolder) {
					storedFolder = (IFolder) pack.getResource();
					maxCount = visitor.getCount();
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
			Set result = new HashSet();
			for (int i = 0; i < methods.length; i++) {
				result.addAll(findSqlFilesFromMethod(methods[i]));
			}
			return (IFile[]) result.toArray(new IFile[result.size()]);
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
			return EMPTY_FILES;
		}
	}
	
	public IFile[] findSqlFiles(IMethod method) {
		if (method == null) {
			return EMPTY_FILES;
		}
		Set result = findSqlFilesFromMethod(method);
		return (IFile[]) result.toArray(new IFile[result.size()]);
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
	
	private Set findSqlFilesFromMethod(IMethod method) {
		IType type = method.getCompilationUnit().findPrimaryType();
		IJavaProject project = type.getJavaProject();
		String packagePath = packageToPath(type);
		String basename = S2DaoUtil.createBaseSqlFileName(method);
		IPath[] sourcePaths = JavaProjectUtil.getSourceFolderPaths(project);
		return findSqlFilesFromPaths(project, sourcePaths, packagePath, basename);
	}
	
	private Set findSqlFilesFromPaths(IJavaProject project, IPath[] paths,
			String packagePath, String basename) {
		Set result = new HashSet();
		for (int i = 0; i < paths.length; i++) {
			SqlFindingVisitor visitor = new SqlFindingVisitor(basename);
			visit(project, paths[i].append(packagePath), visitor);
			result.addAll(visitor.getResult());
		}
		return result;
	}
	
	private void visit(IJavaProject project, IPath path,
			ISqlVisitor visitor) {
		try {
			IPackageFragment pack = project.findPackageFragment(path);
			if (pack != null) {
				visitor.setPathPackage(pack);
				pack.getResource().accept(visitor, IResource.DEPTH_ONE, false);
			}
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	private String packageToPath(IType type) {
		return type.getPackageFragment().getElementName().replace('.', '/');
	}
	
	
	private interface ISqlVisitor extends IResourceVisitor {
		void setPathPackage(IPackageFragment pack);
		IPackageFragment getPathPackage();
	}
	
	private static abstract class AbstractSqlVisitor implements ISqlVisitor {
		
		private IPackageFragment pack;
		
		public void setPathPackage(IPackageFragment pack) {
			this.pack = pack;
		}
		
		public IPackageFragment getPathPackage() {
			return pack;
		}
	}
	
	private static class SqlFindingVisitor extends AbstractSqlVisitor {

		private Set result = new HashSet();
		private String basename;
		
		public SqlFindingVisitor(String basename) {
			this.basename = basename;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE) {
				if (S2DaoUtil.isValidSqlFileName((IFile)resource, basename)) {
					result.add(resource);
				}
				return false;
			}
			return true;
		}
		
		public Set getResult() {
			return result;
		}
	}
	
	private static class SqlCountingVisitor extends AbstractSqlVisitor {

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
