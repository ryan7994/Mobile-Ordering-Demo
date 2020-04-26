package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toEntity
import io.reactivex.Single

object OrderRepository {

    private val orderRealmDao = OrderRealmDao()


    fun getLocalBag(): Single<List<LineItem>> {
        return orderRealmDao.getLineItems().map { it.lineItems.map { item -> item.toDomain() } }
    }

    fun insertLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.insertLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun updateLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.updateLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun postOrder() {

    }
}