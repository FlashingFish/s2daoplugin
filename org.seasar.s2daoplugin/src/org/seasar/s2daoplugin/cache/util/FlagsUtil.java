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
package org.seasar.s2daoplugin.cache.util;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

public class FlagsUtil {

	public static boolean isAbstract(IMember member) {
		return Flags.isAbstract(getFlags(member));
	}
	
	public static boolean isInterface(IMember member) {
		return Flags.isInterface(getFlags(member));
	}
	
	public static boolean isPublic(IMember member) {
		return Flags.isPublic(getFlags(member));
	}
	
	public static boolean isProtected(IMember member) {
		return Flags.isProtected(getFlags(member));
	}
	
	public static boolean isPackagePrivate(IMember member) {
		int flag = getFlags(member);
		return !Flags.isPublic(flag) && !Flags.isProtected(flag) && !Flags.isPrivate(flag);
	}
	
	public static boolean isPrivate(IMember member) {
		return Flags.isPrivate(getFlags(member));
	}
	
	public static boolean isFinal(IMember member) {
		return Flags.isFinal(getFlags(member));
	}
	
	public static boolean isStatic(IMember member) {
		return Flags.isStatic(getFlags(member));
	}
	
	private static int getFlags(IMember member) {
		try {
			if (member != null) {
				return member.getFlags();
			}
		} catch (JavaModelException ignore) {
		}
		return -1;
	}

}
