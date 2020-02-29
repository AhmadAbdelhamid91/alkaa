package com.escodro.repository.datasource

import com.escodro.repository.model.TaskWithCategory

/**
 * Interface to represent the implementation of Search data source.
 */
interface SearchDataSource {

    /**
     * Gets tasks based on the given name.
     *
     * @param query the name to query
     *
     * @return the list of tasks that match the given query
     */
    fun findTaskByName(query: String): List<TaskWithCategory>
}