package com.yurii.foody.screens.admin.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.Service
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.databinding.ItemUserRoleRequestBinding
import retrofit2.HttpException
import java.io.IOException

data class UserRoleRequest(
    val id: Long,
    val user: User,
    val role: UserRoleEnum
) {
    val fullName = "${user.firstName} ${user.lastName}"
}

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

class UserRoleAdapter(private val onClick: (UserRoleRequest) -> Unit) :
    PagingDataAdapter<UserRoleRequest, UserRoleAdapter.UserRoleRequestViewHolder>(COMPARATOR) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<UserRoleRequest>() {
            override fun areItemsTheSame(oldItem: UserRoleRequest, newItem: UserRoleRequest): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UserRoleRequest, newItem: UserRoleRequest): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: UserRoleRequestViewHolder, position: Int) {
        getItem(position)?.run { holder.bind(this, onClick) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoleRequestViewHolder {
        return UserRoleRequestViewHolder.create(parent)
    }

    class UserRoleRequestViewHolder private constructor(private val binding: ItemUserRoleRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(requestData: UserRoleRequest, onClick: (UserRoleRequest) -> Unit) {
            binding.request = requestData
            binding.body.setOnClickListener {
                onClick.invoke(requestData)
            }
        }

        companion object {
            fun create(viewGroup: ViewGroup): UserRoleRequestViewHolder {
                val binding: ItemUserRoleRequestBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_user_role_request, viewGroup, false)
                return UserRoleRequestViewHolder(binding)
            }
        }
    }
}