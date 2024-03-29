package dev.fredag.cheerwithme.data.backend

import retrofit2.Response
import retrofit2.http.*

interface BackendService {
    @POST("/login/google")
    suspend fun loginGoogle(@Body googleUserSignInRequest: GoogleUserSignInRequest): GoogleUserSignInResponse

    @GET("/friends")
    suspend fun friends(): Response<UserFriends>

    @POST("/friends/sendFriendRequest")
    suspend fun sendFriendRequest(@Body acceptFriendRequest: SendFriendRequest): Response<Void>

    @POST("/friends/acceptFriendRequest")
    suspend fun acceptFriendRequest(@Body acceptFriendRequest: AcceptFriendRequest): Response<Void>

    @GET("/users/search")
    suspend fun searchUsersByNick(@Query("nick") nick: String): Response<List<User>>

    @GET("/happenings")
    suspend fun getHappenings(): Response<List<Happening>>

    @GET("/happenings/{happeningId}")
    suspend fun getHappening(@Path("happeningId") happeningId: HappeningId): Response<Happening>

    @POST("/happenings/createHappening")
    suspend fun createHappening(@Body createHappening: CreateHappening): Response<Happening>

    @PUT("/happenings/updateHappening")
    suspend fun updateHappening(@Body updateHappening: UpdateHappening): Response<Happening>

    @DELETE("/happenings/cancelHappening")
    suspend fun cancelHappening(@Body cancelHappening: CancelHappening): Response<Unit>

    @PUT("/happenings/inviteUsers")
    suspend fun inviteUsersToHappening(@Body inviteUsersToHappening: InviteUsersToHappening): Response<Happening>

    @POST("/happenings/acceptHappeningInvite")
    suspend fun acceptHappeningInvite(@Body acceptHappeningInvite: AcceptHappeningInvite): Response<Happening>

    @POST("/happenings/rejectHappeningInvite")
    suspend fun rejectHappeningInvite(@Body rejectHappeningInvite: RejectHappeningInvite): Response<Happening>
}
