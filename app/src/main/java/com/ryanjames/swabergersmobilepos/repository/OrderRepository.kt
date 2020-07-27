package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toEntity
import com.ryanjames.swabergersmobilepos.mappers.toRemoteEntity
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import io.reactivex.Single
import javax.inject.Inject

class OrderRepository @Inject constructor(
    val swabergersService: SwabergersService,
    val orderRealmDao: OrderRealmDao,
    val globalRealmDao: GlobalRealmDao
) {

    fun getLocalBag(): Single<List<LineItem>> {
        return orderRealmDao.getLineItems().map { it.lineItems.map { lineItem -> lineItem.toDomain() } }
    }

    fun getOrderHistory(): Single<List<Order>> {
        return swabergersService.getOrderHistory().map { orderHistory -> orderHistory.toDomain() }
    }

    fun insertLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm ->
            if (orderRealmDao.lineItemsCount(realm) == 0) {
                globalRealmDao.createLocalBagOrderId(realm)
            }
            orderRealmDao.insertLineItem(realm, lineItem.toEntity(realm))
        }
    }

    fun updateLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.updateLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun removeLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.removeLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun postOrder(order: Order): Single<Boolean> {
        var orderId = globalRealmDao.getLocalBagOrderId()
        if (orderId == GlobalRealmDao.NO_LOCAL_ORDER) {
            executeRealmTransaction { realm ->
                orderId = globalRealmDao.createLocalBagOrderId(realm)
            }
        }
        return swabergersService.postOrder(order.toRemoteEntity(orderId)).map { true }
    }

    fun clearLocalBag() {
        executeRealmTransaction { realm ->
            orderRealmDao.deleteAllLineItems(realm)
            globalRealmDao.clearLocalBagOrderId(realm)
        }
    }
}