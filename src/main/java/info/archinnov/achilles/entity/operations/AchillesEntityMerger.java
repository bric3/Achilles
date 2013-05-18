package info.archinnov.achilles.entity.operations;

import info.archinnov.achilles.entity.context.AchillesPersistenceContext;

/**
 * AchillesEntityMerger
 * 
 * @author DuyHai DOAN
 * 
 */
public interface AchillesEntityMerger
{

	public <T> T mergeEntity(AchillesPersistenceContext context);

}