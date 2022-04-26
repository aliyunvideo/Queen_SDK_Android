package io.agora.vlive.protocol.interfaces;

import io.agora.vlive.protocol.model.body.RequestModifySeatStateBody;
import io.agora.vlive.protocol.model.body.RequestSeatInteractionBody;
import io.agora.vlive.protocol.model.response.BooleanResponse;
import io.agora.vlive.protocol.model.response.LongResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SeatService {
    @POST("ent/v1/room/{roomId}/users/{userId}/seats")
    Call<LongResponse> requestSeatInteraction(@Header("token") String token, @Path("roomId") String roomId,
                                              @Path("userId") String userId, @Body RequestSeatInteractionBody body);

    @POST("ent/v1/room/{roomId}/seat")
    Call<BooleanResponse> requestModifySeatStates(@Header("token") String token, @Path("roomId") String roomId,
                                                  @Body RequestModifySeatStateBody body);
}