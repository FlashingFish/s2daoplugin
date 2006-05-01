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
package org.seasar.s2daoplugin.cache.builder;

import java.util.ArrayList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.IComponentCache;

public class CacheBuilderChain implements ICacheBuilder {

	private List builders = new ArrayList();
	
	public CacheBuilderChain addBuilder(ICacheBuilder builder) {
		if (builder == null) {
			return this;
		}
		builders.add(builder);
		return this;
	}
	
	public void setComponentCache(final IComponentCache cache) {
		new AbstractBlock() {
			protected void callback(ICacheBuilder builder) {
				builder.setComponentCache(cache);
			}
		}.execute();
	}

	public void initialize() {
		new AbstractBlock() {
			protected void callback(ICacheBuilder builder) {
				builder.initialize();
			}
		}.execute();
	}

	public void build(final IComponentElement[] components) {
		new AbstractBlock() {
			protected void callback(ICacheBuilder builder) {
				builder.build(components);
			}
		}.execute();
	}

	public void clear(final IComponentElement[] components) {
		new AbstractBlock() {
			protected void callback(ICacheBuilder builder) {
				builder.clear(components);
			}
		}.execute();
	}

	public void finishBuild() {
		new AbstractBlock() {
			protected void callback(ICacheBuilder builder) {
				builder.finishBuild();
			}
		}.execute();
	}
	
	
	private interface Block {
		void execute();
	}
	
	private abstract class AbstractBlock implements Block {
		public final void execute() {
			for (int i = 0; i < builders.size(); i++) {
				callback((ICacheBuilder) builders.get(i));
			}
		}
		
		protected abstract void callback(ICacheBuilder builder);
	}

}
