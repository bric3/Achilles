/**
 *
 * Copyright (C) 2012-2013 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.archinnov.achilles.test.integration.tests;

import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.entity.manager.ThriftEntityManager;
import info.archinnov.achilles.junit.AchillesInternalThriftResource;
import info.archinnov.achilles.junit.AchillesTestResource.Steps;
import info.archinnov.achilles.test.integration.entity.ValuelessEntity;
import info.archinnov.achilles.type.OptionsBuilder;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Rule;
import org.junit.Test;

public class ValuelessEntityIT {

	@Rule
	public AchillesInternalThriftResource resource = new AchillesInternalThriftResource(
			Steps.AFTER_TEST, "ValuelessEntity");

	private ThriftEntityManager em = resource.getEm();

	@Test
	public void should_persist_and_find() throws Exception {
		Long id = RandomUtils.nextLong();
		ValuelessEntity entity = new ValuelessEntity(id);

		em.persist(entity);

		ValuelessEntity found = em.find(ValuelessEntity.class, id);

		assertThat(found).isNotNull();
	}

	@Test
	public void should_merge_and_get_reference() throws Exception {
		Long id = RandomUtils.nextLong();
		ValuelessEntity entity = new ValuelessEntity(id);

		em.merge(entity);

		ValuelessEntity found = em.getReference(ValuelessEntity.class, id);

		assertThat(found).isNotNull();
	}

	@Test
	public void should_persist_with_ttl() throws Exception {
		Long id = RandomUtils.nextLong();
		ValuelessEntity entity = new ValuelessEntity(id);

		em.persist(entity, OptionsBuilder.withTtl(2));

		Thread.sleep(3000);

		assertThat(em.find(ValuelessEntity.class, id)).isNull();
	}

	@Test
	public void should_merge_with_ttl() throws Exception {
		Long id = RandomUtils.nextLong();
		ValuelessEntity entity = new ValuelessEntity(id);

		em.merge(entity, OptionsBuilder.withTtl(2));

		Thread.sleep(3000);

		assertThat(em.find(ValuelessEntity.class, id)).isNull();
	}
}