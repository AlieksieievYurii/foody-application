package com.yurii.foody.screens.admin.requests

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yurii.foody.api.Service
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import retrofit2.HttpException
import java.io.IOException

data class UserRoleRequest(
    val id: Long,
    val user: User,
    val role: UserRoleEnum
)

class UserRoleRequestPagingSource(private val api: Service) : PagingSource<Int, UserRoleRequest>() {
    override fun getRefreshKey(state: PagingState<Int, UserRoleRequest>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserRoleRequest> {
        return try {
            val page = params.key ?: 1
            val unconfirmedRoles = api.usersService.getUsersRoles(isConfirmed = false, page = page, size = params.loadSize)
            val usersId = unconfirmedRoles.results.joinToString(",") { it.userId.toString() }
            val users = api.usersService.getUsers(userIds = usersId, size = params.loadSize)
            val result = unconfirmedRoles.results.map { userRole ->
                UserRoleRequest(
                    id = userRole.id,
                    user = users.results.find { it.id == userRole.userId }!!,
                    role = userRole.role
                )
            }
            LoadResult.Page(result, prevKey = if (page == 1) null else page - 1, nextKey = if (unconfirmedRoles.next != null) page + 1 else null)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

}