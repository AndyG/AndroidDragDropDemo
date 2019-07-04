package com.discord.androiddragdropdemo.repository

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object ExpandedFolderRepository {

    private val expandedFolderIds: MutableSet<Long>
    private val expandedFolderIdsSubject = BehaviorSubject.create<Set<Long>>()

    init {
        expandedFolderIds = HashSet()
        publish()
    }

    fun observeExpandedFolderIds(): Observable<Set<Long>> {
        return expandedFolderIdsSubject
    }

    fun expandFolder(id: Long) {
        if (expandedFolderIds.add(id)) {
            publish()
        }
    }

    fun collapseFolder(id: Long) {
        if (expandedFolderIds.remove(id)) {
            publish()
        }
    }

    private fun publish() {
        expandedFolderIdsSubject.onNext(expandedFolderIds)
    }

}