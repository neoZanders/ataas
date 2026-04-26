package com.chalmers.atas.external;

import java.util.List;

public record FindReservationsResponse(
        List<ReservationResponse> results,
        Long limit,
        Long page,
        Long totalPages,
        Long totalResults
) {}
